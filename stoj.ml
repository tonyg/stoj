open Stojlang

type join_id = JoinId of int
type channel_id = ChannelId of int

module JoinIdMap = Map.Make(struct type t = join_id let compare = compare end)

type input_handler = int -> unit

type message = Message of channel list
and channel = {
    channel_id: channel_id;
    name: string;
    should_log: bool;
    mutable handlers: input_handler JoinIdMap.t;
    queue: message Queue.t
  }

module ChannelMap = Map.Make(struct
  type t = channel
  let compare a b = compare a.channel_id b.channel_id
end)

exception UnboundChannelVariable of cname
exception UnboundProcessVariable of pname

let make_channel =
  let counter = ref 0 in
  fun name should_log ->
    (let result = { channel_id = ChannelId !counter;
		    name = name;
		    should_log = should_log;
		    handlers = JoinIdMap.empty;
		    queue = Queue.create () } in
    counter := !counter + 1;
    result)

let read_lexbuf lexbuf = Stojparse.main Stojlex.token lexbuf
let read_string str = read_lexbuf (Lexing.from_string str)
let read_file filename =
  let f = open_in filename in
  let result = read_lexbuf (Lexing.from_channel f) in
  close_in f;
  result
let read_stdin () = read_lexbuf (Lexing.from_channel stdin)

let runnable_processes: (unit -> unit) Queue.t = Queue.create ()
let enabled_comm : (int ChannelMap.t *
		      channel list * 
		      rate_constant * 
		      (unit -> unit)) JoinIdMap.t ref =
  ref JoinIdMap.empty
let system_clock = ref 0.0
let event_counter = ref 0

let schedule thunk = Queue.add thunk runnable_processes

let advance_clock delta =
  system_clock := !system_clock +. delta

let log_event channel event_name count_new =
  event_counter := !event_counter + 1;
  if channel.should_log
  then (Stojout.log_event
	  !system_clock
	  channel.name
	  (match channel.channel_id with ChannelId id -> id)
	  event_name
	  count_new)

let unhook_and_schedule jid chmap cont =
  schedule cont;
  ChannelMap.iter
    (fun ch usagecount -> ch.handlers <- JoinIdMap.remove jid ch.handlers)
    chmap

let comm_enable (jid, chmap, channels, rate, cont) =
  match rate with
    None -> unhook_and_schedule jid chmap cont
  | Some rc -> enabled_comm := JoinIdMap.add jid (chmap, channels, rc, cont) !enabled_comm
  
let comm_disable jid =
  enabled_comm := JoinIdMap.remove jid !enabled_comm

let channel_msgcount channel = Queue.length channel.queue

(* Number of distinct sets of k elements from a set of n elements *)
let combinations n k =
  match k with
    1 -> float_of_int n
  | _ ->
      let rec loop i acc =
	if i == 0
	then acc
	else loop (i - 1) (acc *. float_of_int (n - (i - 1)) /. float_of_int i)
      in
      loop (min k (n - k)) 1.0

let perform_comm () =
  match
    JoinIdMap.fold
      (fun jid (chmap, channels, rc, cont) ((curr_t_mu, _) as curr) ->
	let a = ChannelMap.fold
	    (fun channel stoichiometry a ->
	      a *. (combinations (channel_msgcount channel) stoichiometry))
	    chmap
	    rc in
	let randval = 1.0 -. (Random.float 1.0) in
	let t_mu = (log (1.0 /. randval)) /. a in
	if (t_mu < curr_t_mu)
	then (t_mu, Some (jid, chmap, cont))
	else curr)
      !enabled_comm
      (Pervasives.max_float, None)
  with
    (t_mu, Some (jid, chmap, cont)) ->
      comm_disable jid;
      unhook_and_schedule jid chmap cont;
      advance_clock t_mu
  | (_, None) -> ()

module CNameMap = Map.Make(struct type t = cname let compare = compare end)
module PNameMap = Map.Make(struct type t = pname let compare = compare end)

let cenv_lookup cenv name =
  try CNameMap.find name cenv
  with Not_found -> raise (UnboundChannelVariable name)

let channel_signal channel =
  let len = channel_msgcount channel in
  JoinIdMap.iter (fun jid handler -> handler len) channel.handlers

let channel_write channel count msg =
  let rec loop i =
    match i with
      0 -> ()
    | _ -> (Queue.add msg channel.queue; loop (i - 1))
  in
  loop count;
  log_event channel "W" (channel_msgcount channel);
  channel_signal channel

let channel_read channel =
  let msg = Queue.take channel.queue in
  log_event channel "R" (channel_msgcount channel);
  channel_signal channel;
  msg

let rec run penv cenv process =
  match process with
    New (annotated_names, k) ->
      let cenv' =
	List.fold_left (fun e (n,x) -> CNameMap.add n (make_channel n x) e) cenv annotated_names in
      run penv cenv' k
  | Par ps ->
      List.iter (fun p -> schedule (fun () -> run penv cenv p)) ps
  | Out (ch, names, count) ->
      let channel = cenv_lookup cenv ch in
      let values = List.map (cenv_lookup cenv) names in
      channel_write channel count (Message values)
  | In (j, r, p) ->
      setup_join penv cenv j r p
  | Rec (name, k) ->
      let rec kfun () = run (PNameMap.add name kfun penv) cenv k in
      kfun ()
  | Var name ->
      let kfun =
	(try PNameMap.find name penv
	with Not_found -> raise (UnboundProcessVariable name)) in
      kfun ()

and setup_join =
  let join_counter = ref 0 in
  fun penv cenv join rate k ->
    let jid = JoinId !join_counter in
    join_counter := !join_counter + 1;
    let lookup = cenv_lookup cenv in
    let channels = List.map (fun (n,_) -> lookup n) join in
    let distinct_channels = ref 0 in
    let chmap =
      List.fold_left
	(fun chmap ch ->
	  try
	    let oldval = ChannelMap.find ch chmap in
	    ChannelMap.add ch (oldval + 1) chmap
	  with Not_found -> (distinct_channels := !distinct_channels + 1;
			     ChannelMap.add ch 1 chmap))
	ChannelMap.empty channels in
    let cont () =
      let cenv' =
	List.fold_left2
	  (fun cenv channel (_, formals) ->
	    let (Message msg) = channel_read channel in
	    List.fold_left2
	      (fun cenv formal actual -> CNameMap.add formal actual cenv)
	      cenv formals msg)
	  cenv channels join in
      run penv cenv' k in
    let distinct_channels = !distinct_channels in
    let satisfied_channels = ref 0 in
    ChannelMap.iter
      (fun ch usagecount ->
	let enabled = ref false in
	let handler msgcount =
	  let newenabled = msgcount >= usagecount in
	  if !enabled <> newenabled
	  then (satisfied_channels := !satisfied_channels + (if newenabled then 1 else -1);
		enabled := newenabled;
		if !satisfied_channels == distinct_channels
		then comm_enable (jid, chmap, channels, rate, cont)
		else comm_disable jid)
	in
	ch.handlers <- JoinIdMap.add jid handler ch.handlers;
	handler (channel_msgcount ch))
      chmap

let toplevel events rows p =
  schedule (fun () -> run PNameMap.empty CNameMap.empty p);
  let continue_fn () =
    (not (Queue.is_empty runnable_processes)) &&
    (events == 0 || !event_counter < events) &&
    (rows == 0 || !Stojout.row_counter < rows)
  in
  while continue_fn () do
    while continue_fn () do
      let kfun = Queue.take runnable_processes in
      kfun ()
    done;
    perform_comm ()
  done;
  !event_counter > 0

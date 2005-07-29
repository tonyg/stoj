open Stojlang;;

let nextnode = ref 0 ;;

let getNode cat name =
  let n = "n" ^ (string_of_int !nextnode) in
  nextnode := !nextnode + 1;
  Printf.printf "node %s %s %s\n" cat n name;
  n
;;

let edges cat tail heads =
  List.iter (fun head -> Printf.printf "edge %s %s %s\n" cat tail head) heads
;;

let render_ann_name (name,recordMe) =
  if recordMe then "!"^name else name
;;

let render_ann_names names =
  List.fold_right (fun n acc -> (render_ann_name n) ^ " " ^ acc) names ""
;;

exception UnboundVariableError of string

let env_lookup env n =
  try
    let (_,node) = List.find (fun (name,_) -> name = n) env in
    node
  with Not_found -> raise (UnboundVariableError n)
;;

let rec dump_graph penv cenv ast =
  match ast with
    New (names, k) ->
      let newnode = getNode "new" "" in (* render_ann_names names *)
      let chs = List.map (fun n -> getNode "new_channel" (render_ann_name n)) names in
      let cenv' =
	List.fold_left2 (fun cenv (name,_) node -> (name, node)::cenv) cenv names chs in
      edges "ch" newnode chs;
      edges "new_k" newnode (dump_graph penv cenv' k);
      [newnode]
  | Par ps ->
      List.concat (List.map (dump_graph penv cenv) ps)
  | Out (ch, msg, count) ->
      let chnode = env_lookup cenv ch in
      let newnode = getNode "out" (if count > 1 then (string_of_int count) else "") in
      let chs = List.map (env_lookup cenv) msg in
      edges "out_subject" newnode [chnode];
      List.iter (fun ch -> edges "object" ch [newnode]) chs;
      [newnode]
  | In (join, rate, k) ->
      let newnode =
	getNode "join"
	  (match rate with
	    None -> ""
	  | Some rc -> string_of_float rc) in
      let subjects = List.map (fun (ch,_) -> env_lookup cenv ch) join in
      let in_nodes = List.map (fun _ -> getNode "in" "") subjects in
      List.iter2 (fun subj in_node -> (edges "subject" subj [in_node];
				       edges "join_in" in_node [newnode]))
	  subjects in_nodes;
      let cenv' =
	List.fold_left2
	  (fun cenv (_, formals) in_node ->
	    let nodes = List.map (fun formal -> getNode "bind_channel" formal) formals in
	    edges "object" in_node nodes;
	    List.fold_left2 (fun cenv formal node -> (formal, node)::cenv) cenv formals nodes)
	  cenv join in_nodes in
      edges "k" newnode (dump_graph penv cenv' k);
      [newnode]
  | Rec (name, k) ->
      let newnode = getNode "rec" name in
      edges "rec_k" newnode (dump_graph ((name, newnode)::penv) cenv k);
      [newnode]
  | Var name ->
      let target = env_lookup penv name in
      [target]
;;

let main () =
  let filename_cell = ref None in
  Arg.parse []
    (fun arg -> filename_cell := Some arg)
    "Usage: visdump [<filename>]";
  let ast =
    (match !filename_cell with
      None -> Stoj.read_stdin ()
    | Some filename -> Stoj.read_file filename) in
  dump_graph [] [] ast
;;

main ()

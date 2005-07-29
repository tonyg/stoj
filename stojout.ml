module StringMap = Map.Make(struct type t = string let compare = compare end)

let known_channels = ref []
let counters = ref StringMap.empty
let last_time = ref 0.0
let row_counter = ref 0

let write_counters () =
  let c = !counters in
  Printf.printf "%g" !last_time;
  List.iter
    (fun chname -> Printf.printf ", ,%s,%d" chname (StringMap.find chname c))
    !known_channels;
  print_char '\n';
  row_counter := !row_counter + 1

let log_event clock chname chnum event_name count_new =
  let chname = chname ^ (string_of_int chnum) in
  if clock <> !last_time then (write_counters (); last_time := clock);
  if not (StringMap.mem chname !counters) then known_channels := !known_channels @ [chname];
  counters := StringMap.add chname count_new !counters

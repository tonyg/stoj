exception NoEvents

let main () =
  let filename_cell = ref None in
  let max_events = ref 0 in
  let max_rows = ref 0 in
  let seed = ref 0 in
  Arg.parse [("--events", Arg.Set_int max_events,
	      "Set the maximum number of events to generate (0 == infinite)");
	     ("--rows", Arg.Set_int max_rows,
	      "Set the maximum number of rows to generate (0 == infinite)");
	     ("--seed", Arg.Set_int seed,
	      "Set the random number generator seed")]
    (fun arg -> filename_cell := Some arg)
    "Usage: stojrun [--events <int>|--rows <int>|--seed <int>]* <filename>";
  Printf.fprintf stdout "seed=%d,max_events=%d,max_rows=%d\n" !seed !max_events !max_rows;
  Random.init !seed;
  let ast =
    (match !filename_cell with
      None -> Stoj.read_stdin ()
    | Some filename -> Stoj.read_file filename)
  in
  if Stoj.toplevel !max_events !max_rows ast
  then exit 0
  else raise NoEvents
;;

main ()

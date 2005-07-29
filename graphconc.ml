let split_string str ch =
  let rec loop pos =
    try
      let i = String.index_from str pos ch in
      let head = String.sub str pos (i - pos) in
      let newpos = i + 1 in
      head :: (loop newpos)
    with Not_found -> [String.sub str pos ((String.length str) - pos)]
  in loop 0
;;

let timelimit = ref 0.0 ;;

let read_line_maybe () =
  try
    let line = read_line () in
    Some line
  with End_of_file -> None
;;

let rec discard_input x =
  match read_line_maybe () with
    Some _ -> discard_input x
  | None -> x
;;

let rec collect_csv all_series =
  match read_line_maybe () with
    Some line ->
      let all_fields = split_string line ',' in
      (match all_fields with
	time :: fields ->
	  if (!timelimit > 0.0) && ((float_of_string time) >= !timelimit)
	  then discard_input all_series
	  else
	    (let rec loop fields series extra_series =
	      match fields with
		(_ :: seriesname :: seriesvalue :: rest) ->
		  (match series with
		    ((_, q) :: moreseries) ->
		      (Queue.add (time, seriesvalue) q;
		       loop rest moreseries extra_series)
		  | [] ->
		      (let q = Queue.create () in
		      Queue.add (time, seriesvalue) q;
		      loop rest [] ((seriesname, q) :: extra_series)))
	      | _ ->
		  (match extra_series with
		    [] -> all_series
		  | _ -> all_series @ (List.rev extra_series))
	    in collect_csv (loop fields all_series []))
      | _ -> collect_csv all_series)
  | None -> all_series
;;

let main () =
  if Array.length Sys.argv > 1 then timelimit := float_of_string (Array.get Sys.argv 1);
  let vars = read_line () in
  print_string "set terminal png\n";
  Printf.printf "set title \"Quantity change with time; %s\"\n" vars;
  print_string "set xlabel \"time (seconds)\"\n";
  print_string "set ylabel \"quantity (messages)\"\n";
  let series = collect_csv [] in
  print_string "plot";
  let need_comma = ref false in
  List.iter (fun (seriesname, _) -> (if !need_comma then print_char ',';
				     need_comma := true;
				     Printf.printf " '-' title \"%s\" with lines" seriesname))
    series;
  print_char '\n';
  List.iter
    (fun (seriesname, q) ->
      Queue.iter (fun (time, value) -> Printf.printf "%s %s\n" time value) q;
      print_string "\ne\n")
    series;
  ()
;;

main ()

(* File lexer.mll *)
{
open Stojparse
exception Eof
}

let white = ' ' | '\t' | '\r' | '\n'
let digit = ['0'-'9']
let alpha = ['A'-'Z' 'a'-'z']

rule token = parse
| [' ' '\t' '\r' '\n']	{ token lexbuf }     (* skip blanks *)
| "//"		{ comment lexbuf }

| "new"		{ NEW }
| "stop"	{ STOP }
| "rec"		{ REC }

| digit+ '.' digit+
		{ FLOAT(float_of_string (Lexing.lexeme lexbuf)) }
| digit+	{ INT(int_of_string (Lexing.lexeme lexbuf)) }
| alpha (alpha | digit | '_')*
		{ ID(Lexing.lexeme lexbuf) }

| "->"		{ ARROW }
| '<'		{ LANGLE }
| '>'		{ RANGLE }
| '('		{ LPAREN }
| ')'		{ RPAREN }
| '['		{ LBRACK }
| ']'		{ RBRACK }
| ','		{ COMMA }
| '.'		{ DOT }
| '!'		{ BANG }
| '&'		{ JOIN }
| '|'		{ PAR }
| '*'		{ STAR }
| eof		{ EOF }

and comment = parse
| ['\n' '\r']	{ token lexbuf }
| _		{ comment lexbuf }
| eof		{ EOF }

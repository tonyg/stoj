%{
open Stojlang

exception InvalidParserState of string

%}

%token NEW STOP LANGLE LPAREN ARROW REC
%token <string> ID
%token <float> FLOAT
%token <int> INT
%token RANGLE RPAREN LBRACK RBRACK COMMA DOT JOIN PAR EOF BANG STAR

%start main
%type <Stojlang.process> main process par process_atom
%type <(Stojlang.cname * Stojlang.cname list) list> prefix

%%

main:
	process					{ $1 }
;

process:
	par					{ $1 }
;

par:
	process_atom PAR par			{ match $3 with
						  Par ps -> Par ($1 :: ps)
						| p -> Par [$1; p] }
|	process_atom				{ $1 }
;

process_atom:
	REC ID DOT process_atom			{ Rec ($2, $4) }
|	NEW annotated_namelist DOT process_atom	{ New ($2, $4) }
|	ID					{ Var $1 }
|	STOP					{ Par [] }
|	ID LANGLE namelist RANGLE out_count	{ Out ($1, $3, $5) }
|	branch					{ $1 }
|	LPAREN process RPAREN			{ $2 }
;

out_count:
	STAR INT				{ $2 }
|						{ 1 }
;

branch:
	prefix suffix				{ $2 $1 }
|	prefix					{ In ($1, None, Par []) }
;

prefix:
	name LPAREN namelist RPAREN JOIN prefix	{ ($1, $3) :: $6 }
|	name LPAREN namelist RPAREN		{ [($1, $3)] }
;

suffix:
	ARROW LBRACK float RBRACK process_atom	{ fun x -> In (x, Some $3, $5) }
|	ARROW process_atom			{ fun x -> In (x, None, $2) }
;

float:
	FLOAT					{ $1 }
|	INT					{ float_of_int($1) }
;

name:
	ID					{ $1 }
;

namelist:
						{ [] }
|	namelist_nonempty			{ $1 }
;

namelist_nonempty:
	name					{ [$1] }
|	name COMMA namelist_nonempty		{ $1 :: $3 }
;

annotated_namelist:
	ann_name				{ [$1] }
|	ann_name COMMA annotated_namelist	{ $1 :: $3 }
;

ann_name:
	BANG ID					{ ($2, true) }
|	ID					{ ($1, false) }
;

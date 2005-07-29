type pname = string
type cname = string
type rate_constant = float
type rate = rate_constant option

type branch_join = (cname * cname list) list

type process =
  | New of (cname * bool) list * process
  | Par of process list
  | Out of cname * cname list * int
  | In of branch_join * rate * process
  | Rec of pname * process
  | Var of pname

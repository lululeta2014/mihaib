scope CScope {
	String name; // e.g. "global" or the function's name
	List symbols;
}

prog
scope CScope;
	: ... ;

func
scope CScope;
	: ... ;

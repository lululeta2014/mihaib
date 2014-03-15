scope name {
	type1 attribute_name1;
	...
}

scope SymbolScope { List symbols; }

classDefinition:
scope SymbolScope;
	: ... ;

methodDefinition:
scope SymbolScope;
	: ... ;

block:
scope SymbolScope;
	: ... ;

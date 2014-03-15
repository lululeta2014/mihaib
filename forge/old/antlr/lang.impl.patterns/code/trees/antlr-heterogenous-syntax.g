primary	: INT<IntNode>
	| ID<VarNode>
	| '[' expr (',' expr)* ']' -> ^(VEC<VectorNode> expr+)
	;

expr	:	multExpr (('+' | '-') multExpr)*;

multExpr:	atom ('*' atom)*;

atom	:	INT
	|	ID
	|	'(' expr ')'
	;

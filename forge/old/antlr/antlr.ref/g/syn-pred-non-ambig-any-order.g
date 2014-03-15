stat	:	(decl) => decl ';'
	|	expr ';'
	|	'return' expr ';'
	|	'break' ';'
	;

stat	:	(decl) => decl ';'
	|	'return' expr ';'
	|	'break' ';'
	|	expr ';'
	;

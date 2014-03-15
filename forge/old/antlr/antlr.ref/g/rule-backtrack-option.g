stat
options {
	backtrack=true;
}
	:	decl ';'
	|	'return' expr ';'
	|	'break' ';'
	|	expr ';'
	;

decl	:	type ID ';'
		{System.out.println("var "
			+ $ID.text + ":" + $type.text + ";");}
	;
type	:	'int' | 'float';

// to allow user-defined types
decl	:	t=ID id=ID ';'
		{System.out.println("var " +
			$id.text + ":" + $t.text + ";");}
	;

// to gather a list of variables
decl	:	type ids+=ID (',' ids+=ID)* ';' ;

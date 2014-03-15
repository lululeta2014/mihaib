method
scope { String name; }
	:	type ID {$method::name = $ID.text;} body
	;

body	:	'{' stat '}';

stat	:	decl { ... $method::name ... } ';'
	|	..
	;

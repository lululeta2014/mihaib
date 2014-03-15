method
@scope {
	String name;
}
	: 'void' ID {$method::name = $ID.text;} '(' args ')' body
	;

atom	: ID {System.out.println("ref " + $ID.text +
		" in " + $method::name);}
	;

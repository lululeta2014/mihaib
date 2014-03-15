r [int a, String b] returns [int c, String d]
	: ... {$c = $a; $d = $b;}
	;

s	: ... v = r[3, "test"] {System.out.println($v.d);}
	;

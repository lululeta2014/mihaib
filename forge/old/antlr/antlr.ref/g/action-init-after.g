r returns [int n]
@init {
	$n = 0;
}
@after {
	System.out.println("returning n=" + $n);
}
	:	... {$n = 23;}
	|	... {$n = 17;}
	|	// empty, use initialized value
	;

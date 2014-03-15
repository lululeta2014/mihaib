f
scope {int x;}
	:	{$f::x = 0;} g ;
g	:	h;
h	:	{int y = $f::x;} ;

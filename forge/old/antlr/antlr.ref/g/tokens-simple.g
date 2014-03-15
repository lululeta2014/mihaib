grammar T;
tokens {
	VARDEF;
}
var	:	type ID ';' -> ^(VARDEF type ID);

fragment CODE [boolean stripCurlies]
	:	'{' ( CODE[stripCurlies] | ~('{'|'}') )* '}'
	{
	if(stripCurlies) {
		String s = getText();
		setText(s.substring(1, s.length() - 1));
	}
	}
	;

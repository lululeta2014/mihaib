WS: (' '|'\t'|'\r'|'\n')+ {$channel=HIDDEN;};
//creates token, sends it to the hidden channel.

WS: (' '|'\t'|'\r'|'\n')+ {skip();};
// matches whitespace but doesn't create a token

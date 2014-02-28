package textproc_test

import (
	"github.com/MihaiB/mihaib/toolbox/textproc"
	"io"
	"io/ioutil"
	"strings"
	"testing"
)

// Reads and concatenates all tokens. If tr reaches EOF, err is nil.
func readTokenReader(tr textproc.TokenReader) (string, error) {
	s := ""
	for {
		tok, err := tr.ReadToken()
		s += string(tok)
		if err != nil {
			if err == io.EOF {
				err = nil
			}
			return s, err
		}
	}
}

func checkTokenReaderEOF(t *testing.T, r textproc.TokenReader) {
	for i := 0; i < 2; i++ {
		tok, err := r.ReadToken()
		if len(tok) != 0 || err != io.EOF {
			t.Error("Bad token or err:", tok, err)
		}
	}
}

func checkTokenReaderCustomErr(t *testing.T, r textproc.TokenReader) {
	for i := 0; i < 2; i++ {
		tok, err := r.ReadToken()
		if len(tok) != 0 || err != customTestingErr {
			t.Error("Bad token or err:", tok, err)
		}
	}
}

func checkReaderEOF(t *testing.T, r io.Reader) {
	b := make([]byte, 10)
	for i := 0; i < 2; i++ {
		n, err := r.Read(b)
		if n != 0 || err != io.EOF {
			t.Error("Bad n or err:", n, err)
		}
	}
}

func checkReaderCustomErr(t *testing.T, r io.Reader) {
	b := make([]byte, 10)
	for i := 0; i < 2; i++ {
		n, err := r.Read(b)
		if n != 0 || err != customTestingErr {
			t.Error("Bad n or err:", n, err)
		}
	}
}

// Constructor returning a new TokenReader chained after its parameter
type trCons func(textproc.TokenReader) textproc.TokenReader

// Chain filters using several NewFixedTokenSize's, with and without a
// newErrTokenReader. Pass string through the chain and check the result.
func chainS(t *testing.T, in string, out string, cons ...trCons) {
	for sz := 1; sz <= 10; sz++ {
		for _, forceErr := range []bool{false, true} {
			tail := textproc.NewTokenReader(strings.NewReader(in))
			if forceErr {
				tail = newErrTokenReader(tail)
			}
			tail = textproc.NewFixedTokenSize(tail, sz)
			for _, c := range cons {
				tail = c(tail)
			}

			tailR := textproc.NewReader(tail)
			bytes, err := ioutil.ReadAll(tailR)
			if !forceErr && err != nil {
				t.Error("Error", err, "reading from chain")
			}
			if forceErr && err != customTestingErr {
				t.Error("Error", err, "want", customTestingErr)
			}

			if !forceErr {
				checkTokenReaderEOF(t, tail)
				checkReaderEOF(t, tailR)
			} else {
				checkTokenReaderCustomErr(t, tail)
				checkReaderCustomErr(t, tailR)
			}

			got := string(bytes)
			if got != out {
				t.Errorf("Got %#v want %#v", got, out)
			}
		}
	}
}

// Like chainS, but checks the tokens.
func chainT(t *testing.T, in string, data [][]rune, cons ...trCons) {
	for sz := 1; sz <= 10; sz++ {
		for _, forceErr := range []bool{false, true} {
			tail := textproc.NewTokenReader(strings.NewReader(in))
			if forceErr {
				tail = newErrTokenReader(tail)
			}
			tail = textproc.NewFixedTokenSize(tail, sz)
			for _, c := range cons {
				tail = c(tail)
			}

			var got []rune
			var err error
			for _, want := range data {
				if err != nil {
					t.Error("Unexpected error", err)
					return
				}
				got, err = tail.ReadToken()
				if string(got) != string(want) {
					t.Error("Want", want, "got", got)
					return
				}
			}

			// The error may come with the last valid token or not.
			// If it did, check what it is.
			if err != nil {
				if !forceErr && err != io.EOF {
					t.Error("Error", err, "reading chainT")
				}
				if forceErr && err != customTestingErr {
					t.Error("Error", err, "want",
						customTestingErr)
				}
			}
			if !forceErr {
				checkTokenReaderEOF(t, tail)
			} else {
				checkTokenReaderCustomErr(t, tail)
			}
		}
	}
}

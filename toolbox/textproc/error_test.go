package textproc_test

import (
	"errors"
	"github.com/MihaiB/toolbox/textproc"
	"io"
	"strings"
	"testing"
)

var customTestingErr = errors.New("Custom testing error")

// Returns a new TokenReader which returns customTestingErr instead of EOF
func newErrTokenReader(r textproc.TokenReader) textproc.TokenReader {
	return &errTokenReader{r: r}
}

type errTokenReader struct {
	r textproc.TokenReader
}

func (r *errTokenReader) ReadToken() ([]rune, error) {
	tok, err := r.r.ReadToken()
	if err == io.EOF {
		err = customTestingErr
	}
	return tok, err
}

func TestErrTokenReader(t *testing.T) {
	for _, s := range []string{"", "€\n§\tabc "} {
		tr := textproc.NewTokenReader(strings.NewReader(s))
		tr = newErrTokenReader(tr)
		got, err := readTokenReader(tr)
		if got != s || err != customTestingErr {
			t.Errorf("Want %#v got %#v err %v", s, got, err)
		}
	}
}

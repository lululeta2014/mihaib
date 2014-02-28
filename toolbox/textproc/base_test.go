// Use a _test package to avoid collisions between top-levels in the testing
// code and top-levels in the tested code.
package textproc_test

import (
	"github.com/MihaiB/mihaib/toolbox/textproc"
	"io"
	"io/ioutil"
	"strings"
	"testing"
	"unicode"
)

func TestNewTokenReader(t *testing.T) {
	for _, want := range []string{"", "$€§A\nb", "¡\uFFFD¿"} {
		for bufSz := 1; bufSz <= 10; bufSz++ {
			r := textproc.NewTokenReader(
				newSizeReader(strings.NewReader(want), bufSz))
			got, err := readTokenReader(r)
			if got != want || err != nil {
				t.Errorf("Bad result or err %#v, %v", got, err)
			}
			checkTokenReaderEOF(t, r)
		}
	}

	// These strings can't include a properly encoded unicode replacement
	// char because we're using a range loop over a string which can't
	// differentiate between properly encoded ones and the ones generated
	// because of invalid byte sequences.
	for _, s := range []string{string("€"[0:1]), string("€"[0:2]),
		"\xff", "\xcc", "a°\xcc", "1«\xcc2", "\xcc3"} {
		want, wantErr := "", textproc.ErrInvalidUTF8
		for _, cp := range s {
			if cp == unicode.ReplacementChar {
				break
			}
			want += string(cp)
		}
		for bufSz := 1; bufSz <= 10; bufSz++ {
			r := textproc.NewTokenReader(
				newSizeReader(strings.NewReader(s), bufSz))
			got, err := readTokenReader(r)
			if got != want || err != wantErr {
				t.Errorf("%#v: want %#v, %#v; err %v, %v",
					s, want, got, wantErr, err)
			}

			tok, err := r.ReadToken()
			if len(tok) > 0 || err != wantErr {
				t.Errorf("Bad token or err: %#v, %v", tok, err)
			}
		}
	}
}

func TestNewReader(t *testing.T) {
	for _, s := range []string{"", "a", "ab", "°",
		"$€§A\nb", "¡\uFFFD¿"} {
		tr := textproc.NewTokenReader(strings.NewReader(s))
		r := textproc.NewReader(tr)
		bytes, err := ioutil.ReadAll(r)
		if err != nil {
			t.Error("Error", err)
		}
		if string(bytes) != s {
			t.Error("Got", string(bytes), "instead of", s)
		}
		checkReaderEOF(t, r)
	}
}

func TestNewReader2(t *testing.T) {
	readWithSize := func(r io.Reader, sz int) ([]byte, error) {
		var result []byte
		buf := make([]byte, sz)
		n, err := r.Read(buf)
		for n > 0 {
			result = append(result, buf[:n]...)
			n, err = r.Read(buf)
		}
		if err == io.EOF {
			err = nil
		}
		return result, err
	}
	for bufsize := 1; bufsize < 5; bufsize++ {
		for _, s := range []string{"", "a", "ab", "°",
			"$€§A\nb", "¡\uFFFD¿"} {
			tr := textproc.NewTokenReader(strings.NewReader(s))
			r := textproc.NewReader(tr)
			bytes, err := readWithSize(r, bufsize)
			if err != nil {
				t.Error("Error", err)
			}
			if string(bytes) != s {
				t.Error("Got", string(bytes), "instead of", s)
			}
			checkReaderEOF(t, r)
		}
	}
}

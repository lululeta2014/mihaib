package textproc_test

import (
	"io"
	"strings"
	"testing"
)

// io.Reader returning at most size bytes on each Read. If < 1, size becomes 1.
func newSizeReader(r io.Reader, size int) io.Reader {
	if size < 1 {
		size = 1
	}
	return &sizeReader{r: r, sz: size}
}

type sizeReader struct {
	r   io.Reader
	buf []byte
	sz  int
	err error
}

func (r *sizeReader) Read(dest []byte) (int, error) {
	if len(r.buf) == 0 && r.err == nil {
		r.buf = make([]byte, r.sz)
		var n int
		n, r.err = r.r.Read(r.buf)
		r.buf = r.buf[:n]
	}

	if len(r.buf) > 0 {
		n := copy(dest, r.buf)
		r.buf = r.buf[n:]
		return n, nil
	}

	if r.err == nil {
		r.err = io.EOF
	}
	return 0, r.err
}

func TestSizeReader(t *testing.T) {
	check := func(s string, sz int) {
		var r io.Reader = strings.NewReader(s)
		r = newSizeReader(r, sz)
		if sz < 1 {
			sz = 1
		}
		got := []byte{}
		buf := make([]byte, sz)
		first := true
		firstRead := sz
		if len(s) < sz {
			firstRead = len(s)
		}

		for {
			n, err := r.Read(buf)
			if first && n < firstRead {
				t.Error("Read", n, "not", firstRead, "bytes")
			}
			first = false
			got = append(got, buf[:n]...)
			if err != nil {
				if err != io.EOF {
					t.Error("Unexpected err", err)
				}
				break
			}
		}
		if string(got) != s {
			t.Errorf("Expected %#v, got %#v", s, string(got))
		}
	}
	for _, s := range []string{"", "a", "¡€§!♯♭"} {
		for sz := range [10]bool{} {
			check(s, sz)
		}
	}
}

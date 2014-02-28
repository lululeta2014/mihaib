package textproc

import (
	"errors"
	"io"
	"unicode/utf8"
)

var (
	ErrInvalidUTF8 = errors.New("Invalid UTF-8")
)

// TokenReader wraps the basic ReadToken method.
//
// ReadToken returns the next token and any error, similar to io.Reader's
// conventions. If len(token) == 0 then err != nil.
type TokenReader interface {
	ReadToken() (token []rune, err error)
}

// NewTokenReader returns a new TokenReader which produces tokens of arbitrary
// length with the runes in r.
//
// It stops at the first invalid UTF-8 byte returning an empty slice and
// ErrInvalidUTF8. Subsequent calls to ReadToken will return the same result.
func NewTokenReader(r io.Reader) TokenReader {
	return &newTokenReader{r: r}
}

type newTokenReader struct {
	r          io.Reader
	bufStorage [4096]byte
	bufView    []byte
	err        error
}

func (tr *newTokenReader) ReadToken() ([]rune, error) {
	var token []rune = nil
	firstRead := true
	for tr.err == nil && !utf8.FullRune(tr.bufView) {
		if firstRead {
			old := copy(tr.bufStorage[:], tr.bufView)
			tr.bufView = tr.bufStorage[:old]
		}
		firstRead = false

		var n int
		n, tr.err = tr.r.Read(tr.bufStorage[len(tr.bufView):])
		tr.bufView = tr.bufStorage[:len(tr.bufView)+n]
	}
	for utf8.FullRune(tr.bufView) {
		rn, n := utf8.DecodeRune(tr.bufView)
		if rn == utf8.RuneError && n == 1 {
			tr.err = ErrInvalidUTF8
			break
		}
		token = append(token, rn)
		tr.bufView = tr.bufView[n:]
	}
	if len(token) > 0 {
		return token, nil
	}

	// now tr.err != nil
	if len(tr.bufView) > 0 {
		if tr.err == io.EOF {
			// underlying reader depleted without error; the end
			// of the stream has an incomplete UTF-8 prefix
			// because utf8.FullRune() == false
			tr.err = ErrInvalidUTF8
		}
	}
	tr.bufView = nil
	return nil, tr.err
}

// NewReader returns a new io.Reader which produces the bytes of the
// concatenated tokens.
func NewReader(tr TokenReader) io.Reader {
	return &newReader{tr: tr}
}

type newReader struct {
	tr  TokenReader
	buf []byte // already read from tr and buffered
	err error  // final error, after writing everything
}

func (nr *newReader) Read(p []byte) (n int, err error) {
	// Reader depleted and all bytes already written
	if nr.err != nil {
		return 0, nr.err
	}

	for n < len(p) && nr.err == nil {
		inc := copy(p[n:], nr.buf)
		n += inc
		nr.buf = nr.buf[inc:]
		if len(nr.buf) == 0 {
			token, err := nr.tr.ReadToken()
			if len(token) > 0 {
				nr.buf = []byte(string(token))
			} else {
				nr.err = err
			}
		}
	}

	if n > 0 {
		return n, nil
	}
	return 0, nr.err
}

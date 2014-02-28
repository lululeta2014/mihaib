package main

import (
	"errors"
	"fmt"
	"github.com/MihaiB/toolbox/textproc"
	"io"
	"strings"
)

// tokenReaderFactory defines the signature of a function that returns a new
// textproc.TokenReader at each invocation. The returned token reader processes
// the token stream from the token reader passed as argument.
type tokenReaderFactory func(textproc.TokenReader) textproc.TokenReader

// tokenReaderAbstractFactory defines the signature of a function that
// takes user-passed parameters and returns a tokenReaderFactory.
type tokenReaderAbstractFactory func(...string) (tokenReaderFactory, error)

// Turn a tokenReaderFactory into a tokenReaderAbstractFactory which always
// returns the original tokenReaderFactory or nil and an error if any
// parameters are passed.
func absFactFromFact(fact tokenReaderFactory) tokenReaderAbstractFactory {
	return func(opts ...string) (tokenReaderFactory, error) {
		if len(opts) != 0 {
			return nil, errors.New(fmt.Sprintf(
				"Takes no options but found %v: %#v",
				len(opts), strings.Join(opts, ":")))
		}
		return fact, nil
	}
}

// Creates a TokenReader from each factory and chains them.
// The input is converted to a TokenReader and back to an io.Reader for the
// return value.
func chain(r io.Reader, factories ...tokenReaderFactory) io.Reader {
	tail := textproc.NewTokenReader(r)
	for _, fact := range factories {
		tail = fact(tail)
	}
	return textproc.NewReader(tail)
}

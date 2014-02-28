package main

import (
	"github.com/MihaiB/toolbox/textproc"
	"testing"
)

func TestAbsFactFromFact(t *testing.T) {
	myFact := func(textproc.TokenReader) textproc.TokenReader {
		return nil
	}

	absFact := absFactFromFact(myFact)

	var result tokenReaderFactory
	var err error
	result, err = absFact()
	if result == nil || err != nil {
		t.Error("nil result or non-nil error", result, err)
	}
	result, err = absFact("some arg")
	if result != nil || err == nil {
		t.Error("non-nil result or nil error", result, err)
	}
}

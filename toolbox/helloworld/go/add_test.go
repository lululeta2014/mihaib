package main

import (
	"fmt"
	"testing"
)

func TestAdd(t *testing.T) {
	got, want := Add(2, 3), 5
	if got != want {
		t.Error("Got", got, "want", want)
	}
}

func ExampleAdd() {
	fmt.Println(Add(2, 7))
	// Output: 9
}

// Package textproc provides utilities for processing UTF-8 text
// by chaining together TokenReader objects which act as filters.
// The interfaces use runes to avoid dealing with non-UTF-8 byte streams.
// The package provides utilities for converting between its interfaces and
// io.Reader.
//
// Each filter produces a stream of tokens. A token is a non-empty []rune.
package textproc

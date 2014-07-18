package kvstore

import (
	"errors"
)

// A basic key-value store. Both keys and values may be empty (nil or an empty
// slice, which are treated identically). Get and Delete return ErrNotFound
// if the key doesn't exist.
type KVStore interface {
	Set(key []rune, value []rune) error
	Get(key []rune) (value []rune, err error)
	Delete(key []rune) error
}

var (
	ErrNotFound = errors.New("not found")
)

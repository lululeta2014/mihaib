package kvstore

import (
	"errors"
)

var (
	ErrNotFound          = errors.New("not found")
	ErrNestedTransaction = errors.New("cannot start a transaction within a transaction")
	ErrNoTransaction     = errors.New("no transaction is active")
)

// A basic key-value store. Both keys and values may be empty (nil or an empty
// slice, which are treated identically). Get and Delete return ErrNotFound
// if the key doesn't exist.
type KVStore interface {
	Set(key, value []rune) error
	Get(key []rune) (value []rune, err error)
	Delete(key []rune) error
}

// A transactional key-value store. All operations between Begin() and Commit()
// or Rollback() form a transaction.
//
// Calling Begin() during a transaction returns ErrNestedTransaction.
// Calling Commit() or Rollback() outside a transaction returns
// ErrNoTransaction.
//
// During a transaction, after calling Set() or Delete(), the return values
// for subsequent Get() or Delete() calls with that same key are undefined.
// This is done to maintain compatibility with as many backends as possible.
// E.g. for Get(), the Google App Engine datastore returns the value from the
// beginning of the transaction, while other databases return the value passed
// to Set().
// The operations will produce the expected result once the transaction is
// committed, only the return values are undefined in the situation above.
type TransStore interface {
	KVStore
	Begin() error
	Commit() error
	Rollback() error
}

func NewPrefixStore(s KVStore, prefix []rune) KVStore {
	return &prefixStore{s, append(prefix, []rune(nil)...)}
}

type prefixStore struct {
	s      KVStore
	prefix []rune
}

func (s *prefixStore) addPrefix(key []rune) []rune {
	return append(s.prefix, key...)
}

func (s *prefixStore) Set(key, value []rune) error {
	return s.s.Set(s.addPrefix(key), value)
}

func (s *prefixStore) Get(key []rune) (value []rune, err error) {
	return s.s.Get(s.addPrefix(key))
}

func (s *prefixStore) Delete(key []rune) error {
	return s.s.Delete(s.addPrefix(key))
}

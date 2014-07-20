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
// or Rollback() form a transaction. Any method call M() from KVStore, if
// called outside of a Begin … Commit or Rollback sequence, forms a transaction
// (i.e. M() is equivalent to Begin(); M(); Commit()).
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
// committed, it's only the return values which are undefined in the situation
// above.
type TransStore interface {
	KVStore
	Begin() error
	Commit() error
	Rollback() error
}

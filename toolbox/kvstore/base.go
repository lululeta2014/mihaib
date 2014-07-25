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
// ErrNoTransaction. InTransaction() shows if a transaction is in progress.
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
	InTransaction() bool
}

// Create a prefixHelper, used by the the two Prefix Stores.
// Encapsulate the boilerplate slice copying.
func newPrefixHelper(prefix []rune) *prefixHelper {
	return &prefixHelper{append([]rune(nil), prefix...)}
}

type prefixHelper struct {
	prefix []rune
}

func (p *prefixHelper) addPrefix(key []rune) []rune {
	result := make([]rune, 0, len(p.prefix)+len(key))
	result = append(result, p.prefix...)
	result = append(result, key...)
	return result
}

// Return a new KVStore which forwards all operation to s after prepending
// prefix to keys.
//
// Can be used to create isolated namespaces if the prefix is not empty and the
// user ensures collisions won't happen. E.g. prefix ‘ab’ and key ‘c’ collides
// with prefix ‘a’ and key ‘bc’.
func NewPrefixStore(s KVStore, prefix []rune) KVStore {
	return &prefixStore{s, newPrefixHelper(prefix)}
}

type prefixStore struct {
	s KVStore
	*prefixHelper
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

// Like NewPrefixStore, but for a TransStore.
func NewTransPrefixStore(s TransStore, prefix []rune) TransStore {
	return &transPrefixStore{s, newPrefixHelper(prefix)}
}

type transPrefixStore struct {
	TransStore
	*prefixHelper
}

func (s *transPrefixStore) Set(key, value []rune) error {
	return s.TransStore.Set(s.addPrefix(key), value)
}

func (s *transPrefixStore) Get(key []rune) (value []rune, err error) {
	return s.TransStore.Get(s.addPrefix(key))
}

func (s *transPrefixStore) Delete(key []rune) error {
	return s.TransStore.Delete(s.addPrefix(key))
}

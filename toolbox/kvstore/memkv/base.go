package memkv

import (
	"github.com/MihaiB/mihaib/toolbox/kvstore"
	"sync"
)

// Returns a new KVStore backed by an in-memory data structure.
func NewKVStore() kvstore.KVStore {
	return &kvstoreT{m: map[string]string{}}
}

type kvstoreT struct {
	// can also be map[string][]rune. string vals ensure we store a copy
	// of the mutable slice value passed to-and-from the client.
	m map[string]string
}

func (s *kvstoreT) Set(key, value []rune) error {
	s.m[string(key)] = string(value)
	return nil
}

func (s *kvstoreT) Get(key []rune) (value []rune, err error) {
	v, ok := s.m[string(key)]
	if ok {
		return []rune(v), nil
	}
	return nil, kvstore.ErrNotFound
}

func (s *kvstoreT) Delete(key []rune) error {
	str := string(key)
	_, ok := s.m[str]
	if ok {
		delete(s.m, str)
		return nil
	}
	return kvstore.ErrNotFound
}

// An in-memory backing store for which you can obtain multiple transactional
// clients. Values of this type are safe for concurrent use by multiple
// goroutines.
type TransBackingStore interface {
	NewTransClient() kvstore.TransStore
}

// Return a new TransBackingStore.
func NewTransBackingStore() TransBackingStore {
	return &transBackStoreT{kvs: NewKVStore(), lock: &sync.Mutex{}}
}

type transBackStoreT struct {
	kvs  kvstore.KVStore
	lock sync.Locker // guards any access to kvs
}

func (s *transBackStoreT) NewTransClient() kvstore.TransStore {
	return &transClientT{
		store:       s,
		newData:     map[string]string{},
		deletedData: map[string]interface{}{},
	}
}

// Transactional store. Acquires the backing store lock on transaction start
// and releases it on commit or rollback. Get() always uses the store state
// from the beginning of the transaction.
type transClientT struct {
	store         *transBackStoreT
	inTransaction bool
	newData       map[string]string
	deletedData   map[string]interface{}
}

func (c *transClientT) InTransaction() bool {
	return c.inTransaction
}

func (c *transClientT) Begin() error {
	if c.inTransaction {
		return kvstore.ErrNestedTransaction
	}
	c.store.lock.Lock()
	c.inTransaction = true
	return nil
}

func (c *transClientT) Rollback() error {
	if !c.inTransaction {
		return kvstore.ErrNoTransaction
	}
	c.newData = map[string]string{}
	c.deletedData = map[string]interface{}{}
	c.inTransaction = false
	c.store.lock.Unlock()
	return nil
}

func (c *transClientT) Commit() error {
	if !c.inTransaction {
		return kvstore.ErrNoTransaction
	}

	for k, v := range c.newData {
		c.store.kvs.Set([]rune(k), []rune(v))
	}
	for k := range c.deletedData {
		c.store.kvs.Delete([]rune(k))
	}

	c.newData = map[string]string{}
	c.deletedData = map[string]interface{}{}
	c.inTransaction = false
	c.store.lock.Unlock()
	return nil
}

func (c *transClientT) Get(key []rune) (value []rune, err error) {
	if !c.InTransaction() {
		c.Begin()
		defer c.Commit()
	}
	return c.store.kvs.Get(key)
}

func (c *transClientT) Set(key, value []rune) error {
	if !c.InTransaction() {
		c.Begin()
		defer c.Commit()
	}
	c.newData[string(key)] = string(value)
	delete(c.deletedData, string(key))
	return nil
}

func (c *transClientT) Delete(key []rune) error {
	if !c.InTransaction() {
		c.Begin()
		defer c.Commit()
	}
	c.deletedData[string(key)] = nil
	delete(c.newData, string(key))

	_, err := c.store.kvs.Get(key)
	return err
}

package memkv

import (
	"github.com/MihaiB/mihaib/toolbox/kvstore"
)

func NewKVStore() kvstore.KVStore {
	return &kvstoreT{m: map[string]string{}}
}

type kvstoreT struct {
	// can also be map[string][]rune. string vals ensure we store a copy
	// of the mutable slice value passed to-and-from the client.
	m map[string]string
}

func (s *kvstoreT) Set(key []rune, value []rune) error {
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

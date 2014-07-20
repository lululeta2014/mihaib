package memkv_test

import (
	"github.com/MihaiB/mihaib/toolbox/kvstore"
	"github.com/MihaiB/mihaib/toolbox/kvstore/memkv"
	"github.com/MihaiB/mihaib/toolbox/kvstore/test"
	"testing"
)

func TestMemKV(t *testing.T) {
	kvs := memkv.NewKVStore()
	test.TestKVStore(t, kvs)
}

func TestTransStore(t *testing.T) {
	bk := memkv.NewTransBackingStore()
	stores := []kvstore.TransStore{}
	for _ = range [5]bool{} {
		stores = append(stores, bk.NewTransClient())
	}
	test.TestTransStore(t, stores...)
}

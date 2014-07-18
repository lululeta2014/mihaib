package memkv_test

import (
	"github.com/MihaiB/mihaib/toolbox/kvstore/memkv"
	"github.com/MihaiB/mihaib/toolbox/kvstore/test"
	"testing"
)

func TestMemKV(t *testing.T) {
	kvs := memkv.NewKVStore()
	test.TestKVStore(t, kvs)
}

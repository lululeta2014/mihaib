package test

import (
	"github.com/MihaiB/mihaib/toolbox/kvstore"
	"testing"
)

func TestKVStore(t *testing.T, kvs kvstore.KVStore) {
	var (
		err error
		v   []rune
	)
	checkNoErr := func() {
		if err != nil {
			t.Error(err.Error())
		}
	}

	checkErr := func(want error) {
		if err != want {
			t.Errorf("Got %v, want %v", err, want)
		}
	}

	checkLen := func(n int) {
		if len(v) != n {
			t.Errorf("Got len %v, want %v", len(v), n)
		}
	}

	checkVal := func(want string) {
		if string(v) != want {
			t.Errorf("Got %v, want %v", string(v), want)
		}
	}

	// nil key
	err = kvs.Set(nil, nil)
	checkNoErr()
	v, err = kvs.Get(nil)
	checkNoErr()
	checkLen(0)
	v, err = kvs.Get([]rune{})
	checkNoErr()
	checkLen(0)
	err = kvs.Delete([]rune{})
	checkNoErr()
	err = kvs.Delete([]rune{})
	checkErr(kvstore.ErrNotFound)
	err = kvs.Delete(nil)
	checkErr(kvstore.ErrNotFound)

	err = kvs.Set(nil, []rune("Today"))
	checkNoErr()
	v, err = kvs.Get(nil)
	checkNoErr()
	checkVal("Today")
	err = kvs.Delete([]rune{})
	checkNoErr()
	err = kvs.Delete([]rune{})
	checkErr(kvstore.ErrNotFound)

	// empty key
	err = kvs.Set([]rune{}, []rune{})
	checkNoErr()
	v, err = kvs.Get(nil)
	checkNoErr()
	checkLen(0)
	v, err = kvs.Get([]rune{})
	checkNoErr()
	checkLen(0)
	err = kvs.Delete(nil)
	checkNoErr()
	err = kvs.Delete(nil)
	checkErr(kvstore.ErrNotFound)
	err = kvs.Delete([]rune{})
	checkErr(kvstore.ErrNotFound)

	err = kvs.Set([]rune{}, []rune("Tomorrow"))
	checkNoErr()
	v, err = kvs.Get([]rune{})
	checkNoErr()
	checkVal("Tomorrow")
	err = kvs.Delete([]rune(""))
	checkNoErr()
	err = kvs.Delete(nil)
	checkErr(kvstore.ErrNotFound)

	// nil/empty key, non-empty value
	err = kvs.Set(nil, []rune("me"))
	checkNoErr()
	err = kvs.Set([]rune{'a'}, []rune{'b'})
	checkNoErr()
	v, err = kvs.Get(nil)
	checkNoErr()
	checkVal("me")
	v, err = kvs.Get([]rune{})
	checkNoErr()
	checkVal("me")
	v, err = kvs.Get([]rune{'a'})
	checkNoErr()
	checkVal("b")
	err = kvs.Delete(nil)
	checkNoErr()
	err = kvs.Delete([]rune("a"))
	checkNoErr()
	err = kvs.Delete(nil)
	checkErr(kvstore.ErrNotFound)
	err = kvs.Delete([]rune("a"))
	checkErr(kvstore.ErrNotFound)
	v, err = kvs.Get(nil)
	checkErr(kvstore.ErrNotFound)
	checkVal("")
	v, err = kvs.Get([]rune{'a'})
	checkErr(kvstore.ErrNotFound)
	checkVal("")

	// set, get, delete
	err = kvs.Set([]rune("hello"), []rune("world"))
	checkNoErr()
	v, err = kvs.Get([]rune("hello"))
	checkNoErr()
	checkVal("world")
	err = kvs.Set([]rune("Hello"), []rune("World"))
	checkNoErr()
	v, err = kvs.Get([]rune("hello"))
	checkNoErr()
	checkVal("world")
	v, err = kvs.Get([]rune("Hello"))
	checkNoErr()
	checkVal("World")
	err = kvs.Set([]rune("Hello"), []rune("WORLD"))
	checkNoErr()
	v, err = kvs.Get([]rune("Hello"))
	checkNoErr()
	checkVal("WORLD")

	v, err = kvs.Get([]rune("Hola"))
	checkErr(kvstore.ErrNotFound)
	checkLen(0)
	err = kvs.Delete([]rune("Hi"))
	checkErr(kvstore.ErrNotFound)
	err = kvs.Delete([]rune("hello"))
	checkNoErr()
	err = kvs.Delete([]rune("hello"))
	checkErr(kvstore.ErrNotFound)
	err = kvs.Delete([]rune("Hello"))
	checkNoErr()
}

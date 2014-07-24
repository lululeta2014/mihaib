package test

import (
	"github.com/MihaiB/mihaib/toolbox/kvstore"
	"strconv"
	"sync"
	"testing"
)

func TestKVStore(t *testing.T, s kvstore.KVStore) {
	testBasicKVStore(t, s)
	testPrefixStore(t, s)
}

// Test the basic KVStore methods, with nothing on top of it
func testBasicKVStore(t *testing.T, s kvstore.KVStore) {
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
			t.Errorf("Got error %v, want %v", err, want)
		}
	}

	checkLen := func(n int) {
		x := len(v)
		if x != n {
			t.Errorf("Got len %v, want %v", x, n)
		}
	}

	checkVal := func(want string) {
		s := string(v)
		if s != want {
			t.Errorf("Got %#v, want %#v", s, want)
		}
	}

	// nil key
	err = s.Set(nil, nil)
	checkNoErr()
	v, err = s.Get(nil)
	checkNoErr()
	checkLen(0)
	v, err = s.Get([]rune{})
	checkNoErr()
	checkLen(0)
	err = s.Delete([]rune{})
	checkNoErr()
	err = s.Delete([]rune{})
	checkErr(kvstore.ErrNotFound)
	err = s.Delete(nil)
	checkErr(kvstore.ErrNotFound)

	err = s.Set(nil, []rune("Today"))
	checkNoErr()
	v, err = s.Get(nil)
	checkNoErr()
	checkVal("Today")
	err = s.Delete([]rune{})
	checkNoErr()
	err = s.Delete([]rune{})
	checkErr(kvstore.ErrNotFound)

	// empty key
	err = s.Set([]rune{}, []rune{})
	checkNoErr()
	v, err = s.Get(nil)
	checkNoErr()
	checkLen(0)
	v, err = s.Get([]rune{})
	checkNoErr()
	checkLen(0)
	err = s.Delete(nil)
	checkNoErr()
	err = s.Delete(nil)
	checkErr(kvstore.ErrNotFound)
	err = s.Delete([]rune{})
	checkErr(kvstore.ErrNotFound)

	err = s.Set([]rune{}, []rune("Tomorrow"))
	checkNoErr()
	v, err = s.Get([]rune{})
	checkNoErr()
	checkVal("Tomorrow")
	err = s.Delete([]rune(""))
	checkNoErr()
	err = s.Delete(nil)
	checkErr(kvstore.ErrNotFound)

	// nil/empty key, non-empty value
	err = s.Set(nil, []rune("me"))
	checkNoErr()
	err = s.Set([]rune{'a'}, []rune{'b'})
	checkNoErr()
	v, err = s.Get(nil)
	checkNoErr()
	checkVal("me")
	v, err = s.Get([]rune{})
	checkNoErr()
	checkVal("me")
	v, err = s.Get([]rune{'a'})
	checkNoErr()
	checkVal("b")
	err = s.Delete(nil)
	checkNoErr()
	err = s.Delete([]rune("a"))
	checkNoErr()
	err = s.Delete(nil)
	checkErr(kvstore.ErrNotFound)
	err = s.Delete([]rune("a"))
	checkErr(kvstore.ErrNotFound)
	v, err = s.Get(nil)
	checkErr(kvstore.ErrNotFound)
	checkVal("")
	v, err = s.Get([]rune{'a'})
	checkErr(kvstore.ErrNotFound)
	checkVal("")

	// set, get, delete
	err = s.Set([]rune("hello"), []rune("world"))
	checkNoErr()
	v, err = s.Get([]rune("hello"))
	checkNoErr()
	checkVal("world")
	err = s.Set([]rune("Hello"), []rune("World"))
	checkNoErr()
	v, err = s.Get([]rune("hello"))
	checkNoErr()
	checkVal("world")
	v, err = s.Get([]rune("Hello"))
	checkNoErr()
	checkVal("World")
	err = s.Set([]rune("Hello"), []rune("WORLD"))
	checkNoErr()
	v, err = s.Get([]rune("Hello"))
	checkNoErr()
	checkVal("WORLD")

	v, err = s.Get([]rune("Hola"))
	checkErr(kvstore.ErrNotFound)
	checkLen(0)
	err = s.Delete([]rune("Hi"))
	checkErr(kvstore.ErrNotFound)
	err = s.Delete([]rune("hello"))
	checkNoErr()
	err = s.Delete([]rune("hello"))
	checkErr(kvstore.ErrNotFound)
	err = s.Delete([]rune("Hello"))
	checkNoErr()

	// non-ascii
	err = s.Set([]rune("α"), []rune("β"))
	checkNoErr()
	v, err = s.Get([]rune("α"))
	checkNoErr()
	checkVal("β")
	err = s.Delete([]rune("α"))
	checkNoErr()
	v, err = s.Get([]rune("α"))
	checkLen(0)
	checkErr(kvstore.ErrNotFound)
	err = s.Delete([]rune("α"))
	checkErr(kvstore.ErrNotFound)
}

// Make a PrefixStore on top of the basic store
func testPrefixStore(t *testing.T, s kvstore.KVStore) {
	for _, pref := range [][]rune{nil, []rune(""), []rune("!"),
		[]rune("§Pref°")} {
		prefS := kvstore.NewPrefixStore(s, pref)
		testBasicKVStore(t, prefS)

		for _, k := range [][]rune{nil, []rune(""), []rune("∞≈")} {
			underlyingKey := append(pref, k...)
			val := []rune("€¢")
			err := prefS.Set(k, val)
			if err != nil {
				t.Error(err.Error())
			}
			got, err := s.Get(underlyingKey)
			if err != nil {
				t.Error(err.Error())
			}
			if string(got) != string(val) {
				t.Errorf("Want %v got %v", got, val)
			}
			err = prefS.Delete(k)
			if err != nil {
				t.Error(err.Error())
			}
		}
	}
}

// Test one or more transactional stores pointing to the same backing store.
func TestTransStore(t *testing.T, stores ...kvstore.TransStore) {
	if len(stores) < 1 {
		t.Error("Must pass at least one TransStore to test")
	}
	for _, s := range stores {
		TestKVStore(t, s)
	}

	for _, s := range stores {
		var err error

		for _, f := range []func() error{s.Rollback, s.Commit} {
			err = f()
			if err != kvstore.ErrNoTransaction {
				t.Errorf("Want %v got %v",
					kvstore.ErrNoTransaction, err)
			}
		}

		err = s.Begin()
		if err != nil {
			t.Errorf("On Begin: %v", err.Error())
		}
		err = s.Begin()
		if err != kvstore.ErrNestedTransaction {
			t.Errorf("Want %v got %v",
				kvstore.ErrNestedTransaction, err)
		}
		s.Rollback()
	}

	// The function must run a single transaction.
	// When starting parallel transactions, if the backend guarantees that
	// at least one succeeds (i.e. it doesn't fail them all) then after at
	// most N retries on each of N clients all transactions will succeed.
	checkParallel := func(f func(kvstore.TransStore) error) {
		wg := sync.WaitGroup{}
		for _, s := range stores {
			wg.Add(1)
			go func(s kvstore.TransStore) {
				defer wg.Done()
				var err error
				for _ = range stores {
					err = f(s)
					if err == nil {
						return
					}
				}
				t.Error(err.Error())
			}(s)
		}
		wg.Wait()
	}

	checkParallel(func(s kvstore.TransStore) (err error) {
		defer func() {
			err2 := s.Commit()
			if err == nil {
				err = err2
			}
		}()
		if err = s.Begin(); err != nil {
			return
		}

		k := []rune("N")
		v, err := s.Get(k)
		switch err {
		case nil:
		case kvstore.ErrNotFound:
			v = []rune("0")
		default:
			return
		}

		n, err := strconv.Atoi(string(v))
		if err != nil {
			return
		}
		n++

		err = s.Set(k, []rune(strconv.Itoa(n)))
		return
	})
	func() {
		k := []rune("N")
		val, err := stores[0].Get(k)
		if err != nil {
			t.Fatalf("Reading N: %v", err.Error())
		}
		want, sval := strconv.Itoa(len(stores)), string(val)
		if sval != want {
			t.Fatalf("Parallel inc want %v got %v", want, sval)
		}
		err = stores[0].Delete(k)
		if err != nil {
			t.Fatalf("Delete key N: %v", err.Error())
		}
	}()

	func() {
		k, v := []rune("€"), []rune("¢")
		err := stores[0].Set(k, v)
		if err != nil {
			t.Fatal(err.Error())
		}

		ch := make(chan bool)
		for _, s := range stores {
			go func(s kvstore.TransStore) {
				err := s.Delete(k)
				if err != nil && err != kvstore.ErrNotFound {
					t.Error(err)
				}
				ch <- err == nil
			}(s)
		}
		n := 0
		for _ = range stores {
			if <-ch {
				n++
			}
		}
		if n != 1 {
			t.Fatalf("Deleted by %v goroutines, want 1", n)
		}

		val, err := stores[0].Get(k)
		wantVal, wantErr := "", kvstore.ErrNotFound
		if string(val) != wantVal {
			t.Fatalf("Want %#v got %#v", wantVal, string(val))
		}
		if err != wantErr {
			t.Fatalf("Want error %v got %v", wantErr, err)
		}
	}()
}

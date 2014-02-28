package main

import (
	"testing"
)

func sameSlices(a, b []string) bool {
	if len(a) != len(b) {
		return false
	}
	for i := range a {
		if a[i] != b[i] {
			return false
		}
	}
	return true
}

func TestSameSlices(t *testing.T) {
	for _, data := range [][2][]string{
		{nil, nil}, {nil, []string{}},
		{[]string{"x"}, []string{"x"}},
		{[]string{"hello", "world"}, []string{"hello", "world"}},
	} {
		a, b := data[0], data[1]
		if !sameSlices(a, b) || !sameSlices(b, a) {
			t.Errorf("Comparison failed %#v %#v", a, b)
		}
	}
}

func TestDifferentSlices(t *testing.T) {
	for _, data := range [][2][]string{
		{nil, []string{"z"}}, {[]string{}, []string{"z"}},
		{[]string{"a"}, []string{"b"}},
		{[]string{"a", "b"}, []string{"ab"}},
	} {
		a, b := data[0], data[1]
		if sameSlices(a, b) || sameSlices(b, a) {
			t.Errorf("Comparison failed %#v %#v", a, b)
		}
	}
}

func checkParseFilter(t *testing.T,
	s string, items []string, withErr bool) {
	gotItems, gotErr := parseFilter(s)
	if !sameSlices(gotItems, items) || (gotErr != nil) != withErr {
		t.Errorf("for %#v expected %#v err=%v, got %#v err is %v",
			s, items, withErr, gotItems, gotErr)
	}
}

func TestParseFilterValid(t *testing.T) {
	checkParseFilter(t, "", nil, false)
	checkParseFilter(t, "\xcc", nil, true)
	checkParseFilter(t, "a\xddb", nil, true)
	checkParseFilter(t, "a\\\xee", nil, true) // invalid utf-8 after ‘\’

	checkParseFilter(t, "jack", []string{"jack"}, false)
	checkParseFilter(t, "add:2:int", []string{"add", "2", "int"}, false)
	checkParseFilter(t, "add::int", []string{"add", "", "int"}, false)
	checkParseFilter(t, "20:::7€$", []string{"20", "", "", "7€$"}, false)

	checkParseFilter(t, ":", []string{"", ""}, false)
	checkParseFilter(t, ":::", []string{"", "", "", ""}, false)
	checkParseFilter(t, ":§", []string{"", "§"}, false)
	checkParseFilter(t, "≥:", []string{"≥", ""}, false)
	checkParseFilter(t, ":✓:", []string{"", "✓", ""}, false)

	checkParseFilter(t, `\\`, []string{`\`}, false)
	checkParseFilter(t, `\\tab`, []string{`\tab`}, false)
	checkParseFilter(t, `end\\`, []string{`end\`}, false)
	checkParseFilter(t, `\:`, []string{`:`}, false)
	checkParseFilter(t, `\:tab`, []string{`:tab`}, false)
	checkParseFilter(t, `end\:`, []string{`end:`}, false)
	checkParseFilter(t, `say\::hello`, []string{`say:`, `hello`}, false)
	checkParseFilter(t, `x:\:y`, []string{`x`, `:y`}, false)
	checkParseFilter(t, `x:y\:z`, []string{`x`, `y:z`}, false)
	checkParseFilter(t, `cut:\:,\\:3`, []string{`cut`, `:,\`, `3`}, false)
}

func TestParseFilterInvalid(t *testing.T) {
	checkParseFilter(t, `a\b`, nil, true)
	checkParseFilter(t, `a\`, nil, true)
	checkParseFilter(t, `\`, nil, true)
	checkParseFilter(t, `a:b:,;\`, nil, true)
}

func checkGetFilterFact(t *testing.T, s string, wantFact bool, wantErr bool) {
	fact, err := getFilterFactory(s)
	gotFact, gotErr := fact != nil, err != nil
	if wantFact != gotFact || wantErr != gotErr {
		t.Errorf("%#v want factory=%v err=%v, got factory=%v err=%v",
			s, wantFact, wantErr, gotFact, gotErr)
	}
}

func TestGetFilterFactory(t *testing.T) {
	checkGetFilterFact(t, "", false, true)
	checkGetFilterFact(t, "noSuchFilter", false, true)
	checkGetFilterFact(t, "noSuchFilter:arg1:arg2", false, true)
	checkGetFilterFact(t, `badEscape\`, false, true)

	checkGetFilterFact(t, "nl", true, false)
	checkGetFilterFact(t, "nl:singleBadArg", false, true)
	checkGetFilterFact(t, "nl:multiple:bad:args", false, true)
}

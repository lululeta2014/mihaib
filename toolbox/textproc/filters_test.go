package textproc_test

import (
	"github.com/MihaiB/mihaib/toolbox/textproc"
	"strconv"
	"strings"
	"testing"
)

func TestNewNewlineAtEnd(t *testing.T) {
	cons := textproc.NewNewlineAtEnd
	for _, s := range []string{"\n", "\r\n", " \n", "©\n123\n€\n"} {
		chainS(t, s, s, cons)
	}

	for _, s := range []string{"", "\r", " ", "\n ",
		"\n€", "«\n20\n‰", "a\nb\nc"} {
		want := s
		if len(s) > 0 {
			want += "\n"
		}
		chainS(t, s, want, cons)
	}
}

func TestNewFixedTokenSize(t *testing.T) {
	for _, str := range []string{"", "a", "ab", "a♭c", "¡Hello\nWorld!"} {
		for arg := -1; arg <= 10; arg++ {
			size := arg
			if size < 1 {
				size = 1
			}
			want := [][]rune{}
			runes := []rune(str)
			i := 0
			for ; i < len(runes)/size; i++ {
				want = append(want, runes[i*size:(i+1)*size])
			}
			if len(runes)%size > 0 {
				want = append(want, runes[i*size:])
			}

			getCons := func(n int) trCons {
				return func(r textproc.TokenReader) textproc.TokenReader {
					return textproc.NewFixedTokenSize(r, n)
				}
			}

			chainT(t, str, want, getCons(arg))

			// test by feeding it smaller tokens
			chainT(t, str, want, getCons(arg/3+1), getCons(arg))

			// test by feeding it larger tokens
			chainT(t, str, want, getCons(arg*3+1), getCons(arg))
		}
	}
}

func TestNewFixedTokenSize_same(t *testing.T) {
	str := "Hello§World"
	baseR := textproc.NewTokenReader(strings.NewReader(str))
	for sz := range [10]int{} {
		r := textproc.NewFixedTokenSize(baseR, sz)
		same := textproc.NewFixedTokenSize(r, sz)
		diff := textproc.NewFixedTokenSize(r, sz+2)
		if r != same || r == diff {
			t.Error("Should reuse reader if and only if same size")
		}
	}
}

func TestNewStripTrailingSpace(t *testing.T) {
	cons := textproc.NewStripTrailingSpace
	for in, want := range map[string]string{
		"": "", " ": "", " \t": "", " \r": "",
		"\n": "\n", " \n": "\n", " \t  \n": "\n",
		"\n\n\n": "\n\n\n", "§ \n\n ‰ \n": "§\n\n ‰\n",
		"¡Hello World \t  \n Hi  \n": "¡Hello World\n Hi\n",
		"My Test.":                   "My Test.",
		" Leading WS-":               " Leading WS-",
	} {
		chainS(t, in, want, cons)
	}
}

func TestNewExpandTab(t *testing.T) {
	var tabSize int
	cons := func(r textproc.TokenReader) textproc.TokenReader {
		return textproc.NewExpandTab(r, tabSize)
	}

	for tabSize = -1; tabSize <= 1; tabSize++ {
		for _, in := range []string{
			"", "\t", "\t\n", "\t\n\t", " \t\n\tabc\t\n  \t",
		} {
			want := strings.Replace(in, "\t", " ", -1)
			chainS(t, in, want, cons)
		}
	}

	tabSize = 4
	for in, want := range map[string]string{
		"": "", "\t": "    ", "ab\tc": "ab  c", "abc\t": "abc ",
		"12345\n\t": "12345\n    ", "  \t": "    ",
		"12345\t": "12345   ", "12\t567\t9\t": "12  567 9   ",
		"«§1»\tA\n«2»\t": "«§1»    A\n«2» ",
	} {
		chainS(t, in, want, cons)
	}
}

func TestNewUnixNewline(t *testing.T) {
	for in, want := range map[string]string{
		"": "", "\n": "\n", "\r\n": "\n", "\r": "\n",
		"\r\r": "\n\n", "\r\r\n": "\n\n", "\ra\r\r\r\n": "\na\n\n\n",
		"A\nB\rC\r\rX": "A\nB\nC\n\nX",
	} {
		chainS(t, in, want, textproc.NewUnixNewline)
	}
}

func TestNewStripLeadingSpace(t *testing.T) {
	for in, want := range map[string]string{
		"": "", "\n": "\n", "\t\r": "", " ": "", " \t ": "",
		" \n": "\n", "\ta \n": "a \n",
		"\n\n": "\n\n", "\n \t a b\nc \n\n d": "\na b\nc \n\nd",
		"a b": "a b",
	} {
		chainS(t, in, want, textproc.NewStripLeadingSpace)
	}
}

func TestNewSingleSpace(t *testing.T) {
	for in, want := range map[string]string{
		"": "", " ": " ", "\t": " ", " \t": " ", " \t\n a": " \n a",
		"  ": " ", "  \n  ": " \n ", "a \t b\t  c": "a b c",
	} {
		chainS(t, in, want, textproc.NewSingleSpace)
	}
}

func TestNewLogicalParagraphs(t *testing.T) {
	for in, want := range map[string]string{
		"": "", "\n": "", "\n\n": "", "\n \t\n": "",
		"ABC\n123\nx\n\n\tWhite Fang \n":   "ABC 123 x\nWhite Fang\n",
		" \n \n\n Mary\n Ann \n\t \nParks": "Mary Ann\nParks\n",
	} {
		chainS(t, in, want, textproc.NewLogicalParagraphs)
	}
}

func TestNewFormattedParagraphs(t *testing.T) {
	getCons := func(sz int) func(textproc.TokenReader) textproc.TokenReader {
		return func(r textproc.TokenReader) textproc.TokenReader {
			return textproc.NewFormattedParagraphs(r, sz)
		}
	}
	cons4 := getCons(4)
	for in, want := range map[string]string{
		"": "", "\n": "", "\n\n": "", "\n \t\n": "",
		"ABC 12 x\nWhite Fang\n": "ABC\n12 x\n\nWhite\nFang\n",
		"A\nB": "A\n\nB\n", "LongWord": "LongWord\n",
		"A B": "A B\n", "A BC D E F": "A BC\nD E\nF\n",
		"Mary Ann\nParks\n": "Mary\nAnn\n\nParks\n",
	} {
		chainS(t, in, want, cons4)
	}

	cons1 := getCons(1)
	cons0 := getCons(0)
	consNeg := getCons(-5)
	for in, want := range map[string]string{
		"": "", "\n": "", "\n\n": "", "A BC DEF": "A\nBC\nDEF\n",
		"A BC\nD E": "A\nBC\n\nD\nE\n",
	} {
		chainS(t, in, want, cons1)
		chainS(t, in, want, cons0)
		chainS(t, in, want, consNeg)
	}
}

func TestNewBreakLines(t *testing.T) {
	getCons := func(sz int) func(textproc.TokenReader) textproc.TokenReader {
		return func(r textproc.TokenReader) textproc.TokenReader {
			return textproc.NewBreakLines(r, sz)
		}
	}
	cons4 := getCons(4)
	for in, want := range map[string]string{
		"": "", "\n": "\n", " \n": "\n", "\n  ": "\n",
		"AB \t C  DE F  G\nHIJKLM": "AB C\nDE F\nG\nHIJKLM",
		"12345 §§§§\n\n∞":          "12345\n§§§§\n\n∞",
	} {
		chainS(t, in, want, cons4)
	}

	cons1 := getCons(1)
	cons0 := getCons(0)
	consNeg := getCons(-5)
	for in, want := range map[string]string{
		"": "", "\n": "\n", "  ABC D E": "ABC\nD\nE", "X\n": "X\n",
	} {
		chainS(t, in, want, cons1)
		chainS(t, in, want, cons0)
		chainS(t, in, want, consNeg)
	}
}

func TestNewUpperCase(t *testing.T) {
	for in, want := range map[string]string{
		"": "", "\n": "\n", "a2z": "A2Z", "șĂ€î": "ȘĂ€Î",
	} {
		chainS(t, in, want, textproc.NewUpperCase)
	}
}

func TestNewLowerCase(t *testing.T) {
	for in, want := range map[string]string{
		"": "", "\n": "\n", "A2Z": "a2z", "ȘĂ€Î": "șă€î",
	} {
		chainS(t, in, want, textproc.NewLowerCase)
	}
}

func TestNewTokenCounter(t *testing.T) {
	getCons := func(sz int) func(r textproc.TokenReader) textproc.TokenReader {
		return func(r textproc.TokenReader) textproc.TokenReader {
			return textproc.NewFixedTokenSize(r, sz)
		}
	}
	for _, in := range []string{
		"", " ", " \t\n", "1234", "Jack in the Box!",
	} {
		for sz := 1; sz <= 3; sz++ {
			n := len(in) / sz
			if len(in)%sz > 0 {
				n++
			}
			want := [][]rune{[]rune(strconv.Itoa(n))}
			chainT(t, in, want,
				getCons(sz), textproc.NewTokenCounter)
		}
	}
}

func TestNewWordToken(t *testing.T) {
	for in, want := range map[string][][]rune{
		"": nil, "\n": [][]rune{}, " \t§—€": [][]rune{},
		"Jî-2ș!To do\n": [][]rune{[]rune("Jî"), []rune{'ș'},
			[]rune("To"), []rune("do")},
	} {
		chainT(t, in, want, textproc.NewWordToken)
	}
}

func TestNewJoinTokens(t *testing.T) {
	getFixCons := func(sz int) func(r textproc.TokenReader) textproc.TokenReader {
		return func(r textproc.TokenReader) textproc.TokenReader {
			return textproc.NewFixedTokenSize(r, sz)
		}
	}
	getJoinCons := func(sep []rune) func(r textproc.TokenReader) textproc.TokenReader {
		return func(r textproc.TokenReader) textproc.TokenReader {
			return textproc.NewJoinTokens(r, sep)
		}
	}
	sep := []rune(", ")
	for _, inS := range []string{
		"", "\n", "AB", " ab \t§cdef", "«ready!¿",
	} {
		inRn := []rune(inS)
		for sz := 1; sz <= 5; sz++ {
			want := [][]rune{}
			for i := 0; i < len(inRn); i += sz {
				if i > 0 {
					want = append(want, sep)
				}
				if i+sz <= len(inRn) {
					want = append(want, inRn[i:i+sz])
				} else {
					want = append(want, inRn[i:])
				}
			}
			chainT(t, inS, want, getFixCons(sz), getJoinCons(sep))
		}
	}

	// test that zero-length gets replaced with ' '
	sep = []rune(" ")
	for _, inS := range []string{
		"", "\n", "AB", " ab \t§cdef", "«ready!¿",
	} {
		inRn := []rune(inS)
		for sz := 1; sz <= 5; sz++ {
			want := [][]rune{}
			for i := 0; i < len(inRn); i += sz {
				if i > 0 {
					want = append(want, sep)
				}
				if i+sz <= len(inRn) {
					want = append(want, inRn[i:i+sz])
				} else {
					want = append(want, inRn[i:])
				}
			}
			chainT(t, inS, want, getFixCons(sz), getJoinCons(nil))
			chainT(t, inS, want, getFixCons(sz),
				getJoinCons([]rune{}))
		}
	}
}

func TestNewPrefixToken(t *testing.T) {
	getFixCons := func(sz int) func(r textproc.TokenReader) textproc.TokenReader {
		return func(r textproc.TokenReader) textproc.TokenReader {
			return textproc.NewFixedTokenSize(r, sz)
		}
	}
	getPrefCons := func(pref []rune) func(r textproc.TokenReader) textproc.TokenReader {
		return func(r textproc.TokenReader) textproc.TokenReader {
			return textproc.NewPrefixToken(r, pref)
		}
	}

	pref := []rune("Hello")
	for _, in := range []string{
		"", "\n", " ", "¡bang!", "Some long\t text",
	} {
		inR := []rune(in)
		want := [][]rune{pref}
		if len(inR) > 0 {
			want = append(want, inR)
		}
		chainT(t, in, want, getFixCons(len(inR)), getPrefCons(pref))
		chainS(t, in, string(pref)+in, getPrefCons(pref))

		pref2 := []rune{' '}
		want = [][]rune{pref2}
		if len(inR) > 0 {
			want = append(want, inR)
		}
		chainT(t, in, want, getFixCons(len(inR)), getPrefCons(nil))
		chainT(t, in, want,
			getFixCons(len(inR)), getPrefCons([]rune{}))
		chainS(t, in, string(pref2)+in, getPrefCons(nil))
		chainS(t, in, string(pref2)+in, getPrefCons([]rune{}))
	}
}

func TestNewSuffixToken(t *testing.T) {
	getFixCons := func(sz int) func(r textproc.TokenReader) textproc.TokenReader {
		return func(r textproc.TokenReader) textproc.TokenReader {
			return textproc.NewFixedTokenSize(r, sz)
		}
	}
	getSufCons := func(suf []rune) func(r textproc.TokenReader) textproc.TokenReader {
		return func(r textproc.TokenReader) textproc.TokenReader {
			return textproc.NewSuffixToken(r, suf)
		}
	}

	suf := []rune("Good bye")
	for _, in := range []string{
		"", "\n", " ", "¡bang!", "Some long\t text",
	} {
		inR := []rune(in)
		want := [][]rune{}
		if len(inR) > 0 {
			want = append(want, inR)
		}
		want = append(want, suf)
		chainT(t, in, want,
			getFixCons(len(inR)), getSufCons(suf))
		chainS(t, in, in+string(suf), getSufCons(suf))

		suf2 := []rune{' '}
		want = [][]rune{}
		if len(inR) > 0 {
			want = append(want, inR)
		}
		want = append(want, suf2)
		chainT(t, in, want, getFixCons(len(inR)), getSufCons(nil))
		chainT(t, in, want, getFixCons(len(inR)), getSufCons([]rune{}))
		chainS(t, in, in+string(suf2), getSufCons(nil))
		chainS(t, in, in+string(suf2), getSufCons([]rune{}))
	}
}

func TestNewTokenGroupFrequency(t *testing.T) {
	getFixCons := func(sz int) func(r textproc.TokenReader) textproc.TokenReader {
		return func(r textproc.TokenReader) textproc.TokenReader {
			return textproc.NewFixedTokenSize(r, sz)
		}
	}
	getCons := func(sz, toShow int) func(r textproc.TokenReader) textproc.TokenReader {
		return func(r textproc.TokenReader) textproc.TokenReader {
			return textproc.NewTokenGroupFrequency(r, sz, toShow)
		}
	}
	in := "abacbaabbb"
	want := `10 items, 3 unique
"b"	5	0.500000
"a"	4	0.400000
"c"	1	0.100000
`
	chainS(t, in, want, getFixCons(1), getCons(-1, -1))

	in = "xyyxyyyyyyy"
	want = `10 items, 3 unique, showing top 3
"y", "y"	7	0.700000
"x", "y"	2	0.200000
"y", "x"	1	0.100000
`
	chainS(t, in, want, getFixCons(1), getCons(2, 3))

	in = ""
	want = `0 items, 0 unique
`
	chainS(t, in, want, getFixCons(1), getCons(1, -1))
}

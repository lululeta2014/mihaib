package textproc

import (
	"testing"
)

func TestTokenGroupFreq_Codec(t *testing.T) {
	p := &tokenGroupFrequency{}
	for _, wordsStr := range [][]string{
		[]string{}, []string{""}, []string{"", ""},
		[]string{"0"}, []string{"3", "14"}, []string{"Hello", "World"},
		[]string{"_"}, []string{"€_", "_§«"},
	} {
		in := [][]rune{}
		for _, w := range wordsStr {
			in = append(in, []rune(w))
		}
		out := p.decode(p.encode(in))

		if len(in) != len(out) {
			t.Errorf("want %v got %v", in, out)
		}
		for i := range in {
			if string(in[i]) != string(out[i]) {
				t.Errorf("want %v got %v", in[i], out[i])
			}
		}
	}
}

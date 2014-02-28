package main

import (
	"testing"
)

func TestCheckOptionCount(t *testing.T) {
	var err error
	for i := range [5]int{} {
		err = checkOptionCount(i)
		if err != nil {
			t.Error("no options shouldn't trigger error")
		}
		for j := 0; j <= i; j++ {
			err = checkOptionCount(i, make([]string, j)...)
			if err != nil {
				t.Errorf("MaxOptCount %v and %v options "+
					"shouldn't trigger error", i, j)
			}
		}

		for _, j := range []int{i + 1, i + 5} {
			err = checkOptionCount(i, make([]string, j)...)
			if err == nil {
				t.Errorf("MaxOptCount %v and %v options "+
					"should trigger error", i, j)
			}
		}
	}
}

// checks several abstract factories which expect a single int argument
func TestAbsFact_SingleIntArg(t *testing.T) {
	for _, underTest := range []tokenReaderAbstractFactory{
		absFact_ExpandTab, absFact_Py, absFact_FixedTokenSize,
		absFact_FormattedParagraphs, absFact_BreakLines,
	} {
		for _, goodArg := range [][]string{
			nil, []string{}, []string{"8"}, []string{"4"},
			[]string{"-5"},
		} {
			fact, err := underTest(goodArg...)
			if fact == nil || err != nil {
				t.Error("Got no factory", fact, "or err", err)
			}
		}

		for _, badArg := range [][]string{
			[]string{""}, []string{" "}, []string{"eight"},
			[]string{"4", "5"},
		} {
			fact, err := underTest(badArg...)
			if fact != nil || err == nil {
				t.Error("Got factory", fact, "or no err", err,
					"for", badArg)
			}
		}
	}
}

func TestAbsFact_JoinTok(t *testing.T) {
	for _, goodArg := range [][]string{
		nil, []string{}, []string{"7"}, []string{"—¡§—"},
		[]string{" "}, []string{"\n"}, []string{"\t"},
	} {
		fact, err := absFact_JoinTok(goodArg...)
		if fact == nil || err != nil {
			t.Error("Got no factory", fact, "or err", err)
		}
	}

	for _, badArg := range [][]string{
		[]string{"", ""}, []string{" ", "§§"}, []string{"¿", "∞", "»"},
	} {
		fact, err := absFact_JoinTok(badArg...)
		if fact != nil || err == nil {
			t.Error("Got factory", fact, "or no err", err,
				"for", badArg)
		}
	}
}

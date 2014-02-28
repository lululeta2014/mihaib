package main

import (
	"errors"
	"fmt"
	"github.com/MihaiB/mihaib/toolbox/textproc"
	"strconv"
	"strings"
)

type filterData struct {
	absFact     tokenReaderAbstractFactory
	description string
	optionNames []string //	used only for displaying help
}

// Catalogue of filters.
var filterCatalogue = makeCatalogue()

func makeCatalogue() map[string]filterData {
	c := map[string]filterData{}

	c["nl"] = filterData{
		absFact: absFactFromFact(textproc.NewNewlineAtEnd),
		description: "Add newline ‘\\n’ at end of input, " +
			"if the input is not empty " +
			"and the last codepoint isn't ‘\\n’.",
	}

	c["trail"] = filterData{
		absFact: absFactFromFact(textproc.NewStripTrailingSpace),
		description: "Strip trailing whitespace before each ‘\\n’ " +
			"and at end of input. ‘\\n’ itself is not removed.",
	}

	c["expandtab"] = filterData{
		absFact: absFact_ExpandTab,
		description: `Expand tabs ‘\t’ to spaces ‘ ’. ` +
			"The default tabSize is 8.",
		optionNames: []string{"tabSize"},
	}

	// example of chaining, and passing some arguments, from the catalogue
	c["py"] = filterData{
		absFact: absFact_Py,
		description: "Basic Python tidying up. " +
			"Chains expandTab:tabSize→trail→nl. " +
			"Default tabSize is 4.",
		optionNames: []string{"tabSize"},
	}

	c["unix"] = filterData{
		absFact:     absFactFromFact(textproc.NewUnixNewline),
		description: `Convert ‘\r\n’ and ‘\r’ to unix newline ‘\n’`,
	}

	c["lead"] = filterData{
		absFact: absFactFromFact(textproc.NewStripLeadingSpace),
		description: `Strip leading whitespace (after each ‘\n’ and ` +
			`at the start of input. ‘\n’ itself is not removed.`,
	}

	c["singlespace"] = filterData{
		absFact: absFactFromFact(textproc.NewSingleSpace),
		description: `Replace consecutive whitespaces (except ‘\n’) ` +
			`with a single space ‘ ’.`,
	}

	c["fixtok"] = filterData{
		absFact: absFact_FixedTokenSize,
		description: "Split the input stream into tokens " +
			"of fixed length, default 1. The last token " +
			"may be shorter. ",
		optionNames: []string{"length"},
	}

	logicalParDesc := `Convert formatted paragraphs, which occupy
		multiple adjacent lines and are separated by empty or
		whitespace-only lines, to logical paragraphs, which occupy
		a single line. Consecutive whitespace is replaced with a
		single space ‘ ’, leading and trailing whitespace and
		empty lines are removed and a newline ‘\n’ is ensured
		at the end if the output isn't empty.

		Lines are separated by newline ‘\n’.`
	c["logicalpar"] = filterData{
		absFact:     absFactFromFact(textproc.NewLogicalParagraphs),
		description: logicalParDesc,
	}

	fmtParDesc := `Convert logical paragraphs, which occupy a single line,
		to formatted paragraphs, which occupy multiple adjacent lines
		and are separated by a blank line. Consecutive whitespace is
		replaced with a single space, leading and trailing whitespace
		and empty lines are removed and a newline ‘\n’ is ensured at
		the end if the output isn't empty.

		Lines are separated by newline ‘\n’.
		The default lineSize is 79.`
	c["fmtpar"] = filterData{
		absFact:     absFact_FormattedParagraphs,
		description: fmtParDesc,
		optionNames: []string{"lineSize"},
	}

	breakDesc := `Break long lines. Strip leading and trailing whitespace
		and replace consecutive whitespace with a single space.

		Lines are separated by newline ‘\n’.
		The default lineSize is 79.`
	c["break"] = filterData{
		absFact:     absFact_BreakLines,
		description: breakDesc,
		optionNames: []string{"lineSize"},
	}

	c["lower"] = filterData{
		absFact:     absFactFromFact(textproc.NewLowerCase),
		description: "lower case",
	}

	c["upper"] = filterData{
		absFact:     absFactFromFact(textproc.NewUpperCase),
		description: "upper case",
	}

	c["tokcount"] = filterData{
		absFact:     absFactFromFact(textproc.NewTokenCounter),
		description: `Count tokens. Produces a single output token.`,
	}

	c["wordtok"] = filterData{
		absFact:     absFactFromFact(textproc.NewWordToken),
		description: "Emit each word as one token, and nothing else.",
	}

	c["jointok"] = filterData{
		absFact: absFact_JoinTok,
		description: `Join input tokens (emit sep between them).
			sep is “, ” by default.`,
		optionNames: []string{"sep"},
	}

	c["pref"] = filterData{
		absFact:     absFact_PrefTok,
		description: `Prefix the token stream with tok, default ‘«’.`,
		optionNames: []string{"tok"},
	}

	c["suf"] = filterData{
		absFact:     absFact_SufTok,
		description: `Suffix the token stream with tok, default ‘»’.`,
		optionNames: []string{"tok"},
	}

	c["freq"] = filterData{
		absFact: absFact_TokGrpFreq,
		description: `Count frequency of token groups.
			Show top ‘toShow’ groups (if < 0, show all groups).
			Default groupSize is 1, toShow is -1.`,
		optionNames: []string{"groupSize", "toShow"},
	}

	return c
}

// Checks the number of passed options and returns an error if more than
// maxOptionCount, otherwise nil.
func checkOptionCount(maxOptionCount int, passedOptions ...string) error {
	found := len(passedOptions)
	if found > maxOptionCount {
		return errors.New(fmt.Sprintf(
			"Too many options, accepts %v but found %v: %#v",
			maxOptionCount, found,
			strings.Join(passedOptions, ":")))
	}
	return nil
}

func absFact_ExpandTab(opts ...string) (tokenReaderFactory, error) {
	tabSize := 8

	err := checkOptionCount(1, opts...)
	if err != nil {
		return nil, err
	}
	if len(opts) >= 1 {
		tabSize, err = strconv.Atoi(opts[0])
		if err != nil {
			return nil, err
		}
	}

	return func(r textproc.TokenReader) textproc.TokenReader {
		return textproc.NewExpandTab(r, tabSize)
	}, nil
}

func absFact_Py(opts ...string) (tokenReaderFactory, error) {
	tabSize := 4

	err := checkOptionCount(1, opts...)
	if err != nil {
		return nil, err
	}
	if len(opts) >= 1 {
		tabSize, err = strconv.Atoi(opts[0])
		if err != nil {
			return nil, err
		}
	}

	return func(r textproc.TokenReader) textproc.TokenReader {
		r = textproc.NewExpandTab(r, tabSize)
		r = textproc.NewStripTrailingSpace(r)
		r = textproc.NewNewlineAtEnd(r)
		return r
	}, nil
}

func absFact_FixedTokenSize(opts ...string) (tokenReaderFactory, error) {
	length := 1
	err := checkOptionCount(1, opts...)
	if err != nil {
		return nil, err
	}
	if len(opts) >= 1 {
		length, err = strconv.Atoi(opts[0])
		if err != nil {
			return nil, err
		}
	}

	return func(r textproc.TokenReader) textproc.TokenReader {
		r = textproc.NewFixedTokenSize(r, length)
		return r
	}, nil
}

func absFact_FormattedParagraphs(opts ...string) (tokenReaderFactory, error) {
	length := 79
	err := checkOptionCount(1, opts...)
	if err != nil {
		return nil, err
	}
	if len(opts) >= 1 {
		length, err = strconv.Atoi(opts[0])
		if err != nil {
			return nil, err
		}
	}

	return func(r textproc.TokenReader) textproc.TokenReader {
		r = textproc.NewFormattedParagraphs(r, length)
		return r
	}, nil
}

func absFact_BreakLines(opts ...string) (tokenReaderFactory, error) {
	length := 79
	err := checkOptionCount(1, opts...)
	if err != nil {
		return nil, err
	}
	if len(opts) >= 1 {
		length, err = strconv.Atoi(opts[0])
		if err != nil {
			return nil, err
		}
	}

	return func(r textproc.TokenReader) textproc.TokenReader {
		r = textproc.NewBreakLines(r, length)
		return r
	}, nil
}

func absFact_JoinTok(opts ...string) (tokenReaderFactory, error) {
	sep := ", "
	err := checkOptionCount(1, opts...)
	if err != nil {
		return nil, err
	}
	if len(opts) >= 1 {
		sep = opts[0]
	}

	return func(r textproc.TokenReader) textproc.TokenReader {
		return textproc.NewJoinTokens(r, []rune(sep))
	}, nil
}

func absFact_PrefTok(opts ...string) (tokenReaderFactory, error) {
	tok := "«"
	err := checkOptionCount(1, opts...)
	if err != nil {
		return nil, err
	}
	if len(opts) >= 1 {
		tok = opts[0]
	}

	return func(r textproc.TokenReader) textproc.TokenReader {
		return textproc.NewPrefixToken(r, []rune(tok))
	}, nil
}

func absFact_SufTok(opts ...string) (tokenReaderFactory, error) {
	tok := "»"
	err := checkOptionCount(1, opts...)
	if err != nil {
		return nil, err
	}
	if len(opts) >= 1 {
		tok = opts[0]
	}

	return func(r textproc.TokenReader) textproc.TokenReader {
		return textproc.NewSuffixToken(r, []rune(tok))
	}, nil
}

func absFact_TokGrpFreq(opts ...string) (tokenReaderFactory, error) {
	grpSize, toShow := 1, -1
	err := checkOptionCount(2, opts...)
	if err != nil {
		return nil, err
	}
	if len(opts) >= 1 {
		grpSize, err = strconv.Atoi(opts[0])
		if err != nil {
			return nil, err
		}
	}
	if len(opts) >= 2 {
		toShow, err = strconv.Atoi(opts[1])
		if err != nil {
			return nil, err
		}
	}

	return func(r textproc.TokenReader) textproc.TokenReader {
		r = textproc.NewTokenGroupFrequency(r, grpSize, toShow)
		return r
	}, nil
}

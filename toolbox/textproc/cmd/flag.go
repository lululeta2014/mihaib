package main

import (
	"flag"
	"fmt"
	"github.com/MihaiB/mihaib/toolbox/textproc"
	"io/ioutil"
	"os"
	"sort"
	"strings"
)

var flagDefs = struct {
	inName, inDefault   string
	outName, outDefault string
}{
	inName:     "i",
	inDefault:  "-",
	outName:    "o",
	outDefault: "-",
}

type argsT struct {
	in             *string
	out            *string
	forceOverwrite *bool
	filterArgs     []string
}

func getCmdlineArgs() *argsT {
	description := `Process text through successive transformations.

	The input is split into strings of arbitrary length called tokens.
	This stream of tokens is passed through a sequence of filters.
	A filter consumes a token stream and produces a new token stream.
	The output tokens of the last filter are concatenated
	and written to output.

	Filters which don't mention the input tokens in their description
	produce the same output string
	(after their output tokens are concatenated)
	regardless of how their input string is split into tokens
	(e.g. a filter which converts everything to lowercase
	behaves this way by definition).`
	flag.Usage = func() {
		fmt.Fprintln(os.Stderr, "Usage: "+os.Args[0]+
			" [options] [filters]")
		flag.PrintDefaults()
		fmt.Fprintln(os.Stderr)

		{
			logPar := textproc.NewLogicalParagraphs
			brk := textproc.NewBreakLines
			bytes, _ := ioutil.ReadAll(textproc.NewReader(
				brk(logPar(textproc.NewTokenReader(
					strings.NewReader(description))), 79)))
			fmt.Fprintln(os.Stderr, string(bytes))
		}

		{
			filterDef := `
			A filter is: ‘filterName[:opt1:opt2:…]’ where:

			― you pass the first N options, N≥0

			― to include ‘:’ in an option value pass ‘\:’,
			to include ‘\’ pass ‘\\’.
			A ‘\’ not followed by one of these is illegal.
			`
			logPar := textproc.NewLogicalParagraphs
			brk := textproc.NewBreakLines
			bytes, _ := ioutil.ReadAll(textproc.NewReader(
				brk(logPar(textproc.NewTokenReader(
					strings.NewReader(filterDef))), 79)))
			fmt.Fprintln(os.Stderr, string(bytes))
		}

		fmt.Fprintln(os.Stderr, "Filters:")
		filterNames := make([]string, 0, len(filterCatalogue))
		for name := range filterCatalogue {
			filterNames = append(filterNames, name)
		}
		sort.Strings(filterNames)
		for _, name := range filterNames {
			fData := filterCatalogue[name]
			nameHelp := name
			if len(fData.optionNames) > 0 {
				nameHelp += "["
				for _, opt := range fData.optionNames {
					nameHelp += ":" + opt
				}
				nameHelp += "]"
			}
			fmt.Fprintln(os.Stderr)

			str := "• " + nameHelp + " → " + fData.description
			logP := textproc.NewLogicalParagraphs
			brk := textproc.NewBreakLines
			bytes, _ := ioutil.ReadAll(textproc.NewReader(
				brk(logP(textproc.NewTokenReader(
					strings.NewReader(str))), 79)))
			fmt.Fprint(os.Stderr, string(bytes))
		}
	}

	args := &argsT{}
	args.in = flag.String(flagDefs.inName, flagDefs.inDefault,
		"Input file, default stdin")
	args.out = flag.String(flagDefs.outName, flagDefs.outDefault,
		"Output file, default stdout")
	args.forceOverwrite = flag.Bool("force", false,
		"Force overwriting output file if it already exists")
	flag.Parse()
	args.filterArgs = flag.Args()
	return args
}

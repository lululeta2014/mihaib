package main

import (
	"flag"
	"fmt"
	"github.com/MihaiB/toolbox/textproc"
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
	description := "Process text through successive transformations. " +
		"The input is split into strings of arbitrary length " +
		"called tokens. A filter consumes a token stream " +
		"and produces a new token stream. The input token stream " +
		"is passed through a sequence of filters and the output " +
		"tokens of the last filter are concatenated and written to " +
		"output. Filters which don't mention the token stream " +
		"produce the same output string (after concatenating " +
		"their output tokens) regardless of how their input string " +
		"was split into tokens."
	flag.Usage = func() {
		fmt.Fprintln(os.Stderr, "Usage: "+os.Args[0]+
			" [options] [filters]")
		flag.PrintDefaults()
		fmt.Fprintln(os.Stderr)
		fmt.Fprintln(os.Stderr, description)
		fmt.Fprintln(os.Stderr, "A filter is: "+
			"‘filterName[:opt1:opt2:…]’ where:")
		fmt.Fprintln(os.Stderr,
			"― you pass the first N options, N≥0")
		fmt.Fprintln(os.Stderr, `― to include ‘:’ in an option `+
			`value pass ‘\:’, to include ‘\’ pass ‘\\’. `+
			`A ‘\’ not followed by one of these is illegal`)

		fmt.Fprintln(os.Stderr)
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

package main

import (
	"errors"
	"fmt"
	"github.com/MihaiB/toolbox/textproc"
	"io"
	"strings"
	"unicode"
)

// Parse a filter name and options from the command line argument.
func parseFilter(s string) ([]string, error) {
	result := []string{}
	sr := strings.NewReader(s)
	var item []rune
	for {
		ch, sz, err := sr.ReadRune()
		if sz == 0 {
			if err != nil && err != io.EOF {
				return nil, err
			}
			// put something in result only if s is not empty
			if len(item) > 0 || len(result) > 0 {
				result = append(result, string(item))
				item = []rune{}
			}
			break
		}
		if ch == unicode.ReplacementChar && sz == 1 {
			return nil, textproc.ErrInvalidUTF8
		}

		if ch == ':' {
			result = append(result, string(item))
			item = []rune{}
			continue
		}

		if ch == '\\' {
			ch2, sz, err := sr.ReadRune()
			if sz == 0 {
				if err != nil && err != io.EOF {
					return nil, err
				}
				return nil, errors.New("Unfinished escape " +
					"sequence " + string(ch))
			}
			if ch2 == unicode.ReplacementChar && sz == 1 {
				return nil, textproc.ErrInvalidUTF8
			}

			switch ch2 {
			case '\\', ':':
				ch = ch2
			default:
				return nil, errors.New("Invalid escape " +
					"sequence " + string([]rune{ch, ch2}))
			}
		}
		item = append(item, ch)
	}

	return result, nil
}

// Construct a tokenReaderFactory from a command-line arg, or return an error.
func getFilterFactory(arg string) (tokenReaderFactory, error) {
	parts, err := parseFilter(arg)
	if err != nil {
		return nil, err
	}
	if len(parts) == 0 {
		return nil, errors.New("No filter name given")
	}
	name, opts := parts[0], parts[1:]
	fData, ok := filterCatalogue[name]
	if !ok {
		return nil, errors.New(fmt.Sprintf("No such filter %#v", name))
	}
	return fData.absFact(opts...)
}

func getRequestedFilters(args *argsT) ([]tokenReaderFactory, error) {
	result := []tokenReaderFactory{}
	for _, userArg := range args.filterArgs {
		factory, err := getFilterFactory(userArg)
		if err != nil {
			err := errors.New(fmt.Sprintf("%#v: %s",
				userArg, err.Error()))
			return nil, err
		}
		result = append(result, factory)
	}
	return result, nil
}

// This command passes a UTF-8 stream from stdin (or a file)
// through a chain of filters from the ‘textproc’ package and writes the output
// to stdout (or a file).
package main

import (
	"fmt"
	"io"
	"os"
)

func main() {
	args := getCmdlineArgs()
	err := process(args)
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		os.Exit(1)
	}
}

func process(args *argsT) (err error) {
	closeAndReport := func(c io.Closer) {
		er := c.Close()
		if err == nil {
			err = er
		}
	}

	var r io.Reader = os.Stdin
	var w io.Writer = os.Stdout
	if *args.in != flagDefs.inDefault {
		var file *os.File
		file, err = os.Open(*args.in)
		if err != nil {
			return
		}
		defer closeAndReport(file)

		r = file
	}
	if *args.out != flagDefs.outDefault {
		flags := os.O_WRONLY | os.O_CREATE | os.O_TRUNC
		if !*args.forceOverwrite {
			flags |= os.O_EXCL
		}
		var file *os.File
		file, err = os.OpenFile(*args.out, flags, 0666)
		if err != nil {
			return
		}
		defer closeAndReport(file)

		w = file
	}

	factories, err := getRequestedFilters(args)
	if err != nil {
		return
	}
	r = chain(r, factories...)
	_, err = io.Copy(w, r)
	if err != nil {
		return
	}

	return
}

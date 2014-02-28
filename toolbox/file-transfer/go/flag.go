package main

import (
	"flag"
	"fmt"
	"os"
)

/*
 * During initialization flags are parsed.
 * If flags are invalid (or absent), the program exits.
 * After initialization, either listen_port is non-zero or
 * connect_port and connect_host are non-zero (non-empty).
 */

var (
	listen_port uint
	remote      string
	fileArgs    []string
)

func init() {
	flag.UintVar(&listen_port, "l", 1027, "listen for connections on port")
	listen_port = 0

	flag.StringVar(&remote, "c", "host:port", "connect to host on port")
	remote = ""

	flag.Usage = func() {
		fmt.Fprintf(os.Stderr, "Usage: ft options [files]\n")
		fmt.Fprintf(os.Stderr, "Send or receive files\n")
		flag.PrintDefaults()
	}

	flag.Parse()

	if listen_port == 0 && remote == "" {
		flag.Usage()
		os.Exit(1)
	}
	if listen_port != 0 && remote != "" {
		fmt.Fprintf(os.Stderr, "error: give only one of -l, -c\n")
		os.Exit(1)
	}

	fileArgs = make([]string, flag.NArg())
	for i := 0; i < flag.NArg(); i++ {
		fileArgs[i] = flag.Arg(i)
	}
}

package main

import (
	"flag"
	"log"
	"net"
)

type cmdlineT struct {
	laddr                      *net.TCPAddr // address to listen to
	caddr                      *net.TCPAddr // address to connect to
	minPreDelay, maxPreDelay   int
	minPostDelay, maxPostDelay int
	bufsize                    int
}

var cmdline = getCmdLine()

func getCmdLine() (result cmdlineT) {
	var lStr, cStr string
	flag.StringVar(&lStr, "listen", ":9090",
		"listen address, \":9090\" or \"localhost:9090\"")
	flag.StringVar(&cStr, "connect", ":8080",
		"connect address, \":8080\" or \"example.com:8080\"")
	flag.IntVar(&result.minPreDelay, "minpre", 0,
		"minimum pre delay millis (before any data is transmitted)")
	flag.IntVar(&result.maxPreDelay, "maxpre", 0,
		"maximum pre delay millis (before any data is transmitted)")
	flag.IntVar(&result.minPostDelay, "minpost", 0,
		"minimum post delay millis (before each chunk of data, "+
			"except the first, is transmitted)")
	flag.IntVar(&result.maxPostDelay, "maxpost", 0,
		"maximum post delay millis (before each chunk of data, "+
			"except the first, is transmitted)")
	flag.IntVar(&result.bufsize, "bufsize", 1024,
		"size of the buffer used to transmit data, in bytes")
	flag.Parse()

	var err error
	result.laddr, err = net.ResolveTCPAddr("tcp", lStr)
	if err != nil {
		log.Fatal(err)
	}
	result.caddr, err = net.ResolveTCPAddr("tcp", cStr)
	if err != nil {
		log.Fatal(err)
	}

	if result.minPreDelay > result.maxPreDelay ||
		result.minPreDelay < 0 || result.maxPreDelay < 0 {
		log.Fatal("Invalid min & max pre delays")
	}
	if result.minPostDelay > result.maxPostDelay ||
		result.minPostDelay < 0 || result.maxPostDelay < 0 {
		log.Fatal("Invalid min & max post delays")
	}
	return
}

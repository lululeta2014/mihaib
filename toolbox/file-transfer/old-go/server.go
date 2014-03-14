package main

import (
	"net"
	"fmt"
	"os"
	"strings"
	"bufio"
)

func run_server(port uint, fileArgs []string, fileSizes []int64) {
	listener, err := net.Listen("tcp", fmt.Sprintf(":%d", port))
	if err != nil {
		fmt.Println(err)
		return
	}
	defer listener.Close()

	for served := false; !served; {
		conn, err := listener.Accept()
		if err != nil {
			fmt.Println(err)
			continue
		}

		fmt.Println("Connection from", conn.RemoteAddr())
		fmt.Printf("Proceed? [Yes/no] ")
		answer, err := readStdinLine()
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
			conn.Close()
			return
		}
		if !strings.HasPrefix("YES\n", strings.ToUpper(answer)) {
			conn.Close()
			continue
		}

		served = true
		serveClient(conn, fileArgs, fileSizes)
		conn.Close()
	}
}

func serveClient(conn net.Conn, fileArgs []string, fileSizes []int64) {
	sockIn := bufio.NewReader(conn)
	sockOut := bufio.NewWriter(conn)

	var expected string
	if len(fileArgs) > 0 {
		expected = "SERVER SENDS"
	} else {
		expected = "CLIENT SENDS"
	}

	got, err := readAsciiLine(sockIn, len(expected)+1)
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		return
	}
	if got != expected {
		fmt.Fprintln(os.Stderr,
			"Client didn't send good transfer direction")
		return
	}

	_, err = sockOut.Write([]byte("DIRECTION OK\n"))
	if err == nil {
		err = sockOut.Flush()
	}
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		return
	}

	if len(fileArgs) > 0 {
		run_sender(sockIn, sockOut, fileArgs, fileSizes)
	} else {
		run_receiver(sockIn, sockOut)
	}
}

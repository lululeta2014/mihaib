package main

import (
	"net"
	"os"
	"fmt"
	"bufio"
)

func run_client(remote string, fileArgs []string, fileSizes []int64) {
	conn, err := net.Dial("tcp", "", remote)
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		return
	}
	defer conn.Close()

	sockIn := bufio.NewReader(conn)
	sockOut := bufio.NewWriter(conn)

	var direction string
	if len(fileArgs) > 0 {
		direction = "CLIENT SENDS\n"
	} else {
		direction = "SERVER SENDS\n"
	}

	_, err = sockOut.Write([]byte(direction))
	if err == nil {
		err = sockOut.Flush()
	}
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		return
	}

	expected := "DIRECTION OK"
	got, err := readAsciiLine(sockIn, len(expected)+1)
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		return
	}
	if got != expected {
		fmt.Println("Server didn't confirm transfer direction")
		return
	}

	if len(fileArgs) > 0 {
		run_sender(sockIn, sockOut, fileArgs, fileSizes)
	} else {
		run_receiver(sockIn, sockOut)
	}
}

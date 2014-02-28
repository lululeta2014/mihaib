package main

import (
	"os"
	"bufio"
	"strconv"
	"container/vector"
	"path"
	"strings"
	"fmt"
	"io"
)

func run_sender(sockIn *bufio.Reader, sockOut *bufio.Writer, fileArgs []string, fileSizes []int64) {
	var v vector.StringVector
	v.Push("FILE COUNT ")
	v.Push(strconv.Itoa(len(fileArgs)))
	v.Push("\n")

	for i := 0; i < len(fileArgs); i++ {
		v.Push(strconv.Itoa(i + 1))
		v.Push("#\n")
		v.Push(path.Base(fileArgs[i]))
		v.Push("\n")
		v.Push(strconv.Itoa64(fileSizes[i]))
		v.Push("\n")
	}
	announce := strings.Join(v, "")

	_, err := sockOut.Write([]byte(announce))
	if err == nil {
		err = sockOut.Flush()
	}
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		return
	}

	expected := "SEND FILES"
	got, err := readAsciiLine(sockIn, len([]byte(expected))+1)
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		return
	}
	if got != expected {
		fmt.Println("Receiver didn't show interest in files")
		return
	}

	for i := 0; i < len(fileArgs); i++ {
		ok := sendFile(sockIn, sockOut, fileArgs, fileSizes, i)
		if !ok {
			return
		}
	}
}

func sendFile(sockIn *bufio.Reader, sockOut *bufio.Writer, fileArgs []string, fileSizes []int64, i int) bool {
	_, err := sockOut.Write([]byte("OFFER " + strconv.Itoa(i+1) + "\n"))
	if err == nil {
		err = sockOut.Flush()
	}
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		return false
	}

	accMsg := "ACCEPT " + strconv.Itoa(i+1)
	skipMsg := "SKIP " + strconv.Itoa(i+1)
	var maxBytes int
	if len(accMsg) > len(skipMsg) {
		maxBytes = len([]byte(accMsg)) + 1
	} else {
		maxBytes = len([]byte(skipMsg)) + 1
	}

	got, err := readAsciiLine(sockIn, maxBytes)
	if err != nil {
		fmt.Fprintln(os.Stderr, err)
		return false
	}

	switch got {
	case accMsg:
		file, err := os.Open(fileArgs[i], os.O_RDONLY, 0)
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
			return false
		}
		fileIn := bufio.NewReader(file)

		_, err = io.Copyn(sockOut, fileIn, fileSizes[i])
		if err == nil {
			err = sockOut.Flush()
		}
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
			return false
		}

		expected := "COMPLETED " + strconv.Itoa(i+1)
		got, err = readAsciiLine(sockIn, len([]byte(expected))+1)
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
			return false
		}
		if got != expected {
			fmt.Println("Invalid response from receiver")
			return false
		}

		fmt.Println(fileArgs[i] + " DONE")
		return true
	case skipMsg:
		fmt.Println(fileArgs[i] + " SKIPPED")
		return true
	}

	fmt.Fprintf(os.Stderr, "Invalid reponse from receiver")
	return false
}

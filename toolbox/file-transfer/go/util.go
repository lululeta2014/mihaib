package main

import (
	"os"
	"bufio"
	"strings"
	"fmt"
)

var (
	stdinReader = bufio.NewReader(os.Stdin)
	nonAlphaNum string = "\n !#$%&'()+,-.;=@[]^_`{}~"
)

// Next line from stdin without trailing '\n', and error other than EOF.
// Returns empty string (and nil error) if EOF was already reached.
func readStdinLine() (string, os.Error) {
	answer, err := stdinReader.ReadString('\n')
	if err != nil {
		if err != os.EOF {
			return answer, err
		}
	} else {
		// strip trailing newline
		answer = answer[0 : len(answer)-1]
	}
	return answer, nil
}

func goodAsciiByte(b byte) bool {
	if b > 127 {
		return false
	}

	if 'A' <= b && b <= 'Z' || 'a' <= b && b <= 'z' {
		return true
	}
	if '0' <= b && b <= '9' {
		return true
	}

	return strings.IndexRune(nonAlphaNum, int(b)) != -1
}

// Reads bytes until '\n' is read or maxBytes are read.
// If a byte not allowed by the protocol is read,
// discards what was read so far and returns ("", err).
// If maxBytes are read without reading '\n', returns ("", err).
// If the reader returns an error, return ("", err).
//
// So if the first byte is '\n', returns ("", nil)
// otherwise returns ("line", nil) or ("", err).
func readAsciiLine(r *bufio.Reader, maxBytes int) (string, os.Error) {
	buf := make([]byte, maxBytes)
	for i := 0; i < maxBytes; i++ {
		b, err := r.ReadByte()
		if err != nil {
			return "", err
		}

		if !goodAsciiByte(b) {
			return "", os.NewError(fmt.Sprint("Invalid byte ", b))
		}

		if b == '\n' {
			return string(buf[0:i]), nil
		}
		buf[i] = b
	}

	return "", os.NewError(
		fmt.Sprintf("Line not ended within %v bytes", maxBytes))
}


func getFileSizes(fileArgs []string) (fileSizes []int64, ok bool) {
	fileSizes = make([]int64, len(fileArgs))
	for i := 0; i < len(fileArgs); i++ {
		fileInfo, err := os.Stat(fileArgs[i])
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
			return nil, false
		}
		if fileInfo.IsDirectory() {
			fmt.Fprintln(os.Stderr, fileArgs[i], "is a directory")
			return nil, false
		}

		fileSizes[i] = fileInfo.Size
	}

	return fileSizes, true
}

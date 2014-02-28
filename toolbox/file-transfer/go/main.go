package main

import (
	"os"
)

func main() {
	fileSizes, ok := getFileSizes(fileArgs)
	if !ok {
		os.Exit(1)
	}

	if listen_port != 0 {
		run_server(listen_port, fileArgs, fileSizes)
	} else {
		run_client(remote, fileArgs, fileSizes)
	}
}

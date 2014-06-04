package main

import (
	"log"
	"net"
)

func main() {
	l, err := net.ListenTCP("tcp", cmdline.laddr)
	if err != nil {
		log.Fatal(err)
	}

	for {
		var conn *net.TCPConn
		conn, err = l.AcceptTCP()
		if err != nil {
			log.Print(err)
			continue
		}

		handleAsync(conn)
	}
}

package main

import (
	"io"
	"log"
	"math/rand"
	"net"
	"time"
)

func delay(min, max int) {
	t := min + rand.Intn(max-min+1)
	time.Sleep(time.Duration(t) * time.Millisecond)
}

func pipe(r *net.TCPConn, w *net.TCPConn) {
	// Calling CloseRead() and CloseWrite() no matter what keeps printing
	// this error: ‘transport endpoint is not connected’.
	// Instead, we'll conditionally close read/write.
	var err error
	defer func() {
		if err != nil && err != io.EOF {
			log.Print(err)
		}
	}()

	firstChunk := true
	buf := make([]byte, cmdline.bufsize)
	for {
		var n int
		n, err = r.Read(buf)
		if n <= 0 {
			if err != io.EOF {
				err2 := r.CloseRead()
				if err != nil {
					err = err2
				}
			}
			err2 := w.CloseWrite()
			if err2 != nil && (err == nil || err == io.EOF) {
				err = err2
			}
			return
		}

		if firstChunk {
			firstChunk = false
		} else if cmdline.maxPostDelay > 0 {
			delay(cmdline.minPostDelay, cmdline.maxPostDelay)
		}

		_, err = w.Write(buf[:n])
		if err != nil {
			r.CloseRead()
			return
		}
	}
}

func handleSync(conn *net.TCPConn) {
	var err error
	defer func() {
		if err != nil {
			log.Print(err)
		}
	}()

	conn2, err := net.DialTCP("tcp", nil, cmdline.caddr)
	if err != nil {
		// The pipe() functions close both connections, but close conn
		// here because something went wrong and we won't reach them.
		conn.Close()
		return
	}

	if cmdline.maxPreDelay > 0 {
		delay(cmdline.minPreDelay, cmdline.maxPreDelay)
	}

	ch := make(chan bool)
	go func() {
		pipe(conn, conn2)
		ch <- true
	}()
	go func() {
		pipe(conn2, conn)
		ch <- true
	}()
	<-ch
	<-ch
}

func handleAsync(conn *net.TCPConn) {
	go handleSync(conn)
}

import socket
import sys

import sender, receiver
from util import sendAll, readASCIILine

def run(host, port, files):
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        sock.connect((host, port))
        if files:
            msg = 'CLIENT SENDS\n'
        else:
            msg = 'SERVER SENDS\n'
        sendAll(sock, msg.encode('ascii'))

        expected = 'DIRECTION OK\n'
        got = readASCIILine(sock, len(expected.encode('ascii')))
        if got != expected:
            print("Server didn't confirm transfer direction", file=sys.stderr)
            return

        if files:
            sender.run(sock, files)
        else:
            receiver.run(sock)
    finally:
        sock.close()

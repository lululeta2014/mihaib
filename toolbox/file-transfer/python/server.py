import socket
import sys

import sender, receiver
from util import readASCIILine, sendAll

def run(listen_port, files):
    serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    try:
        serversocket.bind(('', listen_port))
        serversocket.listen(2)

        client_served = False
        while not client_served:
            sock, addr = serversocket.accept()
            try:
                print('Connection from:', addr)
                answer = input('Proceed [Yes/no]? ')
                if "YES".startswith(answer.upper()):
                    client_served = True
                    __serve_client(sock, files)
            finally:
                sock.close()
    finally:
        serversocket.close()


def __serve_client(sock, files):
    try:
        if files:
            data = 'SERVER SENDS\n'
        else:
            data = 'CLIENT SENDS\n'

        msg = readASCIILine(sock, len(data.encode('ascii')))
        if msg != data:
            print('Client did not send valid transfer direction')
            return
        sendAll(sock, 'DIRECTION OK\n'.encode('ascii'))
    except ValueError as e:
        print(e, file=sys.stderr)
        return

    if files:
        sender.run(sock, files)
    else:
        receiver.run(sock)

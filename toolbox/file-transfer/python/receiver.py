import sys
import os
import time

from util import readASCIILine, sendAll, humansize, updateProgress

def run(sock):
    fileInfos = __getFileInfos(sock)
    __printFileInfos(fileInfos)
    answer = input('Download files? [Yes/select/no] ').strip().upper()

    if "YES".startswith(answer):
        select = False
    elif "SELECT".startswith(answer):
        select = True
    else:
        return

    sendAll(sock, 'SEND FILES\n'.encode('ascii'))

    __receiveFiles(sock, fileInfos, select)



def __getFileInfos(sock):
    """__getFileInfos(sock) -> [(name, size),..]"""
    fileCountMsg = readASCIILine(sock, len('FILE COUNT 100\n'.encode('ascii')))

    if (not fileCountMsg.startswith('FILE COUNT ') or
            not fileCountMsg.endswith('\n')):
        print("Invalid file count", file=sys.stderr)
        sys.exit(1)

    try:
        fileCount = int(fileCountMsg[len('FILE COUNT '):-1])
        if fileCount <= 0 or fileCount > 100:
            print("Invalid file count", file=sys.stderr)
            sys.exit(1)
    except ValueError:
        print("Invalid file count", file=sys.stderr)
        sys.exit(1)

    fileInfos = []

    for i in range(fileCount):
        expected = str(i + 1) + "#\n"
        got = readASCIILine(sock, len(expected.encode('ascii')))
        if got != expected:
            print("Invalid file ID", file=sys.stderr)
            sys.exit(1)

        nameMsg = readASCIILine(sock, 256)
        lenMsg = readASCIILine(sock, 14)

        if len(nameMsg) < 2 or not nameMsg.endswith('\n'):
            print('Sender sent invalid filename', file=sys.stderr)
            sys.exit(1)

        if len(lenMsg) < 2 or not lenMsg.endswith('\n'):
            print('Sender sent invalid file length', file=sys.stderr)
            sys.exit(1)

        try:
            fileLen = int(lenMsg[:-1])
        except ValueError:
            print('Sender sent invalid file length: ', lenMsg, file=sys.stderr)
            sys.exit(1)

        fileInfos.append((nameMsg[:-1], fileLen))

    return fileInfos


def __printFileInfos(fileInfos):
    totalSize = 0
    for (name, size) in fileInfos:
        print("{:41.41} {:>9}".format(name, humansize(size)))
        totalSize += size
    print("Total:", str(len(fileInfos)), "files,", humansize(totalSize))


def __receiveFiles(sock, fileInfos, select):
    for i in range(len(fileInfos)):
        (name, size) = fileInfos[i]

        expected = "OFFER " + str(i + 1) + "\n"
        got = readASCIILine(sock, len(expected.encode('ascii')))
        if got != expected:
            print("Invalid offer", file=sys.stderr)
            return

        localName = name
        if select:
            print()
            print('Name:', name)
            print('Size:', humansize(size))
            Q = ('Download file (' + str(i+1) + ' of ' + str(len(fileInfos))
                    + ')? [Yes/rename/no] ')
            answer = input(Q).strip().upper()

            if "YES".startswith(answer):
                pass
            elif "RENAME".startswith(answer):
                localName = input('Save as (blank for original name):') or name
            else:
                sendAll(sock, ("SKIP " + str(i + 1) + "\n").encode('ascii'))
                continue

        sendAll(sock, ("ACCEPT " + str(i + 1) + "\n").encode('ascii'))

        ok = __recvFileData(sock, localName, size)
        if not ok:
            return

        sendAll(sock, ('COMPLETED ' + str(i + 1) + '\n').encode('ascii'))


def __recvFileData(sock, name, size):
    """recvFileData(sock, name, size) -> success (boolean)"""

    if os.path.exists(name):
        print('File', name, 'exists. Will now exit.', file=sys.stderr)
        return False

    # timestamps
    ts_start = int(time.time())
    ts_update = ts_start
    print("{:41.41} starting..".format(name), end='')

    left = size
    with open(name, 'wb') as fstr:
        while left:
            chunk = sock.recv(min(4096, left))
            if not chunk:
                print('Stream closed after', size - left,
                'of', size, 'bytes', file = sys.stderr)
                return False

            written = 0
            while written < len(chunk):
                written += fstr.write(chunk[written:])
            left -= len(chunk)

            ts_update = updateProgress(name, size, size - left,
                    ts_start, ts_update)

        # the while loop is never entered for zero-length files
        updateProgress(name, size, size, ts_start, ts_update)

    print()
    return True

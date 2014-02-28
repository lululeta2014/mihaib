import os
import sys
import time

from util import readASCIILine, sendAll, updateProgress

def run(sock, files):
    fileinfos = __get_file_infos(files)
    __announce_files(sock, fileinfos)

    expected = 'SEND FILES\n'
    got = readASCIILine(sock, len(expected.encode('ascii')))
    if got != expected:
        print('Receiver showed no interest in files', file=sys.stderr)
        return

    for i in range(len(fileinfos)):
        __send_file(sock, fileinfos[i], i + 1)


def __get_file_infos(files):
    """__get_file_infos([file,..]) -> [(file, basename, length),..]"""
    fileinfos = []
    for f in files:
        basename = os.path.basename(f)
        if not basename:
            print('Basename is empty for ' + f, file=sys.stderr)
            sys.exit(1)
        length = os.path.getsize(f)
        fileinfos.append((f, basename, length))
    return fileinfos


def __announce_files(sock, fileinfos):
    sendAll(sock, ('FILE COUNT ' + str(len(fileinfos)) + '\n').encode('ascii'))
    i = 0
    for (file, basename, length) in fileinfos:
        i += 1
        sendAll(sock, (str(i) + '#\n').encode('ascii'))
        sendAll(sock, (basename + '\n').encode('ascii'))
        sendAll(sock, (str(length) + '\n').encode('ascii'))


def __send_file(sock, fileinfo, i):
    (file, basename, length) = fileinfo
    sendAll(sock, ('OFFER ' + str(i) + '\n').encode('ascii'))

    acceptMsg = 'ACCEPT ' + str(i) + '\n'
    skipMsg = 'SKIP ' + str(i) + '\n'
    maxLen = max(len(acceptMsg.encode('ascii')), len(skipMsg.encode('ascii')))

    got = readASCIILine(sock, maxLen)
    if got == skipMsg:
        print("{:41.41} SKIPPED".format(basename))
        return
    if got != acceptMsg:
        print("Receiver didn't send valid response to offer", file=sys.stderr)
        sys.exit(1)

    # send file
    ts_start = int(time.time())
    ts_update = ts_start
    print("{:41.41} starting..".format(basename), end='')

    left = length
    with open(file, 'rb') as fstr:
        while left:
            chunk = fstr.read(min(4096, left))

            # test end of file
            if not chunk:
                print("error: only " + str(length - left) + " of "
                        + str(length) + " bytes in " + file)
                sys.exit(1)

            sendAll(sock, chunk)
            left -= len(chunk)

            ts_update = updateProgress(basename, length, length - left,
                    ts_start, ts_update)

        # the while loop is never entered for zero-length files
        updateProgress(basename, length, length, ts_start, ts_update)

    print()

    expected = 'COMPLETED ' + str(i) + '\n'
    got = readASCIILine(sock, len(expected.encode('ascii')))
    if got != expected:
        print("Receiver didn't send valid confirmation", file=sys.stderr)
        sys.exit(1)

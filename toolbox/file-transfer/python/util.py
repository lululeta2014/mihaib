import time

def sendAll(sock, b):
    """send all bytes to socket"""

    sent = 0
    while sent < len(b):
        sent += sock.send(b[sent:])


def readASCIILine(sock, maxBytes):
    """readASCIILine(socket, maxBytes) -> string

    Reads ASCII bytes from socket until '\\n' is read, maxBytes are read
    or end of stream is reached. Returns a string with all ASCII chars read.
    If a byte not allowed by the protocol is found, a ValueError is thrown.

    throws ValueError
    """

    line = bytearray()
    while maxBytes:
        chunk = sock.recv(1)

        # check for end of stream
        if not chunk:
            break

        byte = chunk[0]
        if not __good_ascii_byte(byte):
            raise ValueError('Received ASCII byte not allowed by protocol '
                    'in text messages: ' + repr(chunk.decode('ascii')) +
                    ' code ' + str(byte))

        line.append(byte)
        maxBytes -= 1
        if byte == '\n'.encode('ascii')[0]:
            break

    return line.decode('ascii')


def __good_ascii_byte(b):
    # defensive test
    if b < 0 or b > 255:
        return False

    if 'A'.encode('ascii')[0] <= b and b <= 'Z'.encode('ascii')[0]:
        return True
    if 'a'.encode('ascii')[0] <= b and b <= 'z'.encode('ascii')[0]:
        return True
    if '0'.encode('ascii')[0] <= b and b <= '9'.encode('ascii')[0]:
        return True

    chars = "\n !#$%&'()+,-.;=@[]^_`{}~"
    return b in chars.encode('ascii')


def humansize(n):
    if n < 0:
        raise ValueError('humansize requires n >= 0; supplied ' + str(n))

    if n < 1024:
        return str(n) + '  B'

    units = ['KB', 'MB', 'GB']
    unit_size = 1024
    i = 0

    while i < len(units) - 1 and 1024 * unit_size <= n:
        i += 1
        unit_size *= 1024

    return '{:.1f} '.format(n / unit_size) + units[i]


def humantime(t):
    units = [('s', 1), ('m', 60), ('h', 60*60),
            ('d', 24*60*60), ('w', 7*24*60*60)]

    if t <= 0:
        return ''
    elif t < 60:
        return str(t) + 's' + ' ETA'

    for i in range(1, len(units)):
        major = units[i]
        v1 = t // major[1]

        if i < len(units)-1:
            nxt = units[i+1]
            if v1 < nxt[1] // major[1]:
                break

    # choose some maximum to ignore
    if i == len(units)-1 and v1 >= 20:
        return ''

    minor = units[i-1]
    v2 = (t - (v1 * major[1])) // minor[1]
    return str(v1) + major[0] + str(v2) + minor[0] + ' ETA'


def updateProgress(name, size, transferred, ts_start, ts_update):
    """Updates progress if transferred == size or once a second
    
    returns time of last update (ts_now if update performed, else ts_update)"""

    ts_now = int(time.time())
    if transferred == size or ts_now > ts_update:
        percent = transferred * 100 // size if size else 100
        speedStr = ''
        eta = ''
        if ts_now > ts_start:
            speedNr = transferred // (ts_now - ts_start)
            speedStr = humansize(speedNr) + '/s'
            if speedNr:
                eta = humantime((size - transferred) // speedNr)
        print("\r{:41.41} {:3}% {:>9.9} {:>11.11} {:>10.10}".format(name,
            percent, humansize(transferred), speedStr, eta), end='')
        return ts_now
    return ts_update

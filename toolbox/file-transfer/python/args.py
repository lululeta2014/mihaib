from optparse import OptionParser
import sys

# initilize module by creating parser

usage = "%prog [options] [FILE...]"
description = "Send or receive files (send if FILE given, receive otherwise)"
parser = OptionParser(usage=usage, description=description)
parser.add_option("-l", "--listen", type="int",
        metavar="port", help="listen for connections on port")
parser.add_option("-c", "--connect",
        metavar="host:port", help="connect to host:port")


def check_args():
    """Parse command line arguments
    and return (listen_port, remote_host, remote_port, files) if valid.
    Exit (with error code) if not valid
    """

    if len(sys.argv) == 1:
        parser.print_help()
        sys.exit()

    (options, files) = parser.parse_args()

    if (options.listen and options.connect):
        parser.print_usage(file=sys.stderr)
        print("error: both --listen and --connect given", file=sys.stderr)
        sys.exit(1)

    if (not options.listen and not options.connect):
        parser.print_usage(file=sys.stderr)
        print("error: pass either --listen or --connect", file=sys.stderr)
        sys.exit(1)

    host = None
    port = None

    if (options.connect):
        try:
            host, port = options.connect.split(":")
            port = int(port)
        except ValueError as e:
            parser.print_usage(file=sys.stderr)
            print("error in host:port argument: " + str(e), file=sys.stderr)
            sys.exit(1)

    return (options.listen, host, port, files)

if __name__ == '__main__':
    print(check_args())

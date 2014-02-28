import locale
import sys
from optparse import OptionParser

from . import encodings
from . import fileops

# initialize module by creating `parser'

parser = OptionParser(usage="%prog [options] FILE [..FILE]")
parser.add_option("-e", "--encodings", action="store_true",
        dest="print_encodings", default=False,
        help="print known encodings and exit")
parser.add_option("-i", metavar="encoding",
        default=locale.getpreferredencoding(),
        help="input encoding, your default is "
        + locale.getpreferredencoding())
parser.add_option("-o", metavar="encoding",
        default="UTF-8",
        help="output encoding, default is UTF-8")
parser.add_option("-r", "--replace", action="store_true", default=False,
        help="If present, overwrite each FILE (moving original to .bak). "
        "If absent, the FILEs in the list are interpreted as pairs "
        "(src1 dest1 src2 dest2..) and their number must be even.")

def run_with_args():
    # parse args
    (options, files) = parser.parse_args()

    if options.print_encodings:
        encodings.printKnownEncodings()
        sys.exit()

    if len(files) == 0:
        parser.print_help()
        # print "ERROR" only if user invoked us with SOME arguments
        if len(sys.argv) > 1:
            print()
            print("ERROR: missing FILE arguments")
        sys.exit(1)

    if not options.replace and len(files) % 2 != 0:
        print("ERROR: number of FILE arguments is not even")
        sys.exit(1)

    # check encodings
    i_valid = encodings.exists(options.i)
    o_valid = encodings.exists(options.o)
    if not i_valid or not o_valid:
        sys.exit(1)

    if options.replace:
        files = [(f, None) for f in files]
    else:
        new_files = []
        for i in range(0, len(files), 2):
            new_files.append((files[i], files[i+1]))
        files = new_files

    for (f_src, f_dest) in files:
        try:
            fileops.convert(f_src, options.i, f_dest, options.o)
        except IOError as e:
            print(e)
        except OSError as e:
            print(e)
        except Exception as e:
            print(e)

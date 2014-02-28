#! /usr/bin/env python3
import sys
from pecutil import params, gui

if __name__ == '__main__':
    if len(sys.argv) == 1:
        gui.run_with_gui()
    else:
        params.run_with_args()

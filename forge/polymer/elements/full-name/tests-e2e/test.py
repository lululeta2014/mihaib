#! /usr/bin/env python3

import os.path
import subprocess
import sys

def getScriptDir():
    '''May return the empty string, e.g. for ‘python3 module.py’'''
    return os.path.dirname(sys.argv[0])

def getPolymerDir():
    levels = 3
    d = os.path.realpath(os.path.abspath(getScriptDir()))
    for i in range(levels):
        d = os.path.dirname(d)
    return d


if __name__ == '__main__':
    port = 8082
    path = 'elements/full-name/tests-unit/test.html'
    ws = subprocess.Popen(['python3', '-m', 'http.server', str(port)],
            cwd=getPolymerDir())
    subprocess.Popen(['./webdriver.js'], cwd=os.path.realpath(getScriptDir()))
    try:
        ws.wait()
    except KeyboardInterrupt:
        pass

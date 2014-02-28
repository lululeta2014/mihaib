import os
import os.path
import subprocess
import sys


def check_sourceme_bash_or_exit(containing_dir):
    if not os.getenv('MB_SOURCEME_BASH'):
        sourceme_bash = os.path.join(containing_dir, 'sourceme.bash')
        print('source', sourceme_bash, file=sys.stderr)
        sys.exit(1)


def get_stdout_strip(arglist):
    '''Get stdout decoded as utf-8 as string with stripped whitespace'''
    return subprocess.check_output(arglist).decode('utf-8').strip()


def gsettings_get(schema, key):
    'Returns a string: calls get_stdout_strip() for "gsettings get schema key"'
    return get_stdout_strip(['gsettings', 'get', schema, key])


def gsettings_set(schema, key, value):
    'Calls "gsettings set schema key toGvariant(value)"'
    subprocess.check_call(['gsettings', 'set', schema, key, toGvariant(value)])


def toGvariant(x):
    '''
    Converts x to the Gvariant representation.

    Works only if x's type is: int, string, bool, list or tuple
    '''

    if type(x) == list:
        return '[' + ', '.join(toGvariant(v) for v in x) + ']'
    elif type(x) == tuple:
        return '(' + ', '.join(toGvariant(v) for v in x) + ')'
    elif type(x) == int:
        return str(x)
    elif type(x) == str:
        return repr(x)
    elif type(x) == bool:
        return 'true' if x else 'false'
    else:
        raise ValueError('Unknown type {t} for {x}'.format(x=x, t=type(x)))


def dconf_write(key, value):
    subprocess.check_call(['dconf', 'write', key, toGvariant(value)])


def gconftool2_set(dirpath, items):
    for k, v in items.items():
        if type(v) == int:
            t = 'int'
            v = str(v)
        elif type(v) == str:
            t = 'str'
        elif type(v) == bool:
            t = 'bool'
            v = 'true' if v else 'false'
        else:
            raise ValueError('Unknown type {t} for {v}'.format(t=type(v), v=v))

        subprocess.check_call(['gconftool-2', '--set', dirpath + '/' + k,
            '--type', t, v])

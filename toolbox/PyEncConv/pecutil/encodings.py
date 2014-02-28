import pkgutil
import encodings
import codecs

def getEncodings():
    '''Returns a set with all the encoding names'''
    return set(name for importer, name, ispkg
            in pkgutil.iter_modules(encodings.__path__)
            if not ispkg)

def getEncodingsAndAliases():
    '''Dictionary with key = encoding name, value = list of aliases'''
    result = {name:[] for name in getEncodings()}

    aliases = encodings.aliases.aliases
    for alias, name in aliases.items():
        if name in result:
            result[name].append(alias)
        else:
            result[name] = [alias]

    return result

def printKnownEncodings():
    '''Print each known encoding and its aliases on a line'''
    encodings = getEncodingsAndAliases()
    max_len = max([len(name) for name in encodings])
    for name, alias_list in sorted(encodings.items()):
        print(name.ljust(max_len), end=' ')
        print(*alias_list, sep=', ')

def exists(name):
    try:
        codecs.lookup(name)
        return True
    except LookupError:
        print("unknown encoding:", name)

if __name__ == '__main__':
    printKnownEncodings()

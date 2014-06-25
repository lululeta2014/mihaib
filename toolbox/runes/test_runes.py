import runes
import unittest

class Test(unittest.TestCase):

    def test_describe(self):
        for ch, desc in {
            " ": "' '  U+0020  SPACE  [20]",
            "\n": "'\\n' U+000A  ¡no such name!  [0a]",
            "\t": "'\\t' U+0009  ¡no such name!  [09]",
            "a": "'a'  U+0061  LATIN SMALL LETTER A  [61]",
            "€": "'€'  U+20AC  EURO SIGN  [e2 82 ac]",
            "∞": "'∞'  U+221E  INFINITY  [e2 88 9e]",
                }.items():
            self.assertEqual(runes.describe(ch), desc)

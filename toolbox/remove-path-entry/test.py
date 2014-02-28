import remove_path_entry
import unittest


class Test(unittest.TestCase):

    def test_discard(self):
        for pathEntries, toDiscard, want in (
                (['a', 'b', 'c', 'd'], ['c/', 'b'], ['a', 'd']),
                (['/a', '/b/'], ['/a/', '/b'], []),
                (['/my/X', '/a/Y/', '/my/Z/'], ['/a/Y'], ['/my/X', '/my/Z/']),
                ):
            got = remove_path_entry.discardEntries(pathEntries, toDiscard)
            self.assertEqual(got, want)


if __name__ == '__main__':
    unittest.main()

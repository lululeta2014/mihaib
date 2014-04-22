import dircomp9
import unittest


class Test(unittest.TestCase):

    def testPattern(self):
        for name in ['2014.04.22', '2010.12.31-hi', '2020.07.10-3',
                '2010.12.31-hello',
                '2011.03', '2011.08-', '2012.10-hey', '2010.05-09']:
            self.assertTrue(dircomp9.matchesPattern(name), name)

        for name in ['2012.8.10', '2014.04.229', 'A2014.04.22',
                '2010.12.31hi', '2020.07.10.3',
                '2011.03.', '2011.082', '2012.10hey', '2012.10.hey']:
            self.assertFalse(dircomp9.matchesPattern(name), name)

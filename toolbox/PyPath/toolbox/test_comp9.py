from toolbox import comp9
import unittest

class Test(unittest.TestCase):

    def test_comp9_valid(self):
        for x in range(0, 10):
            self.assertEqual(comp9.comp9(x), 9-x)
            self.assertEqual(comp9.comp9(comp9.comp9(x)), x)

    def test_comp9_invalid(self):
        for x in [-1, 10, '', '5', [], None, 'hey!']:
            self.assertRaises(ValueError, comp9.comp9, x)

    def test_num2alpha_valid(self):
        for n, a in {0: 'A', 1: 'B', 8: 'I', 9:'J'}.items():
            self.assertEqual(comp9.num2alpha(n), a)
            self.assertEqual(comp9.num2alpha(n, True), a.lower())

    def test_num2alpha_invalid(self):
        for x in [-1, 10, '', '3', [], None, 'word']:
            self.assertRaises(ValueError, comp9.num2alpha, x)
            self.assertRaises(ValueError, comp9.num2alpha, x, True)

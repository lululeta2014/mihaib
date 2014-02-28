import add
import unittest

class Test(unittest.TestCase):

    def test_add(self):
        self.assertEqual(add.add(3, 4), 7)

if __name__ == '__main__':
    unittest.main()

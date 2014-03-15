if __name__ == '__main__':
    primes_list = [2, 3, 5, 7]
    primes_set = set(primes_list)

    n = 9
    found = 0
    S = 0
    while found < 11:
        n += 2
        x = n
        for f in primes_list:
            if f * f > x:
                break
            while x % f == 0:
                x //= f
        if x > 1:
            primes_list.append(x)
            primes_set.add(x)

        if x == n:
            # n is prime
            left_nr = n
            right_nr = 0
            pow10 = 1

            truncatable = True
            while left_nr >= 10:
                digit = left_nr % 10
                left_nr //= 10
                right_nr = right_nr + digit * pow10
                pow10 *= 10
                if (left_nr not in primes_set) or (right_nr not in primes_set):
                    truncatable = False
                    break

            if truncatable:
                found += 1
                S += n
                print(n)

    print(S)

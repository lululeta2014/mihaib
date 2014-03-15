#! /usr/bin/env python3

fact = [1]
for i in range(1, 101):
    fact.append(i * fact[i-1])

def comb(n, k):
    return fact[n] // fact[k] // fact[n-k]

count = 0
for n in range(1, 101):
    for k in range(n+1):
        if comb(n, k) > 1000000:
            count += 1
print(count)

def t(n):
    return n * (n + 1) // 2

def p(n):
    return n * (3 * n - 1) // 2

def h(n):
    return n * (2 * n - 1)

if __name__ == '__main__':
    n_t, n_p, n_h = 285, 165, 143
    v_t, v_p, v_h = t(n_t), p(n_p), h(n_h)

    n_t += 1;
    v_t = t(n_t)

    while v_t != v_p or v_t != v_h:
        while v_t < v_p:
            n_t += 1
            v_t = t(n_t)

        while v_p < v_t:
            n_p += 1
            v_p = p(n_p)

        if v_p != v_t:
            continue

        # v_p == v_t here

        while v_t < v_h:
            n_t += 1
            v_t = t(n_t)

        while v_h < v_t:
            n_h += 1
            v_h = h(n_h)

    # when all three are equal the above while loop exits
    print(v_t)

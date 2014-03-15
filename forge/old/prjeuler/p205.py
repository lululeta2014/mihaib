from fractions import Fraction

def inc_roll(roll, max_value):
    t = 1
    for i in range(len(roll)):
        s = roll[-1 - i] + t
        roll[-1 - i] = s % max_value
        t = s // max_value
        if t == 0:
            break

def get_count(dice_count, faces_count):
    probab = [0 for i in range(faces_count * dice_count + 1)]
    crt_roll = [0 for i in range(dice_count)]

    for i in range(faces_count ** dice_count):
        probab[sum(crt_roll) + dice_count] += 1
        inc_roll(crt_roll, faces_count)
    return probab

if __name__ == '__main__':
    P_count = get_count(9, 4)
    C_count = get_count(6, 6)
    total_events = 4 ** 9
    P_probab = [Fraction(v, total_events) for v in P_count]
    total_events = 6 ** 6
    C_probab = [Fraction(v, total_events) for v in C_count]

    P_prob_lower = [sum([q for j, q in enumerate(P_probab) if j < i])
            for i, v in enumerate(P_probab)]
    C_prob_lower = [sum([q for j, q in enumerate(C_probab) if j < i])
            for i, v in enumerate(C_probab)]

    # P probabilities 0->36, C probabilities 0->36
    p_win = [p_hit * c_lower for p_hit, c_lower in zip(P_probab, C_prob_lower)]
    print(sum(p_win))

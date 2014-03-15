from prjeuler import get_base_10_digits

def get_digit(digit_pos):
    # make the position zero-based
    digit_pos -= 1
    dig_count = 1
    numbers = 9 * 10 ** (dig_count - 1)
    length = dig_count * numbers

    while digit_pos >= length:
        digit_pos -= length
        dig_count += 1
        numbers *= 10
        length = dig_count * numbers

    number_index = digit_pos // dig_count
    target_number = 10 ** (dig_count - 1) + number_index
    selected_digit = digit_pos % dig_count
    return get_base_10_digits(target_number)[selected_digit]

if __name__ == '__main__':
    p = 1
    for i in range(7):
        p *= get_digit(10 ** i)
    print(p)

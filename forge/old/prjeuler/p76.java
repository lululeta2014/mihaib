public class p76 {
	public static void main(String[] args) {
		int target = 100;
		int[] seq = new int[target];
		int[] partial_sum = new int[target];

		for (int i = 0; i < target; i++) {
			seq[i] = 1;
			partial_sum[i] = i + 1;
		}

		int counter = 0;
		int i = target - 1;
		while (i > 0) {
			if (partial_sum[i] == target) {
				counter++;
				/*for (int j = 0; j <= i; j++)
					System.out.print(seq[j] + " ");
				System.out.println();*/
			}

			i--;
			seq[i]++;
			partial_sum[i]++;
			int paste_val = seq[i];

			while(partial_sum[i] < target) {
				i++;
				seq[i] = paste_val;
				partial_sum[i] = partial_sum[i - 1] + paste_val;
			}
		}

		System.out.println(counter);
	}
}

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.Queue;

public class p298 {

	static class Follow {
		/** score diff (L-R) and destination index, for each input in 1..10 */
		int[] score_dif = new int[11], cell = new int[11];
	}

	// biggest memory value is [6, 7, 8, 9, 10] -> index 98240

	static void getRobinMem(int index, int[] result) {
		for (int i = 4; i >= 0; i--) {
			result[i] = index % 11;
			index /= 11;
		}
	}

	static void getLarryMem(int[] robinMem, int[] result) {
		int i = 0;
		while (i < 5 && robinMem[i] == 0) {
			result[i] = 0;
			i++;
		}

		int x = 1;
		while (i < 5) {
			result[i] = x;
			i++;
			x++;
		}
	}

	static int getIndex(int[] robinMem) {
		int index = 0;
		for (int i = 0; i < 5; i++)
			index = index * 11 + robinMem[i];
		return index;
	}

	static Follow[] transitions = new Follow[98241];

	static void compute_transitions() {
		Queue<Integer> statesToCreate = new LinkedList<Integer>();
		statesToCreate.add(0);

		int[] robinMem = new int[5], larryMem = new int[5];
		int[] nextRobin = new int[5], nextLarry = new int[5];

		while (!statesToCreate.isEmpty()) {
			int index = statesToCreate.remove();
			if (transitions[index] != null)
				continue;
			Follow F = new Follow();
			transitions[index] = F;

			getRobinMem(index, robinMem);
			getLarryMem(robinMem, larryMem);

			for (int N = 1; N <= 10; N++) {
				int robScore = getNextRobin(robinMem, nextRobin, N);
				int larScore = getNextLarry(larryMem, nextLarry, N);
				renameRobin(nextRobin, nextLarry);

				int nextIndex = getIndex(nextRobin);
				if (transitions[nextIndex] == null)
					statesToCreate.add(nextIndex);

				F.cell[N] = nextIndex;
				F.score_dif[N] = larScore - robScore;
			}
		}
	}

	static int getNextRobin(int[] robinMem, int[] nextRobin, int N) {
		for (int i = 0; i < 5; i++)
			if (robinMem[i] == N) {
				for (int j = 0; j < 5; j++)
					nextRobin[j] = robinMem[j];
				return 1;
			}

		for (int i = 0; i < 4; i++)
			nextRobin[i] = robinMem[i + 1];
		nextRobin[4] = N;
		return 0;
	}

	static int getNextLarry(int[] larryMem, int[] nextLarry, int N) {
		for (int i = 0; i < 5; i++)
			if (larryMem[i] == N) {
				for (int j = 0; j < i; j++)
					nextLarry[j] = larryMem[j];
				for (int j = i + 1; j < 5; j++)
					nextLarry[j - 1] = larryMem[j];
				nextLarry[4] = N;
				return 1;
			}

		for (int i = 0; i < 4; i++)
			nextLarry[i] = larryMem[i + 1];
		nextLarry[4] = N;
		return 0;
	}

	static void renameRobin(int[] nextRobin, int[] nextLarry) {
		int[] transpose = new int[11];
		int zeros = 0;

		for (int i = 0; i < 5; i++) {
			if (nextLarry[i] == 0) {
				zeros++;
				continue;
			} else {
				transpose[nextLarry[i]] = i + 1 - zeros;
			}
		}

		int nextNr = 6 - zeros;

		for (int i = 0; i < 5; i++) {
			if (nextRobin[i] == 0)
				continue;
			if (transpose[nextRobin[i]] != 0)
				nextRobin[i] = transpose[nextRobin[i]];
			else
				nextRobin[i] = nextNr++;
		}
	}

	public static void main(String[] args) {
		compute_transitions();
		System.out.println(run_game());
	}

	static double run_game() {
		BigInteger[][] prev_mat = new BigInteger[101][98241];
		BigInteger[][] next_mat;

		prev_mat[50 + 0][0] = BigInteger.ONE;

		for (int game = 1; game <= 50; game++) {
			next_mat = new BigInteger[101][98241];

			for (int prev_line = 0; prev_line < 101; prev_line++) {
				for (int prev_col = 0; prev_col < 98241; prev_col++) {
					BigInteger prevCount = prev_mat[prev_line][prev_col];

					if (prevCount != null) {
						Follow F = transitions[prev_col];
						for (int N = 1; N <= 10; N++) {
							int score_inc = F.score_dif[N];
							int next_index = F.cell[N];

							BigInteger nextCount = next_mat[prev_line
									+ score_inc][next_index];
							if (nextCount == null)
								nextCount = prevCount;
							else
								nextCount = nextCount.add(prevCount);
							next_mat[prev_line + score_inc][next_index] = nextCount;
						}
					}
				}
			}

			prev_mat = next_mat;
			System.out.print(game + ", ");
		}

		System.out.println();

		BigInteger total_cases = BigInteger.ZERO;
		BigInteger sum = BigInteger.ZERO;

		for (int line = 0; line < 101; line++) {
			BigInteger crt_cases = BigInteger.ZERO;
			for (int col = 0; col < 98241; col++) {
				BigInteger crtPossib = prev_mat[line][col];
				if (crtPossib != null)
					crt_cases = crt_cases.add(crtPossib);
			}
			total_cases = total_cases.add(crt_cases);
			sum = sum.add(crt_cases.multiply(BigInteger.valueOf(Math
					.abs(line - 50))));
			System.out.println((line - 50) + ":\t" + crt_cases);
		}

		System.out.println("TOTAL: " + sum + ", " + total_cases);
		return sum.doubleValue() / total_cases.doubleValue();
	}

}

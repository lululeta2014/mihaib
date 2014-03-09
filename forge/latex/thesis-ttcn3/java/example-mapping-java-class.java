package ro.pub;
public class Calculator {
	public static enum
		Operations { ADD, SUBTRACT, MULTIPLY, DIVIDE, OVERWRITE }

	public double result, memory;
	public void reset() { result = memory = 0; }
	public long roundResult() { return Math.round(result); }
	public long roundMem() { return Math.round(memory); }

	// return result := result op value
	public double compute(Operations op, double value)
		throws ArithmeticException;
	// return memory := memory op result
	public double toMemory(Operations op);
}

package helloworld;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.JUnitCore;

public class Test {

	public static void main(String[] args) {
		Class<?>[] classes = { AdderTest.class, };
		List<String> classNames = new ArrayList<>();
		for (Class<?> c : classes)
			classNames.add(c.getName());
		JUnitCore.main(classNames.toArray(new String[0]));
	}

}

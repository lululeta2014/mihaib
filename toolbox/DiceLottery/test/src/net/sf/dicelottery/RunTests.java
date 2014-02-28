package net.sf.dicelottery;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.JUnitCore;

public class RunTests {

	static String rootDir;
	static String testFilesDir;
	static String mainFilesDir;

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Usage: RunTests dicelottery_repository");
			System.exit(1);
		}

		rootDir = args[0];
		testFilesDir = Paths.get(rootDir, "test", "elem-files").toString();
		mainFilesDir = Paths.get(rootDir, "elem-files").toString();

		List<String> test = new ArrayList<String>();
		test.add(EventRepresConverterTest.class.getName());
		test.add(EventMapperTestFixture.class.getName());
		test.add(ElemReprConvTest.class.getName());
		test.add(EventMapperTest.class.getName());
		JUnitCore.main(test.toArray(new String[0]));
	}

}

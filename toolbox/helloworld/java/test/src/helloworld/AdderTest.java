package helloworld;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AdderTest {

	@Test
	public void add() {
		assertEquals(Adder.add(2, 4), 6);
	}

}

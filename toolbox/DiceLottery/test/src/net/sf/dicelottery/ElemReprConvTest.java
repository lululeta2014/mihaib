package net.sf.dicelottery;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Paths;

import net.sf.dicelottery.element.Element;
import net.sf.dicelottery.element.ElementRepresConverter;
import net.sf.dicelottery.element.ElementRepresentation;

import org.junit.Test;

public class ElemReprConvTest {

	/** Asserts the number and user representations of elements in the converter */
	private static void assertElements(ElementRepresConverter erc,
			String... repres) {
		if (repres == null)
			repres = new String[0];
		assertTrue(erc.elementCount.equals(new BigInteger(repres.length + "")));
		BigInteger i = BigInteger.ZERO;
		for (String repr : repres) {
			Element e = erc.getElement(i);
			assertTrue(e.index.equals(i));
			assertTrue(e.userRepres.equals(repr));
			i = i.add(BigInteger.ONE);
		}
	}

	@Test
	public void wordsWs() throws IOException {
		ElementRepresConverter erc;
		String fileName = Paths.get(RunTests.testFilesDir,
				"wordsWs-notrail-speak").toString();

		boolean caseSensitive = true;
		erc = new ElementRepresConverter(ElementRepresentation.CUSTOM_STRINGS,
				null, fileName, caseSensitive, null, false);
		assertElements(erc, "Speak", "low", "if", "you", "speak", "love.");

		caseSensitive = false;
		erc = new ElementRepresConverter(ElementRepresentation.CUSTOM_STRINGS,
				null, fileName, caseSensitive, null, false);
		assertElements(erc, "Speak", "low", "if", "you", "love.");

		fileName = Paths.get(RunTests.testFilesDir, "wordsWs-trail-follow")
				.toString();

		caseSensitive = true;
		erc = new ElementRepresConverter(ElementRepresentation.CUSTOM_STRINGS,
				null, fileName, caseSensitive, null, false);
		assertElements(erc, "Follow", "love", "and", "it", "will", "flee,",
				"flee", "follow", "thee.");

		caseSensitive = false;
		erc = new ElementRepresConverter(ElementRepresentation.CUSTOM_STRINGS,
				null, fileName, caseSensitive, null, false);
		assertElements(erc, "Follow", "love", "and", "it", "will", "flee,",
				"flee", "thee.");
	}

	@Test
	public void wordsSepFile() throws IOException {
		ElementRepresConverter erc;
		String strFileName = Paths.get(RunTests.testFilesDir,
				"wordsWs-trail-follow").toString();
		String sepFileName = Paths.get(RunTests.mainFilesDir, "separators",
				"punctuation").toString();

		boolean caseSensitive = true;
		erc = new ElementRepresConverter(ElementRepresentation.CUSTOM_STRINGS,
				null, strFileName, caseSensitive, sepFileName, true);
		assertElements(erc, "Follow", "love", "and", "it", "will", "flee",
				"follow", "thee");

		caseSensitive = false;
		erc = new ElementRepresConverter(ElementRepresentation.CUSTOM_STRINGS,
				null, strFileName, caseSensitive, sepFileName, true);
		assertElements(erc, "Follow", "love", "and", "it", "will", "flee",
				"thee");

		strFileName = Paths.get(RunTests.testFilesDir, "wordsSep-doubt")
				.toString();
		caseSensitive = false;
		erc = new ElementRepresConverter(ElementRepresentation.CUSTOM_STRINGS,
				null, strFileName, caseSensitive, sepFileName, true);
		assertElements(erc, "Doubt", "thou", "the", "stars", "are", "fire",
				"that", "sun", "doth", "move", "truth", "to", "be", "a",
				"liar", "But", "never", "I", "love");
	}

	@Test
	public void wordsSepFileNoWs() throws IOException {
		String strFileName = Paths.get(RunTests.testFilesDir, "wordsSep-noWs")
				.toString();
		String sepFileName = Paths.get(RunTests.mainFilesDir, "separators",
				"punctuation").toString();

		boolean caseSensitive = true;
		ElementRepresConverter erc = new ElementRepresConverter(
				ElementRepresentation.CUSTOM_STRINGS, null, strFileName,
				caseSensitive, sepFileName, false);
		assertElements(
				erc,
				"Now it is the time of night\nThat the graves",
				" all gaping wide",
				"\nEvery one lets forth his sprite\nIn the church-way paths to glide",
				"\n");

		sepFileName = Paths.get(RunTests.mainFilesDir, "separators", "crlf")
				.toString();

		caseSensitive = true;
		erc = new ElementRepresConverter(ElementRepresentation.CUSTOM_STRINGS,
				null, strFileName, caseSensitive, sepFileName, false);
		assertElements(erc, "Now it is the time of night",
				"That the graves, all gaping wide,",
				"Every one lets forth his sprite",
				"In the church-way paths to glide.");
	}

}

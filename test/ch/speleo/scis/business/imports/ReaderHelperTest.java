package ch.speleo.scis.business.imports;

import java.util.GregorianCalendar;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import ch.speleo.scis.business.imports.ReaderHelper.ConversionResult;

public class ReaderHelperTest {
	
	ReaderHelper readerHelper;
	
	@Before
	public void setup() {
		readerHelper = new ReaderHelper();
	}

	@Test
	public void testToBooleanTrue() {
		Assert.assertTrue(readerHelper.toBoolean("yes"));
		Assert.assertTrue(readerHelper.toBoolean("Y"));
		Assert.assertTrue(readerHelper.toBoolean("YeS"));
		Assert.assertTrue(readerHelper.toBoolean("oui"));
		Assert.assertTrue(readerHelper.toBoolean("ja"));
		Assert.assertTrue(readerHelper.toBoolean("true"));
		Assert.assertTrue(readerHelper.toBoolean("vrai"));
		Assert.assertTrue(readerHelper.toBoolean("wahr"));
	}
	@Test
	public void testToBooleanFalse() {
		Assert.assertFalse(readerHelper.toBoolean(null));
		Assert.assertFalse(readerHelper.toBoolean(" "));
		Assert.assertFalse(readerHelper.toBoolean("no"));
		Assert.assertFalse(readerHelper.toBoolean("N"));
		Assert.assertFalse(readerHelper.toBoolean("No"));
		Assert.assertFalse(readerHelper.toBoolean("non"));
		Assert.assertFalse(readerHelper.toBoolean("nein"));
		Assert.assertFalse(readerHelper.toBoolean("false"));
		Assert.assertFalse(readerHelper.toBoolean("faux"));
		Assert.assertFalse(readerHelper.toBoolean("falsch"));
	}

	@Test
	public void toIntegerFlexibleTrivial() {
		ConversionResult<Integer> result = readerHelper.toIntegerFlexible("21436");
		Assert.assertNull(result.getMessage());
		Assert.assertEquals(Integer.valueOf(21436), result.getResult());
	}
	@Test
	public void toIntegerFlexibleWithSpaces() {
		ConversionResult<Integer> result = readerHelper.toIntegerFlexible(" 21436 ");
		Assert.assertNull(result.getMessage());
		Assert.assertEquals(Integer.valueOf(21436), result.getResult());
	}
	@Test
	public void toIntegerFlexibleWithQuestionMark() {
		ConversionResult<Integer> result = readerHelper.toIntegerFlexible(" 21436?");
		Assert.assertNotNull(result.getMessage());
		Assert.assertTrue(result.getMessage().contains("question"));
		Assert.assertEquals(Integer.valueOf(21436), result.getResult());
	}
	@Test
	public void toIntegerFlexibleWithDot() {
		ConversionResult<Integer> result = readerHelper.toIntegerFlexible("2143.6");
		Assert.assertNotNull(result.getMessage());
		Assert.assertTrue(result.getMessage().contains("round"));
		Assert.assertEquals(Integer.valueOf(2144), result.getResult());
	}
	@Test
	public void toIntegerFlexibleWithComma() {
		ConversionResult<Integer> result = readerHelper.toIntegerFlexible("2143,6");
		Assert.assertNotNull(result.getMessage());
		Assert.assertTrue(result.getMessage().contains("round"));
		Assert.assertEquals(Integer.valueOf(2144), result.getResult());
	}

	@Test
	public void toDate() throws Exception {
		Assert.assertEquals(new GregorianCalendar(1996, 03, 25).getTime(), readerHelper.toDate("25.04.1996"));
		Assert.assertEquals(new GregorianCalendar(1996, 03, 25).getTime(), readerHelper.toDate("25 04 1996 12:00:00"));
	}

}

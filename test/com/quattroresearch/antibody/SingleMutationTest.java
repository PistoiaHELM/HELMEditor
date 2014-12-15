package com.quattroresearch.antibody;

import org.junit.Assert;
import org.junit.Test;

public class SingleMutationTest {

	@Test
	public void testSingleMutation() {

		Mutation testMutation = new Mutation("TestMutation");

		String domainName1 = "IGHG1_CH2_HUMAN";
		String domainName2 = "IGHG1_CH3_HUMAN";
		String domainName3 = "IGHG4_HINGE_HUMAN";

		SingleMutation sm1 = new SingleMutation(domainName1, 1, 'I', 'A',
        testMutation, "X", 0, null);
		SingleMutation sm2 = new SingleMutation(domainName2, 6, 'I', 'A',
        testMutation, "X", 0, null);
		SingleMutation sm3 = new SingleMutation(domainName3, 10, 'I', 'A',
        testMutation, "X", 0, null);

		boolean result = sm1.isMutated(domainName1, "AXXXXX");
		Assert.assertEquals(true, result);

		result = sm1.isMutated(domainName1, "IXXXXX");
		Assert.assertEquals(false, result);

		result = sm1.isMutated(domainName2, "AXXXXX");
		Assert.assertEquals(false, result);

		result = sm1.isMutated(domainName2, "IXXXXX");
		Assert.assertEquals(false, result);

		result = sm2.isMutated(domainName2, "XXNNXAXXXX");
		Assert.assertEquals(true, result);

		result = sm2.isMutated(domainName3, "XXNNXAXXXX");
		Assert.assertEquals(false, result);

		result = sm2.isMutated(domainName2, "XXNNXIXXXX");
		Assert.assertEquals(false, result);

		result = sm3.isMutated(domainName3, "XXNNXXNNXA");
		Assert.assertEquals(true, result);

		result = sm3.isMutated(domainName3, "XXNNXXNNXI");
		Assert.assertEquals(false, result);

		result = sm3.isMutated(domainName1, "XXNNXXNNXA");
		Assert.assertEquals(false, result);
	}

}

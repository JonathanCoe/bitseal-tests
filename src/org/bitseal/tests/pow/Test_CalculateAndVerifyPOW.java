package org.bitseal.tests.pow;

import java.security.SecureRandom;

import junit.framework.TestCase;

import org.bitseal.pow.POWProcessor;

/**
 * Tests the Proof of Work functions of Bitseal.
 * 
 * @author Jonathan Coe
 */
public class Test_CalculateAndVerifyPOW extends TestCase
{
	// The 'time to live' value to use in this test (in seconds)
	private static final long TIME_TO_LIVE = 3600;
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testCalculateAndVerifyPOW()
	{
		byte[] mockPayload = new byte[100];
		new SecureRandom().nextBytes(mockPayload);
		
		POWProcessor powProc = new POWProcessor();
		long powNonce = powProc.doPOW(mockPayload, POWProcessor.NETWORK_NONCE_TRIALS_PER_BYTE, POWProcessor.NETWORK_EXTRA_BYTES, TIME_TO_LIVE);
		
		assertTrue(powProc.checkPOW(mockPayload, powNonce, POWProcessor.NETWORK_NONCE_TRIALS_PER_BYTE, POWProcessor.NETWORK_EXTRA_BYTES));
	}
}
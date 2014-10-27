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
		
		// Set an expiration time of 1 hour from now
		long expirationTime = (System.currentTimeMillis() / 1000) + 3600;
		
		POWProcessor powProc = new POWProcessor();
		long powNonce = powProc.doPOW(mockPayload, expirationTime, POWProcessor.NETWORK_NONCE_TRIALS_PER_BYTE, POWProcessor.NETWORK_EXTRA_BYTES);
		
		assertTrue(powProc.checkPOW(mockPayload, powNonce, expirationTime, POWProcessor.NETWORK_NONCE_TRIALS_PER_BYTE, POWProcessor.NETWORK_EXTRA_BYTES));
	}
}
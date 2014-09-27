package org.bitseal.tests.core;

import java.util.Arrays;

import junit.framework.TestCase;

import org.bitseal.core.AddressProcessor;
import org.bitseal.util.ArrayCopier;
import org.bitseal.util.ByteFormatter;

import android.util.Log;

/**
 * Tests the method AddressProcessor.calculateMessageTag().
 * 
 * @author Jonathan Coe
 */
public class Test_CalculateMessageTag extends TestCase
{
	private static final String TEST_ADDRESS = "BM-87ozvCK4Jkx9Pc4dP7cd6y3T33DcSdmWPaq";
	
	// The input time for test 0
	private static final long TEST_TIME = 1404786854;
	// The expected result data for test 0
	private static final String EXPECTED_MESSAGE_TAG_HEX = "8d2a1e2284791ec0fbbec41b58b1ecba341a9ccde1306d861a1702253f2a21e1";
	
	// The input time for test 1
	private static final long PAST_TIME = 1404522060;
	
	private static final int SECONDS_IN_A_DAY = 86400;
	
	private static final String TAG = "TEST_CALCULATE_MESSAGE_TAG";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testCalculateDoubleHashOfAddressData()
	{
		// Test 0: Test the basic 'calculate message tag' method
		// Calculate the message tag
		AddressProcessor addProc = new AddressProcessor();
		byte[] messageTag = addProc.calculateMessageTag(TEST_ADDRESS, TEST_TIME);
		String messageTagHex = ByteFormatter.byteArrayToHexString(messageTag);
		
		// Check the result of test 0
		Log.i(TAG, "Message tag for address " + TEST_ADDRESS + " and timestamp " + TEST_TIME + ":\n" + messageTagHex);
		assertTrue(messageTagHex.equals(EXPECTED_MESSAGE_TAG_HEX));
		
		// Test 1: Test the 'calculate message tags since' method
		byte[] messageTags = addProc.calculateMessageTagsSince(TEST_ADDRESS,  PAST_TIME);
		int tagsCalculated = messageTags.length / 32;
		Log.i(TAG, "Calculated " + tagsCalculated + " message tag(s) for address " + TEST_ADDRESS + " since " +  PAST_TIME);
		for (int i = 0; i < messageTags.length; i += 32)
		{
			byte[] tag = ArrayCopier.copyOfRange(messageTags, i, i + 32);
			Log.i(TAG, "Message Tag: " + ByteFormatter.byteArrayToHexString(tag));
		}
		
		// Check the result of test 1
		long currentTime = System.currentTimeMillis() / 1000;
		long timeElapsed = currentTime - PAST_TIME;
		long numberOfDaysSince = timeElapsed / SECONDS_IN_A_DAY;
		assertEquals(tagsCalculated, numberOfDaysSince + 1);
		byte[] finalTag = ArrayCopier.copyOfRange(messageTags, messageTags.length - 32, messageTags.length);
		byte[] expectedFinalTag = addProc.calculateMessageTag(TEST_ADDRESS, currentTime);
		assertTrue(Arrays.equals(finalTag, expectedFinalTag));
	}
}
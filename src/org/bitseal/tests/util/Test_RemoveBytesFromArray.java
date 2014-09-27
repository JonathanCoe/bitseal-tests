package org.bitseal.tests.util;

import java.security.SecureRandom;

import junit.framework.TestCase;

import org.bitseal.util.ByteFormatter;
import org.bitseal.util.ByteUtils;

import android.util.Log;

/**
 * Tests the method ByteUtils.removeBytesFromArray()
 * 
 * @author Jonathan Coe
 *
 */
public class Test_RemoveBytesFromArray extends TestCase
{
	private static final String TAG = "TEST_REMOVE_BYTES_FROM_ARRAY";
	private static final int ORIGINAL_ARRAY_LENGTH = 3;
	private static final int START_INDEX = 1;
	private static final int END_INDEX = 2;
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testRemoveBytesFromArray()
	{
		int bytesToBeRemoved = (END_INDEX - START_INDEX);
		
		// Create a byte[] and fill it with some random data
		byte[] original = new byte[ORIGINAL_ARRAY_LENGTH];
		new SecureRandom().nextBytes(original);
		Log.i(TAG, "Original byte array:         " + ByteFormatter.byteArrayToHexString(original));
		Log.i(TAG, "Original byte array length:  " + original.length);
		
		// Now try to remove some bytes from within the array (i.e. not beginning at the start or ending at the end)
		byte[] remainder = ByteUtils.removeBytesFromArray(original, START_INDEX, END_INDEX);
		Log.i(TAG, "Remainder byte array:        " + ByteFormatter.byteArrayToHexString(remainder));
		Log.i(TAG, "Remainder byte array length: " + remainder.length);
		
		// Check that the removal was done correctly
		assertTrue(bytesToBeRemoved == original.length - remainder.length);
	}
}
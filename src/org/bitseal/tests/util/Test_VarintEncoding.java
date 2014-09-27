package org.bitseal.tests.util;

import junit.framework.TestCase;

import org.bitseal.util.ByteFormatter;
import org.bitseal.util.VarintEncoder;

import android.util.Log;

/**
 * Tests the app's encoding and decoding functions for variable length integer encoding.<br><br>
 * 
 * See https://bitmessage.org/wiki/Protocol_specification#Variable_length_integer
 * 
 * @author Jonathan Coe
 */
public class Test_VarintEncoding extends TestCase
{
	private static final String TAG = "TEST_VARINT_ENCODING";
		
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testStringToByteArrayConversion()
	{
		// Test with 0
		byte[] encoded0 = VarintEncoder.encode(0);
		long[] decoded0 = VarintEncoder.decode(encoded0);
		Log.i(TAG, "Encoded 0: " + ByteFormatter.byteArrayToHexString(encoded0));
		Log.i(TAG, "Decoded 0: Decimal value " + decoded0[0] + ", Bytes used " + decoded0[1]);
		assertEquals(decoded0[0], 0);
		assertEquals(decoded0[1], 1);
		
		// Test with 1
		byte[] encoded1 = VarintEncoder.encode(1);
		long[] decoded1 = VarintEncoder.decode(encoded1);
		Log.i(TAG, "Encoded 1: " + ByteFormatter.byteArrayToHexString(encoded1));
		Log.i(TAG, "Decoded 1: Decimal value " + decoded1[0] + ", Bytes used " + decoded1[1]);
		assertEquals(decoded1[0], 1);
		assertEquals(decoded1[1], 1);
		
		// Test with 200
		byte[] encoded200 = VarintEncoder.encode(200);
		long[] decoded200 = VarintEncoder.decode(encoded200);
		Log.i(TAG, "Encoded 200: " + ByteFormatter.byteArrayToHexString(encoded200));
		Log.i(TAG, "Decoded 200: Decimal value " + decoded200[0] + ", Bytes used " + decoded200[1]);
		assertEquals(decoded200[0], 200);
		assertEquals(decoded200[1], 1);
		
		//Test with 1,000
		byte[] encoded1000 = VarintEncoder.encode(1000);
		long[] decoded1000 = VarintEncoder.decode(encoded1000);
		Log.i(TAG, "Encoded 1000: " + ByteFormatter.byteArrayToHexString(encoded1000));
		Log.i(TAG, "Decoded 1000: Decimal value " + decoded1000[0] + ", Bytes used  " + decoded1000[1]);
		assertEquals(decoded1000[0], 1000);
		assertEquals(decoded1000[1], 3);
		
		//Test with 100,000
		byte[] encoded100000 = VarintEncoder.encode(100000);
		long[] decoded100000 = VarintEncoder.decode(encoded100000);
		Log.i(TAG, "Encoded 100000: " + ByteFormatter.byteArrayToHexString(encoded100000));
		Log.i(TAG, "Decoded 100000: Decimal value " + decoded100000[0] + ", Bytes used " + decoded100000[1]);
		assertEquals(decoded100000[0], 100000);
		assertEquals(decoded100000[1], 5);
		
		//Test with 10,000,000,000 (ten billion)
		byte[] encoded10000000000 = VarintEncoder.encode(10000000000L);
		long[] decoded10000000000 = VarintEncoder.decode(encoded10000000000);
		Log.i(TAG, "Encoded 10000000000: " + ByteFormatter.byteArrayToHexString(encoded10000000000));
		Log.i(TAG, "Decoded 10000000000: Decimal value " + decoded10000000000[0] + ", Bytes used " + decoded10000000000[1]);
		assertEquals(decoded10000000000[0], 10000000000L);
		assertEquals(decoded10000000000[1], 9);
	}
}
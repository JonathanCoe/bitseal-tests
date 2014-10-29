package org.bitseal.tests.util;

import java.security.SecureRandom;
import java.util.Arrays;

import junit.framework.TestCase;

import org.bitseal.util.ArrayCopier;
import org.bitseal.util.ByteFormatter;
import org.bitseal.util.ByteUtils;

import android.util.Log;

/**
 * Tests the ByteUtils class.
 * 
 * @author Jonathan Coe
 */
public class Test_ConcatenateByteArrays extends TestCase
{
	SecureRandom secRand;
	
	private static final String TAG = "TEST_CONCATENATE_BYTE_ARRAYS";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testByteUtils()
	{
		secRand = new SecureRandom();
		
		concatenate2ByteArrays();
		concatenate3ByteArrays();
		concatenate4ByteArrays();
		concatenate5ByteArrays();
	}
	
	private void concatenate2ByteArrays()
	{
		byte[] a = new byte[10];
		byte[] b = new byte[10];
		
		secRand.nextBytes(a);
		secRand.nextBytes(b);
		
		byte[] c = ByteUtils.concatenateByteArrays(a, b);
		
		Log.i(TAG, "Byte [] a: " + ByteFormatter.byteArrayToHexString(a));
		Log.i(TAG, "Byte [] b: " + ByteFormatter.byteArrayToHexString(b));
		Log.i(TAG, "Byte [] c: " + ByteFormatter.byteArrayToHexString(c));
		
		assertEquals(c.length, a.length + b.length);
		
		assertTrue(Arrays.equals(a, ArrayCopier.copyOfRange(c, 0, a.length)));
		assertTrue(Arrays.equals(b, ArrayCopier.copyOfRange(c, a.length, a.length + b.length)));
	}
	
	private void concatenate3ByteArrays()
	{
		byte[] a = new byte[10];
		byte[] b = new byte[10];
		byte[] c = new byte[10];
		
		secRand.nextBytes(a);
		secRand.nextBytes(b);
		secRand.nextBytes(c);
		
		byte[] d = ByteUtils.concatenateByteArrays(a, b, c);
		
		Log.i(TAG, "Byte [] a: " + ByteFormatter.byteArrayToHexString(a));
		Log.i(TAG, "Byte [] b: " + ByteFormatter.byteArrayToHexString(b));
		Log.i(TAG, "Byte [] c: " + ByteFormatter.byteArrayToHexString(c));
		Log.i(TAG, "Byte [] d: " + ByteFormatter.byteArrayToHexString(d));
		
		assertEquals(d.length, a.length + b.length + c.length);
		
		assertTrue(Arrays.equals(a, ArrayCopier.copyOfRange(d, 0, a.length)));
		assertTrue(Arrays.equals(b, ArrayCopier.copyOfRange(d, a.length, a.length + b.length)));
		assertTrue(Arrays.equals(c, ArrayCopier.copyOfRange(d, a.length + b.length, a.length + b.length + c.length)));
	}
	
	private void concatenate4ByteArrays()
	{
		byte[] a = new byte[10];
		byte[] b = new byte[10];
		byte[] c = new byte[10];
		byte[] d = new byte[10];
		
		secRand.nextBytes(a);
		secRand.nextBytes(b);
		secRand.nextBytes(c);
		secRand.nextBytes(d);
		
		byte[] e = ByteUtils.concatenateByteArrays(a, b, c, d);
		
		Log.i(TAG, "Byte [] a: " + ByteFormatter.byteArrayToHexString(a));
		Log.i(TAG, "Byte [] b: " + ByteFormatter.byteArrayToHexString(b));
		Log.i(TAG, "Byte [] c: " + ByteFormatter.byteArrayToHexString(c));
		Log.i(TAG, "Byte [] d: " + ByteFormatter.byteArrayToHexString(d));
		Log.i(TAG, "Byte [] e: " + ByteFormatter.byteArrayToHexString(e));
		
		assertEquals(e.length, a.length + b.length + c.length + d.length);
		
		assertTrue(Arrays.equals(a, ArrayCopier.copyOfRange(e, 0, a.length)));
		assertTrue(Arrays.equals(b, ArrayCopier.copyOfRange(e, a.length, a.length + b.length)));
		assertTrue(Arrays.equals(c, ArrayCopier.copyOfRange(e, a.length + b.length, a.length + b.length + c.length)));
		assertTrue(Arrays.equals(d, ArrayCopier.copyOfRange(e, a.length + b.length + c.length, a.length + b.length + c.length + d.length)));
	}
	
	private void concatenate5ByteArrays()
	{
		byte[] a = new byte[10];
		byte[] b = new byte[10];
		byte[] c = new byte[10];
		byte[] d = new byte[10];
		byte[] e = new byte[10];
		
		secRand.nextBytes(a);
		secRand.nextBytes(b);
		secRand.nextBytes(c);
		secRand.nextBytes(d);
		secRand.nextBytes(e);
		
		byte[] f = ByteUtils.concatenateByteArrays(a, b, c, d, e);
		
		Log.i(TAG, "Byte [] a: " + ByteFormatter.byteArrayToHexString(a));
		Log.i(TAG, "Byte [] b: " + ByteFormatter.byteArrayToHexString(b));
		Log.i(TAG, "Byte [] c: " + ByteFormatter.byteArrayToHexString(c));
		Log.i(TAG, "Byte [] d: " + ByteFormatter.byteArrayToHexString(d));
		Log.i(TAG, "Byte [] e: " + ByteFormatter.byteArrayToHexString(e));
		Log.i(TAG, "Byte [] f: " + ByteFormatter.byteArrayToHexString(f));
		
		assertEquals(f.length, a.length + b.length + c.length + d.length + e.length);
		
		assertTrue(Arrays.equals(a, ArrayCopier.copyOfRange(f, 0, a.length)));
		assertTrue(Arrays.equals(b, ArrayCopier.copyOfRange(f, a.length, a.length + b.length)));
		assertTrue(Arrays.equals(c, ArrayCopier.copyOfRange(f, a.length + b.length, a.length + b.length + c.length)));
		assertTrue(Arrays.equals(d, ArrayCopier.copyOfRange(f, a.length + b.length + c.length, a.length + b.length + c.length + d.length)));
		assertTrue(Arrays.equals(e, ArrayCopier.copyOfRange(f, a.length + b.length + c.length + d.length, a.length + b.length + c.length + d.length + e.length)));
	}
}
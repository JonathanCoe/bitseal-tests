package org.bitseal.tests.core;

import junit.framework.TestCase;

import org.bitseal.core.AddressProcessor;
import org.bitseal.util.ArrayCopier;
import org.bitseal.util.ByteFormatter;

import android.util.Log;

/**
 * Tests the method AddressProcessor.calculateDoubleHashOfAddressData().
 * 
 * @author Jonathan Coe
 *
 */
public class Test_CalculateDoubleHashOfAddressData extends TestCase
{
	private static final String TAG = "TEST_CALCULATE_DOUBLE_HASH_ADDRESS_DATA";
	
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
		String testAddressString = "BM-2DBMp51U9M3BHpR9sPvNw4G1ZWLxbHbvLE";
		String expectedEncryptionKeyInHex = "aa86c9907f5d882cda84b6003ce209c4910619acbb7e958f6dd9cec88a7dd798";
		String expectedTagInHex = "fd46c9d16cdede8b6a5ecaa5fa5c53f9bcd17d6cfb3406eea89682d8b470d29c";
		
		AddressProcessor addProc = new AddressProcessor();
		byte[] doubleHash = addProc.calculateDoubleHashOfAddressData(testAddressString);
		byte[] encryptionKey = ArrayCopier.copyOfRange(doubleHash, 0, 32);
		byte[] tag = ArrayCopier.copyOfRange(doubleHash, 32, doubleHash.length);
		String tagInHex = ByteFormatter.byteArrayToHexString(tag);
		String encryptionKeyInHex = ByteFormatter.byteArrayToHexString(encryptionKey);
		
		Log.i(TAG, "Calculated double hash of address data, in hex: " + ByteFormatter.byteArrayToHexString(doubleHash));
		Log.i(TAG, "Tag taken bytes 0-32 of the double hash, in hex: " + tagInHex);
		Log.i(TAG, "Encryption key taken from bytes 32-64 of the double hash, in hex: " + encryptionKeyInHex);
		
		assertEquals(expectedTagInHex, tagInHex);
		assertEquals(expectedEncryptionKeyInHex, encryptionKeyInHex);
	}
}
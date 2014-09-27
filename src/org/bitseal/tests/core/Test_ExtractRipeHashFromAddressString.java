package org.bitseal.tests.core;

import java.util.Arrays;

import junit.framework.TestCase;

import org.bitseal.core.AddressProcessor;
import org.bitseal.core.App;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.data.Address;
import org.bitseal.database.AddressProvider;
import org.bitseal.util.ByteFormatter;

import android.util.Log;

/** 
 * Tests the method AddressProcessor.extractRipeHashFromAddressString()
 * 
 * @author Jonathan Coe
**/
public class Test_ExtractRipeHashFromAddressString extends TestCase
{
	private static final String TAG = "TEST_EXTRACT_RIPE_HASH_FROM_ADDRESS_STRING";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testExtractRipeHashFromAddressString()
	{
		// Test check 0: Test the extraction method with a known address
		String testAddress = "BM-ooL2AQnmArWafE116wqZpxf7bjyvZP8ZD";
		byte[] ripeHash = new AddressProcessor().extractRipeHashFromAddress(testAddress);
		String  ripeHashHex = ByteFormatter.byteArrayToHexString(ripeHash);
		Log.i(TAG, "Length of extracted ripe hash:             " + ripeHash.length);
		Log.i(TAG, "Extracted ripe hash in hex:                " + ripeHashHex);
		Log.i(TAG, "Length of extracted ripe hash in hex:      " + ripeHashHex.length());
		assertEquals(ripeHashHex, "0058cc24084449e2937fba011dc25fa3e21fe716");
		
		// Test check 1: Test the extraction method with a newly generated address
		// Generate a new address to use in the test
		AddressGenerator addGen = new AddressGenerator();
		Address address = addGen.generateAndSaveNewAddress();
		
		// Compare the ripe has created during the address generation to the one recreated from the address string
		byte[] expectedRipeHash = address.getRipeHash();
		byte[] extractedRipeHash = new AddressProcessor().extractRipeHashFromAddress(address.getAddress());
		
		Log.i(TAG, "Length of expected ripe hash:  " + expectedRipeHash.length);
		Log.i(TAG, "Length of extracted ripe hash:  " + extractedRipeHash.length);
		
		Log.i(TAG, "Expected ripe hash in hex:  " + ByteFormatter.byteArrayToHexString(expectedRipeHash));
		Log.i(TAG, "Extracted ripe hash in hex: " + ByteFormatter.byteArrayToHexString(extractedRipeHash));
		
		Log.i(TAG, "Length of expected ripe hash in hex:  " + ByteFormatter.byteArrayToHexString(expectedRipeHash).length());
		Log.i(TAG, "Length of extracted ripe hash in hex:  " + ByteFormatter.byteArrayToHexString(extractedRipeHash).length());
		
		assertTrue(Arrays.equals(expectedRipeHash, extractedRipeHash));
		
		// Cleaning up - delete the address we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(address);
	}
}
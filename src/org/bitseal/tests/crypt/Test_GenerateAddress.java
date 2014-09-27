package org.bitseal.tests.crypt;

import java.util.Arrays;

import junit.framework.TestCase;

import org.bitseal.core.AddressProcessor;
import org.bitseal.core.App;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.SHA512;
import org.bitseal.data.Address;
import org.bitseal.database.AddressProvider;
import org.bitseal.util.ArrayCopier;
import org.bitseal.util.Base58;
import org.bitseal.util.ByteFormatter;

import android.util.Log;

/** 
 * Tests the generation of new Bitmessage addresses. 
**/
public class Test_GenerateAddress extends TestCase
{
	private static final String VERSION_3_ADDRESS_PREFIX = "6L"; // Address prefix for address version 3, stream number 1
	private static final String VERSION_4_ADDRESS_PREFIX = "87" ;// Address prefix for address version 4, stream number 1
	
	private static final String TAG = "TEST_GENERATE_ADDRESSES";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testAddressGeneration()
	{
		AddressGenerator addGen = new AddressGenerator();
		Address address = addGen.generateAndSaveNewAddress();
		
		Log.i(TAG, "Generated Address:             " + address.getAddress());
		
		String addressString = address.getAddress();
		String addressPrefix = addressString.substring(0, 3);
		String addressData = addressString.substring(3, addressString.length());
		
		Log.i(TAG, "Address String:   " + addressString);
		Log.i(TAG, "Address Prefix:   " + addressPrefix);
		Log.i(TAG, "Address Data:   " + addressData);
		
		// Test check 1: Check that the first 3 characters of the generated address are the normal "BM-" prefix
		assertEquals(addressPrefix, "BM-");
		
		byte[] addressDataBytes = null;
		
		addressDataBytes = Base58.decode(addressData);
		
		Log.i(TAG, "Address Data Bytes:          " + ByteFormatter.byteArrayToHexString(addressDataBytes));
		
		byte[] combinedAddressData = ArrayCopier.copyOfRange(addressDataBytes, 0, (addressDataBytes.length - 4));
		Log.i(TAG, "Combined Address Data:          " + ByteFormatter.byteArrayToHexString(combinedAddressData));
		
		byte[] checksum = ArrayCopier.copyOfRange(addressDataBytes, (addressDataBytes.length - 4), addressDataBytes.length);
		Log.i(TAG, "Checksum:          " + ByteFormatter.byteArrayToHexString(checksum));
		
		byte[] testChecksumFullHash = SHA512.doubleDigest(combinedAddressData);
		Log.i(TAG, "Test Checksum Full Hash:          " + ByteFormatter.byteArrayToHexString(testChecksumFullHash));
		
		byte[] testChecksum = ArrayCopier.copyOfRange(testChecksumFullHash, 0, 4);
		Log.i(TAG, "Test Checksum:          " + ByteFormatter.byteArrayToHexString(testChecksum));
		
		// Test check 2: After re-calculating the checksum from the address data, check that it matches the checksum found in the generated address
		assertTrue(Arrays.equals(checksum, testChecksum));
		
		// Test check 3: Checks the part of the address that corresponds to the address version and stream number. 
		// Note that this test will fail if the stream number is not 1. 
		AddressProcessor addProc = new AddressProcessor();
		int[] decodedAddressData = addProc.decodeAddressNumbers(addressString);
		int addressVersion = decodedAddressData[0];
		int streamNumber = decodedAddressData[1];
		if (addressVersion == 3 && streamNumber == 1)
		{
			assertEquals(addressString.substring(3, 5), VERSION_3_ADDRESS_PREFIX);
		}
		else if (addressVersion == 4 && streamNumber == 1)
		{
			assertEquals(addressString.substring(3, 5), VERSION_4_ADDRESS_PREFIX);
		}
		
		// Cleaning up - delete the address we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(address);
	}
}
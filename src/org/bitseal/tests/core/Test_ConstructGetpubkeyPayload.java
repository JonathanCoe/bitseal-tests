package org.bitseal.tests.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.TestCase;

import org.bitseal.core.AddressProcessor;
import org.bitseal.core.App;
import org.bitseal.core.OutgoingGetpubkeyProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.data.Address;
import org.bitseal.data.Payload;
import org.bitseal.database.AddressProvider;
import org.bitseal.pow.POWProcessor;
import org.bitseal.util.ArrayCopier;
import org.bitseal.util.ByteFormatter;
import org.bitseal.util.ByteUtils;
import org.bitseal.util.VarintEncoder;

import android.util.Log;

/** 
 * Tests the method GetpubkeyProcessor.constructGetpubkeyPayload()<br><br>
 * 
 * Note: This test uses reflection to access one or more private methods
 * 
 * @author Jonathan Coe
**/
public class Test_ConstructGetpubkeyPayload extends TestCase
{
	/** See https://bitmessage.org/wiki/Proof_of_work for an explanation of these values **/
	private static final long DEFAULT_NONCE_TRIALS_PER_BYTE = 320;
	private static final long DEFAULT_EXTRA_BYTES = 14000;
	
	private static final String TAG = "TEST_CONSTRUCT_GETPUBKEY_PAYLOAD";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testConstructGetpubkeyPayload() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		// Generate a new address
		AddressGenerator addGen = new AddressGenerator();
		Address testAddress = addGen.generateAndSaveNewAddress();
		
		// Construct a getpubkey payload for the new address, using reflection to access the private method
		OutgoingGetpubkeyProcessor getProc = new OutgoingGetpubkeyProcessor();
		Method method1 = OutgoingGetpubkeyProcessor.class.getDeclaredMethod("constructGetpubkeyPayload", String.class);
		method1.setAccessible(true);
		Payload getpubkeyPayload = (Payload) method1.invoke(getProc, testAddress.getAddress());
		byte[] payloadBytes = getpubkeyPayload.getPayload();
		
		Log.i(TAG, "getpubkey payload bytes in hex:    " + ByteFormatter.byteArrayToHexString(payloadBytes));
		
		// Extract the data from the generated getpubkey payload
		int readPosition = 0;
		long powNonce = ByteUtils.bytesToLong(ArrayCopier.copyOfRange(payloadBytes, readPosition, readPosition + 8));
		readPosition += 8; //The pow nonce should always be 8 bytes in length
		Log.i(TAG, "getpubkey payload pow nonce:       " + powNonce);
		
		long time = ByteUtils.bytesToInt((ArrayCopier.copyOfRange(payloadBytes, readPosition, readPosition + 4)));
		if (time == 0) // Check whether 4 or 8 byte time has been used
		{
			time = ByteUtils.bytesToLong((ArrayCopier.copyOfRange(payloadBytes, readPosition, readPosition + 8)));
			readPosition += 8;
		}
		else
		{
			readPosition += 4;
		}
		Log.i(TAG, "getpubkey payload time:            " + time);
		
		long[] decoded = VarintEncoder.decode(ArrayCopier.copyOfRange(payloadBytes, readPosition, readPosition + 9)); // Take 9 bytes, the maximum length for an encoded var_int
		int addressVersion = (int) decoded[0]; // Get the var_int encoded value
		readPosition += (int) decoded[1]; // Find out how many bytes the var_int was in length and adjust the read position accordingly
		Log.i(TAG, "getpubkey payload address version: " + addressVersion);
		
		decoded = VarintEncoder.decode(ArrayCopier.copyOfRange(payloadBytes, readPosition, readPosition + 9)); // Take 9 bytes, the maximum length for an encoded var_int
		int streamNumber = (int) decoded[0]; // Get the var_int encoded value
		readPosition += (int) decoded[1]; // Find out how many bytes the var_int was in length and adjust the read position accordingly
		Log.i(TAG, "getpubkey payload stream number:   " + streamNumber);
		
		byte[] identifier = ArrayCopier.copyOfRange(payloadBytes, readPosition, payloadBytes.length); // Either the ripe hash or the 'tag'
		Log.i(TAG, "getpubkey payload identifier:      " + ByteFormatter.byteArrayToHexString(identifier));
		
		// Now check that the values extracted from the getpubkey payload are valid
		POWProcessor powProc = new POWProcessor();
		boolean powNonceValid = powProc.checkPOW(ArrayCopier.copyOfRange(payloadBytes, 8, payloadBytes.length), powNonce, DEFAULT_NONCE_TRIALS_PER_BYTE, DEFAULT_EXTRA_BYTES);
		Log.i(TAG, "getpubkey payload pow nonce valid: " + powNonceValid);
		assertTrue(powNonceValid);
		
		long currentTime = System.currentTimeMillis() / 1000; // Gets the current time in seconds
    	long maxTime = currentTime + 300; // 300 seconds equals 5 minutes
    	long minTime = currentTime - 300;
		assertTrue(time < maxTime);
		assertTrue(time > minTime);
		
		AddressProcessor addProc = new AddressProcessor();
		int[] expectedAddressNumbers = addProc.decodeAddressNumbers(testAddress.getAddress());
		int expectedAddressVersion = expectedAddressNumbers[0];
		int expectedStreamNumber = expectedAddressNumbers[1];
		assertEquals(addressVersion, expectedAddressVersion);
		assertEquals(streamNumber, expectedStreamNumber);
		
		if (addressVersion <= 3)
		{
			byte[] expectedIdentifier = testAddress.getRipeHash();
			assertTrue(Arrays.equals(identifier, expectedIdentifier));
		}
		else
		{
			byte[] expectedIdentifier = testAddress.getTag();
			assertTrue(Arrays.equals(identifier, expectedIdentifier));
		}
		
		// Cleaning up - delete the address  we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(testAddress);
	}
}
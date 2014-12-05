package org.bitseal.tests.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import junit.framework.TestCase;

import org.bitseal.core.AddressProcessor;
import org.bitseal.core.App;
import org.bitseal.core.ObjectProcessor;
import org.bitseal.core.OutgoingGetpubkeyProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.data.Address;
import org.bitseal.data.BMObject;
import org.bitseal.data.Payload;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.DatabaseContentProvider;
import org.bitseal.util.ByteFormatter;

import android.os.SystemClock;
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
	private static final long TEST_GETPUBKEY_TIME_TO_LIVE = 600;
	
	private static final String TAG = "TEST_CONSTRUCT_GETPUBKEY_PAYLOAD";
	
	protected void setUp() throws Exception
	{
		super.setUp();
		
		// Open the database
		DatabaseContentProvider.openDatabase();
		SystemClock.sleep(5000); // We have to allow some extra time for the database to be opened
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
		Method method1 = OutgoingGetpubkeyProcessor.class.getDeclaredMethod("constructGetpubkeyPayload", String.class, long.class);
		method1.setAccessible(true);
		Payload getpubkeyPayload = (Payload) method1.invoke(getProc, testAddress.getAddress(), TEST_GETPUBKEY_TIME_TO_LIVE);
		byte[] payloadBytes = getpubkeyPayload.getPayload();
		
		Log.i(TAG, "getpubkey payload bytes in hex:    " + ByteFormatter.byteArrayToHexString(payloadBytes));
		
		// Parse the standard Bitmessage object data
		BMObject getpubkeyObject = new ObjectProcessor().parseObject(payloadBytes);
		
		byte[] identifier = getpubkeyObject.getPayload(); // Either the ripe hash or the 'tag'
		Log.i(TAG, "getpubkey payload identifier:      " + ByteFormatter.byteArrayToHexString(identifier));
		
		// Now check that the values extracted from the getpubkey payload are valid
		AddressProcessor addProc = new AddressProcessor();
		int[] expectedAddressNumbers = addProc.decodeAddressNumbers(testAddress.getAddress());
		int expectedAddressVersion = expectedAddressNumbers[0];
		int expectedStreamNumber = expectedAddressNumbers[1];
		assertEquals(expectedAddressVersion, getpubkeyObject.getObjectVersion());
		assertEquals(expectedStreamNumber, getpubkeyObject.getStreamNumber());
		
		if (getpubkeyObject.getObjectVersion() <= 3)
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
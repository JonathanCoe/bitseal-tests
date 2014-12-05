package org.bitseal.tests.crypt;

import junit.framework.TestCase;

import org.bitseal.core.App;
import org.bitseal.core.BehaviourBitfieldProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.KeyConverter;
import org.bitseal.crypt.PubkeyGenerator;
import org.bitseal.crypt.SigProcessor;
import org.bitseal.data.Address;
import org.bitseal.data.Pubkey;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.DatabaseContentProvider;
import org.bitseal.database.PubkeyProvider;
import org.bitseal.util.ByteFormatter;

import android.os.SystemClock;
import android.util.Log;

/** 
 * Tests the generation of new pubkeys.
**/
public class Test_GeneratePubkey extends TestCase
{
	private static final String TAG = "TEST_GENERATE_PUBKEYS";
	
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
	
	public void testPubkeyGeneration()
	{
		// First generate a new address to base the pubkey on
		AddressGenerator addGen = new AddressGenerator();
		Address address = addGen.generateAndSaveNewAddress();
		
		// Now generate a new pubkey based on that address
		PubkeyGenerator pubkeyGen = new PubkeyGenerator();
		Pubkey pubkey = pubkeyGen.generateAndSaveNewPubkey(address);
		
		// Check the pubkey's "correspondingAddressID" matches the ID of the address that it was based on
		Log.i(TAG, "Generated Pubkey 'correspondingAddressID':  " + pubkey.getCorrespondingAddressId());
		assertEquals(pubkey.getCorrespondingAddressId(), address.getId());
		
		// Check the pubkey's "belongsToMe" value is set to true
		Log.i(TAG, "Generated Pubkey 'belongsToMe':             " + pubkey.belongsToMe());
		assertEquals(pubkey.belongsToMe(), true);
		
		// Check the pubkey's "time" value is within the range of the current time plus or minus 8 minutes (extra time to allow for POW to be calculated)
		long currentTime = System.currentTimeMillis() / 1000; // Gets the current time in seconds
    	long maxTime = currentTime + 2419500; // 28 days and five minutes in the future
    	long minTime = currentTime + 300; // Five minutes in the future	
    	Log.i(TAG, "Pubey max time: " + maxTime);
    	Log.i(TAG, "Pubey min time: " + minTime);
		Log.i(TAG, "Generated Pubkey 'expiration time':         " + pubkey.getExpirationTime());
		assertTrue(pubkey.getExpirationTime() < maxTime);
		assertTrue(pubkey.getExpirationTime() > minTime);
		
		// Check the pubkey's "signatureLength" value is equal to the value of its signature		
		Log.i(TAG, "Generated Pubkey 'signatureLength':         " + pubkey.getSignatureLength());
		assertEquals(pubkey.getSignatureLength(), pubkey.getSignature().length);
		
		// Use the pubkey's "addressVersion", "streamNumber", "publicSigningKey", and "publicEncryptionKey" values to recalculate the address String
		// of the address that it was based on and check if the two match
		String recreatedAddressString = addGen.recreateAddressString(pubkey.getObjectVersion(), pubkey.getStreamNumber(), pubkey.getPublicSigningKey(), pubkey.getPublicEncryptionKey());
		Log.i(TAG, "Original Address String:                    " + address.getAddress());
		Log.i(TAG, "Recreated Address String:                   " + recreatedAddressString);
		assertEquals(address.getAddress(), recreatedAddressString);
		
		// Check that the pubkey's "behaviourBitfield" value matches the one provided by my BehaviourBitfieldHandler class	
		Log.i(TAG, "Generated Pubkey 'behaviourBitfield':       " + pubkey.getBehaviourBitfield());
		assertEquals(pubkey.getBehaviourBitfield(), BehaviourBitfieldProcessor.getBitfieldForMyPubkeys());
		
		// Test that the pubkey's signature is valid
		Log.i(TAG, "Generated Pubkey 'signature':               " + ByteFormatter.byteArrayToHexString(pubkey.getSignature()));
		SigProcessor sigProc = new SigProcessor();
		byte[] signaturePayload = sigProc.createPubkeySignaturePayload(pubkey);
		boolean sigValid = (sigProc.verifySignature(signaturePayload, pubkey.getSignature(), new KeyConverter().reconstructPublicKey(pubkey.getPublicSigningKey())));
		assertTrue(sigValid);
		
		// Cleaning up - delete the address and pubkey we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(address);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(pubkey);
	}
}
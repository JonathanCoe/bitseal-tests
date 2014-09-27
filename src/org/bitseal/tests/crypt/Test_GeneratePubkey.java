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
import org.bitseal.database.PubkeyProvider;
import org.bitseal.util.ByteFormatter;

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
		
		// Test check 1: Check the pubkey's "correspondingAddressID" matches the ID of the address that it was based on
		Log.i(TAG, "Generated Pubkey 'correspondingAddressID':  " + pubkey.getCorrespondingAddressId());
		assertEquals(pubkey.getCorrespondingAddressId(), address.getId());
		
		// Test check 2: Check the pubkey's "belongsToMe" value is set to true
		Log.i(TAG, "Generated Pubkey 'belongsToMe':             " + pubkey.belongsToMe());
		assertEquals(pubkey.belongsToMe(), true);
		
		// Test check 3: Check the pubkey's "time" value is within the range of the current time plus or minus 8 minutes (extra time to allow for POW to be calculated)
		long time = System.currentTimeMillis() / 1000; // Gets the current time in seconds
    	long maxTime = time + 480; // 480 seconds equals 8 minutes
    	long minTime = time - 480;	
		Log.i(TAG, "Generated Pubkey 'time':                    " + pubkey.getTime());
		assertTrue(pubkey.getTime() < maxTime && pubkey.getTime() > minTime);
		
		// Test check 4: Check the pubkey's "signatureLength" value is equal to the value of its signature		
		Log.i(TAG, "Generated Pubkey 'signatureLength':         " + pubkey.getSignatureLength());
		assertEquals(pubkey.getSignatureLength(), pubkey.getSignature().length);
		
		// Test check 5: Use the pubkey's "addressVersion", "streamNumber", "publicSigningKey", and "publicEncryptionKey" values to recalculate the address String
		// of the address that it was based on and check if the two match
		String recreatedAddressString = addGen.recreateAddressString(pubkey.getAddressVersion(), pubkey.getStreamNumber(), pubkey.getPublicSigningKey(), pubkey.getPublicEncryptionKey());
		Log.i(TAG, "Original Address String:                    " + address.getAddress());
		Log.i(TAG, "Recreated Address String:                   " + recreatedAddressString);
		assertEquals(address.getAddress(), recreatedAddressString);
		
		// Test check 6: Test that the pubkey's "behaviourBitfield" value matches the one provided by my BehaviourBitfieldHandler class	
		Log.i(TAG, "Generated Pubkey 'behaviourBitfield':       " + pubkey.getBehaviourBitfield());
		assertEquals(pubkey.getBehaviourBitfield(), BehaviourBitfieldProcessor.getBitfieldForMyPubkeys());
		
		// Test check 7: Test that the pubkey's signature is valid
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
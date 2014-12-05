package org.bitseal.tests.core;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.bitseal.core.App;
import org.bitseal.core.PubkeyProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.PubkeyGenerator;
import org.bitseal.data.Address;
import org.bitseal.data.Payload;
import org.bitseal.data.Pubkey;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.DatabaseContentProvider;
import org.bitseal.database.PubkeyProvider;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;

/** 
 * Tests the method PubkeyProcessor.constructPubkeyPayload()<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
 * 
 * @author Jonathan Coe
**/

public class Test_ConstructPubkeyPayload extends AndroidTestCase
{
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
	
	// Note: This override of the setContext() method is necessary because AndroidTestCase
	// will in some cases return a null application context. 
	// See https://stackoverflow.com/questions/6516441/why-does-androidtestcase-getcontext-getapplicationcontext-return-null
	// Credit to James Wald on StackOverflow for this method
	@Override
    public void setContext(Context context) 
	{
        super.setContext(context);

        long endTime = SystemClock.elapsedRealtime() + TimeUnit.SECONDS.toMillis(2);

        while (null == context.getApplicationContext())
        {

            if (SystemClock.elapsedRealtime() >= endTime)
            {
                fail();
            }

            SystemClock.sleep(16);
        }
	 }
	
	public void testConstructPubkeyPayload()
	{
		// Generate a new address which the pubkey will be based on
		AddressGenerator addGen = new AddressGenerator();
		Address address = addGen.generateAndSaveNewAddress();
		
		// Generate a new pubkey for the address we just created
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey pubkey = pubGen.generateAndSaveNewPubkey(address);
		
		// Construct the payload for this pubkey. This is the data 
		// that would be sent over the network.
		PubkeyProcessor pubProc = new PubkeyProcessor();
		Payload payload = pubProc.constructPubkeyPayload(pubkey, true);
		byte[] payloadBytes = payload.getPayload();
		
		// Parse the standard Bitmessage object data
		Pubkey reconstructedPubkey = new PubkeyProcessor().reconstructPubkey(payloadBytes, address.getAddress());
		
		// Now check that the reconstructed pubkey is valid for the address we started with
		assertTrue(pubProc.validatePubkey(reconstructedPubkey, address.getAddress()));
		
		// Now compare the reconstructed pubkey to the original
		assertEquals(pubkey.getStreamNumber(), reconstructedPubkey.getStreamNumber());
		assertEquals(pubkey.getBehaviourBitfield(), reconstructedPubkey.getBehaviourBitfield());
		assertEquals(pubkey.getNonceTrialsPerByte(), reconstructedPubkey.getNonceTrialsPerByte());
		assertEquals(pubkey.getExtraBytes(), reconstructedPubkey.getExtraBytes());
		assertEquals(pubkey.getSignatureLength(), reconstructedPubkey.getSignatureLength());
		assertTrue(Arrays.equals(pubkey.getRipeHash(), reconstructedPubkey.getRipeHash()));
		assertTrue(Arrays.equals(pubkey.getPublicSigningKey(), reconstructedPubkey.getPublicSigningKey()));
		assertTrue(Arrays.equals(pubkey.getPublicEncryptionKey(), reconstructedPubkey.getPublicEncryptionKey()));
		assertTrue(Arrays.equals(pubkey.getSignature(), reconstructedPubkey.getSignature()));
		
		// Cleaning up - delete the address and pubkey we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(address);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(pubkey);
	}
}
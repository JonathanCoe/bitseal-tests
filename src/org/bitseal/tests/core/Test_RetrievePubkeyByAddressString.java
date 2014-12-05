package org.bitseal.tests.core;

import java.util.concurrent.TimeUnit;

import org.bitseal.core.App;
import org.bitseal.core.PubkeyProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.PubkeyGenerator;
import org.bitseal.data.Address;
import org.bitseal.data.Pubkey;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.DatabaseContentProvider;
import org.bitseal.database.PubkeyProvider;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;

/** 
 * Tests the method PubkeyProcessor.retrievePubkeyByAddressString()<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
 * 
 * @author Jonathan Coe
**/
public class Test_RetrievePubkeyByAddressString extends AndroidTestCase
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
	
	public void testRetrievePubkeyByAddressString()
	{
		// Test 0: Generate a new address and its corresponding pubkey, then use the ripe hash to search for that pubkey
		AddressGenerator addGen = new AddressGenerator();
		Address address0 = addGen.generateAndSaveNewAddress();
		
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey pubkey0 = pubGen.generateAndSaveNewPubkey(address0);
		
		PubkeyProcessor pubProc = new PubkeyProcessor();
		Pubkey retrievedPubkey = pubProc.retrievePubkeyByAddressString(address0.getAddress());
		
		// Now check that the pubkey is valid 
		assertTrue(pubProc.validatePubkey(retrievedPubkey, address0.getAddress()));
		
		
		
		// Test 1: Generate a new address and then generate several duplicate pubkeys for it, then search for that pubkey
		// to see how the duplicates are handled
		Address address1 = addGen.generateAndSaveNewAddress();
		Pubkey pubkey1 = pubGen.generateAndSaveNewPubkey(address1);
		Pubkey pubkey2 = pubGen.generateAndSaveNewPubkey(address1);
		Pubkey pubkey3 = pubGen.generateAndSaveNewPubkey(address1);
		
		retrievedPubkey = pubProc.retrievePubkeyByAddressString(address1.getAddress());
		// Now check that the pubkey is valid 
		assertTrue(pubProc.validatePubkey(retrievedPubkey, address1.getAddress()));
		
		
		
		
		// Cleaning up - delete the addresses and pubkeys we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(address0);
		addProv.deleteAddress(address1);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(pubkey0);
		pubProv.deletePubkey(pubkey1);
		pubProv.deletePubkey(pubkey2);
		pubProv.deletePubkey(pubkey3);
	}
}
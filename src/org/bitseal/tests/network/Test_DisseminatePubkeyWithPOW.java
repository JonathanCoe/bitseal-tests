package org.bitseal.tests.network;

import java.util.concurrent.TimeUnit;

import org.bitseal.core.App;
import org.bitseal.core.PubkeyProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.PubkeyGenerator;
import org.bitseal.data.Address;
import org.bitseal.data.Payload;
import org.bitseal.data.Pubkey;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.PubkeyProvider;
import org.bitseal.network.ServerCommunicator;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

/** 
 * Tests the disseminatePubkeyWithPOW() method in the ServerCommunicator class.<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
**/
public class Test_DisseminatePubkeyWithPOW extends AndroidTestCase
{
	private static final String TAG = "TEST_DISSEMINATE_PUBKEY_WITH_POW";
	
	protected void setUp() throws Exception
	{
		super.setUp();
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

        long endTime = SystemClock.elapsedRealtime() + TimeUnit.SECONDS.toMillis(50);

        while (null == context.getApplicationContext())
        {
            if (SystemClock.elapsedRealtime() >= endTime)
            {
                Log.e(TAG, "Attempt to get application context timed out");
            	fail();
            }
            SystemClock.sleep(50);
        }
	}
	
	public void testDisseminatePubkeyWithPOW()
	{
		// Wait for five seconds in order to make it more likely that we will be able to get application context
		SystemClock.sleep(5000);
				
		// Generate a new Address object
		AddressGenerator addGen = new AddressGenerator();
		Address address = addGen.generateAndSaveNewAddress();
		
		// Generate a Pubkey for the new address. This will be the pubkey that we disseminate
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey pubkey = pubGen.generateAndSaveNewPubkey(address);
		
		// Process the generated pubkey, giving a payload in byte[] form that is ready to be sent over the network
		PubkeyProcessor pubProc = new PubkeyProcessor();
		Payload pubkeyPayload = pubProc.constructPubkeyPayload(pubkey, true);
				
		// Disseminate the pubkey payload to the rest of the network via a PyBitmessage server
		ServerCommunicator servCom = new ServerCommunicator();
		boolean disseminationSuccessful = servCom.disseminatePubkey(pubkeyPayload.getPayload());
		
		if (disseminationSuccessful == true)
		{
			Log.i(TAG, "The pubkey was successfully sent to a server!");
		}
		else
		{
			Log.e(TAG, "The attempt to disseminate the pubkey failed!");
		}
		assertTrue(disseminationSuccessful);
		
		// Cleaning up - delete the address and pubkey we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(address);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(pubkey);
	}
}
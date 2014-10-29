package org.bitseal.tests.network;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

import org.bitseal.core.App;
import org.bitseal.core.OutgoingGetpubkeyProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.data.Address;
import org.bitseal.data.Payload;
import org.bitseal.database.AddressProvider;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

/** 
 * Tests the disseminateGetpubkeyWithPOW() method in the ServerCommunicator class.<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
 * 
 * @author Jonathan Coe
**/
public class Test_DisseminateGetpubkey extends AndroidTestCase
{
	private static final long GETPUBKEY_TIME_TO_LIVE = 3600; // In seconds, so currently equal to 1 hour
	
	private static final String TAG = "TEST_DISSEMINATE_GETPUBKEY";
	
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
	
	public void testDisseminateGetpubkey() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		// Wait for five seconds in order to make it more likely that we will be able to get application context
		SystemClock.sleep(5000);
				
		// Generate a new Address object
		AddressGenerator addGen = new AddressGenerator();
		Address testAddress = addGen.generateAndSaveNewAddress();
		
		// Disseminate the pubkey payload to the rest of the network via a Bitseal
		OutgoingGetpubkeyProcessor outProc = new OutgoingGetpubkeyProcessor();
		Payload getpubkeyPayload = outProc.constructAndDisseminateGetpubkeyRequst(testAddress.getAddress(), GETPUBKEY_TIME_TO_LIVE);
		
		// If the getpubkey payload is disseminated successfully, its time value will be set to the current time. Otherwise it will be zero. 
		if (getpubkeyPayload.getTime() > 0)
		{
			Log.i(TAG, "The getpubkey was successfully sent to a server!");
		}
		else
		{
			Log.e(TAG, "The attempt to disseminate the getpubkey failed!");
		}
		assertTrue(getpubkeyPayload.getTime() > 0);
		
		// Cleaning up - delete the address we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(testAddress);
	}
}
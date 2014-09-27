package org.bitseal.tests.network;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.bitseal.core.App;
import org.bitseal.core.PubkeyProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.data.Address;
import org.bitseal.data.Payload;
import org.bitseal.database.AddressProvider;
import org.bitseal.network.ServerCommunicator;

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
 * Note: This test uses reflection to access one or more private methods
**/
public class Test_DisseminateGetpubkeyWithPOW extends AndroidTestCase
{
	private static final String TAG = "TEST_DISSEMINATE_GETPUBKEY_WITH_POW";
	
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
	
	public void testDisseminateGetpubkeyWithPOW() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		// Wait for five seconds in order to make it more likely that we will be able to get application context
		SystemClock.sleep(5000);
				
		// Generate a new Address object
		AddressGenerator addGen = new AddressGenerator();
		Address testAddress = addGen.generateAndSaveNewAddress();
		
		// Construct a getpubkey payload for the new address, using reflection to access the private method
		PubkeyProcessor pubProc = new PubkeyProcessor();
		Method method1 = PubkeyProcessor.class.getDeclaredMethod("constructGetpubkeyPayload", String.class);
		method1.setAccessible(true);
		Payload getpubkeyPayload = (Payload) method1.invoke(pubProc, testAddress.getAddress());
		byte[] payloadBytes = getpubkeyPayload.getPayload();
				
		// Disseminate the pubkey payload to the rest of the network via a PyBitmessage server
		ServerCommunicator servCom = new ServerCommunicator();
		boolean disseminationSuccessful = servCom.disseminateGetpubkey(payloadBytes);
		
		if (disseminationSuccessful == true)
		{
			Log.i(TAG, "The getpubkey was successfully sent to a server!");
		}
		else
		{
			Log.e(TAG, "The attempt to disseminate the getpubkey failed!");
		}
		assertTrue(disseminationSuccessful);
		
		// Cleaning up - delete the address we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(testAddress);
	}
}
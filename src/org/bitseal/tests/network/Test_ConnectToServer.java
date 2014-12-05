package org.bitseal.tests.network;

import java.util.concurrent.TimeUnit;

import org.bitseal.core.App;
import org.bitseal.database.DatabaseContentProvider;
import org.bitseal.database.ServerRecordProvider;
import org.bitseal.network.ApiCaller;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

/** 
 * This class tests whether the client is able to connect to any of the PyBitmessage
 * servers that it has records for.<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
 * 
 * @author Jonathan Coe
**/
public class Test_ConnectToServer extends AndroidTestCase
{
	private static final String TAG = "TEST_CONNECT_TO_SERVER";
	
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
	
	public void testConnectToServer()
	{
		// Wait for five seconds in order to make it more likely that we will be able to get application context
		SystemClock.sleep(5000);
		
		// Clear out any existing server records so that the ApiCaller.setupDefaultServers() method is tested
		ServerRecordProvider servProv = ServerRecordProvider.get(App.getContext());
		servProv.deleteAllServerRecords();
		
		ApiCaller caller = new ApiCaller();
		int result = (Integer) caller.call("add", 459, 821);
		int expectedResult = 459 + 821;
		
		Log.i(TAG, "Expected test result: " + expectedResult + ", Actual test result: " + result);
		assertEquals(result, expectedResult);
	}
}
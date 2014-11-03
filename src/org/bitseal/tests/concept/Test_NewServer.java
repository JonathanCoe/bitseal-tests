package org.bitseal.tests.concept;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;
import de.timroes.axmlrpc.XMLRPCClient;
import de.timroes.axmlrpc.XMLRPCException;

/** 
 * Used to test new XMLRPC servers.<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
 * 
 * @author Jonathan Coe
**/
public class Test_NewServer extends AndroidTestCase
{
	private static final String URL = "http://bitmessage.mobi:8081/RPC2";
	private static final String USERNAME = "";
	private static final String PASSWORD = "";
	
	private static final String TEST_METHOD_ADD = "sample.add";
	private static final int FIRST_TEST_INT = 45;
	private static final int SECOND_TEST_INT = 45;
	
	private static final String TEST_METHOD_LIST_METHODS = "system.listMethods";
	
	private static final int TIMEOUT_SECONDS = 20;
	
	private static final String TAG = "TEST_NEW_SERVER";
	
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
	
	public void testNewServer()
	{
		// Wait for five seconds in order to make it more likely that we will be able to get application context
		SystemClock.sleep(5000);
		
		try
		{
			// Set up a new XMLRPC client
			XMLRPCClient client = new XMLRPCClient(new URL(URL));
			client.setLoginData(USERNAME, PASSWORD);
			client.setTimeout(TIMEOUT_SECONDS);
			
			//--------------------------------Test addition of integers------------------------------------
			
			// Make a test call
			Log.i(TAG, "About to make an API call to " + URL);
			Object result = client.call(TEST_METHOD_ADD, FIRST_TEST_INT, SECOND_TEST_INT);
			String resultString = result.toString();
			int resultNumber = Integer.valueOf(resultString);
			
			// Examine the result
			Log.i(TAG, "Result String: " + resultString);
			int expectedResult = FIRST_TEST_INT + SECOND_TEST_INT;
			assertEquals(expectedResult, resultNumber);
			//--------------------------------------------------------------------------------------------
			
			
			//--------------------------------Test 'list methods' function--------------------------------
			Log.i(TAG, "About to make an API call to " + URL);
			Object[] resultArray = (Object[]) client.call(TEST_METHOD_LIST_METHODS);
			
			// Check if the response isArray
			Log.i(TAG, String.valueOf(result.getClass().isArray()));
			 
			//(2) If isArray, then iterate over it
			for(Object o : resultArray)
			{
				//(3) Get the type of object in the loop
				Log.i(TAG, "Result: " + o.toString());
			}
			//--------------------------------------------------------------------------------------------
		}
		catch (MalformedURLException e)
		{
			Log.e(TAG, "MalformedURLException occurred in Test_NewServer.testNewServer(). The exception message was: \n"
					+ e.getMessage());
		}
		catch (XMLRPCException e)
		{
			Log.e(TAG, "XMLRPCException occurred in Test_NewServer.testNewServer(). The exception message was: \n"
					+ e.getMessage());
		}	
	}
}
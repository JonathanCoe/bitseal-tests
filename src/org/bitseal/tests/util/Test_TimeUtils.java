package org.bitseal.tests.util;

import junit.framework.TestCase;

import org.bitseal.util.TimeUtils;

import android.util.Log;

/**
 * Tests the TimeUtils class.
 * 
 * @author Jonathan Coe
 */
public class Test_TimeUtils extends TestCase
{
	private static final String TAG = "TEST_TIME_UTILS";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testTimeUtils()
	{	
		long inputTime0 = 107329;
		String expectedResult0 = "1 day and 5 hours";
		Log.i(TAG, "Testing " + inputTime0);
		String result0 = TimeUtils.getTimeMessage(inputTime0);
		Log.i(TAG, inputTime0 + " returns " + result0);
		assertTrue(result0.equals(expectedResult0));
		
		long inputTime1 = 77329;
		String expectedResult1 = "21 hours and 28 minutes";
		Log.i(TAG, "Testing " + inputTime1);
		String result1 = TimeUtils.getTimeMessage(inputTime1);
		Log.i(TAG, inputTime1 + " returns " + result1);
		assertTrue(result1.equals(expectedResult1));
		
		long inputTime2 = 6000;
		String expectedResult2 = "1 hour and 40 minutes";
		Log.i(TAG, "Testing " + inputTime2);
		String result2 = TimeUtils.getTimeMessage(inputTime2);
		Log.i(TAG, inputTime2 + " returns " + result2);
		assertTrue(result2.equals(expectedResult2));
		
		long inputTime3 = 1370;
		String expectedResult3 = "22 minutes and 50 seconds";
		Log.i(TAG, "Testing " + inputTime2);
		String result3 = TimeUtils.getTimeMessage(inputTime3);
		Log.i(TAG, inputTime3 + " returns " + result3);
		assertTrue(result3.equals(expectedResult3));
		
		long inputTime4 = 23;
		String expectedResult4 = "23 seconds";
		Log.i(TAG, "Testing " + inputTime4);
		String result4 = TimeUtils.getTimeMessage(inputTime4);
		Log.i(TAG, inputTime4 + " returns " + result4);
		assertTrue(result4.equals(expectedResult4));
		
		long inputTime5 = 1;
		String expectedResult5 = "1 second";
		Log.i(TAG, "Testing " + inputTime5);
		String result5 = TimeUtils.getTimeMessage(inputTime5);
		Log.i(TAG, inputTime5 + " returns " + result5);
		assertTrue(result5.equals(expectedResult5));
		
		long inputTime6 = 0;
		String expectedResult6 = "0 seconds";
		Log.i(TAG, "Testing " + inputTime6);
		String result6 = TimeUtils.getTimeMessage(inputTime6);
		Log.i(TAG, inputTime6 + " returns " + result6);
		assertTrue(result6.equals(expectedResult6));
	}
}
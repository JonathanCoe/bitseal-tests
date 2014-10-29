package org.bitseal.tests.pow;

import java.security.SecureRandom;
import java.util.ArrayList;

import junit.framework.TestCase;

import org.bitseal.pow.POWProcessor;
import org.bitseal.util.ByteFormatter;

import android.util.Log;

/**
 * Runs time trials for Bitmessage proof of work calculations
 * in Bitseal.
 * 
 * @author Jonathan Coe
 */
public class Test_POWTimeTrials extends TestCase
{
	private static final int TRIALS_TO_RUN = 3;
	
	private static final int PAYLOAD_LENGTH = 500;
	private static final long TIME_TO_LIVE = 3600; // In seconds, so currently set to 1 hour
	
	private static final long NONCE_TRIALS_PER_BYTE = 1000;
	private static final long EXTRA_BYTES = 1000;
	
	private static final String TAG = "POW_TIME_TRIALS";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testPOWTimeTrials()
	{
		byte[] payload = new byte[PAYLOAD_LENGTH];
		SecureRandom secRand = new SecureRandom();
		POWProcessor powProc = new POWProcessor();
		ArrayList<Long> times = new ArrayList<Long>();
		
		// Set the expiration time for this payload
		long currentTime = System.currentTimeMillis() / 1000;
		long expirationTime = currentTime + TIME_TO_LIVE;
		
		for (int i = 0; i < TRIALS_TO_RUN; i++)
		{
 			long startTime = 0;
 			long endTime = 0;
 			long timeTaken = 0;
 			
 			secRand.nextBytes(payload);
 			Log.i(TAG, "About to do POW calculations for the following payload: \n" + ByteFormatter.byteArrayToHexString(payload));
			
			//----------------------------------BEGIN TIMED TEST----------------------------------------------
 			startTime = System.nanoTime();
			
			long powNonce = powProc.doPOW(payload, expirationTime, NONCE_TRIALS_PER_BYTE, EXTRA_BYTES);
			
 			endTime = System.nanoTime();
 			//----------------------------------END TIMED TEST----------------------------------------------
 			
 			assertTrue(powProc.checkPOW(payload, powNonce, expirationTime, NONCE_TRIALS_PER_BYTE, EXTRA_BYTES));
 			timeTaken = endTime - startTime;
 			long timeTakenInSeconds = timeTaken / 1000000000; // Convert from nanoseconds to seconds
 			Log.i(TAG, "Time taken in seconds:          " + timeTakenInSeconds);
 			times.add(Long.valueOf(timeTakenInSeconds));
		}
		
 		// Get the average time taken to do the POW
 		long sumOfTimes = 0;
 		for (Long l : times)
 		{
 			sumOfTimes += l;
 		}
 		long averageTime = sumOfTimes / times.size();
 		Log.d(TAG, "Average time taken in seconds:  " + averageTime);
	}
}
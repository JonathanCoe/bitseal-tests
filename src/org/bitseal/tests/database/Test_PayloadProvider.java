package org.bitseal.tests.database;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.bitseal.data.Payload;
import org.bitseal.database.PayloadProvider;
import org.bitseal.database.PayloadsTable;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Base64;
import android.util.Log;

/**
 * Tests the functions of the PayloadProvider class.<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
 * 
 * @author Jonathan Coe
 */
public class Test_PayloadProvider extends AndroidTestCase
{
	private static final String TAG = "TEST_PAYLOAD_PROVIDER";
	
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
	
	public void testPayloadProvider()
	{
		// Now we should be able to get the application context safely
		PayloadProvider provider = PayloadProvider.get(getContext());
		
		//First clear out any existing records to ensure a fair test:
		provider.deleteAllPayloads();
		
		// Test adding records:		
		byte[] payload0 = new String ("adfiuq345S*DNocay8JU95jfsd(*4328eatq87c4qih454").getBytes();
		byte[] payload1 = new String ("sdiuq3ste324GRSocay8GDghdfd(*434efGEq87c4qiFEV4").getBytes();
		
		String payload1String = android.util.Base64.encodeToString(payload1, Base64.DEFAULT);
		
		Payload record0 = new Payload();
		record0.setBelongsToMe(true);
		record0.setProcessingComplete(true);
		record0.setType(Payload.OBJECT_TYPE_MSG);
		record0.setAck(false);
		record0.setPOWDone(true);
		record0.setPayload(payload0);
	
		Payload record1 = new Payload();
		record1.setBelongsToMe(true);
		record1.setProcessingComplete(false);
		record1.setType(Payload.OBJECT_TYPE_PUBKEY);
		record0.setAck(true);
		record1.setPOWDone(false);
		record1.setPayload(payload1);
		
		Payload record2 = new Payload(); // Deliberate duplicate for testing - see below
		record2.setBelongsToMe(true);
		record2.setProcessingComplete(false);
		record2.setType(Payload.OBJECT_TYPE_PUBKEY);
		record0.setAck(true);
		record2.setPOWDone(false);
		record2.setPayload(payload1);
		
		provider.addPayload(record0);
		provider.addPayload(record1);
		provider.addPayload(record2);
		
		// Test searching for records:
		ArrayList<Payload> searchResults0 = provider.searchPayloads(PayloadsTable.COLUMN_PAYLOAD, payload1String);
		Payload result0 = searchResults0.get(0);
		int recordsRetrieved = searchResults0.size();
		
		Log.i(TAG, "Expected number of results from search: 2");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 2);
		
		Log.i(TAG, "Expected result (from first result): " + payload1String);
		Log.i(TAG, "Actual result (from first result):   " + Base64.encodeToString(result0.getPayload(), Base64.DEFAULT));
		assertEquals(Base64.encodeToString(result0.getPayload(), Base64.DEFAULT), payload1String);
		
		// Test getting all records:
		ArrayList<Payload> searchResults1 = provider.getAllPayloads();
		recordsRetrieved = searchResults1.size();
		
		Log.i(TAG, "Expected number of records: 3");
		Log.i(TAG, "Actual number of records:   " + recordsRetrieved);
		assertEquals (recordsRetrieved, 3);
		
		// Test updating a record:
		result0.setBelongsToMe(false); // Let's update result0 - the first of the duplicate entries that we retrieved above
		provider.updatePayload(result0);
		// Now let us retrieve the updated record and examine it to make sure that has been updated correctly
		ArrayList<Payload> searchResults2 = provider.searchPayloads(PayloadsTable.COLUMN_BELONGS_TO_ME, String.valueOf(0));
		Payload result1 = searchResults2.get(0);
		recordsRetrieved = searchResults2.size();
		
		Log.i(TAG, "Expected number of results from search: 1");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 1);
		
		Log.i(TAG, "Expected belongsToMe value: " + false);
		Log.i(TAG, "Actual belongsToMe value  : " + result1.belongsToMe());
		assertEquals(result1.belongsToMe(), false);
		
		// Test searching for a single record by its ID:
		Payload result2 = provider.searchForSingleRecord(result1.getId());
		Log.i(TAG, "Expected belongsToMe value: " + false);
		Log.i(TAG, "Actual belongsToMe value  : " + result2.belongsToMe());
		assertEquals(result2.belongsToMe(), false);
		
		// Test deleting a single record:
		provider.deletePayload(result1); // Let us delete the address that we just updated
		
		// Now let us search for the record to make sure that it has in fact been deleted
		ArrayList<Payload> searchResults3 = provider.searchPayloads(PayloadsTable.COLUMN_BELONGS_TO_ME, String.valueOf(0));
		recordsRetrieved =searchResults3.size();
		
		Log.i(TAG, "Expected number of results from search: 0");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 0);
		
		// Finally, delete the records we have added so that they don't mess up the rest of the application
		provider.deleteAllPayloads();
	}
}
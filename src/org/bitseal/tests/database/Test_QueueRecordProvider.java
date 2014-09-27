package org.bitseal.tests.database;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.bitseal.data.QueueRecord;
import org.bitseal.database.QueueRecordProvider;
import org.bitseal.database.QueueRecordsTable;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Tests the functions of the QueueRecordProvider class.<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
 * 
 * @author Jonathan Coe
 */
public class Test_QueueRecordProvider extends AndroidTestCase
{
	private static final String TAG = "TEST_QUEUE_RECORD_PROVIDER";
	
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
	
	public void testQueueRecordProvider()
	{
		// Now we should be able to get the application context safely
		QueueRecordProvider provider = QueueRecordProvider.get(getContext());
		
		//First clear out any existing records to ensure a fair test:
		provider.deleteAllQueueRecords();
		
		// Test adding records:
		QueueRecord record0 = new QueueRecord();	
		record0.setTask("DisseminatePubkey");
		record0.setLastAttemptTime(System.currentTimeMillis() / 1000);
		record0.setAttempts(5);
		record0.setObject0Id(435);
		record0.setObject0Type("Pubkey");

		QueueRecord record1 = new QueueRecord();
		record1.setTask("SendMsg");
		record1.setLastAttemptTime(System.currentTimeMillis() / 1000);
		record1.setAttempts(1);
		record1.setObject0Id(2821);
		record1.setObject0Type("EncryptedMsg");

		
		QueueRecord record2 = new QueueRecord(); // Deliberate duplicate for testing - see below
		record2.setTask("SendMsg");
		record2.setLastAttemptTime(System.currentTimeMillis() / 1000);
		record2.setAttempts(1);
		record2.setObject0Id(2821);
		record2.setObject0Type("EncryptedMsg");
		record2.setObject1Id(2345);
		record2.setObject1Type("Pubkey");
		
		provider.addQueueRecord(record0);
		provider.addQueueRecord(record1);
		provider.addQueueRecord(record2);
		
		// Test searching for records:
		ArrayList<QueueRecord> searchResults0 = provider.searchQueueRecords(QueueRecordsTable.COLUMN_OBJECT_0_TYPE, "EncryptedMsg");
		QueueRecord result0 = searchResults0.get(0);
		int recordsRetrieved = searchResults0.size();
		
		Log.i(TAG, "Expected number of results from search: 2");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 2);
		
		Log.i(TAG, "Expected result (from first result): EncryptedMsg");
		Log.i(TAG, "Actual result (from first result):   " + result0.getObject0Type());
		assertEquals(result0.getObject0Type(), "EncryptedMsg");
		
		// Test getting all records:
		ArrayList<QueueRecord> searchResults1 = provider.getAllQueueRecords();
		recordsRetrieved = searchResults1.size();
		
		Log.i(TAG, "Expected number of records: 3");
		Log.i(TAG, "Actual number of records:   " + recordsRetrieved);
		assertEquals (recordsRetrieved, 3);
		
		// Test updating a record:
		result0.setObject0Id(555); // Let's update result0 - the first of the duplicate entries that we retrieved above
		provider.updateQueueRecord(result0);
		// Now let us retrieve the updated record and examine it to make sure that has been updated correctly
		ArrayList<QueueRecord> searchResults2 = provider.searchQueueRecords(QueueRecordsTable.COLUMN_OBJECT_0_ID, String.valueOf(555));
		QueueRecord result1 = searchResults2.get(0);
		recordsRetrieved = searchResults2.size();
		
		Log.i(TAG, "Expected number of results from search: 1");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 1);
		
		Log.i(TAG, "Expected object id: 555");
		Log.i(TAG, "Actual label  : " + result1.getObject0Id());
		assertEquals(result1.getObject0Id(), 555);
		
		// Test searching for a single record by its ID:
		QueueRecord result2 = provider.searchForSingleRecord(result1.getId());
		Log.i(TAG, "Expected object id: 555");
		Log.i(TAG, "Actual label  : " + result2.getObject0Id());
		assertEquals(result2.getObject0Id(), 555);
		
		// Test deleting a single record:
		provider.deleteQueueRecord(result1); // Let us delete the address that we just updated
		
		// Now let us search for the record to make sure that it has in fact been deleted
		ArrayList<QueueRecord> searchResults3 = provider.searchQueueRecords(QueueRecordsTable.COLUMN_OBJECT_0_ID, String.valueOf(555));
		recordsRetrieved =searchResults3.size();
		
		Log.i(TAG, "Expected number of results from search: 0");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 0);
		
		// Test searching for a QueueRecord using the second object type field
		ArrayList<QueueRecord> searchResults4 = provider.searchQueueRecords(QueueRecordsTable.COLUMN_OBJECT_1_TYPE, "Pubkey");
		recordsRetrieved =searchResults4.size();
		
		Log.i(TAG, "Expected number of results from search: 1");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 1);
		
		// Finally, delete the records we have added so that they don't mess up the rest of the application
		provider.deleteAllQueueRecords();
	}
}
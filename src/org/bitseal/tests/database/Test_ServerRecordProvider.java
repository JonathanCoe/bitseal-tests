package org.bitseal.tests.database;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.bitseal.data.ServerRecord;
import org.bitseal.database.DatabaseContentProvider;
import org.bitseal.database.ServerRecordProvider;
import org.bitseal.database.ServerRecordsTable;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Tests the functions of the ServerRecordProvider class.<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
 * 
 * @author Jonathan Coe
 */
public class Test_ServerRecordProvider extends AndroidTestCase
{
	private static final String TAG = "TEST_SERVER_RECORD_PROVIDER";
	
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
	
	public void testServerRecordProvider()
	{
		// Now we should be able to get the application context safely
		ServerRecordProvider provider = ServerRecordProvider.get(getContext());
		
		//First clear out any existing records to ensure a fair test:
		provider.deleteAllServerRecords();
		
		// Test adding records:
		ServerRecord record0 = new ServerRecord();
		record0.setURL("http://21.22.126.16:8442");
		record0.setUsername("alzg44");
		record0.setPassword("JGIDinasdfh29345trandsdsfdafar");
		
		ServerRecord record1 = new ServerRecord();
		record1.setURL("http://95.24.157.16:8442");
		record1.setUsername("ewqer4q");
		record1.setPassword("GRer345trand342rf");
		
		ServerRecord record2 = new ServerRecord(); // Deliberate duplicate for testing - see below
		record2.setURL("http://95.24.157.16:8442");
		record2.setUsername("ewqer4q");
		record2.setPassword("GRer345trand342rf"); 
		
		provider.addServerRecord(record0);
		provider.addServerRecord(record1);
		provider.addServerRecord(record2);
		
		// Test searching for records:
		ArrayList<ServerRecord> searchResults0 = provider.searchServerRecords(ServerRecordsTable.COLUMN_PASSWORD, "GRer345trand342rf");
		ServerRecord result0 = searchResults0.get(0);
		int recordsRetrieved = searchResults0.size();
		
		Log.i(TAG, "Expected number of results from search: 2");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 2);
		
		Log.i(TAG, "Expected result (from first result): GRer345trand342rf");
		Log.i(TAG, "Actual result (from first result):   " + result0.getPassword());
		assertEquals(result0.getPassword(), "GRer345trand342rf");
		
		// Test getting all records:
		ArrayList<ServerRecord> searchResults1 = provider.getAllServerRecords();
		recordsRetrieved = searchResults1.size();
		
		Log.i(TAG, "Expected number of records: 3");
		Log.i(TAG, "Actual number of records:   " + recordsRetrieved);
		assertEquals (recordsRetrieved, 3);
		
		// Test updating a record:
		result0.setUsername("A new updated username"); // Let's update result0 - the first of the duplicate entries that we retrieved above
		provider.updateServerRecord(result0);
		// Now let us retrieve the updated record and examine it to make sure that has been updated correctly
		ArrayList<ServerRecord> searchResults2 = provider.searchServerRecords(ServerRecordsTable.COLUMN_USERNAME, "A new updated username");
		ServerRecord result1 = searchResults2.get(0);
		recordsRetrieved = searchResults2.size();
		
		Log.i(TAG, "Expected number of results from search: 1");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 1);
		
		Log.i(TAG, "Expected label: A new updated username");
		Log.i(TAG, "Actual label  : " + result1.getUsername());
		assertEquals(result1.getUsername(), "A new updated username");
		
		// Test searching for a single record by its ID:
		ServerRecord result2 = provider.searchForSingleRecord(result1.getId());
		Log.i(TAG, "Expected label: A new updated username");
		Log.i(TAG, "Actual label  : " + result2.getUsername());
		assertEquals(result2.getUsername(), "A new updated username");
		
		// Test deleting a single record:
		provider.deleteServerRecord(result1); // Let us delete the address that we just updated
		
		// Now let us search for the record to make sure that it has in fact been deleted
		ArrayList<ServerRecord> searchResults3 = provider.searchServerRecords(ServerRecordsTable.COLUMN_USERNAME, "A new updated username");
		recordsRetrieved =searchResults3.size();
		
		Log.i(TAG, "Expected number of results from search: 0");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 0);
		
		// Finally, delete the records we have added so that they don't mess up the rest of the application
		provider.deleteAllServerRecords();
	}
}
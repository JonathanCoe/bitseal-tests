package org.bitseal.tests.database;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.bitseal.data.AddressBookRecord;
import org.bitseal.database.AddressBookRecordProvider;
import org.bitseal.database.AddressBookRecordsTable;
import org.bitseal.database.DatabaseContentProvider;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Tests the functions of the AddressBookRecordProvider class.<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
 * 
 * @author Jonathan Coe
 */
public class Test_AddressBookRecordProvider extends AndroidTestCase
{
	private static final String TAG = "TEST_ADDRESS_BOOK_RECORD_PROVIDER";
	
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
	
	public void testAddressBookRecordProvider()
	{
		// Now we should be able to get the application context safely
		AddressBookRecordProvider provider = AddressBookRecordProvider.get(getContext());
		
		//First clear out any existing records to ensure a fair test:
		provider.deleteAllAddressBookRecords();
		
		// Test adding records:
		AddressBookRecord record0 = new AddressBookRecord();
		record0.setLabel("My friend's address");
		record0.setAddress("BM-6LX2m1mRH5DHyXGeL8rL38uxm73xotdkXhR");
		
		AddressBookRecord record1 = new AddressBookRecord();
		record1.setLabel("David's new address");
		record1.setAddress("BM-6Lq5nMP947r8yTS2kV4Njtbz6QFuYTVvgrG");
		
		AddressBookRecord record2 = new AddressBookRecord();
		record2.setLabel("A duplicate entry of David's address");
		record2.setAddress("BM-6Lq5nMP947r8yTS2kV4Njtbz6QFuYTVvgrG"); // Deliberate duplicate for testing - see below
		
		provider.addAddressBookRecord(record0);
		provider.addAddressBookRecord(record1);
		provider.addAddressBookRecord(record2);
		
		// Test searching for records:
		ArrayList<AddressBookRecord> searchResults0 = provider.searchAddressBookRecords(AddressBookRecordsTable.COLUMN_ADDRESS, "BM-6Lq5nMP947r8yTS2kV4Njtbz6QFuYTVvgrG");
		AddressBookRecord result0 = searchResults0.get(0);
		int recordsRetrieved = searchResults0.size();
		
		Log.i(TAG, "Expected number of results from search: 2");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 2);
		
		Log.i(TAG, "Expected result (from first result): BM-6Lq5nMP947r8yTS2kV4Njtbz6QFuYTVvgrG");
		Log.i(TAG, "Actual result (from first result):   " + result0.getAddress());
		assertEquals(result0.getAddress(), "BM-6Lq5nMP947r8yTS2kV4Njtbz6QFuYTVvgrG");
		
		// Test getting all records:
		ArrayList<AddressBookRecord> searchResults1 = provider.getAllAddressBookRecords();
		recordsRetrieved = searchResults1.size();
		
		Log.i(TAG, "Expected number of records: 3");
		Log.i(TAG, "Actual number of records:   " + recordsRetrieved);
		assertEquals (recordsRetrieved, 3);
		
		// Test updating a record:
		result0.setLabel("A new updated label"); // Let's update result0 - the first of the duplicate entries that we retrieved above
		result0.setAddress("BM-2cXi1DUvyYbevheWbpQsT4mLwN2ZwQUeTr");
		
		provider.updateAddressBookRecord(result0);
		// Now let us retrieve the updated record and examine it to make sure that has been updated correctly
		ArrayList<AddressBookRecord> searchResults2 = provider.searchAddressBookRecords(AddressBookRecordsTable.COLUMN_ADDRESS, "BM-2cXi1DUvyYbevheWbpQsT4mLwN2ZwQUeTr");
		AddressBookRecord result1 = searchResults2.get(0);
		recordsRetrieved = searchResults2.size();
		
		Log.i(TAG, "Expected number of results from search: 1");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 1);
		
		Log.i(TAG, "Expected label: A new updated label");
		Log.i(TAG, "Actual label  : " + result1.getLabel());
		assertEquals(result1.getLabel(), "A new updated label");
		
		Log.i(TAG, "Expected address: BM-2cXi1DUvyYbevheWbpQsT4mLwN2ZwQUeTr");
		Log.i(TAG, "Actual address  : " + result1.getAddress());
		assertEquals(result1.getAddress(), "BM-2cXi1DUvyYbevheWbpQsT4mLwN2ZwQUeTr");
				
		// Test searching for a single record by its ID:
		AddressBookRecord result2 = provider.searchForSingleRecord(result1.getId());
		Log.i(TAG, "Expected label: A new updated label");
		Log.i(TAG, "Actual label  : " + result2.getLabel());
		assertEquals(result2.getLabel(), "A new updated label");
		
		Log.i(TAG, "Expected address: BM-2cXi1DUvyYbevheWbpQsT4mLwN2ZwQUeTr");
		Log.i(TAG, "Actual address  : " + result2.getAddress());
		assertEquals(result2.getAddress(), "BM-2cXi1DUvyYbevheWbpQsT4mLwN2ZwQUeTr");
		
		// Test deleting a single record:
		provider.deleteAddressBookRecord(result1); // Let us delete the address that we just updated
		
		// Now let us search for the record to make sure that it has in fact been deleted
		ArrayList<AddressBookRecord> searchResults3 = provider.searchAddressBookRecords(AddressBookRecordsTable.COLUMN_ADDRESS, "BM-2cXi1DUvyYbevheWbpQsT4mLwN2ZwQUeTr");
		recordsRetrieved =searchResults3.size();
		
		Log.i(TAG, "Expected number of results from search: 0");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 0);
		
		// Finally, delete the records we have added so that they don't mess up the rest of the application
		provider.deleteAllAddressBookRecords();
	}
}
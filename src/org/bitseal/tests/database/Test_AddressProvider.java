package org.bitseal.tests.database;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.bitseal.data.Address;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.AddressesTable;
import org.bitseal.database.DatabaseContentProvider;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Tests the functions of the AddressProvider class.<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
 * 
 * @author Jonathan Coe
 */
public class Test_AddressProvider extends AndroidTestCase
{
	private static final String TAG = "TEST_ADDRESS_PROVIDER";
	
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
	
	public void testAddressProvider()
	{
		// Now we should be able to get the application context safely
		AddressProvider provider = AddressProvider.get(getContext());
		
		//First clear out any existing records to ensure a fair test:
		provider.deleteAllAddresses();
				
		// Test adding records:
		Address record0 = new Address();
		record0.setLabel("My first address");
		record0.setAddress("BM-NBniqBpDRZHLx7rVWyyrEf1XmPgSiSrr");
		record0.setPrivateSigningKey("5K7iiqcVLYc9ibVUo2uMGsrSeTAXPER5DUkYeCaaBHFncdrmH4C");
		record0.setPrivateEncryptionKey("56w3efFC1m3oxsFDzfHTLFpJ1UYhCaY4ksyw2UjisHnxdRF19aE");
		record0.setRipeHash(new String("1439F0A32DDDE4936239A2BD96442E53F95CB1D6").getBytes());
		record0.setTag(new String("45634456ergsdfgsdfDE4936239A2BD96fgsdfgsdfgSFERE53F95jcyfb").getBytes());
		
		Address record1 = new Address();
		record1.setLabel("My second address");
		record1.setAddress("BM-NBpe4wbtC59sWFKxwaiGGNCb715D6xvY");
		record1.setPrivateSigningKey("5K7iiqcVLYc9ibVUo2uMGsrSeTAXFER5DUFRbCaaBHFncdrmH4C");
		record1.setPrivateEncryptionKey("5Hw3efFC1m3oxsWQzfHTLFpJ1UYhCaY4ksyw2UhhsHnxdRF19aE");
		record1.setRipeHash(new String("1239F0A32DDDE4936239A2BD96432E63F95CB1D7").getBytes());
		record1.setTag(new String("345gsdfgsdfDE493623ERGgsdfgsdfgSFERvefefdadefGSTHRTb").getBytes());
		
		Address record2 = new Address();
		record2.setLabel("A duplicate entry my second address");
		record2.setAddress("BM-NBpe4wbtC59sWFKxwaiGGNCb715D6xvY"); // Deliberate duplicate for testing - see below
		record2.setPrivateSigningKey("59TiiqcVLYc9ibVUo2uMGsrSeTAXPER5DUkYeCaaBHFncdrmH4C");
		record2.setPrivateEncryptionKey("5Xw3efFC1m3oxsWQzfHTLFpJ1UYhCaY4ksyw2UhhsHnxdRF19aE");
		record2.setRipeHash(new String("1639F0A32DDDE4936239A2BD96442E53F95AB1D6").getBytes());
		record2.setTag(new String("345gsdfgsdfDE493623ERGgsdfgsdfgSFERvefefdadefGSTHRTb").getBytes());
		
		provider.addAddress(record0);
		provider.addAddress(record1);
		provider.addAddress(record2);
		
		// Test searching for records:
		ArrayList<Address> searchResults0 = provider.searchAddresses(AddressesTable.COLUMN_ADDRESS, "BM-NBpe4wbtC59sWFKxwaiGGNCb715D6xvY");
		Address result0 = searchResults0.get(0);
		int recordsRetrieved = searchResults0.size();
		
		Log.i(TAG, "Expected number of results from search: 2");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 2);
		
		Log.i(TAG, "Expected result (from first result): BM-NBpe4wbtC59sWFKxwaiGGNCb715D6xvY");
		Log.i(TAG, "Actual result (from first result):   " + result0.getAddress());
		assertEquals(result0.getAddress(), "BM-NBpe4wbtC59sWFKxwaiGGNCb715D6xvY");
		
		// Test getting all records:
		ArrayList<Address> searchResults1 = provider.getAllAddresses();
		recordsRetrieved = searchResults1.size();
		
		Log.i(TAG, "Expected number of records: 3");
		Log.i(TAG, "Actual number of records:   " + recordsRetrieved);
		assertEquals (recordsRetrieved, 3);
		
		// Test updating a record:
		result0.setLabel("A new updated label"); // Let's update result0 - the first of the duplicate entries that we retrieved above
		result0.setAddress("BM-ooTaRTxkbFry5wbmnxRN1Gr3inFYYp2aD");
		
		provider.updateAddress(result0);
		// Now let us retrieve the updated record and examine it to make sure that has been updated correctly
		ArrayList<Address> searchResults2 = provider.searchAddresses(AddressesTable.COLUMN_ADDRESS, "BM-ooTaRTxkbFry5wbmnxRN1Gr3inFYYp2aD");
		Address result1 = searchResults2.get(0);
		recordsRetrieved = searchResults2.size();
		
		Log.i(TAG, "Expected number of results from search: 1");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 1);
		
		Log.i(TAG, "Expected label: A new updated label");
		Log.i(TAG, "Actual label  : " + result1.getLabel());
		assertEquals(result1.getLabel(), "A new updated label");
		
		Log.i(TAG, "Expected address: BM-ooTaRTxkbFry5wbmnxRN1Gr3inFYYp2aD");
		Log.i(TAG, "Actual address  : " + result1.getAddress());
		assertEquals(result1.getAddress(), "BM-ooTaRTxkbFry5wbmnxRN1Gr3inFYYp2aD");
		
		// Test searching for a single record by its ID:
		Address result2 = provider.searchForSingleRecord(result1.getId());
		Log.i(TAG, "Expected label: A new updated label");
		Log.i(TAG, "Actual label  : " + result2.getLabel());
		assertEquals(result2.getLabel(), "A new updated label");
		
		Log.i(TAG, "Expected address: BM-ooTaRTxkbFry5wbmnxRN1Gr3inFYYp2aD");
		Log.i(TAG, "Actual address  : " + result2.getAddress());
		assertEquals(result2.getAddress(), "BM-ooTaRTxkbFry5wbmnxRN1Gr3inFYYp2aD");
		
		// Test deleting a single record:
		provider.deleteAddress(result1); // Let us delete the address that we just updated
		
		// Now let us search for the record to make sure that it has in fact been deleted
		ArrayList<Address> searchResults3 = provider.searchAddresses(AddressesTable.COLUMN_ADDRESS, "BM-ooTaRTxkbFry5wbmnxRN1Gr3inFYYp2aD");
		recordsRetrieved =searchResults3.size();
		
		Log.i(TAG, "Expected number of results from search: 0");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 0);
		
		// Finally, delete the records we have added so that they don't mess up the rest of the application
		provider.deleteAllAddresses();
	}
}
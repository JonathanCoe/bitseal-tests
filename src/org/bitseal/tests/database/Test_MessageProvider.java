package org.bitseal.tests.database;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.bitseal.data.Message;
import org.bitseal.database.DatabaseContentProvider;
import org.bitseal.database.MessageProvider;
import org.bitseal.database.MessagesTable;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Tests the functions of the MessageProvider class.<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
 * 
 * @author Jonathan Coe
 */
public class Test_MessageProvider extends AndroidTestCase
{
	private static final String TAG = "TEST_MESSAGE_PROVIDER";
	
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
	
	public void testMessageProvider()
	{
		// Now we should be able to get the application context safely
		MessageProvider provider = MessageProvider.get(getContext());
		
		//First clear out any existing records to ensure a fair test:
		provider.deleteAllMessages();
		
		// Test adding records:
		Message record0 = new Message();
		record0.setBelongsToMe(true);
		record0.setRead(true);
		record0.setStatus("Received");
		record0.setTime(System.currentTimeMillis() / 1000);
		record0.setToAddress("BM-6LX2m1mRH5DHyXGeL8rL38uxm73xotdkXhR");
		record0.setFromAddress("BM-6Lq5nMP947r8yTS2kV4Njtbz6QFuYTVvgrG");
		record0.setSubject("Hi there");
		record0.setBody("I think that we should conduct the meeting at a later date");

		Message record1 = new Message();
		record1.setBelongsToMe(true);
		record1.setRead(false);
		record1.setStatus("Sent");
		record1.setTime(System.currentTimeMillis() / 1000);
		record1.setToAddress("BM-6LfXSPfDkGh118zV8jGzHhqs5kophkxr7aa");
		record1.setFromAddress("BM-6Ljth1S9unVrphhn52Wzee2xLSmBbDSg9EB");
		record1.setSubject("HELLO!!!!!!!!!!!");
		record1.setBody("LOUUUUUUUUD NOISES!");

		Message record2 = new Message(); // Deliberate duplicate for testing - see below
		record2.setBelongsToMe(true);
		record2.setRead(false);
		record2.setStatus("Sent");
		record2.setTime(System.currentTimeMillis() / 1000);
		record2.setToAddress("BM-6LfXSPfDkGh118zV8jGzHhqs5kophkxr7aa");
		record2.setFromAddress("BM-6Ljth1S9unVrphhn52Wzee2xLSmBbDSg9EB");
		record2.setSubject("HELLO!!!!!!!!!!!");
		record2.setBody("LOUUUUUUUUD NOISES!"); 
		
		provider.addMessage(record0);
		provider.addMessage(record1);
		provider.addMessage(record2);
		
		// Test searching for records:
		ArrayList<Message> searchResults0 = provider.searchMessages(MessagesTable.COLUMN_BODY, "LOUUUUUUUUD NOISES!");
		Message result0 = searchResults0.get(0);
		int recordsRetrieved = searchResults0.size();
		
		Log.i(TAG, "Expected number of results from search: 2");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 2);
		
		Log.i(TAG, "Expected result (from first result): LOUUUUUUUUD NOISES!");
		Log.i(TAG, "Actual result (from first result):   " + result0.getBody());
		assertEquals(result0.getBody(), "LOUUUUUUUUD NOISES!");
		
		// Test getting all records:
		ArrayList<Message> searchResults1 = provider.getAllMessages();
		recordsRetrieved = searchResults1.size();
		
		Log.i(TAG, "Expected number of records: 3");
		Log.i(TAG, "Actual number of records:   " + recordsRetrieved);
		assertEquals (recordsRetrieved, 3);
		
		// Test searching for duplicates
		Message record3 = new Message(); // Deliberate duplicate for testing - see below
		record3.setBelongsToMe(true);
		record3.setRead(false);
		record3.setStatus("Sent");
		record3.setTime(System.currentTimeMillis() / 1000);
		record3.setToAddress("BM-6LfXSPfDkGh118zV8jGzHhqs5kophkxr7aa");
		record3.setFromAddress("BM-6Ljth1S9unVrphhn52Wzee2xLSmBbDSg9EB");
		record3.setSubject("HELLO!!!!!!!!!!!");
		record3.setBody("LOUUUUUUUUD NOISES!"); 
		
		Message record4 = new Message(); // Not a duplicate
		record4.setBelongsToMe(true);
		record4.setRead(false);
		record4.setStatus("Sent");
		record4.setTime(System.currentTimeMillis() / 1000);
		record4.setToAddress("BM-2cV7r3RePFg9sDSDZFvxQtP8rjizvYzM4s");
		record4.setFromAddress("BM-6Ljth1S9unVrphhn52Wzee2xLSmBbDSg9EB");
		record4.setSubject("Some sort of subject");
		record4.setBody("Some kind of message"); 
		
		boolean duplicatesDetected = provider.detectDuplicateMessage(record3);
		assertTrue(duplicatesDetected);
		duplicatesDetected = provider.detectDuplicateMessage(record4);
		assertFalse(duplicatesDetected);
		
		// Test updating a record:
		result0.setSubject("A new updated subject"); // Let's update result0 - the first of the duplicate entries that we retrieved above
		provider.updateMessage(result0);
		
		// Now let us retrieve the updated record and examine it to make sure that has been updated correctly
		ArrayList<Message> searchResults2 = provider.searchMessages(MessagesTable.COLUMN_SUBJECT, "A new updated subject");
		Message result1 = searchResults2.get(0);
		recordsRetrieved = searchResults2.size();
		
		Log.i(TAG, "Expected number of results from search: 1");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 1);
		
		Log.i(TAG, "Expected label: A new updated subject");
		Log.i(TAG, "Actual label  : " + result1.getSubject());
		assertEquals(result1.getSubject(), "A new updated subject");
		
		// Test searching for a single record by its ID:
		Message result2 = provider.searchForSingleRecord(result1.getId());
		Log.i(TAG, "Expected label: A new updated subject");
		Log.i(TAG, "Actual label  : " + result2.getSubject());
		assertEquals(result2.getSubject(), "A new updated subject");
		
		// Test deleting a single record:
		provider.deleteMessage(result1); // Let us delete the address that we just updated
		
		// Now let us search for the record to make sure that it has in fact been deleted
		ArrayList<Message> searchResults3 = provider.searchMessages(MessagesTable.COLUMN_SUBJECT, "A new updated subject");
		recordsRetrieved =searchResults3.size();
		
		Log.i(TAG, "Expected number of results from search: 0");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 0);
		
		// Finally, delete the records we have added so that they don't mess up the rest of the application
		provider.deleteAllMessages();
	}
}
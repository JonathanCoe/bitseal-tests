package org.bitseal.tests.network;

import java.util.concurrent.TimeUnit;

import org.bitseal.core.App;
import org.bitseal.core.OutgoingMessageProcessor;
import org.bitseal.core.PubkeyProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.PubkeyGenerator;
import org.bitseal.data.Address;
import org.bitseal.data.Message;
import org.bitseal.data.Payload;
import org.bitseal.data.Pubkey;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.DatabaseContentProvider;
import org.bitseal.database.PubkeyProvider;
import org.bitseal.network.ServerCommunicator;
import org.bitseal.util.ByteFormatter;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

/** 
 * Tests the disseminateMsgWithPOW() method in the ServerCommunicator class.<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
**/
public class Test_DisseminateMsg extends AndroidTestCase
{
	// Start the test with a String representing a Bitmessage address. For 'real world' testing,
	// this should be an address owned by a real Bitmessage node that is active on the network.
	private static final String TEST_ADDRESS = "BM-2cWH3y8Kyzyy7j4fYkwj6qDWzqZRUqqb2a";
	
	private static final String TEST_MESSAGE_SUBJECT = "Sent from.....YKW";
	private static final String TEST_MESSAGE_BODY = "As they say, 'Sent from my Android phone' :)";
	
	private static final long TEST_MSG_TIME_TO_LIVE = 600;
	
	private static final String TAG = "TEST_DISSEMINATE_MSG";
	
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
	
	public void testDisseminateMsg()
	{
		// Wait for five seconds in order to make it more likely that we will be able to get application context
		SystemClock.sleep(5000);
		
		// Generate a new Address object, which will be the 'from' addresses of the message
		AddressGenerator addGen = new AddressGenerator();
		Address fromAddress = addGen.generateAndSaveNewAddress();
		
		// Generate a Pubkey for the 'from' address. This will be used during the message sending process
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey fromPubkey = pubGen.generateAndSaveNewPubkey(fromAddress);
		
		// Retrieve the pubkey for the 'to' Address (this will automatically save it to the database)
		PubkeyProcessor pubProc = new PubkeyProcessor();
		Pubkey toPubkey = pubProc.retrievePubkeyByAddressString(TEST_ADDRESS);
		
		// Create a new Message object, as if it had been written by the user through the UI
		Message message = new Message();
		message.setBelongsToMe(true);
		message.setToAddress(TEST_ADDRESS);
		message.setFromAddress(fromAddress.getAddress());
		message.setSubject(TEST_MESSAGE_SUBJECT);
		message.setBody(TEST_MESSAGE_BODY);
		
		// Take the message and process it, giving a msg that is ready to be sent over the network
		OutgoingMessageProcessor outMsgProc = new OutgoingMessageProcessor();
		Payload msgPayload = outMsgProc.processOutgoingMessage(message, toPubkey, true, TEST_MSG_TIME_TO_LIVE);
		Log.i(TAG, "msg to be sent over the network: " + ByteFormatter.byteArrayToHexString(msgPayload.getPayload()));
		
		// Disseminate the message payload to the rest of the network via a PyBitmessage server
		ServerCommunicator servCom = new ServerCommunicator();
		boolean disseminationSuccessful = servCom.disseminateMsg(msgPayload.getPayload());
		
		if (disseminationSuccessful == true)
		{
			Log.i(TAG, "The message was successfully sent to a server!");
		}
		else
		{
			Log.e(TAG, "The attempt to disseminate the message failed!");
		}
		assertTrue(disseminationSuccessful);
		
		// Cleaning up - delete the addresses and pubkeys we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(fromAddress);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(toPubkey);
		pubProv.deletePubkey(fromPubkey);
	}
}
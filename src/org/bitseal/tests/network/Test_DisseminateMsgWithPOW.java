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
public class Test_DisseminateMsgWithPOW extends AndroidTestCase
{
	private static final String TAG = "TEST_DISSEMINATE_MSG_WITH_POW";
	
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
	
	public void testDisseminateMsgWithPOW()
	{
		// Wait for five seconds in order to make it more likely that we will be able to get application context
		SystemClock.sleep(5000);
		
		// Start the test with a String representing a Bitmessage address. For 'real world' testing,
		// this should be an address owned by a real Bitmessage node that is active on the network.
		String testAddress = "BM-2cVMnheVsC4oMzqmTV5xXfRYworaBnoJn7";
		
		// Generate a new Address object, which will be the 'from' addresses of the message
		AddressGenerator addGen = new AddressGenerator();
		Address fromAddress = addGen.generateAndSaveNewAddress();
		
		// Generate a Pubkey for the 'from' address. This will be used during the message sending process
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey fromPubkey = pubGen.generateAndSaveNewPubkey(fromAddress);
		
		// Retrieve the pubkey for the 'to' Address (this will automatically save it to the database)
		PubkeyProcessor pubProc = new PubkeyProcessor();
		Pubkey toPubkey = pubProc.retrievePubkeyByAddressString(testAddress);
		
		// Create a new Message object, as if it had been written by the user through the UI
		Message message = new Message();
		message.setBelongsToMe(true);
		message.setToAddress(testAddress);
		message.setFromAddress(fromAddress.getAddress());
		message.setSubject("Sent from.....YKW");
		message.setBody("As they say, 'Sent from my Android phone' :)");
		
		// Take the message and process it, giving a msg that is ready to be sent over the network
		OutgoingMessageProcessor outMsgProc = new OutgoingMessageProcessor();
		Payload msgPayload = outMsgProc.processOutgoingMessage(message, toPubkey, true);
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
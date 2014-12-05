package org.bitseal.tests.network;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.bitseal.core.App;
import org.bitseal.core.IncomingMessageProcessor;
import org.bitseal.core.OutgoingMessageProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.PubkeyGenerator;
import org.bitseal.data.Address;
import org.bitseal.data.Message;
import org.bitseal.data.Payload;
import org.bitseal.data.Pubkey;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.DatabaseContentProvider;
import org.bitseal.database.PayloadProvider;
import org.bitseal.database.PayloadsTable;
import org.bitseal.database.PubkeyProvider;
import org.bitseal.network.ServerCommunicator;
import org.bitseal.util.ByteFormatter;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

/** 
 * Tests the requestMessagesFromServer() method in the ServerCommunicator class.<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
**/
public class Test_RequestMessagesFromServer extends AndroidTestCase
{
	private static final String MESSAGE_0_SUBJECT = "Shall Ezekiel suffer?";
	private static final String MESSAGE_0_BODY = "Bullishly plowing on through the Judoka's snow mill";
	private static final String MESSAGE_1_SUBJECT = "I hope the summer lasts";
	private static final String MESSAGE_1_BODY = "Call upon dawn in the West";
	private static final String MESSAGE_2_SUBJECT = "Have we seen the last days of lent?";
	private static final String MESSAGE_2_BODY = "Wriggling softly, the mouse shuffles onwards";
	
	private static final long TEST_MSG_TIME_TO_LIVE = 3600; // In seconds, so currently equals to 1 hour
	
	private static final String TAG = "TEST_REQUEST_MESSAGES_FROM_SERVER";
	
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
	
	public void testRequestMessagesFromServer()
	{
		// Wait for five seconds in order to make it more likely that we will be able to get application context
		SystemClock.sleep(5000);
		
		// Generate a 'to' address and a 'from' address
		AddressGenerator addGen = new AddressGenerator();
		Address toAddress = addGen.generateAndSaveNewAddress();
		Address fromAddress = addGen.generateAndSaveNewAddress();
		
		// Generate a 'to' pubkey and a 'from' pubkey
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey toPubkey = pubGen.generateAndSaveNewPubkey(toAddress);
		Pubkey fromPubkey = pubGen.generateAndSaveNewPubkey(fromAddress);
		
		// Generate three new messages to send out to servers. All will share the same
		// 'to' and 'from' addresses
		// Create a new Message object, as if it had been written by the user through the UI
		Message message0 = new Message();
		message0.setBelongsToMe(true);
		message0.setToAddress(toAddress.getAddress());
		message0.setFromAddress(fromAddress.getAddress());
		message0.setSubject(MESSAGE_0_SUBJECT);
		message0.setBody(MESSAGE_0_BODY);
		
		Message message1 = new Message();
		message1.setBelongsToMe(true);
		message1.setToAddress(toAddress.getAddress());
		message1.setFromAddress(fromAddress.getAddress());
		message1.setSubject(MESSAGE_1_SUBJECT);
		message1.setBody(MESSAGE_1_BODY);
		
		Message message2 = new Message();
		message2.setBelongsToMe(true);
		message2.setToAddress(toAddress.getAddress());
		message2.setFromAddress(fromAddress.getAddress());
		message2.setSubject(MESSAGE_2_SUBJECT);
		message2.setBody(MESSAGE_2_BODY);
		
		// Take the messages and process them, giving three msgs that are ready to be sent over the network
		OutgoingMessageProcessor outMsgProc = new OutgoingMessageProcessor();
		Payload msgPayload0 = outMsgProc.processOutgoingMessage(message0, toPubkey, true, TEST_MSG_TIME_TO_LIVE);
		Payload msgPayload1 = outMsgProc.processOutgoingMessage(message1, toPubkey, true, TEST_MSG_TIME_TO_LIVE);
		Payload msgPayload2 = outMsgProc.processOutgoingMessage(message2, toPubkey, true, TEST_MSG_TIME_TO_LIVE);
		
		Log.i(TAG, "msg0 bytes in hex: " + ByteFormatter.byteArrayToHexString(msgPayload0.getPayload()));
		Log.i(TAG, "msg1 bytes in hex: " + ByteFormatter.byteArrayToHexString(msgPayload1.getPayload()));
		Log.i(TAG, "msg2 bytes in hex: " + ByteFormatter.byteArrayToHexString(msgPayload2.getPayload()));
		
		// Disseminate the msgs to the rest of the network via a Bitseal server
		ServerCommunicator servCom = new ServerCommunicator();
		boolean disseminationSuccessful = servCom.disseminateMsg(msgPayload0.getPayload());	
		if (disseminationSuccessful == true)
		{
			Log.i(TAG, "msg0 was successfully sent to a server!");
		}
		else
		{
			Log.e(TAG, "The attempt to disseminate msg0 failed!");
		}
		assertTrue(disseminationSuccessful);
		
		disseminationSuccessful = servCom.disseminateMsg(msgPayload1.getPayload());	
		if (disseminationSuccessful == true)
		{
			Log.i(TAG, "msg1 was successfully sent to a server!");
		}
		else
		{
			Log.e(TAG, "The attempt to disseminate msg1 failed!");
		}
		assertTrue(disseminationSuccessful);
		
		disseminationSuccessful = servCom.disseminateMsg(msgPayload2.getPayload());	
		if (disseminationSuccessful == true)
		{
			Log.i(TAG, "msg2 was successfully sent to a server!");
		}
		else
		{
			Log.e(TAG, "The attempt to disseminate msg2 failed!");
		}
		assertTrue(disseminationSuccessful);
		
		// Wait for an amount of time that should be sufficient for the msgs to be circulated around the network
		int waitTimeSeconds = 60;
		Log.i(TAG, "About to wait for " + waitTimeSeconds + " seconds so that the msgs can circulate around the network");
		SystemClock.sleep(waitTimeSeconds * 1000);
		
		// Delete any existing Payload objects in the database to avoid false positive results
		PayloadProvider payProv = PayloadProvider.get(App.getContext());
		payProv.deleteAllPayloads();
		
		// Now request these messages from a server
		servCom.checkServerForNewMsgs();
		
		// Search the database for the Payloads of any possible new msgs
		String[] columnNames = new String[]{PayloadsTable.COLUMN_TYPE, PayloadsTable.COLUMN_BELONGS_TO_ME};
		String[] searchTerms = new String[]{Payload.OBJECT_TYPE_MSG, "0"}; // Zero stands for false in the database
		ArrayList<Payload> msgsToProcess = payProv.searchPayloads(columnNames, searchTerms);
		
		boolean msg0RetrievedAndDecrypted = false;
		boolean msg1RetrievedAndDecrypted = false;
		boolean msg2RetrievedAndDecrypted = false;
		
		if (msgsToProcess.size() > 0)
		{
			for (Payload p : msgsToProcess)
			{
				//Process each msg Payload that is found
				IncomingMessageProcessor inMsgProc = new IncomingMessageProcessor();
				Message decryptedMessage = inMsgProc.processReceivedMsg(p);
				
				if (decryptedMessage != null)
				{
					// Check each decrypted Message
					String messageSubject = decryptedMessage.getSubject();
					String messageBody = decryptedMessage.getBody();			
					Log.i(TAG, "Decrypted message subject: " + messageSubject);
					Log.i(TAG, "Decrypted message body:    " + messageBody);
					
					if (messageSubject.equals(MESSAGE_0_SUBJECT) && messageBody.equals(MESSAGE_0_BODY))
					{
						msg0RetrievedAndDecrypted = true;
					}
					else if (messageSubject.equals(MESSAGE_1_SUBJECT) && messageBody.equals(MESSAGE_1_BODY))
					{
						msg1RetrievedAndDecrypted = true;
					}
					else if (messageSubject.equals(MESSAGE_2_SUBJECT) && messageBody.equals(MESSAGE_2_BODY))
					{
						msg2RetrievedAndDecrypted = true;
					}
					
					// After the Payload has been processed, delete it from the database
					payProv.deletePayload(p);
				}
			}
		}
		
		// Check whether we managed to retrieve and decrypt the msgs
		assertTrue(msg0RetrievedAndDecrypted);
		assertTrue(msg1RetrievedAndDecrypted);
		assertTrue(msg2RetrievedAndDecrypted);
		
		// Cleaning up - delete the addresses and pubkeys we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(toAddress);
		addProv.deleteAddress(fromAddress);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(toPubkey);
		pubProv.deletePubkey(fromPubkey);
	}
}
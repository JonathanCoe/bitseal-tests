package org.bitseal.tests.concept;

import java.util.ArrayList;
import java.util.Collections;

import junit.framework.TestCase;

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
import org.bitseal.database.PubkeyProvider;

import android.util.Log;

/**
 * A test which attempts to evaluate the work required for
 * the Android client to retrieve messages by simply requesting
 * all msgs in the stream(s) it is a part of and attempting to 
 * decrypt them all - the 'brute force' method. <br><br>
 * 
 * <b>NOTE:</b> Tests in the 'concept' package are for experimentation
 * related to Protocol Version 3, not for checking the correctness or
 * performance of Bitseal. 
 * 
 * @author Jonathan Coe
 */
public class Test_BruteForce extends TestCase
{
	private static final String VALID_MESSAGE_SUBJECT = "Valid message subject ";
	private static final String VALID_MESSAGE_BODY = "Valid message body ";
	private static final int NUMBER_OF_VALID_MESSAGES = 1;
	
	private static final String INVALID_MESSAGE_SUBJECT = "Invalid message subject ";
	private static final String INVALID_MESSAGE_BODY = "Invalid message body ";
	private static final int NUMBER_OF_INVALID_MESSAGES = 19;
	
	private static final long TEST_MSG_TIME_TO_LIVE = 600;
	
	private static final String TAG = "TEST_BRUTE_FORCE";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testBruteForce()
	{
		// First remove any existing addresses in the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAllAddresses();
		
		// Generate a 'to address', a 'from address', and an 'other address'
		AddressGenerator addGen = new AddressGenerator();
		Address toAddress = addGen.generateAndSaveNewAddress();
		Address fromAddress = addGen.generateAndSaveNewAddress();
		Address otherAddress = addGen.generateAndSaveNewAddress();
		
		// Generate a Pubkey object for all 3 addresses. 
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey toPubkey = pubGen.generateAndSaveNewPubkey(toAddress);
		Pubkey fromPubkey = pubGen.generateAndSaveNewPubkey(fromAddress);
		Pubkey otherPubkey = pubGen.generateAndSaveNewPubkey(otherAddress);
		
		// Generate some new encrypted msgs between the 'to address' and the 'from address'.
		// These are designated as the 'valid' messages, because they are destined for the 'to address'
		OutgoingMessageProcessor outProc = new OutgoingMessageProcessor();
		ArrayList<Payload> validMsgs = new ArrayList<Payload>();
		int messageCounter = 0;
		for (int i = 0; i < NUMBER_OF_VALID_MESSAGES; i++)
		{
			String messageSubject = VALID_MESSAGE_SUBJECT + messageCounter;
			String messageBody = VALID_MESSAGE_BODY + messageCounter;
			
			Message message = new Message();
			message.setToAddress(toAddress.getAddress());
			message.setFromAddress(fromAddress.getAddress());
			message.setSubject(messageSubject);
			message.setBody(messageBody);
			
			Log.i(TAG, "Created message with subject '" + messageSubject + "' and body '" + messageBody + "'");
			
			Payload msgPayload = outProc.processOutgoingMessage(message, toPubkey, true, TEST_MSG_TIME_TO_LIVE);
			validMsgs.add(msgPayload);
			
			messageCounter ++;
		}
		
		// Generate some new encrypted msgs between the 'other address' and the 'from address'
		// These are designated as the 'invalid' messages, because they are destined for the 'other address'
		ArrayList<Payload> invalidMsgs = new ArrayList<Payload>();
		messageCounter = 0;
		for (int i = 0; i < NUMBER_OF_INVALID_MESSAGES; i++)
		{
			String messageSubject = INVALID_MESSAGE_SUBJECT + messageCounter;
			String messageBody = INVALID_MESSAGE_BODY + messageCounter;
			
			Message message = new Message();
			message.setToAddress(otherAddress.getAddress());
			message.setFromAddress(fromAddress.getAddress());
			message.setSubject(messageSubject);
			message.setBody(messageBody);
			
			Log.i(TAG, "Created message with subject '" + messageSubject + "' and body '" + messageBody + "'");
			
			Payload msgPayload = outProc.processOutgoingMessage(message, otherPubkey, true, TEST_MSG_TIME_TO_LIVE);
			invalidMsgs.add(msgPayload);
			
			messageCounter ++;
		}
		
		// Create an ArrayList containing both the 'valid' and 'invalid' msgs and randomize its order
		ArrayList<Payload> allMsgs = new ArrayList<Payload>();
		allMsgs.addAll(validMsgs);
		allMsgs.addAll(invalidMsgs);
		Collections.shuffle(allMsgs);
		
		// Before we attempt decryption, delete the 'other' address and pubkey from our database
		addProv.deleteAddress(otherAddress);
		addProv.deleteAddress(fromAddress);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(otherPubkey);
		
		// Set up an ArrayList to contain the timing data collected
		ArrayList<Long> times = new ArrayList<Long>();
		 		
		// Decrypt the matching msgs
 		IncomingMessageProcessor inProc = new IncomingMessageProcessor();
 		for (Payload p : allMsgs)
 		{
 	 		// Set up the timing variables
 			long startTime = 0;
 			long endTime = 0;
 			long timeTaken = 0;
 			
 			//----------------------------------BEGIN TIMED TEST----------------------------------------------
 			startTime = System.nanoTime();
 			
 			Message decryptedMessage = inProc.processReceivedMsg(p);
 			
 			endTime = System.nanoTime();
 			//----------------------------------END TIMED TEST----------------------------------------------
 			
 			if (decryptedMessage != null)
 			{
 	 			Log.i(TAG, "Decrypted message subject:      " + decryptedMessage.getSubject());
 	 			Log.i(TAG, "Decrypted message body:         " + decryptedMessage.getSubject());
 	 			Log.i(TAG, "Decrypted message to address:   " + decryptedMessage.getToAddress());
 	 			Log.i(TAG, "Decrypted message from address: " + decryptedMessage.getFromAddress());
 	 			
 	 			assertTrue(toAddress.getAddress().equals(decryptedMessage.getToAddress()));
 	 			assertTrue(fromAddress.getAddress().equals(decryptedMessage.getFromAddress()));
 			}
 			
 			timeTaken = endTime - startTime;
 			long timeTakenMilliseconds = timeTaken / 1000000;
 			Log.i(TAG, "Time taken in millisecods:          " + timeTakenMilliseconds);
 			times.add(Long.valueOf(timeTakenMilliseconds));
 		}
 		
 		// Get the average time taken to process each msg
 		long sumOfTimes = 0;
 		for (Long l : times)
 		{
 			sumOfTimes += l;
 		}
 		long averageTime = sumOfTimes / times.size();
 		Log.i(TAG, "Average time taken in milliseconds: " + averageTime);
 		
		// Cleaning up - delete the remaining addresses and pubkeys we created from the database
		addProv.deleteAddress(toAddress);
		addProv.deleteAddress(fromAddress);
		
		pubProv.deletePubkey(toPubkey);
		pubProv.deletePubkey(fromPubkey);
	}
}
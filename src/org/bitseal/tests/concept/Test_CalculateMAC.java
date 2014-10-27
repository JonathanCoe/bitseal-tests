package org.bitseal.tests.concept;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;

import org.bitseal.core.App;
import org.bitseal.core.IncomingMessageProcessor;
import org.bitseal.core.OutgoingMessageProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.KeyConverter;
import org.bitseal.crypt.PubkeyGenerator;
import org.bitseal.crypt.SHA256;
import org.bitseal.data.Address;
import org.bitseal.data.EncryptedPayload;
import org.bitseal.data.Message;
import org.bitseal.data.Payload;
import org.bitseal.data.Pubkey;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.PubkeyProvider;
import org.bitseal.util.ArrayCopier;
import org.bitseal.util.ByteFormatter;
import org.bitseal.util.ByteUtils;
import org.spongycastle.jce.interfaces.ECPrivateKey;
import org.spongycastle.jce.interfaces.ECPublicKey;
import org.spongycastle.math.ec.ECPoint;

import android.util.Log;

/**
 * This test attempts to estimate how long it takes for a
 * server to calculate a MAC value for a given ciphertext and key_m
 * and then compare it to the original MAC<br><br>
 * 
 * Note: In real use, this operation would be done by a server, and thus
 * might well run MUCH faster than it would on a smartphone or similar<br><br>
 * 
 * See: https://bitmessage.org/forum/index.php?topic=3931.msg8347#msg8347<br><br>
 * 
 * <b>NOTE:</b> Tests in the 'concept' package are for experimentation
 * related to Protocol Version 3, not for checking the correctness or
 * performance of Bitseal. 
 * 
 * @author Jonathan Coe
 */
public class Test_CalculateMAC extends TestCase
{
	private static final String VALID_MESSAGE_SUBJECT = "Valid message subject ";
	private static final String VALID_MESSAGE_BODY = "Valid message body ";
	private static final int NUMBER_OF_VALID_MESSAGES = 1;
	
	private static final String INVALID_MESSAGE_SUBJECT = "Invalid message subject ";
	private static final String INVALID_MESSAGE_BODY = "Invalid message body ";
	private static final int NUMBER_OF_INVALID_MESSAGES = 2;
	
	private static final long TEST_MSG_TIME_TO_LIVE = 600;
	
	private static final String TAG = "TEST_CALCULATE_MAC";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testCalculateMac()
	{
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
		
		// Extract the public key R values from the msg payloads
		ArrayList<ECPublicKey> rValues = new ArrayList<ECPublicKey>();
 		for (Payload p : allMsgs)
		{
			// Skip over the pow nonce, time, and stream number
 			byte[] fullPayload = p.getPayload();
 			byte[] encryptedPayload = ArrayCopier.copyOfRange(fullPayload, 17, fullPayload.length);
			
			// Parse the data from the payload
 			ECPublicKey R = ExtraCryptMethods.extractPublicKeyR(encryptedPayload);
			
			rValues.add(R);
		}

 		
 		
 		
 		
 		
 		
 		// Now we move to the first operations that would be performed by the client:
 		// For each R value, calculate the key_m value (using the private encryption key of the 'to address')
 		byte[] keyMValues = new byte[0];
 		Log.i(TAG, "About to process " + rValues.size() + " R values");
 		for (ECPublicKey R : rValues)
 		{
 			// Create the ECPrivateKey object that we will use to calcualate the key_m
 			KeyConverter keyConv = new KeyConverter();
 			ECPrivateKey k = keyConv.decodePrivateKeyFromWIF(toAddress.getPrivateEncryptionKey());
 			
 			// Do a point multiplication to get the ECPoint that they key_m will be derived from
 			ECPoint point = R.getQ().multiply(k.getD());

 			// Calculate the key_m value
 			byte[] key_m = ExtraCryptMethods.calculateKeyM(point);
 			
 			keyMValues = ByteUtils.concatenateByteArrays(keyMValues, key_m);
 		}

 		
 		
 		
 		

 		// Now we move back to work done by the server:
 		// For every key_m value, use the corresponding R value to identify the msg it relates to and 
 		// then use the key_m value to calculate a mac for the msgs's ciphertext. Then compare the 
 		// calculated mac to the original to see if they match
 		int numberOfKeyMValues = keyMValues.length / 32;
 		Log.i(TAG, "Number of key_m values:    " + numberOfKeyMValues);
 		Log.i(TAG, "Number of of msg payloads: " + allMsgs.size());
 		int matchingMsgs = 0;
 		int counter = 0;
 		ArrayList<Payload> matchingPayloads = new ArrayList<Payload>();
 		
 		// Loop through all the msgs and check for any matches
 		ArrayList<Long> times = new ArrayList<Long>();
 		int readPosition = 0;
		for (Payload p : allMsgs)
		{
 			long startTime = 0;
 			long endTime = 0;
 			long timeTaken = 0;
			
			//----------------------------------BEGIN TIMED TEST----------------------------------------------
 			startTime = System.nanoTime();
			
			byte[] key_m = ArrayCopier.copyOfRange(keyMValues, readPosition, readPosition + 32);
			readPosition += 32;
			
			// Skip over the pow nonce, time, and stream number
 			byte[] fullPayload = p.getPayload();
 			byte[] encryptedPayload = ArrayCopier.copyOfRange(fullPayload, 17, fullPayload.length);
			EncryptedPayload encPay = ExtraCryptMethods.parseEncryptedPayload(encryptedPayload);
			byte[] cipherText = encPay.getCipherText();
			byte[] actualMac = encPay.getMac();
			
			// Calculate the mac for the cipher text and the supplied key_m value
			byte[] calculatedMac = SHA256.hmacSHA256(cipherText, key_m);
			
			if (Arrays.equals(actualMac, calculatedMac))
			{
				Log.i(TAG, "Found a matching message!");
 				Log.i(TAG, "Calculated MAC " + counter + ": " + ByteFormatter.byteArrayToHexString(calculatedMac));
 				Log.i(TAG, "Actual MAC " + counter + ":     " + ByteFormatter.byteArrayToHexString(actualMac));
 				Log.i(TAG, "   ");
				matchingMsgs ++;
				p.setRelatedAddressId(toAddress.getId());
				matchingPayloads.add(p);
			}
			counter ++;
			
 			endTime = System.nanoTime();
 			//----------------------------------END TIMED TEST----------------------------------------------
 			
 			timeTaken = endTime - startTime;
 			long timeTakenMilliseconds = timeTaken / 1000000;
 			Log.i(TAG, "Time taken in millisecods:          " + timeTakenMilliseconds);
 			times.add(Long.valueOf(timeTakenMilliseconds));
 		}
 		
 		Log.i(TAG, "Number of MAC values calculated: " + counter);
 		Log.i(TAG, "Found a total of " + matchingMsgs + " matching msgs");
 		assertEquals(NUMBER_OF_VALID_MESSAGES, matchingMsgs);
 		
 		// Get the average time taken to calculate and compare the mac
 		long sumOfTimes = 0;
 		for (Long l : times)
 		{
 			sumOfTimes += l;
 		}
 		long averageTime = sumOfTimes / times.size();
 		Log.i(TAG, "Average time taken in milliseconds: " + averageTime);
 		
 		
 		

 		// Decrypt the matching msgs
 		IncomingMessageProcessor inProc = new IncomingMessageProcessor();
 		for (Payload p : matchingPayloads)
 		{
 			Message decryptedMessage = inProc.processReceivedMsg(p);
 			Log.i(TAG, "Decrypted message subject:      " + decryptedMessage.getSubject());
 			Log.i(TAG, "Decrypted message body:         " + decryptedMessage.getSubject());
 			Log.i(TAG, "Decrypted message to address:   " + decryptedMessage.getToAddress());
 			Log.i(TAG, "Decrypted message from address: " + decryptedMessage.getFromAddress());
 			
 			assertTrue(toAddress.getAddress().equals(decryptedMessage.getToAddress()));
 			assertTrue(fromAddress.getAddress().equals(decryptedMessage.getFromAddress()));
 		}

 		
		// Cleaning up - delete the addresses and pubkeys we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(toAddress);
		addProv.deleteAddress(fromAddress);
		addProv.deleteAddress(otherAddress);
		
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(toPubkey);
		pubProv.deletePubkey(fromPubkey);
		pubProv.deletePubkey(otherPubkey);
	}
}
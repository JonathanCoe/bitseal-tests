package org.bitseal.tests.concept;

import java.util.ArrayList;
import java.util.Collections;

import junit.framework.TestCase;

import org.bitseal.core.App;
import org.bitseal.core.OutgoingMessageProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.KeyConverter;
import org.bitseal.crypt.PubkeyGenerator;
import org.bitseal.data.Address;
import org.bitseal.data.Message;
import org.bitseal.data.Payload;
import org.bitseal.data.Pubkey;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.PubkeyProvider;
import org.bitseal.util.ArrayCopier;
import org.bitseal.util.ByteUtils;
import org.spongycastle.jce.interfaces.ECPrivateKey;
import org.spongycastle.jce.interfaces.ECPublicKey;
import org.spongycastle.math.ec.ECPoint;

import android.util.Log;

/**
 * This test attempts to determine how long it takes for the
 * Android client to calculate a key_m value for a given set
 * of R and k values. <br><br>
 * 
 * See: https://bitmessage.org/forum/index.php?topic=3931.msg8347#msg8347<br><br>
 * 
 * <b>NOTE:</b> Tests in the 'concept' package are for experimentation
 * related to Protocol Version 3, not for checking the correctness or
 * performance of Bitseal. 
 * 
 * @author Jonathan Coe
 */
public class Test_CalculateKeyM extends TestCase
{	
	private static final int NUMBER_OF_MESSAGES = 3; // This is effectively the number of times to run the test
	
	private static final String MESSAGE_SUBJECT = "The message subject ";
	private static final String MESSAGE_BODY = "The message body ";
	
	private static final String TAG = "TEST_CALCULATE_KEY_M";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testCalculateKeyM()
	{
		// Generate a 'to address' and a 'from address'
		AddressGenerator addGen = new AddressGenerator();
		Address toAddress = addGen.generateAndSaveNewAddress();
		Address fromAddress = addGen.generateAndSaveNewAddress();
		
		// Generate a Pubkey object for both addresses. 
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey toPubkey = pubGen.generateAndSaveNewPubkey(toAddress);
		Pubkey fromPubkey = pubGen.generateAndSaveNewPubkey(fromAddress);
		
		// Generate some new msgs between the 'to address' and the 'from address'.
		OutgoingMessageProcessor outProc = new OutgoingMessageProcessor();
		ArrayList<Payload> msgs = new ArrayList<Payload>();
		int messageCounter = 0;
		for (int i = 0; i < NUMBER_OF_MESSAGES; i++)
		{
			String messageSubject = MESSAGE_SUBJECT + messageCounter;
			String messageBody = MESSAGE_BODY + messageCounter;
			
			Message message = new Message();
			message.setToAddress(toAddress.getAddress());
			message.setFromAddress(fromAddress.getAddress());
			message.setSubject(messageSubject);
			message.setBody(messageBody);
			
			Log.i(TAG, "Created message with subject '" + messageSubject + "' and body '" + messageBody + "'");
			
			Payload msgPayload = outProc.processOutgoingMessage(message, toPubkey, true);
			msgs.add(msgPayload);
			
			messageCounter ++;
		}
			
		// Create an ArrayList containing the generated msgs and randomize its order
		ArrayList<Payload> allMsgs = new ArrayList<Payload>();
		allMsgs.addAll(msgs);
		Collections.shuffle(allMsgs);
		
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
 		
 		// For each R value, calculate the key_m value (using the private encryption key of the 'to address')
 		byte[] keyMValues = new byte[0];
 		Log.i(TAG, "About to process " + rValues.size() + " R values");
 		ArrayList<Long> times = new ArrayList<Long>();
 		for (ECPublicKey R : rValues)
 		{
 			// Create the ECPrivateKey object that we will use to calcualate the key_m
 			KeyConverter keyConv = new KeyConverter();
 			ECPrivateKey k = keyConv.decodePrivateKeyFromWIF(toAddress.getPrivateEncryptionKey());
 			
 			// Initialize time values
 			long startTime = 0;
 			long endTime = 0;
 			long timeTaken = 0;
 			
 			//----------------------------------BEGIN TIMED TEST----------------------------------------------
 			startTime = System.nanoTime();
 			
 			// Do a point multiplication to get the ECPoint that they key_m will be derived from
 			ECPoint point = R.getQ().multiply(k.getD());

 			// Calculate the key_m value
			byte[] key_m = ExtraCryptMethods.calculateKeyM(point);
 			keyMValues = ByteUtils.concatenateByteArrays(keyMValues, key_m);
 			
 			endTime = System.nanoTime();
 			//----------------------------------END TIMED TEST----------------------------------------------
 			
 			timeTaken = endTime - startTime;
 			long timeTakenMilliseconds = timeTaken / 1000000;
 			Log.i(TAG, "Time taken in millisecods:          " + timeTakenMilliseconds);
 			times.add(Long.valueOf(timeTakenMilliseconds));
 		}
 		
 		// Get the average time taken to calculate the key_m
 		long sumOfTimes = 0;
 		for (Long l : times)
 		{
 			sumOfTimes += l;
 		}
 		long averageTime = sumOfTimes / times.size();
 		Log.i(TAG, "Average time taken in milliseconds: " + averageTime);
 		
		// Cleaning up - delete the addresses and pubkeys we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(toAddress);
		addProv.deleteAddress(fromAddress);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(toPubkey);
		pubProv.deletePubkey(fromPubkey);
	}
}
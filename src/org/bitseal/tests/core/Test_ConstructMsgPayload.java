package org.bitseal.tests.core;

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
import org.bitseal.pow.POWProcessor;
import org.bitseal.util.ArrayCopier;
import org.bitseal.util.ByteFormatter;
import org.bitseal.util.ByteUtils;
import org.bitseal.util.VarintEncoder;

import android.util.Log;

/** 
 * Tests the method OutgoingMessageProcessor.processOutgoingMessage()
 * 
 * @author Jonathan Coe
**/
public class Test_ConstructMsgPayload extends TestCase
{
	private static final String TEST_SUBJECT = "I like cake, and it is imperative that you do too.";
	private static final String TEST_BODY = "I saw you, flouncing around with your Zizek tote bag";
	
	private static final String TAG = "TEST_CONSTRUCT_MSG_PAYLOAD";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testConstructMsgPayload()
	{
		// Create a 'to address' and a 'from address'
		AddressGenerator addGen = new AddressGenerator();
		Address toAddress = addGen.generateAndSaveNewAddress();
		Address fromAddress = addGen.generateAndSaveNewAddress();
		
		// Generate pubkeys for both addresses
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey toPubkey = pubGen.generateAndSaveNewPubkey(toAddress);
		Pubkey fromPubkey = pubGen.generateAndSaveNewPubkey(fromAddress);
		
		// Create a new message - this will be the basis for the payload
		Message message = new Message();
		message.setBelongsToMe(true);
		message.setToAddress(toAddress.getAddress());
		message.setFromAddress(fromAddress.getAddress());
		message.setSubject(TEST_SUBJECT);
		message.setBody(TEST_BODY);
		
		// Generate the encrypted msg payload
		OutgoingMessageProcessor outProc = new OutgoingMessageProcessor();
		Payload msgPayload = outProc.processOutgoingMessage(message, toPubkey, true);
		msgPayload.setRelatedAddressId(toAddress.getId());
		byte[] payloadBytes = msgPayload.getPayload();
		String payloadHex = ByteFormatter.byteArrayToHexString(payloadBytes);
		Log.i(TAG, "Msg payload:                " + payloadHex);
		
		// Extract the pow nonce, time, stream number, and encrypted data from the payload
		byte[] powNonceBytes = ArrayCopier.copyOfRange(payloadBytes, 0, 8);
		byte[] timeBytes = ArrayCopier.copyOfRange(payloadBytes, 8, 16);
		byte[] streamNumberBytes = ArrayCopier.copyOfRange(payloadBytes, 16, 17);
		byte[] encryptedDataBytes = ArrayCopier.copyOfRange(payloadBytes, 17, payloadBytes.length);
		
		// Log the hex values of those byte arrays
		String powNonceHex = ByteFormatter.byteArrayToHexString(powNonceBytes);
		String timeHex = ByteFormatter.byteArrayToHexString(timeBytes);
		String streamNumberHex = ByteFormatter.byteArrayToHexString(streamNumberBytes);
		String encryptedDataHex = ByteFormatter.byteArrayToHexString(encryptedDataBytes);
		Log.i(TAG, "Msg payload POW nonce:      " + powNonceHex);
		Log.i(TAG, "Msg payload time:           " + timeHex);
		Log.i(TAG, "Msg payload stream number:  " + streamNumberHex);
		Log.i(TAG, "Msg payload encrypted data: " + encryptedDataHex);
		
		// Check the pow nonce
		long powNonce = ByteUtils.bytesToLong(powNonceBytes);
		POWProcessor powProc = new POWProcessor();
		byte[] payloadWithoutNonce = ArrayCopier.copyOfRange(payloadBytes, 8, payloadBytes.length); // The pow nonce is calculated for the rest of the payload
		boolean powValid = powProc.checkPOW(payloadWithoutNonce, powNonce, toPubkey.getNonceTrialsPerByte(), toPubkey.getExtraBytes()); // Dividing the nonceTrialsPerByte value by 320 gives us the difficulty factor
		assertTrue(powValid);
		
		// Check the time value
		long time = ByteUtils.bytesToLong(timeBytes);
		long currentTime = System.currentTimeMillis() / 1000; // Gets the current time in seconds
    	long maxTime = currentTime + 1800; // 1800 seconds equals 30 minutes - allows plenty of time for POW to be completed
    	long minTime = currentTime - 1800;
		assertTrue(time < maxTime);
		assertTrue(time > minTime);
		
		// Check the stream number
		int streamNumber = (int) (VarintEncoder.decode(streamNumberBytes))[0];
		int expectedStreamNumber = toPubkey.getStreamNumber();
		assertEquals(streamNumber, expectedStreamNumber);
				
		// Process the msg, giving us the decrypted message data
		IncomingMessageProcessor inProc = new IncomingMessageProcessor();
		Message decryptedMessage = inProc.processReceivedMsg(msgPayload);
		
		// Extract the message data
		String toAddressString = decryptedMessage.getToAddress();
		String fromAddressString = decryptedMessage.getFromAddress();
		String subject = decryptedMessage.getSubject();
		String body = decryptedMessage.getBody();
		
		Log.i(TAG, "Decrypted message to address:        " + toAddressString);
		Log.i(TAG, "Decrypted message from address:      " + fromAddressString);
		Log.i(TAG, "Decrypted message subject:           " + subject);
		Log.i(TAG, "Decrypted message body:              " + body);
		
		// Check the message data
		assertTrue(toAddressString.equals(toAddress.getAddress()));
		assertTrue(fromAddressString.equals(fromAddress.getAddress()));
		assertTrue(subject.equals(TEST_SUBJECT));
		assertTrue(body.equals(TEST_BODY));
		
		// Cleaning up - delete the addresses and pubkeys we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(toAddress);
		addProv.deleteAddress(fromAddress);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(toPubkey);
		pubProv.deletePubkey(fromPubkey);
	}
}
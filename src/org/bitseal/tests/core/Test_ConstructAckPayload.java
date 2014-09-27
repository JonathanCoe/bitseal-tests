package org.bitseal.tests.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.Arrays;

import junit.framework.TestCase;

import org.bitseal.core.App;
import org.bitseal.core.OutgoingMessageProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.PubkeyGenerator;
import org.bitseal.crypt.SHA512;
import org.bitseal.data.Address;
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
 * Tests the method OutgoingMessageProcessor.generateFullAckMsg()<br><br>
 * 
 * Note: This test uses reflection to access one or more private methods
 * 
 * @author Jonathan Coe
**/
public class Test_ConstructAckPayload extends TestCase
{
	/** A magic hexadecimal value used by Bitmessage to identify network packets. See https://bitmessage.org/wiki/Protocol_specification#Message_structure */
	private static final String BITMESSAGE_MAGIC_IDENTIFIER = "E9BEB4D9";
	
	/** The Bitmessage msg command in hex */
	private static final String BITMESSAGE_MSG_COMMAND_HEX = "6d7367000000000000000000";
	
	/** See https://bitmessage.org/wiki/Proof_of_work for an explanation of these values **/
	private static final long DEFAULT_NONCE_TRIALS_PER_BYTE = 320;
	private static final long DEFAULT_EXTRA_BYTES = 14000;
	
	private static final String TAG = "TEST_CONSTRUCT_ACK_PAYLOAD";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testConstructAckPayload() throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException
	{
		// Generate a new address
		AddressGenerator addGen = new AddressGenerator();
		Address toAddress = addGen.generateAndSaveNewAddress();
		
		// Generate a pubkey for the 'to address'
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey toPubkey = pubGen.generateAndSaveNewPubkey(toAddress);
		
		// Calculate the ack data (32 random bytes)
		byte[] generatedAckData = new byte[32];
		new SecureRandom().nextBytes(generatedAckData);
		
		// Generate the full ack msg, using reflection to access the private method generateFullAckMsg()
		OutgoingMessageProcessor outProc = new OutgoingMessageProcessor();	
		Method method1 = OutgoingMessageProcessor.class.getDeclaredMethod("generateFullAckMsg", byte[].class, int.class, int.class);
		method1.setAccessible(true);
		byte[] fullAckMsg = (byte[]) method1.invoke(outProc, generatedAckData, toPubkey.getStreamNumber(), toPubkey.getNonceTrialsPerByte());
		String fullAckMsgHex = ByteFormatter.byteArrayToHexString(fullAckMsg);
		Log.i(TAG, "Full ack msg in hex:      " + fullAckMsgHex);
		
		// Check the magic identifier
		int readPosition = 0;
		byte[] magicIdentifier = ArrayCopier.copyOfRange(fullAckMsg, readPosition, readPosition + 4);
		readPosition += 4;
		String magicIdentifierHex = ByteFormatter.byteArrayToHexString(magicIdentifier);
		Log.i(TAG, "Magic identifier in hex:  " + magicIdentifierHex);
		assertTrue(magicIdentifierHex.equalsIgnoreCase(BITMESSAGE_MAGIC_IDENTIFIER));
		
		// Check the command string
		byte[] commandStringBytes = ArrayCopier.copyOfRange(fullAckMsg, readPosition, readPosition + 12);
		readPosition += 12;
		String commandStringHex = ByteFormatter.byteArrayToHexString(commandStringBytes);
		Log.i(TAG, "Command String:           " + commandStringHex);
		assertTrue(commandStringHex.equals(BITMESSAGE_MSG_COMMAND_HEX));
		
		// Extract the payload of the full ack msg so that we can check the header values
		byte[] payload = ArrayCopier.copyOfRange(fullAckMsg, 24, fullAckMsg.length);
		int expectedPayloadLength = payload.length;
		byte[] expectedPayloadChecksum = SHA512.sha512(payload, 4);
		
		// Check the payload length
		byte[] payloadLengthBytes = ArrayCopier.copyOfRange(fullAckMsg, readPosition, readPosition + 4);
		readPosition += 4;
		String payloadLengthHex = ByteFormatter.byteArrayToHexString(payloadLengthBytes);
		Log.i(TAG, "Payload length in hex:    " + payloadLengthHex);
		int payloadLength = ByteUtils.bytesToInt(payloadLengthBytes);
		assertEquals(payloadLength, expectedPayloadLength);
		
		// Check the payload checksum
		byte[] payloadChecksum = ArrayCopier.copyOfRange(fullAckMsg, readPosition, readPosition + 4);
		readPosition += 4;
		String payloadChecksumHex = ByteFormatter.byteArrayToHexString(payloadChecksum);
		Log.i(TAG, "Payload checksum in hex:  " + payloadChecksumHex);
		assertTrue(Arrays.equals(payloadChecksum, expectedPayloadChecksum));
		
		// Check the pow nonce
		byte[] powNonceBytes = ArrayCopier.copyOfRange(fullAckMsg, readPosition, readPosition + 8);
		readPosition += 8;
		long powNonce = ByteUtils.bytesToLong(powNonceBytes);
		Log.i(TAG, "POW nonce:                " + powNonce);
		POWProcessor powProc = new POWProcessor();
		byte[] payloadWithoutNonce = ArrayCopier.copyOfRange(payload, 8, payload.length);
		assertTrue(powProc.checkPOW(payloadWithoutNonce, powNonce, DEFAULT_NONCE_TRIALS_PER_BYTE, DEFAULT_EXTRA_BYTES));
		
		// Check the embedded time
		long time = ByteUtils.bytesToInt((ArrayCopier.copyOfRange(fullAckMsg, readPosition, readPosition + 4)));
		if (time == 0) // Need to check whether 4 or 8 byte time has been used
		{
			time = ByteUtils.bytesToLong((ArrayCopier.copyOfRange(fullAckMsg, readPosition, readPosition + 8)));
			readPosition += 8;
		}
		else
		{
			readPosition += 4;
		}
		long currentTime = System.currentTimeMillis() / 1000; // Gets the current time in seconds
    	long maxTime = currentTime + 1800; // 1800 seconds equals 30 minutes - allows plenty of time for POW to be completed
    	long minTime = currentTime - 1800;
		assertTrue(time < maxTime);
		assertTrue(time > minTime);
		
		// Check the stream number
		long[] decoded = VarintEncoder.decode(ArrayCopier.copyOfRange(fullAckMsg, readPosition, readPosition + 9)); // Take 9 bytes, the maximum length for an encoded var_int
		int streamNumber = (int) decoded[0]; // Get the var_int encoded value
		readPosition += (int) decoded[1]; // Find out how many bytes the var_int was in length and adjust the read position accordingly
		int expectedStreamNumber = toPubkey.getStreamNumber();
		assertEquals(streamNumber, expectedStreamNumber);
		
		// Check the ackData
		byte[] ackData = ArrayCopier.copyOfRange(fullAckMsg, readPosition, fullAckMsg.length);
		String ackDataHex = ByteFormatter.byteArrayToHexString(ackData);
		Log.i(TAG, "Ack data in hex:          " + ackDataHex);
		assertEquals(ackData.length, 32);
		
		// Cleaning up - delete the address and pubkey we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(toAddress);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(toPubkey);
	}
}
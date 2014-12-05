package org.bitseal.tests.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.Arrays;

import junit.framework.TestCase;

import org.bitseal.core.App;
import org.bitseal.core.ObjectProcessor;
import org.bitseal.core.OutgoingMessageProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.PubkeyGenerator;
import org.bitseal.crypt.SHA512;
import org.bitseal.data.Address;
import org.bitseal.data.BMObject;
import org.bitseal.data.Message;
import org.bitseal.data.Pubkey;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.DatabaseContentProvider;
import org.bitseal.database.PubkeyProvider;
import org.bitseal.util.ArrayCopier;
import org.bitseal.util.ByteFormatter;
import org.bitseal.util.ByteUtils;

import android.os.SystemClock;
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
	private static final String BITMESSAGE_OBJECT_COMMAND_HEX = "6f626a656374000000000000";
	
	private static final int STANDARD_ACK_DATA_LENGTH = 32;
	
	private static final long TEST_ACK_TIME_TO_LIVE = 600;
	
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
		// Initialize the database
		DatabaseContentProvider.openDatabase();
		SystemClock.sleep(10000); // We have to allow some time for the database to be opened
		
		// Generate a new address
		AddressGenerator addGen = new AddressGenerator();
		Address toAddress = addGen.generateAndSaveNewAddress();
		
		// Generate a pubkey for the 'to address'
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey toPubkey = pubGen.generateAndSaveNewPubkey(toAddress);
		
		// Calculate the ack data (32 random bytes)
		byte[] generatedAckData = new byte[32];
		new SecureRandom().nextBytes(generatedAckData);
		
		// Create a dummy Message object so that we can use the "generateFullAckMsg" method
		Message dummyMessage = new Message();
		
		// Generate the full ack msg, using reflection to access the private method generateFullAckMsg()
		OutgoingMessageProcessor outProc = new OutgoingMessageProcessor();	
		Method method1 = OutgoingMessageProcessor.class.getDeclaredMethod("generateFullAckMessage", Message.class, byte[].class, int.class, boolean.class, long.class);
		method1.setAccessible(true);
		byte[] fullAckMsg = (byte[]) method1.invoke(outProc, dummyMessage, generatedAckData, toPubkey.getStreamNumber(), true, TEST_ACK_TIME_TO_LIVE);
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
		assertTrue(commandStringHex.equals(BITMESSAGE_OBJECT_COMMAND_HEX));
		
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
		assertEquals(expectedPayloadLength, payloadLength);
		
		// Check the payload checksum
		byte[] payloadChecksum = ArrayCopier.copyOfRange(fullAckMsg, readPosition, readPosition + 4);
		readPosition += 4;
		String payloadChecksumHex = ByteFormatter.byteArrayToHexString(payloadChecksum);
		Log.i(TAG, "Payload checksum in hex:  " + payloadChecksumHex);
		assertTrue(Arrays.equals(payloadChecksum, expectedPayloadChecksum));
		
		// Parse the standard Bitmessage object data
		byte[] ackObjectBytes = ArrayCopier.copyOfRange(fullAckMsg, readPosition, fullAckMsg.length);
		BMObject ackObject = new ObjectProcessor().parseObject(ackObjectBytes);
		
		// Check the ackData
		byte[] ackData = ackObject.getPayload();
		String ackDataHex = ByteFormatter.byteArrayToHexString(ackData);
		Log.i(TAG, "Ack data in hex:          " + ackDataHex);
		assertEquals(STANDARD_ACK_DATA_LENGTH, ackData.length);
		
		// Cleaning up - delete the address and pubkey we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(toAddress);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(toPubkey);
	}
}
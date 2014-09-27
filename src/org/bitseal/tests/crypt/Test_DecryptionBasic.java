package org.bitseal.tests.crypt;

import java.math.BigInteger;

import junit.framework.TestCase;

import org.bitseal.core.App;
import org.bitseal.core.IncomingMessageProcessor;
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
import org.bitseal.util.ByteFormatter;

import android.util.Log;

/**
 * A simple test that runs through all the code necessary
 * to decrypt an EncryptedMsg.
 * 
 * @author Jonathan Coe
 */
public class Test_DecryptionBasic extends TestCase
{
	private static final String TAG = "TEST_DECRYPTION_BASIC";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testDecryptionBasic()
	{
		// Generate a 'to' address and a 'from' address
		AddressGenerator addGen = new AddressGenerator();
		Address toAddress = addGen.generateAndSaveNewAddress();
		Address fromAddress = addGen.generateAndSaveNewAddress();
		
		KeyConverter keyConv = new KeyConverter();
		BigInteger toAddressPrivateEncryptionKeyD = keyConv.decodePrivateKeyFromWIF(toAddress.getPrivateEncryptionKey()).getD();
		BigInteger fromAddressPrivateEncryptionKeyD = keyConv.decodePrivateKeyFromWIF(fromAddress.getPrivateEncryptionKey()).getD();
		
		Log.i(TAG, "To address private encryption key D value:   " + toAddressPrivateEncryptionKeyD);
		Log.i(TAG, "From address private encryption key D value: " + fromAddressPrivateEncryptionKeyD);
		
		// Generate a Pubkey object for the 'to' address and 'from' address
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey toPubkey = pubGen.generateAndSaveNewPubkey(toAddress);
		Pubkey fromPubkey = pubGen.generateAndSaveNewPubkey(fromAddress);
		
		// Generate a new Message object, as if it had been created by the user of the app through the UI
		Message message = new Message();
		message.setBelongsToMe(true);
		message.setToAddress(toAddress.getAddress());
		message.setFromAddress(fromAddress.getAddress());
		message.setSubject("An extremely interesting subject line");
		message.setBody("I hope that this message is suitably loquacious");
		
		// Process the Message, giving us the byte[] of msg data ready to be sent over the network
		OutgoingMessageProcessor outMsgProc = new OutgoingMessageProcessor();
		Payload msgToSend = outMsgProc.processOutgoingMessage(message, toPubkey, true);
		msgToSend.setRelatedAddressId(toAddress.getId());
		Log.i(TAG, "msgToSend :                                 " + ByteFormatter.byteArrayToHexString(msgToSend.getPayload()));
				
		// Decrypt the msg Payload
		IncomingMessageProcessor inMsgProc = new IncomingMessageProcessor();
		Message decryptedMessage = inMsgProc.processReceivedMsg(msgToSend);
		
		// Take the decrypted message and check if it is correct
		Log.i(TAG, "Decrypted message text: " + decryptedMessage.getBody());
		assertEquals(decryptedMessage.getBody(), "I hope that this message is suitably loquacious");
		
		// Cleaning up - delete the addresses and pubkeys we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(toAddress);
		addProv.deleteAddress(fromAddress);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(toPubkey);
		pubProv.deletePubkey(fromPubkey);
	}
}
package org.bitseal.tests.crypt;

import junit.framework.TestCase;

import org.bitseal.core.App;
import org.bitseal.core.OutgoingMessageProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.PubkeyGenerator;
import org.bitseal.data.Address;
import org.bitseal.data.Message;
import org.bitseal.data.Payload;
import org.bitseal.data.Pubkey;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.DatabaseContentProvider;
import org.bitseal.database.PubkeyProvider;
import org.bitseal.util.ByteFormatter;

import android.os.SystemClock;
import android.util.Log;

/**
 * A simple test that runs through all the code necessary to take a message written by a user and turn it into 
 * and encrypted message that is ready for transmission across the Bitmessage network. <br><br>
 * 
 * This basic test is not particularly rigorous in checking its output, but should at least
 * allow us to detect any crashes.
 * 
 * @author Jonathan Coe
 */
public class Test_EncryptionBasic extends TestCase
{
	private static final long TEST_MSG_TIME_TO_LIVE = 600;
	
	private static final String TAG = "TEST_ENCRYPTION_BASIC";
	
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
	
	public void testEncryptionBasic()
	{
		// Generate a 'to address' and a 'from address'
		AddressGenerator addGen = new AddressGenerator();
		Address toAddress = addGen.generateAndSaveNewAddress();
		Address fromAddress = addGen.generateAndSaveNewAddress();
				
		// Generate pubkeys for both addresses
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey toPubkey = pubGen.generateAndSaveNewPubkey(toAddress);
		Pubkey fromPubkey = pubGen.generateAndSaveNewPubkey(fromAddress);
				
		// Now generate a new Message object, as if it had been created by the user of the app through the UI
		Message message = new Message();
		message.setBelongsToMe(true);
		message.setToAddress(toAddress.getAddress());
		message.setFromAddress(fromAddress.getAddress());
		message.setSubject("An extremely interesting subject line");
		message.setBody("The user wrote a beautiful messsage explaining everything from the meaning of football to the infinitely flexible nature of morality");
		
		// Process the Message, giving us the byte[] of msg data ready to be sent over the network
		OutgoingMessageProcessor outMsgProc = new OutgoingMessageProcessor();
		Payload msgToSend = outMsgProc.processOutgoingMessage(message, toPubkey, true, TEST_MSG_TIME_TO_LIVE);
		
		Log.i(TAG, "msgToSend :                                 " + ByteFormatter.byteArrayToHexString(msgToSend.getPayload()));
		
		// Cleaning up - delete the addresses and pubkeys we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(toAddress);
		addProv.deleteAddress(fromAddress);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(toPubkey);
		pubProv.deletePubkey(fromPubkey);
	}
}
package org.bitseal.tests.crypt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.bitseal.core.App;
import org.bitseal.core.OutgoingMessageProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.KeyConverter;
import org.bitseal.crypt.PubkeyGenerator;
import org.bitseal.crypt.SigProcessor;
import org.bitseal.data.Address;
import org.bitseal.data.Message;
import org.bitseal.data.Pubkey;
import org.bitseal.data.UnencryptedMsg;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.PubkeyProvider;
import org.bitseal.util.ByteFormatter;
import org.spongycastle.jce.interfaces.ECPublicKey;

import android.util.Log;

/**
 * A simple test that runs through all the code necessary to create and verify a signature.
 * This is done for an UnencryptedMsg and for two Pubkeys.<br><br>
 * 
 * Note: This test uses reflection to access one or more private methods
 * 
 * @author Jonathan Coe
 */
public class Test_SignatureBasic extends TestCase
{
	private static final long TEST_MSG_TIME_TO_LIVE = 600;
	
	private static final String TAG = "TEST_SIGNATURE_BASIC";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testSignatureBasic() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		// Generate a 'to' address and a 'from' address
		AddressGenerator addGen = new AddressGenerator();
		Address toAddress = addGen.generateAndSaveNewAddress();
		Address fromAddress = addGen.generateAndSaveNewAddress();
				
		// Generate a Pubkey object for the to address and from address. This process will include the creation
		// of a signature for each of the pubkeys
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey toPubkey = pubGen.generateAndSaveNewPubkey(toAddress);
		Pubkey fromPubkey = pubGen.generateAndSaveNewPubkey(fromAddress);
				
		// Generate a new Message object, as if it had been created by the user of the app through the UI
		Message message = new Message();
		message.setBelongsToMe(true);
		message.setToAddress(toAddress.getAddress());
		message.setFromAddress(fromAddress.getAddress());
		message.setSubject("A rather fanciful subject line");
		message.setBody("Today, five incredible events happened one after the other");
		
		// Process the Message and use it to create an UnencryptedMsg object. This process will include the creation
		// of a signature for the UnencryptedMsg. Use reflection to access the private method constructUnencryptedMsg()
		OutgoingMessageProcessor outMsgProc = new OutgoingMessageProcessor();		
		Method method = OutgoingMessageProcessor.class.getDeclaredMethod("constructUnencryptedMsg", Message.class, Pubkey.class, boolean.class, long.class);
		method.setAccessible(true);
		UnencryptedMsg unencMsg = (UnencryptedMsg) method.invoke(outMsgProc, message, toPubkey, true, TEST_MSG_TIME_TO_LIVE);
		
		// Verify the UnencryptedMsg's signature
		byte[] unencMsgSignature = unencMsg.getSignature();
		Log.i(TAG, "UnencryptedMsg signature in hex: " + ByteFormatter.byteArrayToHexString(unencMsgSignature));
		ECPublicKey publicSigningKey = new KeyConverter().reconstructPublicKey(fromPubkey.getPublicSigningKey());
		SigProcessor sigProc = new SigProcessor();
		byte[] unencmsgSignaturePayload = sigProc.createUnencryptedMsgSignaturePayload(unencMsg);
		boolean sigValid = sigProc.verifySignature(unencmsgSignaturePayload, unencMsgSignature, publicSigningKey);
		if (sigValid == true)
		{
			Log.i(TAG, "UnencryptedMsg signature was valid");
		}
		else
		{
			Log.e(TAG, "UnencryptedMsg signature was NOT valid. Something is wrong!");
		}
		assertTrue(sigValid);
		
		// Verify the signature of the 'to' Pubkey
		byte[] toPubkeySignature = toPubkey.getSignature();
		Log.i(TAG, "toPubkey signature in hex: " + ByteFormatter.byteArrayToHexString(toPubkeySignature));
		ECPublicKey toPubkeySigningPublicKey = new KeyConverter().reconstructPublicKey(toPubkey.getPublicSigningKey());
		sigValid = false; // Reset this to ensure a valid test
		byte[] toPubkeySignaturePayload = sigProc.createPubkeySignaturePayload(toPubkey);
		sigValid = sigProc.verifySignature(toPubkeySignaturePayload, toPubkeySignature, toPubkeySigningPublicKey);
		if (sigValid == true)
		{
			Log.i(TAG, "toPubkey signature was valid");
		}
		else
		{
			Log.e(TAG, "toPubkey signature was NOT valid. Something is wrong!");
		}
		assertTrue(sigValid);
		
		// Verify the signature of the 'from' Pubkey
		byte[] fromPubkeySignature = fromPubkey.getSignature();
		Log.i(TAG, "fromPubkey signature in hex: " + ByteFormatter.byteArrayToHexString(fromPubkeySignature));
		ECPublicKey fromPubkeySigningPublicKey = new KeyConverter().reconstructPublicKey(fromPubkey.getPublicSigningKey());
		sigValid = false; // Reset this to ensure a valid test
		byte[] fromPubkeySignaturePayload = sigProc.createPubkeySignaturePayload(fromPubkey);
		sigValid = sigProc.verifySignature(fromPubkeySignaturePayload, fromPubkeySignature, fromPubkeySigningPublicKey);
		if (sigValid == true)
		{
			Log.i(TAG, "fromPubkey signature was valid");
		}
		else
		{
			Log.e(TAG, "fromPubkey signature was NOT valid. Something is wrong!");
		}
		assertTrue(sigValid);
		
		// Cleaning up - delete the addresses and pubkeys we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(toAddress);
		addProv.deleteAddress(fromAddress);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(toPubkey);
		pubProv.deletePubkey(fromPubkey);
	}
}
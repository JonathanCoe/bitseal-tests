package org.bitseal.tests.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

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

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

/** 
 * Tests the method OutgoingMessageProcessor.constructUnencryptedMsg()<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. <br><br>
 * 
 * Note: This test uses reflection to access one or more private methods
 * 
 * @author Jonathan Coe
**/
public class Test_ConstructUnencryptedMsg extends AndroidTestCase
{
	private static final long TEST_MSG_TIME_TO_LIVE = 600;
	
	private static final String TAG = "TEST_CONSTRUCT_UNENCRYPTED_MSG";
	
	protected void setUp() throws Exception
	{
		super.setUp();
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

        long endTime = SystemClock.elapsedRealtime() + TimeUnit.SECONDS.toMillis(2);

        while (null == context.getApplicationContext())
        {

            if (SystemClock.elapsedRealtime() >= endTime)
            {
                fail();
            }

            SystemClock.sleep(16);
        }
	 }
	
	public void testConstructUnencryptedMsg() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
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
		message.setSubject("Hello there! Please reply ASAP!");
		message.setBody("We must accelerate our plans.");
		
		// Use reflection to test the private method constructUnencryptedMsg()
		OutgoingMessageProcessor outMsgProc = new OutgoingMessageProcessor();	
		Method method = OutgoingMessageProcessor.class.getDeclaredMethod("constructUnencryptedMsg", Message.class, Pubkey.class, boolean.class, long.class);
		method.setAccessible(true);
		UnencryptedMsg unencMsg = (UnencryptedMsg) method.invoke(outMsgProc, message, toPubkey, true, TEST_MSG_TIME_TO_LIVE);
		
		// Check if the ripe hash matches the input
		Log.i(TAG, "Expected ripe hash from unencMsg0: " + ByteFormatter.byteArrayToHexString(toPubkey.getRipeHash()));
		Log.i(TAG, "Actual ripe hash from unencMsg0  : " + ByteFormatter.byteArrayToHexString(unencMsg.getDestinationRipe()));
		assertTrue(Arrays.equals(unencMsg.getDestinationRipe(), toPubkey.getRipeHash()));
		
		// Check if the message text matches the input
		String rawMessage = new String(unencMsg.getMessage());
		String messageSubject = rawMessage.substring(rawMessage.indexOf("Subject:") + 8);
		messageSubject = messageSubject.substring(0, messageSubject.indexOf("\n"));
		String messageBody = rawMessage.substring(rawMessage.lastIndexOf("Body:") + 5);
		
		Log.i(TAG, "Expected message subject: " + "Hello there! Please reply ASAP!");
		Log.i(TAG, "Actual message subject  : " + messageSubject);
		assertEquals(messageSubject, "Hello there! Please reply ASAP!");
		
		Log.i(TAG, "Expected message body: " + "We must accelerate our plans.");
		Log.i(TAG, "Actual message body  : " + messageBody);
		assertEquals(messageBody, "We must accelerate our plans.");
		
		// Check whether the unencryptedMsg's signature is valid
		byte[] signature = unencMsg.getSignature();
		Log.i(TAG, "UnencryptedMsg signature in hex: " + ByteFormatter.byteArrayToHexString(signature));
		ECPublicKey signingPublicKey = new KeyConverter().reconstructPublicKey(fromPubkey.getPublicSigningKey());
		SigProcessor sigProc = new SigProcessor();
		byte[] signaturePayload = sigProc.createUnencryptedMsgSignaturePayload(unencMsg);
		boolean sigValid = sigProc.verifySignature(signaturePayload, signature, signingPublicKey);
		if (sigValid == true)
		{
			Log.i(TAG, "UnencryptedMsg signature was valid");
		}
		else
		{
			Log.e(TAG, "UnencryptedMsg signature was NOT valid. Something is wrong!");
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
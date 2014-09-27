package org.bitseal.tests.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.bitseal.core.App;
import org.bitseal.core.IncomingMessageProcessor;
import org.bitseal.core.OutgoingMessageProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.PubkeyGenerator;
import org.bitseal.data.Address;
import org.bitseal.data.Message;
import org.bitseal.data.Pubkey;
import org.bitseal.data.UnencryptedMsg;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.PubkeyProvider;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

/** 
 * Tests the method MessageProcessor.extractMessageFromUnencryptedMsg()<br><br>
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
public class Test_ExtractMessageFromUnencryptedMsg extends AndroidTestCase
{
	private static final String TAG = "TEST_EXTRACT_MESSAGE_FROM_UNENCRYPTED_MSG";
	
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
	
	public void testExtractMessageFromUnencryptedMsg() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
		// Test 0: Construct a new Message, then use it to construct a new UnencryptedMsg, then use that to create a new 
		// Message and check whether all the data has been converted properly
		AddressGenerator addGen = new AddressGenerator();
		Address toAddress = addGen.generateAndSaveNewAddress();
		Address fromAddress = addGen.generateAndSaveNewAddress();
		
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey toPubkey = pubGen.generateAndSaveNewPubkey(toAddress);
		Pubkey fromPubkey = pubGen.generateAndSaveNewPubkey(fromAddress);
		
		Message message = new Message();
		message.setBelongsToMe(true);
		message.setToAddress(toAddress.getAddress());
		message.setFromAddress(fromAddress.getAddress());
		message.setSubject("I live in the shadow of Mount Snowdonia");
		message.setBody("Working in public libraries is superior to working in private libraries");
		
		// Use reflection to test the private method constructUnencryptedMsg()
		OutgoingMessageProcessor outMsgProc = new OutgoingMessageProcessor();		
		Method method = OutgoingMessageProcessor.class.getDeclaredMethod("constructUnencryptedMsg", Message.class, Pubkey.class, boolean.class);
		method.setAccessible(true);
		UnencryptedMsg unencMsg = (UnencryptedMsg) method.invoke(outMsgProc, message, toPubkey, false);

		// Use reflection to test the private method extractMessageFromUnencryptedMsg()
		IncomingMessageProcessor inMsgProc = new IncomingMessageProcessor();		
		Method method1 = IncomingMessageProcessor.class.getDeclaredMethod("extractMessageFromUnencryptedMsg", UnencryptedMsg.class);
		method1.setAccessible(true);
		Message extractedMessage = (Message) method1.invoke(inMsgProc, unencMsg);
		
		// Check if the message text (subject and body) matches the input
		String subject = extractedMessage.getSubject();
		String body = extractedMessage.getBody();
		
		Log.i(TAG, "Expected message subject: " + "I live in the shadow of Mount Snowdonia");
		Log.i(TAG, "Actual message subject  : " + subject);
		assertEquals(subject, "I live in the shadow of Mount Snowdonia");
		
		Log.i(TAG, "Expected message body: " + "Working in public libraries is superior to working in private libraries");
		Log.i(TAG, "Actual message body  : " + body);
		assertEquals(body, "Working in public libraries is superior to working in private libraries");
		
		// Check whether the extracted message's 'toAddress' and 'fromAddress' fields are correct
		Log.i(TAG, "Expected to address: " + toAddress.getAddress());
		Log.i(TAG, "Actual to address  : " + extractedMessage.getToAddress());
		assertEquals(extractedMessage.getToAddress(), toAddress.getAddress());
		
		Log.i(TAG, "Expected from address: " + fromAddress.getAddress());
		Log.i(TAG, "Actual from address  : " + extractedMessage.getFromAddress());
		assertEquals(extractedMessage.getFromAddress(), fromAddress.getAddress());
		
		// Cleaning up - delete the addresses and pubkeys we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(toAddress);
		addProv.deleteAddress(fromAddress);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(toPubkey);
		pubProv.deletePubkey(fromPubkey);
	}
}
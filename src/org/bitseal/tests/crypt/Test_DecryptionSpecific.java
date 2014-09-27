package org.bitseal.tests.crypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import junit.framework.TestCase;

import org.bitseal.crypt.CryptProcessor;
import org.bitseal.crypt.KeyConverter;
import org.bitseal.data.Address;
import org.bitseal.data.Msg;
import org.bitseal.util.ByteFormatter;
import org.bitseal.util.ByteUtils;
import org.spongycastle.jce.interfaces.ECPrivateKey;

import android.util.Log;

/**
 * A test that uses data from the encryption example found here:<br> 
 * https://bitmessage.org/wiki/Encryption#Example<br><br> 
 * 
 * The tests starts with the known correct input from the wiki
 * example, and uses it to construct an EncryptedMsg. This EncryptedMsg
 * is then decrypted, and the resulting data is compared to the 
 * example data from the wiki. <br><br>
 * 
 * @author Jonathan Coe
 */
public class Test_DecryptionSpecific extends TestCase
{
	private static final String TAG = "TEST_DECRYPTION_SPECIFIC";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testDecryptionSpecific()
	{
//		Starting data:
//			Encrypted message data:
//				IV
//				Curve Type
//				X length
//				X
//				Y length
//				Y
//				Cipher Text (result of AES encryption)
//				Mac (HMACSHA256 with key_m as salt and cipher text as input)
//			Private key k
		
		String ivString = "bddb7c2829b08038753084a2f3991681";
		int curveType = 714;
		int xLength = 32;
		String xString = "0293213dcf1388b61c2ae5cf80fee6ffffc049a2f9fe7365fe3867813ca81292";
		int yLength = 32;
		String yString = "df94686c6afb565ac6149b153d61b3b287ee2c7f997c14238796c12b43a3865a";
		String cipherTextString = "64203d5b24688e2547bba345fa139a5a1d962220d4d48a0cf3b1572c0d95b61643a6f9a0d75af7eacc1bd957147bf723";
		String macString = "4c08ac6c93c7377bac5a2e873dd3511b127aff6d0d1638cdae4989c4d2fe7de1";
		String privateKeykString = "02ba2744e65ccd7b1954b0a33b80d75e16cab47f2b331ff0b6d184b71983da85";
		
		// Convert the starting data to byte[] form
		byte[] ivBytes = ByteFormatter.hexStringToByteArray(ivString);
		byte[] curveTypeBytes = ByteUtils.shortToBytes((short) curveType);
		byte[] xLengthBytes = ByteUtils.shortToBytes((short) xLength);
		byte[] xBytes = ByteFormatter.hexStringToByteArray(xString);
		byte[] yLengthBytes = ByteUtils.shortToBytes((short) yLength);
		byte[] yBytes = ByteFormatter.hexStringToByteArray(yString);
		byte[] cipherTextBytes = ByteFormatter.hexStringToByteArray(cipherTextString);
		byte[] macBytes = ByteFormatter.hexStringToByteArray(macString);
		byte[] privateKeykBytes = ByteFormatter.hexStringToByteArray(privateKeykString);
		
// 		Expected result data: 
//			Plain text - "The quick brown fox jumps over the lazy dog."
		String expectedPlainText = "54686520717569636b2062726f776e20666f78206a756d7073206f76657220746865206c617a7920646f672e";
		
		// Combine the starting data into a sinlge byte[]. This will be the encrypted message data that we decrypt.
		byte[] encryptedMsgData = null;
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try 
		{
			outputStream.write(ivBytes);
			outputStream.write(curveTypeBytes);
			outputStream.write(xLengthBytes);
			outputStream.write(xBytes);
			outputStream.write(yLengthBytes);
			outputStream.write(yBytes);
			outputStream.write(cipherTextBytes);
			outputStream.write(macBytes);
			
			encryptedMsgData = outputStream.toByteArray();
			outputStream.close();
		}
		catch (IOException e) 
		{
			throw new RuntimeException("IOException occurred in Test_DecyptSionpecific", e);
		}
		
		// Create a new Address object and set the private key k from the example as its private encryption key
		Address toAddress = new Address();
		toAddress.setPrivateEncryptionKey(new KeyConverter().encodePrivateKeyToWIF(privateKeykBytes));
		
		// Use the example data to construct a new EncryptedMsg object
		Msg encMsg = new Msg();
		encMsg.setMessageData(encryptedMsgData);
		
		// Create the ECPrivateKey object that we will use to decrypt the message data
		KeyConverter keyConv = new KeyConverter();
		ECPrivateKey k = keyConv.decodePrivateKeyFromWIF(toAddress.getPrivateEncryptionKey());
		
		// Decrypt the EncryptedMsg
		CryptProcessor cryptProc = new CryptProcessor();
		byte[] decryptedBytes = cryptProc.decrypt(encMsg.getMessageData(), k);
		
		// Compare the decrypted plain text with the expected plain text
		String decryptedPlainText = ByteFormatter.byteArrayToHexString(decryptedBytes);
		Log.i(TAG, "Expected plain text:   " + expectedPlainText);
		Log.i(TAG, "Decrypted plain text:  " + decryptedPlainText);
		assertEquals(expectedPlainText, decryptedPlainText);
	}
}

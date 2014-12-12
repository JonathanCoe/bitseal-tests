package org.bitseal.tests.crypt;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.bitseal.crypt.CryptProcessor;
import org.bitseal.crypt.KeyConverter;
import org.bitseal.crypt.SHA256;
import org.bitseal.database.DatabaseContentProvider;
import org.bitseal.util.ArrayCopier;
import org.bitseal.util.ByteFormatter;
import org.bitseal.util.ByteUtils;
import org.spongycastle.jce.interfaces.ECPrivateKey;
import org.spongycastle.jce.interfaces.ECPublicKey;
import org.spongycastle.math.ec.ECPoint;

import android.os.SystemClock;
import android.util.Log;

/**
 * A test that attempts to replicate the results of the encryption example here: <br><br>
 * 
 * https://bitmessage.org/wiki/Encryption#Example<br><br>
 * 
 * Note: Since the original example was posted, the data used to calculate the MAC has changed. 
 * It used to be calculated from the ciphertext alone. Now it is calculated from IV + R + ciphertext. <br><br>
 * 
 * Note: This test uses reflection to access one or more private methods
 * 
 * @author Jonathan Coe
 */
public class Test_EncryptionSpecific extends TestCase
{
	private static final String TAG = "TEST_ENCRYPTION_SPECIFIC";
	
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
	
	public void testEncryptionSpecific() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException
	{
// 		Starting data: 
//			Public Key K
//			IV	
//			Private Key r
//			Public Key R
//			Plain text - "The quick brown fox jumps over the lazy dog."
		
		String pubKeyKString = "0409d4e5c0ab3d25fe048c64c9da1a242c7f19417e9517cd266950d72c755713585c6178e97fe092fc897c9a1f1720d5770ae8eaad2fa8fcbd08e9324a5dde1857";
		String ivString = "bddb7c2829b08038753084a2f3991681";
		String privKeyRString = "5be6facd941b76e9d3ead03029fbdb6b6e0809293f7fb197d0c51f84e96b8ba4";
		String pubKeyRString = "040293213dcf1388b61c2ae5cf80fee6ffffc049a2f9fe7365fe3867813ca81292df94686c6afb565ac6149b153d61b3b287ee2c7f997c14238796c12b43a3865a";
		String plainTextString = "54686520717569636b2062726f776e20666f78206a756d7073206f76657220746865206c617a7920646f672e";
		
		Log.i(TAG, "Length of pubKeyKString: " + pubKeyKString.length());
		Log.i(TAG, "Length of ivString: " + ivString.length());
		Log.i(TAG, "Length of privKeyRString: " + privKeyRString.length());
		Log.i(TAG, "Length of pubKeyRString: " + pubKeyRString.length());
		Log.i(TAG, "Length of plainTextString: " + plainTextString.length());
		
		byte[] pubKeyKBytes = ByteFormatter.hexStringToByteArray(pubKeyKString);
		byte[] ivBytes = ByteFormatter.hexStringToByteArray(ivString);
		byte[] privKeyRBytes = ByteFormatter.hexStringToByteArray(privKeyRString);
		byte[] pubKeyRBytes = ByteFormatter.hexStringToByteArray(pubKeyRString);
		byte[] plainTextBytes = ByteFormatter.hexStringToByteArray(plainTextString);
		
		Log.i(TAG, "Length of pubKeyKBytes: " + pubKeyKBytes.length);
		Log.i(TAG, "Length of ivBytes: " + ivBytes.length);
		Log.i(TAG, "Length of privKeyRBytes: " + privKeyRBytes.length);
		Log.i(TAG, "Length of pubKeyRBytes: " + pubKeyRBytes.length);
		Log.i(TAG, "Length of plainTextBytes: " + plainTextBytes.length);
		
		
//		Expected resulting data:
//			Derived public key P (point multiply r with K)
//			(From SHA512 of public key P X component (H))
//			First 32 bytes of H called key_e
//			Last 32 bytes of H called key_m	
//			Cipher Text (result of AES encryption)
//			Mac (HMACSHA256 with key_m as salt and cipher text as input)
		
		String expectedPubKeyPXString = "0db8e3ad8c0cd73fa2b34671b7b247729b101141579d199e0dc0bd024eaefd89";
		String expectedPubKeyPYString = "00cac8f528dc90b66811abac517d7497be5292931229be0b743e0503f443c3d296"; // Note: First byte is a prepended "00" value
		String expectedKeyEString = "170543828267867105263d4828efff82d9d59cbf08743b696bcc5d69fa1897b4";
		String expectedKeyMString = "f83f1e9cc5d6b8448d39dc6a9d5f5b7f460e4a78e9286ee8d91ce1660a53eacd";
		String expectedCipherTextString = "64203d5b24688e2547bba345fa139a5a1d962220d4d48a0cf3b1572c0d95b61643a6f9a0d75af7eacc1bd957147bf723";
		String expectedMacString = "d961859a9bc3065a25a7f41e840c57626da9532ce153f91d43c17f32ec4ad1b2";
		
		Log.i(TAG, "Length of expectedPubKeyPXString: " + expectedPubKeyPXString.length());
		Log.i(TAG, "Length of expectedPubKeyPYString: " + expectedPubKeyPYString.length());
		Log.i(TAG, "Length of expectedKeyEString: " + expectedKeyEString.length());
		Log.i(TAG, "Length of expectedKeyMString: " + expectedKeyMString.length());
		Log.i(TAG, "Length of expectedCipherTextString: " + expectedCipherTextString.length());
		Log.i(TAG, "Length of expectedMacString: " + expectedMacString.length());

		byte[] expectedPubKeyPXBytes = ByteFormatter.hexStringToByteArray(expectedPubKeyPXString);
		byte[] expectedPubKeyPYBytes = ByteFormatter.hexStringToByteArray(expectedPubKeyPYString);
		byte[] expectedKeyEBytes = ByteFormatter.hexStringToByteArray(expectedKeyEString);
		byte[] expectedKeyMBytes = ByteFormatter.hexStringToByteArray(expectedKeyMString);
		byte[] expectedCipherTextBytes = ByteFormatter.hexStringToByteArray(expectedCipherTextString);
		byte[] expectedMacBytes = ByteFormatter.hexStringToByteArray(expectedMacString);
		
		Log.i(TAG, "Length of expectedPubKeyPXBytes: " + expectedPubKeyPXBytes.length);
		Log.i(TAG, "Length of expectedPubKeyPYBytes: " + expectedPubKeyPYBytes.length);
		Log.i(TAG, "Length of expectedKeyEBytes: " + expectedKeyEBytes.length);
		Log.i(TAG, "Length of expectedKeyMBytes: " + expectedKeyMBytes.length);
		Log.i(TAG, "Length of expectedCipherTextBytes: " + expectedCipherTextBytes.length);	
		Log.i(TAG, "Length of expectedMacBytes: " + expectedMacBytes.length);
		
		
		// Now take the input data and use it to perform the encryption operation, in 5 stages:
		
		KeyConverter keyConv = new KeyConverter();
		ECPublicKey pubKeyK = keyConv.reconstructPublicKey(pubKeyKBytes);
		ECPrivateKey privKeyR = keyConv.reconstructPrivateKey(privKeyRBytes);
		
		// 1) Do an EC point multiply with public key K and private key r. This gives you public key P.
		ECPoint pointP = pubKeyK.getQ().multiply((privKeyR.getD()));
		
		// 2) Use the X component of public key P and calculate the SHA512 hash H. Use reflection
		// to access the private method deriveKey()
		CryptProcessor cryptProc = new CryptProcessor();		
		Method method0 = CryptProcessor.class.getDeclaredMethod("deriveKey", ECPoint.class);
		method0.setAccessible(true);
		byte[] tmpKey = (byte[]) method0.invoke(cryptProc, pointP);
		
		// 3) The first 32 bytes of H are called key_e and the last 32 bytes are called key_m.
		byte[] enc_key = ArrayCopier.copyOfRange(tmpKey, 0, 32);
		byte[] mac_key = ArrayCopier.copyOfRange(tmpKey, 32, 64);
		
		// 4) Encrypt the data with AES-256-CBC, using IV as initialization vector, key_e as encryption key and the
		// plain text as payload. Call the output cipher text. Use reflection to access the private method doAES().
		Method method1 = CryptProcessor.class.getDeclaredMethod("doAES", byte[].class, byte[].class, byte[].class, boolean.class);
		method1.setAccessible(true);
		byte[] cipherText = (byte[]) method1.invoke(cryptProc, enc_key, ivBytes, plainTextBytes, true);
		
		// 5) Calculate a 32 byte MAC with HMACSHA256, using key_m as salt and IV + R + Cipher text as data. Call the output MAC.
		byte[] dataForMac = ByteUtils.concatenateByteArrays(ivBytes, pubKeyRBytes, cipherText);
		byte[] mac = SHA256.hmacSHA256(dataForMac, mac_key);
		
		
		
		// Now compare the actual resulting data with the expected resulting data
		
		String pubKeyPXString = ByteFormatter.byteArrayToHexString(pointP.getX().toBigInteger().toByteArray());
		String pubKeyPYString = ByteFormatter.byteArrayToHexString(pointP.getY().toBigInteger().toByteArray());
		String keyEString = ByteFormatter.byteArrayToHexString(enc_key);
		String keyMString = ByteFormatter.byteArrayToHexString(mac_key);
		String cipherTextString = ByteFormatter.byteArrayToHexString(cipherText);
		String macString = ByteFormatter.byteArrayToHexString(mac);
		
		Log.i(TAG, "Length of pubKeyPXString: " + pubKeyPXString.length());
		Log.i(TAG, "Length of pubKeyPYString: " + pubKeyPYString.length());
		Log.i(TAG, "Length of keyEString: " + keyEString.length());
		Log.i(TAG, "Length of keyMString: " + keyMString.length());
		Log.i(TAG, "Length of cipherTextString: " + cipherTextString.length());	
		Log.i(TAG, "Length of macString: " + macString.length());
		
		
		Log.i(TAG, "Expected value of pubKeyPX:   " + expectedPubKeyPXString);
		Log.i(TAG, "Actual value of pubKeyPX:     " + pubKeyPXString);
		
		Log.i(TAG, "Expected value of pubKeyPY:   " + expectedPubKeyPYString);
		Log.i(TAG, "Actual value of pubKeyPY:     " + pubKeyPYString);
		
		Log.i(TAG, "Expected value of keyE:       " + expectedKeyEString);
		Log.i(TAG, "Actual value of keyE:         " + keyEString);
		
		Log.i(TAG, "Expected value of keyM:       " + expectedKeyMString);
		Log.i(TAG, "Actual value of keyM:         " + keyMString);
		
		Log.i(TAG, "Expected value of cipherText: " + expectedCipherTextString);
		Log.i(TAG, "Actual value of cipherText:   " + cipherTextString);
		
		Log.i(TAG, "Expected value of mac:        " + expectedMacString);
		Log.i(TAG, "Actual value of mac:          " + macString);

		
		assertEquals(pubKeyPXString, expectedPubKeyPXString);
		assertEquals(pubKeyPYString, expectedPubKeyPYString);
		assertEquals(keyEString, expectedKeyEString);
		assertEquals(keyMString, expectedKeyMString);
		assertEquals(cipherTextString, expectedCipherTextString);
		assertEquals(macString, expectedMacString);
	}
}
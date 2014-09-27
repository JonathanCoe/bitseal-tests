package org.bitseal.tests.crypt;

import java.util.Arrays;

import junit.framework.TestCase;

import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.SHA256;
import org.bitseal.data.Address;
import org.bitseal.util.ArrayCopier;
import org.bitseal.util.Base58;
import org.bitseal.util.ByteFormatter;

import android.util.Log;

/** 
 * Tests the conversion of the two private keys generated with a new Bitmessage address
 * to Wallet Import Format (WIF).
**/
public class Test_EncodePrivateKeysInWIF extends TestCase
{
	private static final String TAG = "TEST_ENCODE_PRIVATE_KEYS_IN_WIF";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testEncodePrivateKeysInWIF()
	{
		AddressGenerator addressGenerator = new AddressGenerator();
		
		Address bitmessageAddress = addressGenerator.generateAndSaveNewAddress();
				
		Log.i(TAG, "Generated Address:             " + bitmessageAddress.getAddress());
		
		String privateSigningKey = bitmessageAddress.getPrivateSigningKey();
		String privateEncryptionKey = bitmessageAddress.getPrivateEncryptionKey();
		
		// Test check 1: Check that both private keys are 51 characters in length, as they should be
		assertEquals(privateSigningKey.length(), 51);
		assertEquals(privateEncryptionKey.length(), 51);
		
		// Test check 2: Check that the first character of the output is 5
		assertEquals(privateSigningKey.substring(0, 1), "5");	
		assertEquals(privateEncryptionKey.substring(0, 1), "5");
		
		byte[] privateSigningKeyBytes = null;
		byte[] privateEncryptionKeyBytes = null;

		privateSigningKeyBytes = Base58.decode(privateSigningKey);
		privateEncryptionKeyBytes = Base58.decode(privateEncryptionKey);
		
		byte[] privateSigningKeyBytesMinusChecksum = ArrayCopier.copyOfRange(privateSigningKeyBytes, 0, (privateSigningKeyBytes.length - 4));
		byte[] privateEncryptionKeyBytesMinusChecksum = ArrayCopier.copyOfRange(privateEncryptionKeyBytes, 0, (privateEncryptionKeyBytes.length - 4));
		
		byte[] privateSigningKeyChecksum = ArrayCopier.copyOfRange(privateSigningKeyBytes, (privateSigningKeyBytes.length - 4), (privateSigningKeyBytes.length));
		byte[] privateEncryptionKeyChecksum = ArrayCopier.copyOfRange(privateEncryptionKeyBytes, (privateEncryptionKeyBytes.length - 4), (privateEncryptionKeyBytes.length));
		
		byte[] hashOfPrivateSigningKeyBytesMinusChecksum = SHA256.doubleDigest(privateSigningKeyBytesMinusChecksum);
		Log.i(TAG, "Hash of Private Signing Key:          " + ByteFormatter.byteArrayToHexString(hashOfPrivateSigningKeyBytesMinusChecksum));
		
		byte[] hashOfPrivateEncryptionKeyBytesMinusChecksum = SHA256.doubleDigest(privateEncryptionKeyBytesMinusChecksum);
		Log.i(TAG, "Hash of Private Encryption Key:          " + ByteFormatter.byteArrayToHexString(hashOfPrivateEncryptionKeyBytesMinusChecksum));
		
		byte[] privateSigningKeyTestChecksum = ArrayCopier.copyOfRange(hashOfPrivateSigningKeyBytesMinusChecksum, 0, 4);
		Log.i(TAG, "Private Signing Key Test Checksum:          " + ByteFormatter.byteArrayToHexString(privateSigningKeyTestChecksum));
		
		byte[] privateEncryptionKeyTestChecksum = ArrayCopier.copyOfRange(hashOfPrivateEncryptionKeyBytesMinusChecksum, 0, 4);
		Log.i(TAG, "Private Encryption Key Test Checksum:          " + ByteFormatter.byteArrayToHexString(privateEncryptionKeyTestChecksum));
		
		// Test check 3: Having re-calculated the checksum for each key, check if it is equal to the checksum of the key provided
		assertTrue(Arrays.equals(privateSigningKeyChecksum, privateSigningKeyTestChecksum));
		assertTrue(Arrays.equals(privateEncryptionKeyChecksum, privateEncryptionKeyTestChecksum));
	}
}
package org.bitseal.tests.crypt;

import java.util.Arrays;

import junit.framework.TestCase;

import org.bitseal.core.App;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.SHA256;
import org.bitseal.data.Address;
import org.bitseal.database.AddressProvider;
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
	private static final int WIF_PRIVATE_KEY_MIN_LENGTH = 49;
	private static final int WIF_PRIVATE_KEY_MAX_LENGTH = 51;
	
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
		// Generate a new Bitmessage address
		AddressGenerator addressGenerator = new AddressGenerator();
		Address address = addressGenerator.generateAndSaveNewAddress();		
		Log.i(TAG, "Generated Address:             " + address.getAddress());
		
		String privateSigningKey = address.getPrivateSigningKey();
		String privateEncryptionKey = address.getPrivateEncryptionKey();
		
		// Check that both private keys are of a valid length
		Log.i(TAG, "Length of private signing key:    " + privateSigningKey.length());
		Log.i(TAG, "Length of private encryption key: " + privateEncryptionKey.length());
		assertTrue((privateSigningKey.length() >= WIF_PRIVATE_KEY_MIN_LENGTH) && (privateSigningKey.length() <= WIF_PRIVATE_KEY_MAX_LENGTH));
		assertTrue((privateEncryptionKey.length() >= WIF_PRIVATE_KEY_MIN_LENGTH) && (privateEncryptionKey.length() <= WIF_PRIVATE_KEY_MAX_LENGTH));
		
		// Check that the first character of the output is 5
		assertEquals("5", privateSigningKey.substring(0, 1));	
		assertEquals("5", privateEncryptionKey.substring(0, 1));
		
		// Check whether the checksum of each key is correct
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
		
		assertTrue(Arrays.equals(privateSigningKeyChecksum, privateSigningKeyTestChecksum));
		assertTrue(Arrays.equals(privateEncryptionKeyChecksum, privateEncryptionKeyTestChecksum));
		
		// Cleaning up - delete the address we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(address);
	}
}
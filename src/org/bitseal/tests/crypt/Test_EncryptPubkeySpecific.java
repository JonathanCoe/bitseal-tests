package org.bitseal.tests.crypt;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.bitseal.core.AddressProcessor;
import org.bitseal.core.App;
import org.bitseal.core.PubkeyProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.data.Address;
import org.bitseal.data.Payload;
import org.bitseal.data.Pubkey;
import org.bitseal.database.AddressProvider;
import org.bitseal.util.ByteFormatter;
import org.bitseal.util.ByteUtils;

import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Log;

/**
 * Tests Bitseal's encryption of pubkeys. We start with an encrypted pubkey payload 
 * produced by PyBitmessage. We then generate a new encrypted pubkey payload using the
 * same data that the original pubkey was generated from. We then attempt to decrypt both
 * payloads and check that the decrypted data matches. <br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
 * 
 * @author Jonathan Coe
 */
public class Test_EncryptPubkeySpecific extends AndroidTestCase
{
	private static final String TAG = "TEST_ENCRYPT_PUBKEY_SPECIFIC";
	
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

        long endTime = SystemClock.elapsedRealtime() + TimeUnit.SECONDS.toMillis(50);

        while (null == context.getApplicationContext())
        {
            if (SystemClock.elapsedRealtime() >= endTime)
            {
                Log.e(TAG, "Attempt to get application context timed out");
            	fail();
            }
            SystemClock.sleep(50);
        }
	}
	
	@SuppressWarnings("unused")
	public void testEncryptPubkeySpecific()
	{
		// Wait for five seconds in order to make it more likely that we will be able to get application context
		SystemClock.sleep(5000);
		
		// Starting data:
		String testAddressString = "BM-2cXdemXcNztvNhoGTau7Zpq1nf7odwUaKr";
		
		String pybitmessageEncryptedPubkeyHex = "000000000076272c000000005361600f0401c4574a1ac4e28927a5c5148e14325da16a1348f6d40ca1" +
												"ff3429ed8ea2bdfa61b4fa03074db45a6d43a324b90250c0ad02ca00209fcbafbc1a2c48a4d46bcfb4" +
												"884033fa0d96ec3545fc7def13b91e26f951c82900200c8ea0d205f7778ed9875886fa18156dae6a63" +
												"075f51a6e5a52aa6327a82f6277c0d0c20f6441a8f6a45c9e3ed90189a9ed4dee47c6ea567c772a510" +
												"44497e80fca8690263197fc28061da6fa2f51101e140d4bcd11ee409be1723814a51f85fbbaf778b10" +
												"c45e62312e16c662f6cb81f83f3d7702e44694ef790aa5ed026078c21e209fc4ba3878cf51f13bb11e" +
												"c6a6d936bce7f75f6b6f1e55da68f71a5f76608ce5cb5b218a4a0db71aca2e74f9010d01fab6a9205d" +
												"5712eae9ba136ea57012064cb8c2ec5d96a0707a231b2e23453b9c2564b1bce0a4ff0672a2f49fe4cf" +
												"5b6af8cd97a1eacf12d080fb1ca32da08cbbc067ea1917897dda2360ddd3145213a217381c2960fbec" +
												"a999c8f9f2a27f164d11955f510379e3c43c65c6fc3403";
		String powNonce = "7743276";
		String time = "1398890511";
		String addressVersionHex = "04";
		String streamNumberHex = "01";
		String behaviourBitfieldHex = "00000001";
		String publicSigningKeyHex = "853b3e742b41f1c3c1c0ea6cfc230da982adc5c3e6a07e3ef30bdb1f4d2101e6df904a286aae9a482493d671c353584ebec8a95f1cd3bc0e6620633af2227c2d";
		String publicEncryptionKeyHex = "e4fc38aac159547f26fb0ea78ea65e97764ea0f2f8957aa5edb83f54957ad0c48adba6b33fe32376875c0701f769307978c37e76bc1efc920c7bdc4c110904db";
		String nonceTrialsPerByteHex = "fd0280";
		String signatureLengthHex = "48";
		String signatureHex = "3046022100d84bb537198c3727f135a475bfa0aadf77aaa608cee2a6fdabda0042cfb1a2da02210091145a3a5ca984bbca" +
							  "5264045d77c4ad608525378fab271a9e381522c5181517";
		
		// Where necessary, convert the starting values into byte form
		byte[] pybitmessagePayload = ByteFormatter.hexStringToByteArray(pybitmessageEncryptedPubkeyHex);
		byte[] publicSigningKey = ByteFormatter.hexStringToByteArray(publicSigningKeyHex);
		byte[] publicEncryptionKey = ByteFormatter.hexStringToByteArray(publicEncryptionKeyHex);
		byte[] signature = ByteFormatter.hexStringToByteArray(signatureHex);
		
		// Save the address from which this pubkey is derived to the database. It will be used when 
		// the pubkey payload is constructed. Also calculate the tag of the addres. Set some placeholder
		// data for the rest of the fields in the Address object, as they will not be used. 
		Address testAddress = new Address();
		testAddress.setAddress(testAddressString);
		AddressProcessor addProc = new AddressProcessor();
		byte[] tag = addProc.calculateAddressTag(testAddressString);
		testAddress.setTag(tag);
		testAddress.setPrivateSigningKey("xxx");
		testAddress.setPrivateEncryptionKey("xxx");
		testAddress.setRipeHash(new AddressGenerator().calculateRipeHash(publicSigningKey, publicEncryptionKey));
		AddressProvider addProv = AddressProvider.get(App.getContext());
		long addressId = addProv.addAddress(testAddress);
		testAddress.setId(addressId);
		
		// Create the Pubkey object that we will encrypt, using the starting data specified above
		Pubkey bitsealPubkey = new Pubkey();
		bitsealPubkey.setCorrespondingAddressId(addressId);
		bitsealPubkey.setRipeHash(testAddress.getRipeHash());
		bitsealPubkey.setPOWNonce(Long.valueOf(powNonce));
		bitsealPubkey.setTime(Long.valueOf(time));
		bitsealPubkey.setAddressVersion(4);
		bitsealPubkey.setStreamNumber(1);
		bitsealPubkey.setBehaviourBitfield(1);
		bitsealPubkey.setPublicSigningKey(publicSigningKey);
		bitsealPubkey.setPublicEncryptionKey(publicEncryptionKey);
		bitsealPubkey.setNonceTrialsPerByte(640);
		bitsealPubkey.setExtraBytes(14000);
		bitsealPubkey.setSignatureLength(72);
		bitsealPubkey.setSignature(signature);
		
		// Create the Pubkey payload. This should give us the encrypted data we want to test.
		PubkeyProcessor pubProc = new PubkeyProcessor();
		Payload bitsealPubkeyPayload = pubProc.constructPubkeyPayload(bitsealPubkey, false);
		byte[] bitsealPayload = bitsealPubkeyPayload.getPayload();
		// Append the POW nonce to the payload
		bitsealPayload = ByteUtils.concatenateByteArrays(ByteUtils.longToBytes(Long.valueOf(powNonce)), bitsealPayload);
		
		Log.i(TAG, "Encrypted pubkey payload produced by Bitseal:      \n" + ByteFormatter.byteArrayToHexString(bitsealPayload));
		Log.i(TAG, "Encrypted pubkey payload produced by PyBitmessage: \n" + pybitmessageEncryptedPubkeyHex);
		
		// Attempt to decrypt both payloads, creating new Pubkey objects from the decrypted data
		Pubkey decryptedBitsealPubkey = pubProc.reconstructPubkey(bitsealPayload, testAddressString);
		Pubkey pybitmessagePubkey = pubProc.reconstructPubkey(pybitmessagePayload, testAddressString);
		
		// Check that the pubkeys are valid 
		assertTrue(pubProc.validatePubkey(decryptedBitsealPubkey, testAddressString));
		assertTrue(pubProc.validatePubkey(pybitmessagePubkey, testAddressString));
		
		// Check that the data from the pubkeys match
		assertEquals(decryptedBitsealPubkey.getPOWNonce(), pybitmessagePubkey.getPOWNonce());
		assertEquals(decryptedBitsealPubkey.getTime(), pybitmessagePubkey.getTime());
		assertEquals(decryptedBitsealPubkey.getAddressVersion(), pybitmessagePubkey.getAddressVersion());
		assertEquals(decryptedBitsealPubkey.getStreamNumber(), pybitmessagePubkey.getStreamNumber());
		assertEquals(decryptedBitsealPubkey.getBehaviourBitfield(), pybitmessagePubkey.getBehaviourBitfield());
		assertTrue(Arrays.equals(decryptedBitsealPubkey.getPublicSigningKey(), pybitmessagePubkey.getPublicSigningKey()));
		assertTrue(Arrays.equals(decryptedBitsealPubkey.getPublicEncryptionKey(), pybitmessagePubkey.getPublicEncryptionKey()));
		assertEquals(decryptedBitsealPubkey.getNonceTrialsPerByte(), pybitmessagePubkey.getNonceTrialsPerByte());
		assertEquals(decryptedBitsealPubkey.getExtraBytes(), pybitmessagePubkey.getExtraBytes());
		assertEquals(decryptedBitsealPubkey.getSignatureLength(), pybitmessagePubkey.getSignatureLength());
		assertTrue(Arrays.equals(decryptedBitsealPubkey.getSignature(), pybitmessagePubkey.getSignature()));
		
		// Cleaning up - delete the address we created from the database
		addProv.deleteAddress(testAddress);
	}
}
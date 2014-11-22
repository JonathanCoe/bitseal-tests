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
	/** The object type number for pubkeys, as defined by the Bitmessage protocol */
	private static final int OBJECT_TYPE_PUBKEY = 1;
	
	private static final int PUBKEY_VERSION = 4;
	
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
	
	public void testEncryptPubkeySpecific()
	{
		// Wait for five seconds in order to make it more likely that we will be able to get application context
		SystemClock.sleep(5000);
		
		// Starting data:
		String testAddressString = "BM-2cTtrF6GswTv1g57YbRA18pzW89VE8tpdB";
		String pybitmessagePubkeyPayload = "00000000039eba3500000000548db9a1000000010401d980499dcca2236360d159963802836198c7bcfdf183e2df9d51762fa01b761a8f13e565e409"
				+ "7e71e4050debffcde7c902ca002001f689983ce6e729662ab7a8c05be293d77d4d85ece7502e2d13248e4ae82e6700209f72eef3de0441c74ebb076fa43d93cae764ca1981d6edae0"
				+ "f41437452e73357875a081cac4ad5fa8998d1ac99f03f6dadd5cc0a35ec4059fb583ecfab98adeeb9267c9fae1e5f273b2cb72baa6eeb4b71fd103a7c481f14f624071d2f61720409"
				+ "1a05f4dfd206a1c15ff89acb5c0d1e5d38570b429620becfa7fabaedd2b1eabb776f195b825e778d749c205e9f682ed10cec8092bb3cbf45f6047835755520e95b86abda840233994"
				+ "222fd9735f98f547f775c121370f65187b88c3a6cc032b290d183334167b293d916796cce2daf1c1eb0dc8bed593e586190e3971aaacde4e0f561c00f093c3b80de48c491e191eb7d"
				+ "aed03a8be0817d1418cbbe7d95b4bd7fb46ac8d3f0f42d2c52401c5d95fa4b04de8f0de896ec905520ab9b08850d";
		String powNonce = "60734005";
		String time = "1418574241";
		String publicSigningKeyHex = "47c5dd9490b8c53f1e8484cb124f6d52fd1a8b11dd2b96f2606a0b1c34a372096b90012c1341fad39c1c18a4120328feb791012e3d8226cbcfba78594cccd2f5";
		String publicEncryptionKeyHex = "46cab71b4bc8cb5eb7857d9d13b7f55358959005853a02ebe16ba49b54a70479bf246eedfbd5e37f5e37b38370a948febccc594c9c0c6756eeb39b2871e602a6";
		String signatureHex = "304402207051124cd43ca83e378d5a19e1e6458a381032e5309324aa2a1563689d21056c0220455feb332564fa4fe6313991147269fe4e3d7622cd5b8d4a7bc993edd8d5d956";
				
		// Where necessary, convert the starting values into byte form
		byte[] pybitmessagePayload = ByteFormatter.hexStringToByteArray(pybitmessagePubkeyPayload);
		byte[] publicSigningKey = ByteFormatter.hexStringToByteArray(publicSigningKeyHex);
		byte[] publicEncryptionKey = ByteFormatter.hexStringToByteArray(publicEncryptionKeyHex);
		byte[] signature = ByteFormatter.hexStringToByteArray(signatureHex);
		
		// Save the address from which this pubkey is derived to the database. It will be used when 
		// the pubkey payload is constructed. Also calculate the tag of the address. Set some placeholder
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
		long testAddressId = addProv.addAddress(testAddress);
		testAddress.setId(testAddressId);
		
		// Create the Pubkey object that we will encrypt, using the starting data specified above
		Pubkey bitsealPubkey = new Pubkey();
		bitsealPubkey.setBelongsToMe(true);
		bitsealPubkey.setPOWNonce(Long.valueOf(powNonce));
		bitsealPubkey.setExpirationTime(Long.valueOf(time));
		bitsealPubkey.setObjectType(OBJECT_TYPE_PUBKEY);
		bitsealPubkey.setObjectVersion(PUBKEY_VERSION);
		bitsealPubkey.setStreamNumber(1);
		bitsealPubkey.setCorrespondingAddressId(testAddressId);
		bitsealPubkey.setRipeHash(testAddress.getRipeHash());
		bitsealPubkey.setBehaviourBitfield(1);
		bitsealPubkey.setPublicSigningKey(publicSigningKey);
		bitsealPubkey.setPublicEncryptionKey(publicEncryptionKey);
		bitsealPubkey.setNonceTrialsPerByte(1000);
		bitsealPubkey.setExtraBytes(1000);
		bitsealPubkey.setSignatureLength(signature.length);
		bitsealPubkey.setSignature(signature);
		
		// Create the Pubkey payload. This should give us the encrypted data we want to test.
		PubkeyProcessor pubProc = new PubkeyProcessor();
		Payload bitsealPubkeyPayload = pubProc.constructPubkeyPayload(bitsealPubkey, false);
		byte[] bitsealPayload = bitsealPubkeyPayload.getPayload();
		// Append the POW nonce to the payload
		bitsealPayload = ByteUtils.concatenateByteArrays(ByteUtils.longToBytes(Long.valueOf(powNonce)), bitsealPayload);
		
		Log.i(TAG, "Pubkey payload produced by Bitseal:      \n" + ByteFormatter.byteArrayToHexString(bitsealPayload));
		Log.i(TAG, "Pubkey payload produced by PyBitmessage: \n" + pybitmessagePubkeyPayload);
		
		// Attempt to decrypt both payloads, creating new Pubkey objects from the decrypted data
		Pubkey decryptedBitsealPubkey = pubProc.reconstructPubkey(bitsealPayload, testAddressString);
		Pubkey pybitmessagePubkey = pubProc.reconstructPubkey(pybitmessagePayload, testAddressString);
		
		// Check that the pubkeys are valid 
		assertTrue(pubProc.validatePubkey(decryptedBitsealPubkey, testAddressString));
		assertTrue(pubProc.validatePubkey(pybitmessagePubkey, testAddressString));
		
		// Check that the data from the pubkeys match
		assertEquals(decryptedBitsealPubkey.getPOWNonce(), pybitmessagePubkey.getPOWNonce());
		assertEquals(decryptedBitsealPubkey.getExpirationTime(), pybitmessagePubkey.getExpirationTime());
		assertEquals(decryptedBitsealPubkey.getObjectVersion(), pybitmessagePubkey.getObjectVersion());
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
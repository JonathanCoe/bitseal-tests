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
		String testAddressString = "BM-2cWH3y8Kyzyy7j4fYkwj6qDWzqZRUqqb2a";
		
		String pybitmessagePubkeyPayload = "0000000000ab887c00000000546ccdc700000001040180b3f3318094e78c31dbc6fe6e06fe63e62df8bbe542"
				+ "aa3a1bb32e3780ed31c202d4d1ed21c70b2588a6aad61e3b694602ca0020a1a8708a03ccc08fd776a2d1657cb1ab197579c0f5de2c35e8de75d8c1"
				+ "5558250020aaf151c4293c048d4d157eac234c644d6cffd5a611b9757356bd3375c6c290377e1c0724ba0224367bc647e96ec54f5bf18f002e1e93"
				+ "eec6c22268d8860177f8ae27781248ece9418d8bac80c3a2ea58217f797bffb5c831b64ea3dc2e18bbb8180da38c9cf82bda0611f9375d3a9c6eb0"
				+ "c6882622e38a1bf1abbe1175fa5f49d3a47ea248767ec8744f94f4cf9df0391a2481895eb5ddef08ea00ff409709879149795e422394e69d1fc335"
				+ "9b84c4861a3f25a88371b409081c253819df4c9b639600c6d0824807483df602bfd597dc7e5d0dc966585ff0b65b11ee894fa1c6f884ac31fa5021"
				+ "c4dc285cc14aca145cd4e00a32a41e9b8b0f416463ff97ecc6491d93fac6b78812941c2364dbc4312e24297f2b6e14d2dba804a87eda0337d9";
		String powNonce = "11241596";
		String time = "1416416711";
		String publicSigningKeyHex = "873b21948da767745327e9c5d8f41cb1187e185a74e0442be7c6a8e37c59ecbd035ca33018219992ac20b3a28d55fb138212d57e0b3e41f0b0cba2b153be035b";
		String publicEncryptionKeyHex = "e20190032915a09f8a3aefdd626421ba90aaaa630eadc432154543ccd5a49a751f3600fa833344f65a7ac02c4902a54ff134b8d846e050ad4c823136687717ae";
		String signatureHex = "3045022100f541db088b465f742f18bc5682dd2bde0998bdd9c8cb193effb805151e71b01802204b18754cc7e89738dc773f9800103c13ca2f9ddc6537d5ec0a092af8a573152c";
				
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
		bitsealPubkey.setObjectVersion(4);
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
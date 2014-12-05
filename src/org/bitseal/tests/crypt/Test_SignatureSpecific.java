package org.bitseal.tests.crypt;

import junit.framework.TestCase;

import org.bitseal.crypt.KeyConverter;
import org.bitseal.crypt.SigProcessor;
import org.bitseal.data.Pubkey;
import org.bitseal.database.DatabaseContentProvider;
import org.bitseal.util.ByteFormatter;
import org.spongycastle.jce.interfaces.ECPublicKey;

import android.os.SystemClock;
import android.util.Log;

/**
 * Tests Bitseal's signature creation and verification. A new pubkey is generated
 * using data taken from a pubkey payload produced by PyBitmessage. The new pubkey
 * is then signed by Bitseal. The test then checks that both the signature produced
 * by Bitseal and the signature taken from the pubkey payload are valid. This works
 * because ECDSA signatures are not deterministic. (However see http://tools.ietf.org/html/rfc6979)
 * 
 * @author Jonathan Coe
 */
public class Test_SignatureSpecific extends TestCase
{
	/** The object type number for pubkeys, as defined by the Bitmessage protocol */
	private static final int OBJECT_TYPE_PUBKEY = 1;
	
	private static final String TAG = "TEST_SIGNATURE_SPECIFIC";
	
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
	
	public void testSignatureSpecific()
	{
		// Address that this starting data derives from: BM-2cW7h5aUmvJC9WApzfWsXDcMCSqjPYWbLG
		
		// Starting data:
		String inputPrivateSigningKey = "5HynQJefWMVXbkft2RB9tkLJ3EWk4UgsbFg1CAsNN5DEHtw9tTR";
		long inputTime = 1419079365;
		int inputAddressVersion = 4;
		int inputStreamNumber = 1;
		int inputBehaviourBitfield = 1;
		String inputPublicSigningKeyHex = "04eac9804fced9771b5b43135de14a5290a8b5628770358eb51e881d2a9ad8d77cc2e63eafb7ac6073866838b42663f8e8eabccafa42a1c039d1141ffbf365d7b5";
		String inputPublicEncryptionKeyHex = "0466b2ee6c326235f0eedca14f3a4203c8a2698a8d571191ecb1ae49847f270e9b7729c5a790b5ce049be7bf5190f5c67ec62a61e9d035e75ed5edc42e2f8d75ab";
		String inputRipeHash = "00988701cbf9866f45096aa1b8487118eece2015";
		int inputNonceTrialsPerByte = 1000;
		int inputExtraBytes = 1000;
		
		// PyBitmessage signature
		String pybitmessageSignatureHex = "3046022100a8a4e6aeed2e41560ed87cb867c7386fb967dc992f11d4009c1d21d8f8f244c0022100d17cb6f5d830e273b251572c73474f6e7742c5a1e782769abff8d021454c0877";
		
		// First convert the public signing key, public encryption key, and PyBitmessage signature from hex to byte[] form
		byte[] publicSigningKey = ByteFormatter.hexStringToByteArray(inputPublicSigningKeyHex);
		byte[] publicEncryptionKey = ByteFormatter.hexStringToByteArray(inputPublicEncryptionKeyHex);
		byte[] pybitmessageSignature = ByteFormatter.hexStringToByteArray(pybitmessageSignatureHex);
		
		// Create a Pubkey object which we will sign.
		Pubkey pubkey = new Pubkey();
		pubkey.setBelongsToMe(true);
		pubkey.setExpirationTime(inputTime);
		pubkey.setObjectType(OBJECT_TYPE_PUBKEY);
		pubkey.setObjectVersion(inputAddressVersion);
		pubkey.setStreamNumber(inputStreamNumber);
		pubkey.setRipeHash(ByteFormatter.hexStringToByteArray(inputRipeHash));
		pubkey.setBehaviourBitfield(inputBehaviourBitfield);
		pubkey.setPublicSigningKey(publicSigningKey);
		pubkey.setPublicEncryptionKey(publicEncryptionKey);
		pubkey.setNonceTrialsPerByte(inputNonceTrialsPerByte);
		pubkey.setExtraBytes(inputExtraBytes);
		
		// Sign the Pubkey we just created.
		SigProcessor sigProc = new SigProcessor();
		byte[] signaturePayload = sigProc.createPubkeySignaturePayload(pubkey);
		byte[] bitsealSignature = sigProc.signWithWIFKey(signaturePayload, inputPrivateSigningKey);
		String signatureHex = ByteFormatter.byteArrayToHexString(bitsealSignature);
		Log.i(TAG, "Signature produced by Bitseal: \n" + signatureHex);
		Log.i(TAG, "Signature produced by PyBitmessage: \n" + pybitmessageSignatureHex);
		
		// Reconstruct the public signing key so that we can use it to verify the signatures.
		KeyConverter keyConv = new KeyConverter();
		ECPublicKey ecPublicSigningKey = keyConv.reconstructPublicKey(publicSigningKey);
		
		// Check that both signatures are valid. 
		boolean bitsealSignatureValid = (sigProc.verifySignature(signaturePayload, bitsealSignature, ecPublicSigningKey));
		boolean pybitmessageSignatureValid = (sigProc.verifySignature(signaturePayload, pybitmessageSignature, ecPublicSigningKey));
		
		if (bitsealSignatureValid && pybitmessageSignatureValid)
		{
			Log.i(TAG, "Both signatures are valid.");
		}
		if (bitsealSignatureValid == false)
		{
			Log.e(TAG, "Bitseal's signature is invalid");
		}
		if (pybitmessageSignatureValid == false)
		{
			Log.e(TAG, "PyBitmessage's signature is invalid");
		}
			
		assertTrue(bitsealSignatureValid && pybitmessageSignatureValid);
	}
}
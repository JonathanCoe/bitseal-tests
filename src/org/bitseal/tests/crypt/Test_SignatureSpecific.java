package org.bitseal.tests.crypt;

import junit.framework.TestCase;

import org.bitseal.crypt.KeyConverter;
import org.bitseal.crypt.SigProcessor;
import org.bitseal.data.Pubkey;
import org.bitseal.util.ByteFormatter;
import org.spongycastle.jce.interfaces.ECPublicKey;

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
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testSignatureSpecific()
	{
		// Address that this starting data derives from: BM-2cW7h5aUmvJC9WApzfWsXDcMCSqjPYWbLG
		
		// Starting data:
		String inputPrivateSigningKey = "5JNWmdv1jovwpeMer3sYX6mtJ1S4WeFqDkvLBW24RQbko966WZr";
		long inputTime = 1416396813;
		int inputAddressVersion = 4;
		int inputStreamNumber = 1;
		int inputBehaviourBitfield = 1;
		String inputPublicSigningKeyHex = "3f957a60845d1288bc1c06b10e8b1bd1425d4beb6ff612bd4521870d43c2e568462f77a004f5d33a8d9a4e0de5500f855b3a9ecf9ac5f418bc6d5ab25d8bb072";
		String inputPublicEncryptionKeyHex = "11475ba16bd378e7f1d4bbb37ed3afb7d5ac8a21737705e469636cb587b2859f30300d1b9f4f0d25373a87d0bfb072d81f81ca6e0a92a6b348789ada3aa64d3e";
		String inputRipeHash = "a5589e1196c5c1d9cd502272e8cc3b3c9de26a";
		int inputNonceTrialsPerByte = 1000;
		int inputExtraBytes = 1000;
		
		// PyBitmessage signature
		String pybitmessageSignatureHex = "304502203660cdca5470961956a8f273fab809399834a95bc2dfae4c414de8f75f1d7972022100dc6642727b9bbd75d83ae3f6812bb55981076a5ccf858051e989ce7bacb52762";
		
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
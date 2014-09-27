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
		// 		Starting data:
		// Address that this starting data derives from: BM-2cXdemXcNztvNhoGTau7Zpq1nf7odwUaKr
		
		String inputPrivateSigningKey = "5HzorSc8AJRNAbtg9WBfvxregwJunP6puBU9dQZvhZYY6zmRCmv";
		long inputTime = 1398890511;
		int inputAddressVersion = 4;
		int inputStreamNumber = 1;
		int inputBehaviourBitfield = 1;
		String inputPublicSigningKeyHex = "853b3e742b41f1c3c1c0ea6cfc230da982adc5c3e6a07e3ef30bdb1f4d2101e6df904a286aae9a482493d671c353584ebec8a95f1cd3bc0e6620633af2227c2d";
		String inputPublicEncryptionKeyHex = "e4fc38aac159547f26fb0ea78ea65e97764ea0f2f8957aa5edb83f54957ad0c48adba6b33fe32376875c0701f769307978c37e76bc1efc920c7bdc4c110904db";
		int inputNonceTrialsPerByte = 640;
		int inputExtraBytes = 14000;
		
		// Expected resulting signature
		String pybitmessageSignatureHex = "3046022100d84bb537198c3727f135a475bfa0aadf77aaa608cee2a6fdabda0042cfb1a2da02210091145a3a5ca984bbca5264045d77c4ad608525378fab271a9e381522c5181517";
		
		// First convert the public signing key, public encryption key, and PyBitmessage signature from hex to byte[] form
		byte[] publicSigningKey = ByteFormatter.hexStringToByteArray(inputPublicSigningKeyHex);
		byte[] publicEncryptionKey = ByteFormatter.hexStringToByteArray(inputPublicEncryptionKeyHex);
		byte[] pybitmessageSignature = ByteFormatter.hexStringToByteArray(pybitmessageSignatureHex);
		
		// Create a Pubkey object which we will sign.
		Pubkey pubkey = new Pubkey();
		pubkey.setTime(inputTime);
		pubkey.setAddressVersion(inputAddressVersion);
		pubkey.setStreamNumber(inputStreamNumber);
		pubkey.setBehaviourBitfield(inputBehaviourBitfield);
		pubkey.setPublicSigningKey(publicSigningKey);
		pubkey.setPublicEncryptionKey(publicEncryptionKey);
		pubkey.setNonceTrialsPerByte(inputNonceTrialsPerByte);
		pubkey.setExtraBytes(inputExtraBytes);
		
		// Sign the Pubkey we just created.
		SigProcessor sigProc = new SigProcessor();
		byte[] signaturePayload = sigProc.createPubkeySignaturePayload(pubkey);
		byte[] bitcloakSignature = sigProc.signWithWIFKey(signaturePayload, inputPrivateSigningKey);
		String signatureHex = ByteFormatter.byteArrayToHexString(bitcloakSignature);
		Log.i(TAG, "Signature produced by Bitcloak: \n" + signatureHex);
		Log.i(TAG, "Signature produced by PyBitmessage: \n" + pybitmessageSignatureHex);
		
		// Reconstruct the public signing key so that we can use it to verify the signatures.
		KeyConverter keyConv = new KeyConverter();
		ECPublicKey ecPublicSigningKey = keyConv.reconstructPublicKey(publicSigningKey);
		
		// Check that both signatures are valid. 
		boolean bitcloakSignatureValid = (sigProc.verifySignature(signaturePayload, bitcloakSignature, ecPublicSigningKey));
		boolean pybitmessageSignatureValid = (sigProc.verifySignature(signaturePayload, pybitmessageSignature, ecPublicSigningKey));
		
		if (bitcloakSignatureValid && pybitmessageSignatureValid)
		{
			Log.i(TAG, "Both signatures are valid.");
		}
		assertTrue(bitcloakSignatureValid && pybitmessageSignatureValid);
	}
}
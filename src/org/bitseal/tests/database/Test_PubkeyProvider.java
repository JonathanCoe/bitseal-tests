package org.bitseal.tests.database;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.bitseal.crypt.ECKeyPair;
import org.bitseal.data.Pubkey;
import org.bitseal.database.DatabaseContentProvider;
import org.bitseal.database.PubkeyProvider;
import org.bitseal.database.PubkeysTable;
import org.bitseal.util.ByteUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.SystemClock;
import android.test.AndroidTestCase;
import android.util.Base64;
import android.util.Log;

/**
 * Tests the functions of the PubkeyProvider class.<br><br>
 * 
 * Note: The use of AndroidTestCase is necessary in order to ensure that 
 * the application context will be available when the main body of the test 
 * runs. The static methods provided by the ApplicationContextProvider class
 * will often throw a null pointer exception if the app has only been running
 * for a very short time. 
 * 
 * @author Jonathan Coe
 */
public class Test_PubkeyProvider extends AndroidTestCase
{
	private static final String TAG = "TEST_PUBKEY_PROVIDER";
	
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
	
	// Note: This override of the setContext() method is necessary because AndroidTestCase
	// will in some cases return a null application context. 
	// See https://stackoverflow.com/questions/6516441/why-does-androidtestcase-getcontext-getapplicationcontext-return-null
	// Credit to James Wald on StackOverflow for this method
	@Override
    public void setContext(Context context) 
	{
        super.setContext(context);

        long endTime = SystemClock.elapsedRealtime() + TimeUnit.SECONDS.toMillis(2);

        while (null == context.getApplicationContext())
        {

            if (SystemClock.elapsedRealtime() >= endTime)
            {
                fail();
            }

            SystemClock.sleep(16);
        }
	 }
	
	@SuppressLint("TrulyRandom") // We've (hopefully) got this covered, using the PRNGFixes class from Google
	public void testPubkeyProvider()
	{
		// Now we should be able to get the application context safely
		PubkeyProvider provider = PubkeyProvider.get(getContext());
		
		//First clear out any existing records to ensure a fair test:
		provider.deleteAllPubkeys();
		
		// Test adding records:
		SecureRandom sr = new SecureRandom();
		byte[] powNonce0 = new byte[8];
		byte[] powNonce1 = new byte[8];
		byte[] powNonce2 = new byte[8];
		sr.nextBytes(powNonce0);
		sr.nextBytes(powNonce1);
		sr.nextBytes(powNonce2);
		long nonce0 = ByteUtils.bytesToLong(powNonce0);
		long nonce1 = ByteUtils.bytesToLong(powNonce1);
		long nonce2 = ByteUtils.bytesToLong(powNonce2);
		
		byte[] ripeHash0 = new String("afdgjk453sdGDDGDS").getBytes();		
		byte[] publicSigningKey0 = new ECKeyPair().getPubKey();
		byte[] publicEncryptionKey0 = new ECKeyPair().getPubKey();
		byte[] signature0 = new String("agfhaicrp9&FY*&DCDGYIfeiho3ihoiequrhf").getBytes();
		int signatureLength0 = signature0.length;
		
		byte[] ripeHash1 = new String("ertjk4sfhdGGFDSS").getBytes();	
		byte[] publicSigningKey1 = new ECKeyPair().getPubKey();
		byte[] publicEncryptionKey1 = new ECKeyPair().getPubKey();
		byte[] signature1 = new String("KHCcrp9345gfdIfeihoO&*VDHsd432gfdhbwe2r").getBytes();
		int signatureLength1 = signature0.length;
		
		String publicSigningKey1String = android.util.Base64.encodeToString(publicSigningKey1, Base64.DEFAULT);
		
		long currentTime = System.currentTimeMillis() / 1000;
		
		Pubkey record0 = new Pubkey();
		record0.setBelongsToMe(true);
		record0.setRipeHash(ripeHash0);
		record0.setPOWNonce(nonce0);
		record0.setExpirationTime(currentTime);
		record0.setObjectVersion(3);
		record0.setStreamNumber(1);
		record0.setBehaviourBitfield(3);
		record0.setPublicSigningKey(publicSigningKey0);
		record0.setPublicEncryptionKey(publicEncryptionKey0);
		record0.setNonceTrialsPerByte(1000);
		record0.setExtraBytes(1000);
		record0.setSignatureLength(signatureLength0);
		record0.setSignature(signature0);
			
		Pubkey record1 = new Pubkey();
		record1.setBelongsToMe(true);
		record1.setRipeHash(ripeHash1);
		record1.setPOWNonce(nonce1);
		record1.setExpirationTime(currentTime + 600);
		record1.setObjectVersion(4);
		record1.setStreamNumber(1);
		record1.setBehaviourBitfield(3);
		record1.setPublicSigningKey(publicSigningKey1);
		record1.setPublicEncryptionKey(publicEncryptionKey1);
		record1.setNonceTrialsPerByte(1000);
		record1.setExtraBytes(1000);
		record1.setSignatureLength(signatureLength1);
		record1.setSignature(signature1);
		
		Pubkey record2 = new Pubkey(); // Deliberate duplicate for testing - see below
		record2.setBelongsToMe(true);
		record2.setRipeHash(ripeHash1);
		record2.setPOWNonce(nonce1);
		record2.setExpirationTime(currentTime + 600);
		record2.setObjectVersion(4);
		record2.setStreamNumber(1);
		record2.setBehaviourBitfield(3);
		record2.setPublicSigningKey(publicSigningKey1);
		record2.setPublicEncryptionKey(publicEncryptionKey1);
		record2.setNonceTrialsPerByte(1000);
		record2.setExtraBytes(1000);
		record2.setSignatureLength(signatureLength1);
		record2.setSignature(signature1);
		
		provider.addPubkey(record0);
		provider.addPubkey(record1);
		provider.addPubkey(record2);
		
		// Test searching for records:
		ArrayList<Pubkey> searchResults0 = provider.searchPubkeys(PubkeysTable.COLUMN_PUBLIC_SIGNING_KEY, publicSigningKey1String);
		Pubkey result0 = searchResults0.get(0);
		int recordsRetrieved = searchResults0.size();
		
		Log.i(TAG, "Expected number of results from search: 2");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 2);
		
		Log.i(TAG, "Expected result (from first result): " + publicSigningKey1String);
		Log.i(TAG, "Actual result (from first result):   " + Base64.encodeToString(result0.getPublicSigningKey(), Base64.DEFAULT));
		assertEquals(Base64.encodeToString(result0.getPublicSigningKey(), Base64.DEFAULT), publicSigningKey1String);
		
		// Test getting all records:
		ArrayList<Pubkey> searchResults1 = provider.getAllPubkeys();
		recordsRetrieved = searchResults1.size();
		
		Log.i(TAG, "Expected number of records: 3");
		Log.i(TAG, "Actual number of records:   " + recordsRetrieved);
		assertEquals (recordsRetrieved, 3);
		
		// Test updating a record:
		result0.setPOWNonce(nonce2); // Let's update result0 - the first of the duplicate entries that we retrieved above
		provider.updatePubkey(result0);
		// Now let us retrieve the updated record and examine it to make sure that has been updated correctly
		ArrayList<Pubkey> searchResults2 = provider.searchPubkeys(PubkeysTable.COLUMN_POW_NONCE, String.valueOf(nonce2));
		Pubkey result1 = searchResults2.get(0);
		recordsRetrieved = searchResults2.size();
		
		Log.i(TAG, "Expected number of results from search: 1");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 1);
		
		Log.i(TAG, "Expected powNonce: " + nonce2);
		Log.i(TAG, "Actual powNonce  : " + result1.getPOWNonce());
		assertEquals(result1.getPOWNonce(), nonce2);
		
		// Test searching for a single record by its ID:
		Pubkey result2 = provider.searchForSingleRecord(result1.getId());
		Log.i(TAG, "Expected powNonce: " + nonce2);
		Log.i(TAG, "Actual powNonce  : " + result2.getPOWNonce());
		assertEquals(result2.getPOWNonce(), nonce2);
		
		// Test deleting a single record:
		provider.deletePubkey(result1); // Let us delete the address that we just updated
		
		// Now let us search for the record to make sure that it has in fact been deleted
		ArrayList<Pubkey> searchResults3 = provider.searchPubkeys(PubkeysTable.COLUMN_POW_NONCE, String.valueOf(nonce2));
		recordsRetrieved =searchResults3.size();
		
		Log.i(TAG, "Expected number of results from search: 0");
		Log.i(TAG, "Actual number of results from search:   " + recordsRetrieved);
		assertEquals(recordsRetrieved, 0);
		
		// Finally, delete the records we have added so that they don't mess up the rest of the application
		provider.deleteAllPubkeys();
	}
}
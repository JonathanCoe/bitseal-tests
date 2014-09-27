package org.bitseal.tests.core;

import junit.framework.TestCase;

import org.bitseal.core.App;
import org.bitseal.core.PubkeyProcessor;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.crypt.PubkeyGenerator;
import org.bitseal.data.Address;
import org.bitseal.data.Pubkey;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.PubkeyProvider;

/** 
 * Tests the method PubkeyProcessor.validatePubkey()
 * 
 * @author Jonathan Coe
**/
public class Test_ValidatePubkey extends TestCase
{	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testValidatePubkey()
	{
		// Generate several new addresses which we can use to create new pubkeys
		AddressGenerator addGen = new AddressGenerator();
		Address address0 = addGen.generateAndSaveNewAddress();
		Address address1 = addGen.generateAndSaveNewAddress();
		Address address2 = addGen.generateAndSaveNewAddress();
		Address address3 = addGen.generateAndSaveNewAddress();
		
		// Generate a new pubkey for the address we just created
		PubkeyGenerator pubGen = new PubkeyGenerator();
		Pubkey pubkey0 = pubGen.generateAndSaveNewPubkey(address0);
		Pubkey pubkey1 = pubGen.generateAndSaveNewPubkey(address1);
		Pubkey pubkey2 = pubGen.generateAndSaveNewPubkey(address2);
		Pubkey pubkey3 = pubGen.generateAndSaveNewPubkey(address3);
		
		// Validate the pubkeys against their addresses. 
		PubkeyProcessor pubProc = new PubkeyProcessor();	
		assertTrue(pubProc.validatePubkey(pubkey0, address0.getAddress()));
		assertTrue(pubProc.validatePubkey(pubkey1, address1.getAddress()));
		assertTrue(pubProc.validatePubkey(pubkey2, address2.getAddress()));
		assertTrue(pubProc.validatePubkey(pubkey3, address3.getAddress()));
		
		// Check that the validatePubkey() method is not giving false positives
		assertFalse(pubProc.validatePubkey(pubkey0, address3.getAddress()));
		assertFalse(pubProc.validatePubkey(pubkey1, address2.getAddress()));
		assertFalse(pubProc.validatePubkey(pubkey2, address1.getAddress()));
		assertFalse(pubProc.validatePubkey(pubkey3, address0.getAddress()));
		
		// Cleaning up - delete the addresses and pubkeys we created from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(address0);
		addProv.deleteAddress(address1);
		addProv.deleteAddress(address2);
		addProv.deleteAddress(address3);
		PubkeyProvider pubProv = PubkeyProvider.get(App.getContext());
		pubProv.deletePubkey(pubkey0);
		pubProv.deletePubkey(pubkey1);
		pubProv.deletePubkey(pubkey2);
		pubProv.deletePubkey(pubkey3);
	}
}
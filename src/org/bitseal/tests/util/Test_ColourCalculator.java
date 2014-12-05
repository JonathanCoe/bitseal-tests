package org.bitseal.tests.util;

import junit.framework.TestCase;

import org.bitseal.core.App;
import org.bitseal.crypt.AddressGenerator;
import org.bitseal.data.Address;
import org.bitseal.database.AddressProvider;
import org.bitseal.database.DatabaseContentProvider;
import org.bitseal.util.ColourCalculator;

import android.os.SystemClock;
import android.util.Log;

/**
 * Tests the ColourCalculator class.
 * 
 * @author Jonathan Coe
 */
public class Test_ColourCalculator extends TestCase
{
	private static final int NUMBER_OF_TEST_RUNS = 10;
	
	private static final String TAG = "TEST_COLOUR_CALCULATOR";
	
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
	
	public void testColourCalculator()
	{
		// Test with randomly generated Bitmessage addresses
		for (int i = 0; i < NUMBER_OF_TEST_RUNS; i++)
		{
			AddressGenerator addGen = new AddressGenerator();
			Address address = addGen.generateAndSaveNewAddress();
			
			try
			{
				int[] values = ColourCalculator.calculateColoursFromAddress(address.getAddress());
				
				Log.d(TAG, "Generated the following values for address " + address.getAddress() + ": " + values[0] + " " + values[1] + " " + values[2]);
			}
			catch (Exception e)
			{
				Log.e(TAG, "Exception occurred in Test_ColourCalculator! The exception message was:\n" + 
						e.getMessage());
				fail("Test_ColourCalculator failed");
			}
			
			// Remove the generated data from the database
			AddressProvider addProv = AddressProvider.get(App.getContext());
			addProv.deleteAddress(address);
		}
		
		// Generate one new Bitmessage address, then check that we always get the same result for it
		AddressGenerator addGen = new AddressGenerator();
		Address address = addGen.generateAndSaveNewAddress();
		int[] originalValues = ColourCalculator.calculateColoursFromAddress(address.getAddress());
		int originalR = originalValues[0];
		int originalG = originalValues[1];
		int originalB = originalValues[2];
		
		for (int i = 0; i < NUMBER_OF_TEST_RUNS; i++)
		{
			try
			{
				int[] values = ColourCalculator.calculateColoursFromAddress(address.getAddress());
				int r = values[0];
				int g = values[1];
				int b = values[2];
				
				assertEquals(r, originalR);
				assertEquals(g, originalG);
				assertEquals(b, originalB);
				
				Log.d(TAG, "Generated the following values for address " + address.getAddress() + ": " + values[0] + " " + values[1] + " " + values[2]);
			}
			catch (Exception e)
			{
				Log.e(TAG, "Exception occurred in Test_ColourCalculator! The exception message was:\n" + 
						e.getMessage());
				fail("Test_ColourCalculator failed");
			}
		}
		
		// Remove the generated data from the database
		AddressProvider addProv = AddressProvider.get(App.getContext());
		addProv.deleteAddress(address);
	}
}
package org.bitseal.tests.core;

import junit.framework.TestCase;

import org.bitseal.core.BehaviourBitfieldProcessor;

/**
 * Tests the BehaviourBitfieldProcessor class
 * 
 * @author Jonathan Coe
 *
 */
public class Test_BehaviourBitfieldProcessor extends TestCase
{
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
	
	public void testBehaviourBitfieldProcessor()
	{
		int myBehaviourBitfield = BehaviourBitfieldProcessor.getBitfieldForMyPubkeys();
		
		boolean sendsAcks = BehaviourBitfieldProcessor.checkSendsAcks(myBehaviourBitfield);
		
		assertTrue(sendsAcks); // Currently this is the only flag we should have set in the Behaviour Bitfields we generate.
	}
}
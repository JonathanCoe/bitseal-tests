package org.bitseal.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.bitseal.tests.core.Test_BehaviourBitfieldProcessor;
import org.bitseal.tests.core.Test_CalculateDoubleHashOfAddressData;
import org.bitseal.tests.core.Test_CalculateMessageTag;
import org.bitseal.tests.core.Test_ConstructUnencryptedMsg;
import org.bitseal.tests.core.Test_ExtractMessageFromUnencryptedMsg;
import org.bitseal.tests.core.Test_ExtractRipeHashFromAddressString;
import org.bitseal.tests.core.Test_RetrievePubkeyByAddressString;
import org.bitseal.tests.core.Test_ValidatePubkey;
import org.bitseal.tests.crypt.Test_DecryptionBasic;
import org.bitseal.tests.crypt.Test_DecryptionSpecific;
import org.bitseal.tests.crypt.Test_EncodePrivateKeysInWIF;
import org.bitseal.tests.crypt.Test_EncryptPubkeySpecific;
import org.bitseal.tests.crypt.Test_EncryptionBasic;
import org.bitseal.tests.crypt.Test_EncryptionSpecific;
import org.bitseal.tests.crypt.Test_GenerateAddress;
import org.bitseal.tests.crypt.Test_SignatureBasic;
import org.bitseal.tests.crypt.Test_SignatureSpecific;
import org.bitseal.tests.database.Test_AddressBookRecordProvider;
import org.bitseal.tests.database.Test_AddressProvider;
import org.bitseal.tests.database.Test_MessageProvider;
import org.bitseal.tests.database.Test_PayloadProvider;
import org.bitseal.tests.database.Test_PubkeyProvider;
import org.bitseal.tests.database.Test_QueueRecordProvider;
import org.bitseal.tests.database.Test_ServerRecordProvider;
import org.bitseal.tests.services.Test_SortQueueRecords;
import org.bitseal.tests.util.Test_ColourCalculator;
import org.bitseal.tests.util.Test_RemoveBytesFromArray;
import org.bitseal.tests.util.Test_TimeUtils;
import org.bitseal.tests.util.Test_VarintEncoding;

/**
 * Runs through all the unit tests which do not depend on proof of work
 * or a network connection to pass, and can thus be completed quickly.<br><br>
 * 
 * NOTE: To make this set of tests run quickly, use the 'fake POW' code in POWProcessor.
 * This sets the POW nonce as a random value. 
 * 
 * @author Jonathan Coe
 */
public class Test_RunQuickTests
{
	public static Test suite()
	{
		TestSuite suite = new TestSuite(Test_RunQuickTests.class.getName());
		
		// Tests from "core" package:
		suite.addTestSuite(Test_BehaviourBitfieldProcessor.class);
		suite.addTestSuite(Test_CalculateDoubleHashOfAddressData.class);
		suite.addTestSuite(Test_CalculateMessageTag.class);
		suite.addTestSuite(Test_ConstructUnencryptedMsg.class);
		suite.addTestSuite(Test_ExtractMessageFromUnencryptedMsg.class);
		suite.addTestSuite(Test_ExtractRipeHashFromAddressString.class);
		suite.addTestSuite(Test_RetrievePubkeyByAddressString.class);
		suite.addTestSuite(Test_ValidatePubkey.class);
		
		// Tests from "crypt" package:
		suite.addTestSuite(Test_DecryptionBasic.class);
		suite.addTestSuite(Test_DecryptionSpecific.class);
		suite.addTestSuite(Test_EncodePrivateKeysInWIF.class);
		suite.addTestSuite(Test_EncryptionBasic.class);
		suite.addTestSuite(Test_EncryptionSpecific.class);
		suite.addTestSuite(Test_EncryptPubkeySpecific.class);
		suite.addTestSuite(Test_GenerateAddress.class);
		suite.addTestSuite(Test_SignatureBasic.class);
		suite.addTestSuite(Test_SignatureSpecific.class);
		
		// Tests from "database" package:
		suite.addTestSuite(Test_AddressBookRecordProvider.class);
		suite.addTestSuite(Test_AddressProvider.class);
		suite.addTestSuite(Test_MessageProvider.class);
		suite.addTestSuite(Test_PayloadProvider.class);
		suite.addTestSuite(Test_PubkeyProvider.class);
		suite.addTestSuite(Test_QueueRecordProvider.class);
		suite.addTestSuite(Test_ServerRecordProvider.class);
		
		// Tests from "services" package
		suite.addTestSuite(Test_SortQueueRecords.class);
		
		// Tests from "util" package
		suite.addTestSuite(Test_ColourCalculator.class);
		suite.addTestSuite(Test_RemoveBytesFromArray.class);
		suite.addTestSuite(Test_TimeUtils.class);
		suite.addTestSuite(Test_VarintEncoding.class);
		
		return suite;
	}
}
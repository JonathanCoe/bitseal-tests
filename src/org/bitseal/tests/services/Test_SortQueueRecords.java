package org.bitseal.tests.services;

import java.util.ArrayList;
import java.util.Collections;

import junit.framework.TestCase;

import org.bitseal.data.QueueRecord;

import android.util.Log;

/** 
 * This class tests the application's ability to properly sort QueueRecords.
 * 
 * @author Jonathan Coe
**/
public class Test_SortQueueRecords extends TestCase
{
	// The tasks for performing the first major function of the application: sending messages
	public static final String TASK_SEND_MESSAGE = "sendMessage";
	public static final String TASK_PROCESS_OUTGOING_MESSAGE = "processOutgoingMessage";
	public static final String TASK_DISSEMINATE_MESSAGE = "disseminateMessage";
	
	// The tasks for performing the second major function of the application: receiving messages
	public static final String TASK_PROCESS_INCOMING_MSGS_AND_SEND_ACKS = "processIncomingMsgsAndSendAcks";
	public static final String TASK_SEND_ACKS = "sendAcks";
	
	// The tasks for performing the third major function of the application: creating a new identity
	public static final String TASK_CREATE_IDENTITY = "createIdentity";
	public static final String TASK_DISSEMINATE_PUBKEY = "disseminatePubkey";
	
	// Constant values for blank "Object Id" and "Object Type" fields in QueueRecords
	private static final int QUEUE_RECORD_BLANK_OBJECT_ID = 0;
	private static final String QUEUE_RECORD_BLANK_OBJECT_TYPE = "0";
	
	// Constant values for the "Object Type" Strings in QueueRecords
	public static final String QUEUE_RECORD_OBJECT_TYPE_MESSAGE = "Message";
	public static final String QUEUE_RECORD_OBJECT_TYPE_MSG_SET = "MsgSet";
	public static final String QUEUE_RECORD_OBJECT_TYPE_PUBKEY = "Pubkey";
	public static final String QUEUE_RECORD_OBJECT_TYPE_PAYLOAD = "Payload";
	
	private static final String TAG = "TEST_SORT_QUEUE_RECORDS";
	
	protected void setUp() throws Exception
	{
		super.setUp();
	}

	protected void tearDown() throws Exception
	{
		super.tearDown();
	}
		
	public void testSortQueueRecords()
	{
		// Create several new QueueRecords with decreasing (earlier)
		// values for 'last attempt time'
		long currentTime = System.currentTimeMillis() / 1000;
		
		QueueRecord qr0 = new QueueRecord();
		qr0.setTask(TASK_CREATE_IDENTITY);
		qr0.setLastAttemptTime(currentTime);
		qr0.setAttempts(0);
		qr0.setObject0Id(QUEUE_RECORD_BLANK_OBJECT_ID);
		qr0.setObject0Type(QUEUE_RECORD_BLANK_OBJECT_TYPE);
		qr0.setObject1Id(QUEUE_RECORD_BLANK_OBJECT_ID);
		qr0.setObject1Type(QUEUE_RECORD_BLANK_OBJECT_TYPE);
		
		QueueRecord qr1 = new QueueRecord();
		qr1.setTask(TASK_SEND_ACKS);
		qr1.setLastAttemptTime(currentTime - 600); // Ten minutes in the past
		qr1.setAttempts(0);
		qr1.setObject0Id(QUEUE_RECORD_BLANK_OBJECT_ID);
		qr1.setObject0Type(QUEUE_RECORD_BLANK_OBJECT_TYPE);
		qr1.setObject1Id(QUEUE_RECORD_BLANK_OBJECT_ID);
		qr1.setObject1Type(QUEUE_RECORD_BLANK_OBJECT_TYPE);
		
		QueueRecord qr2 = new QueueRecord();
		qr2.setTask(TASK_SEND_MESSAGE);
		qr2.setLastAttemptTime(currentTime - 1200); // Twenty minutes in the past
		qr2.setAttempts(0);
		qr2.setObject0Id(5);
		qr2.setObject0Type(QUEUE_RECORD_OBJECT_TYPE_MESSAGE);
		qr2.setObject1Id(QUEUE_RECORD_BLANK_OBJECT_ID);
		qr2.setObject1Type(QUEUE_RECORD_BLANK_OBJECT_TYPE);
		
		QueueRecord qr3 = new QueueRecord();
		qr3.setTask(TASK_DISSEMINATE_PUBKEY);
		qr3.setLastAttemptTime(currentTime - 1800); // Thirty minutes in the past
		qr3.setAttempts(0);
		qr3.setObject0Id(62);
		qr3.setObject0Type(QUEUE_RECORD_OBJECT_TYPE_PUBKEY);
		qr3.setObject1Id(QUEUE_RECORD_BLANK_OBJECT_ID);
		qr3.setObject1Type(QUEUE_RECORD_BLANK_OBJECT_TYPE);
		
		// Create an ArrayList to hold the QueueRecords
		ArrayList<QueueRecord> queueRecords = new ArrayList<QueueRecord>();
		
		// Add the QueueRecords to the list in the order that they were created. 
		queueRecords.add(qr0);
		queueRecords.add(qr1);
		queueRecords.add(qr2);
		queueRecords.add(qr3);
		
		Log.i(TAG, "Last attempt time of QueueRecord at position 0 before sort: " + queueRecords.get(0).getLastAttemptTime());
		Log.i(TAG, "Last attempt time of QueueRecord at position 1 before sort: " + queueRecords.get(1).getLastAttemptTime());
		Log.i(TAG, "Last attempt time of QueueRecord at position 2 before sort: " + queueRecords.get(2).getLastAttemptTime());
		Log.i(TAG, "Last attempt time of QueueRecord at position 3 before sort: " + queueRecords.get(3).getLastAttemptTime());
		
		// Now attempt to sort the QueueRecords in the list and check if the resulting
		// order is correct (earliest 'last attempt time' should come first)
		Collections.sort(queueRecords);
		assertEquals(queueRecords.get(0), qr3);
		assertEquals(queueRecords.get(1), qr2);
		assertEquals(queueRecords.get(2), qr1);
		assertEquals(queueRecords.get(3), qr0);
		
		Log.i(TAG, "Last attempt time of QueueRecord at position 0 after sort: " + queueRecords.get(0).getLastAttemptTime());
		Log.i(TAG, "Last attempt time of QueueRecord at position 1 after sort: " + queueRecords.get(1).getLastAttemptTime());
		Log.i(TAG, "Last attempt time of QueueRecord at position 2 after sort: " + queueRecords.get(2).getLastAttemptTime());
		Log.i(TAG, "Last attempt time of QueueRecord at position 3 after sort: " + queueRecords.get(3).getLastAttemptTime());
		
		// Now clear the list, add the QueueRecords in a different order, then sort the list
		// again and check that the sorted order is still correct
		queueRecords.clear();
		queueRecords.add(qr2);
		queueRecords.add(qr3);
		queueRecords.add(qr1);
		queueRecords.add(qr0);
		
		Log.i(TAG, "Last attempt time of QueueRecord at position 0 before sort: " + queueRecords.get(0).getLastAttemptTime());
		Log.i(TAG, "Last attempt time of QueueRecord at position 1 before sort: " + queueRecords.get(1).getLastAttemptTime());
		Log.i(TAG, "Last attempt time of QueueRecord at position 2 before sort: " + queueRecords.get(2).getLastAttemptTime());
		Log.i(TAG, "Last attempt time of QueueRecord at position 3 before sort: " + queueRecords.get(3).getLastAttemptTime());
		
		Collections.sort(queueRecords);
		assertEquals(queueRecords.get(0), qr3);
		assertEquals(queueRecords.get(1), qr2);
		assertEquals(queueRecords.get(2), qr1);
		assertEquals(queueRecords.get(3), qr0);
		
		Log.i(TAG, "Last attempt time of QueueRecord at position 0 after sort: " + queueRecords.get(0).getLastAttemptTime());
		Log.i(TAG, "Last attempt time of QueueRecord at position 1 after sort: " + queueRecords.get(1).getLastAttemptTime());
		Log.i(TAG, "Last attempt time of QueueRecord at position 2 after sort: " + queueRecords.get(2).getLastAttemptTime());
		Log.i(TAG, "Last attempt time of QueueRecord at position 3 after sort: " + queueRecords.get(3).getLastAttemptTime());
	}
}
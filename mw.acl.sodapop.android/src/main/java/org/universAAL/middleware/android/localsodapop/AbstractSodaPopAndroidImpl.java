/**
 * 
 *  OCO Source Materials 
 *      © Copyright IBM Corp. 2012 
 *
 *      See the NOTICE file distributed with this work for additional 
 *      information regarding copyright ownership 
 *       
 *      Licensed under the Apache License, Version 2.0 (the "License"); 
 *      you may not use this file except in compliance with the License. 
 *      You may obtain a copy of the License at 
 *       	http://www.apache.org/licenses/LICENSE-2.0 
 *       
 *      Unless required by applicable law or agreed to in writing, software 
 *      distributed under the License is distributed on an "AS IS" BASIS, 
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *      See the License for the specific language governing permissions and 
 *      limitations under the License. 
 *
 */
package org.universAAL.middleware.android.localsodapop;

import java.util.Collection;
import java.util.List;

import org.universAAL.middleware.acl.SodaPopPeer;
import org.universAAL.middleware.android.common.IAndroidBus;
import org.universAAL.middleware.android.common.IAndroidSodaPop;
import org.universAAL.middleware.android.common.StringUtils;
import org.universAAL.middleware.android.localsodapop.encryption.AndroidBase64;
import org.universAAL.middleware.android.localsodapop.intents.LocalSodaPopIntentFactory;
import org.universAAL.middleware.android.localsodapop.persistence.SodaPopPeersSQLiteMngr;
import org.universAAL.middleware.android.localsodapop.persistence.tables.rows.ContactedPeerRowDB;
import org.universAAL.middleware.android.localsodapop.persistence.tables.rows.LocalBusRowDB;
import org.universAAL.middleware.android.localsodapop.persistence.tables.rows.LocalPeerInfoRowDB;
import org.universAAL.middleware.android.localsodapop.persistence.tables.rows.PeerRowDB;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.bus.model.AbstractBus;
import org.universAAL.middleware.sodapop.SodaPop;
import org.universAAL.middleware.sodapop.impl.CryptUtil;
import org.universAAL.middleware.bus.msg.Message;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.bus.msg.PeerIDGenerator;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * @author <a href="mailto:noamsh@il.ibm.com">noamsh </a>
 * 
 *         Apr 5, 2012
 * 
 */
public abstract class AbstractSodaPopAndroidImpl implements SodaPop, SodaPopPeer {

    private static Object peersTableSync = new Object();
    private static Object localBusesTableSync = new Object();
    private static Object coordinatorTableSync = new Object();

    private static final String TAG = AbstractSodaPopAndroidImpl.class.getCanonicalName();

    private Context context;
    private SodaPopPeersSQLiteMngr sqliteMngr;

    // Indicates if to initialize the crypt utils. It is a static field.
    // Therefore if the app will be unloaded the filed will be false again and
    // crypt utils will be initialized again
    private static boolean cryptUtilsWasInitialized = false;

    public AbstractSodaPopAndroidImpl(Context context, SodaPopPeersSQLiteMngr sqliteMngr) {
	super();
	this.context = context;
	this.sqliteMngr = sqliteMngr;

	initCryptUtils();
    }

    private void initCryptUtils() {
	try {
	    if (!cryptUtilsWasInitialized) {
		CryptUtil.init(IAndroidSodaPop.CONFIG_HOME_PATH, new AndroidBase64());
		cryptUtilsWasInitialized = true;
	    }
	} catch (Exception e) {
	    Log.e(TAG, "Unable to initialize crypt utils due to [" + e.getMessage() + "]");
	}
    }

    public Context getContext() {
	return context;
    }

    public void addLocalPeer() {
	// Open
	sqliteMngr.open();

	try {
	    LocalPeerInfoRowDB localPeer = sqliteMngr.queryLocalPeerInfo();
	    if (null == localPeer) {
		localPeer = sqliteMngr.setLocalPeerInfo(PeerIDGenerator.generatePeerID());

		// Add the local peer to the peers table - otherwise this peer
		// already exists
		sqliteMngr.addPeer(localPeer.getLocalPeerID(), getProtocol(), true, true);
	    }
	    if (null == System.getProperty(PeerIDGenerator.SYS_PROPERTY_SODAPOP_PEER_ID)) {
		// Make sure that the system property was set
		System.setProperty(PeerIDGenerator.SYS_PROPERTY_SODAPOP_PEER_ID,
			localPeer.getLocalPeerID());
	    }
	} finally {
	    // Close
	    sqliteMngr.close();
	}
    }

    /**
     * Return the local peer ID
     */
    public String getID() {
	// Open
	sqliteMngr.open();

	try {
	    LocalPeerInfoRowDB localPeer = sqliteMngr.queryLocalPeerInfo();
	    return localPeer.getLocalPeerID();
	} finally {
	    // Close
	    sqliteMngr.close();
	}

    }

    public void noticeNewPeer(String peerID, String protocol) {
	synchronized (peersTableSync) {
	    // Open
	    sqliteMngr.open();

	    try {
		// Add the peer (the remote one)
		PeerRowDB remotePeer = sqliteMngr.addPeer(peerID, protocol, true, false);

		// Query for the local one
		LocalPeerInfoRowDB localPeerInfo = sqliteMngr.queryLocalPeerInfo();

		// Query for the local peer
		PeerRowDB localPeer = sqliteMngr.queryPeerByPeerID(localPeerInfo.getLocalPeerID());

		// Extract the bus names
		String busNames = localPeer.getFormattedBussesNames();

		// Check if the local peer ID is lower than the remote one, if
		// yes - send the remote the local busses
		if (localPeerInfo.getLocalPeerID().compareTo(peerID) < 0) {
		    // Send intent
		    Intent noticePeerBusses = LocalSodaPopIntentFactory
			    .createNoticeLocalSodaPopPeerBuses(peerID, localPeer.getPeerID(),
				    getProtocol(), busNames);
		    context.sendBroadcast(noticePeerBusses);

		    // Add the peer to the contacted peers
		    sqliteMngr.addContactedPeer(peerID);
		} else {
		    // Check if the busses were received for the remote peer, if
		    // yes reply him with the local busses
		    if (remotePeer.isReceivedBusses()) {
			// Send intent
			Intent replyPeerBusses = LocalSodaPopIntentFactory
				.createLocalReplyPeerBusses(peerID, localPeer.getPeerID(),
					getProtocol(), busNames);
			context.sendBroadcast(replyPeerBusses);

			// Add the peer to the contacted peers
			sqliteMngr.addContactedPeer(peerID);
		    }
		}
	    } finally {
		// Close
		sqliteMngr.close();
	    }
	}
    }

    public void noticeLeavePeer(String peerID) {
	synchronized (peersTableSync) {
	    // Open
	    sqliteMngr.open();

	    try {
		sqliteMngr.removePeerByID(peerID);
	    } finally {
		// Close
		sqliteMngr.close();
	    }

	}
    }

    /**
     * This method is invoked by a remote peer via a broadcast message to inform
     * our SodaPop layer that this remote peer joined the bus
     * 
     * @param String
     *            busName
     * @param String
     *            joiningPeer - the joining peer ID
     * 
     * @see org.universAAL.middleware.acl.SodaPopPeer#joinBus(String, String)
     */
    public void joinBus(String busName, String joiningPeer) {
	synchronized (peersTableSync) {
	    // Open
	    sqliteMngr.open();

	    try {
		sqliteMngr.addBusesToPeer(joiningPeer, getProtocol(), new String[] { busName });
	    } finally {
		// Close
		sqliteMngr.close();
	    }
	}
    }

    /**
     * This method is invoked by a remote peer via a broadcast message to inform
     * our SodaPop layer that this remote peer left the bus
     * 
     * @param String
     *            busName
     * @param String
     *            leavingPeer - the leaving peer ID
     * 
     * @see org.universAAL.middleware.acl.SodaPopPeer#leaveBus(String, String)
     */
    public void leaveBus(String busName, String leavingPeer) {
	synchronized (peersTableSync) {
	    // Open
	    sqliteMngr.open();

	    try {
		sqliteMngr.removeBus(leavingPeer, busName);
	    } finally {
		// Close
		sqliteMngr.close();
	    }
	}
    }

    /**
     * This method is invoked by remote peers via a broadcast message to inform
     * our local peer about their buses
     * 
     * @param String
     *            peerID
     * @param String
     *            busNames - the names of the buses separated by commas
     * 
     * @see org.universAAL.middleware.acl.SodaPopPeer#noticePeerBusses(String,
     *      String)
     */
    public void noticePeerBusses(String peerID, String busNames) {
	synchronized (peersTableSync) {
	    // Open
	    sqliteMngr.open();

	    try {
		// Add the busses
		PeerRowDB peer = sqliteMngr.addBusesToPeer(peerID, getProtocol(),
			busNames.split(","));

		// Check if the remote peer was already discovered, only then
		// send the remote peer the local busses
		if (peer.isDiscovered()) {
		    PeerRowDB localPeer = getLocalPeer();

		    Intent replyPeerBusses = LocalSodaPopIntentFactory.createLocalReplyPeerBusses(
			    peerID, localPeer.getPeerID(), localPeer.getProtocol(), busNames);
		    context.sendBroadcast(replyPeerBusses);
		}
	    } finally {
		// Close
		sqliteMngr.close();
	    }
	}
    }

    /**
     * This method is invoked by a remote peer via a broadcast message to send a
     * message to our local bus instance
     * 
     * @param String
     *            busName
     * @param String
     *            msg
     * 
     * @see org.universAAL.middleware.acl.SodaPopPeer#processBusMessage(String,
     *      String)
     */
    public void processBusMessage(String busName, String msg) {

	// Open
	sqliteMngr.open();

	try {
	    // Get the local bus by name from the DB
	    LocalBusRowDB localBus = sqliteMngr.queryForLocalBusByName(busName);

	    if (null == localBus) {
		Log.w(TAG, "No bus [" + busName + "] exists!");
		return;
	    }

	    // Decrypt the message
	    String decryptedMsg;
	    try {
		Log.d(TAG, "Got encrypted message [" + msg + "]");
		decryptedMsg = CryptUtil.decrypt(msg);
	    } catch (Exception e) {
		Log.e(TAG, "Unable to decrypt the message due to [" + e.getMessage() + "]");
		return; // TODO: consider how to notify the user on the error
	    }

	    // Using reflection to initiate the corresponding bus android
	    // service + populate the intent
	    Intent androidBusServiceIntent = LocalSodaPopIntentFactory.createProcessBusMessage(
		    decryptedMsg, context.getPackageName(), localBus.getBusClassName(),
		    getProtocol()); // TODO:
				    // get
				    // rid
				    // of
				    // the
				    // package
				    // name
				    // of
				    // the
				    // bus

	    // Start the service and forward the created intent
	    context.startService(androidBusServiceIntent);
	} finally {
	    // Close
	    sqliteMngr.close();
	}
    }

    /**
     * This method is invoked by remote peers (via our exporting connectors) to
     * inform our local peer about their buses, as a reply to our notification
     * to them about our buses
     * 
     * @param String
     *            peerID
     * @param String
     *            busNames - the names of the buses separated by commas
     * 
     * @see org.universAAL.middleware.acl.SodaPopPeer#noticePeerBusses(String,
     *      String)
     */
    public void replyPeerBusses(String peerID, String busNames) {
	synchronized (peersTableSync) {
	    // Open
	    sqliteMngr.open();

	    try {
		sqliteMngr.addBusesToPeer(peerID, getProtocol(), busNames.split(","));

		// Add the peer to the contacted peers
		sqliteMngr.addContactedPeer(peerID);
	    } finally {
		// Close
		sqliteMngr.close();
	    }
	}
    }

    public MessageContentSerializer getContentSerializer(ModuleContext arg0) {
	return null;
    }

    public AbstractBus getLocalBusByName(String busName) {
	return null;
    }

    public void join(AbstractBus bus) {
	// Cast to the interface
	IAndroidBus androidBus = (IAndroidBus) bus;

	// Open
	sqliteMngr.open();

	try {
	    // Query for the local peer
	    PeerRowDB localPeer = getLocalPeer();

	    synchronized (localBusesTableSync) {
		// Add the local bus
		sqliteMngr.addLocalBus(androidBus.getName(), androidBus.getPackageName(),
			androidBus.getClassName());
		sqliteMngr.addBusesToPeer(localPeer.getPeerID(), getProtocol(),
			new String[] { androidBus.getName() });
	    }

	    // Notify the contacted peers
	    synchronized (peersTableSync) {
		// Get the contacted peers as comma separated values
		String contactedPeers = contactedPeersToString();

		if (!StringUtils.isEmpty(contactedPeers)) {
		    Intent joinBusContactedPeers = LocalSodaPopIntentFactory.createJoinLocalBus(
			    contactedPeers, localPeer.getPeerID(), localPeer.getProtocol(),
			    androidBus.getName());
		    context.sendBroadcast(joinBusContactedPeers);
		}
	    }
	} finally {
	    // Close
	    sqliteMngr.close();
	}
    }

    public void leave(AbstractBus bus) {
	// Cast to the interface
	IAndroidBus androidBus = (IAndroidBus) bus;

	// Open
	sqliteMngr.open();

	try {
	    synchronized (localBusesTableSync) {
		sqliteMngr.removeLocalBus(androidBus.getName());
	    }

	    // Notify the contacted peers
	    synchronized (peersTableSync) {
		// Get the contacted peers as comma separated values
		String contactedPeers = contactedPeersToString();

		if (!StringUtils.isEmpty(contactedPeers)) {
		    // Query for the local peer
		    PeerRowDB localPeer = getLocalPeer();

		    Intent leaveBusContactedPeers = LocalSodaPopIntentFactory.createLeaveLocalBus(
			    contactedPeers, localPeer.getPeerID(), localPeer.getProtocol(),
			    androidBus.getName());
		    context.sendBroadcast(leaveBusContactedPeers);
		}
	    }
	} finally {
	    // Close
	    sqliteMngr.close();
	}
    }

    /**
     * This method sends a message from a local bus instance to remote peers on
     * the bus passed as the first parameter
     * 
     * @param AbstractBus
     *            bus
     * @param Message
     *            msh
     * 
     * @see org.universAAL.middleware.sodapop.SodaPop#propagateMessage(AbstractBus,
     *      Message)
     */
    public int propagateMessage(AbstractBus bus, Message msg) {
	int numOfReceivers = 0;

	// Get the local ID
	String localID = getID();

	// Validate the message and verify that the message is addressed to the
	// local peer
	if (null == msg || !localID.equals(msg.getSource())) {
	    Log.w(TAG,
		    "Local ID [" + localID + "] is not identical to the message source ["
			    + msg.getSource() + "]");
	    return numOfReceivers;
	}

	Log.d(TAG, "Is about to propagateMessage for bus [" + bus.getBusName() + "]");

	// Open
	sqliteMngr.open();

	try {
	    String busName = bus.getBusName();
	    String msgAsStr = msg.toString();
	    String cipher = msgAsStr;
	    String[] receivers = msg.getReceivers(); // Extract the receivers
						     // from the message
	    StringBuffer csvReceivers = new StringBuffer();

	    synchronized (peersTableSync) {
		// Extract the remote peers with the given bus name
		Collection<PeerRowDB> peersWithGivenBus = sqliteMngr
			.queryForAllPeersByBusName(busName);

		// Only if there are remote peers with the given bus send them
		// the message
		if (!peersWithGivenBus.isEmpty()) {
		    numOfReceivers = getReceiversToSendThePropagteMessageTo(peersWithGivenBus,
			    receivers, csvReceivers, localID);

		    // Send the message if result is greater then 0
		    if (numOfReceivers > 0) {
			// Encrypt the message
			try {
			    cipher = CryptUtil.encrypt(msgAsStr);
			    Log.d(TAG, "The message has been encrypted to [" + cipher + "]");
			} catch (Exception e) {
			    Log.e(TAG, "Unable to encrypt the message due to [" + e.getMessage()
				    + "]");
			    return numOfReceivers; // TODO: consider how to
						   // notify the user on the
						   // error
			}

			// Create intent
			Intent intent = LocalSodaPopIntentFactory.createPropagateBusMessage(
				csvReceivers.toString(), busName, cipher);

			// Broadcast
			context.sendBroadcast(intent);
		    }
		}
	    }

	    Log.i(TAG, "Message [" + msg.getID() + "] has been sent to [" + numOfReceivers
		    + "] receivers" + (0 != csvReceivers.length() ? " [" + csvReceivers + "]" : "")); // Add
												      // the
												      // receivers
												      // to
												      // the
												      // log
												      // message
												      // if
												      // there
												      // are
												      // any...

	    return numOfReceivers;
	} finally {
	    // Close
	    sqliteMngr.close();
	}
    }

    /**
     * 
     * @param peersWithGivenBus
     * @param receiversFromMessage
     * @param csvReceivers
     * @param localPeerID
     *            to exclude the local ID from the receivers list
     * @return
     */
    private int getReceiversToSendThePropagteMessageTo(Collection<PeerRowDB> peersWithGivenBus,
	    String[] receiversFromMessage, StringBuffer csvReceivers, String localPeerID) {
	int result = 0;

	// For each receiver check if the receiver has the given bus
	if (null != receiversFromMessage && receiversFromMessage.length != 0) {
	    for (String curReceiver : receiversFromMessage) {
		// Check if equal to the local peer ID - we don't want to send
		// to the local...
		if (curReceiver.equals(localPeerID)) {
		    continue;
		}

		boolean receiverHasBus = checkIfPeerIDExistInList(peersWithGivenBus, curReceiver);
		if (receiverHasBus) {
		    // The receiver has the bus, add it to string
		    csvReceivers.append(curReceiver + ",");
		    result++;
		}
	    }
	    // Cut the last ","
	    csvReceivers.delete(csvReceivers.length() - ",".length(), csvReceivers.length());
	} // Else - no receiver is specified in the message - the csvReceivers
	  // string will remain empty - this means - send all peers that have
	  // the bus
	else {
	    result = peersWithGivenBus.size();
	    if (doesPeersListContainGivenPeer(peersWithGivenBus, localPeerID)) {
		result--; // Reduce the local peer...
	    }
	    String peersIds = peersToString(peersWithGivenBus, localPeerID);
	    csvReceivers.append(peersIds);
	}

	return result;
    }

    private boolean doesPeersListContainGivenPeer(Collection<PeerRowDB> peers, String peerID) {
	boolean contain = false;

	for (PeerRowDB checkedPeer : peers) {
	    if (checkedPeer.getPeerID().equals(peerID)) {
		contain = true;
		break;
	    }
	}

	return contain;
    }

    public void printStatus() {
	// Open
	sqliteMngr.open();

	try {
	    Log.i(TAG, "");

	    Log.i(TAG, "localPeerInfo");
	    Log.i(TAG, sqliteMngr.queryLocalPeerInfo().toString());
	    Log.i(TAG, "===========\n");

	    Log.i(TAG, "localBusses");
	    int i = 1;
	    for (LocalBusRowDB busRow : sqliteMngr.queryForAllLocalBuses()) {
		Log.i(TAG, "	LocalBus no.[" + (i++) + "]: " + busRow.toString() + "]");
	    }
	    Log.i(TAG, "===========\n");

	    Log.i(TAG, "Peers");
	    i = 1;
	    for (PeerRowDB peerRow : sqliteMngr.queryForAllPeers()) {
		Log.i(TAG, "	peer no.[" + (i++) + "]: " + peerRow.toString() + "]");
	    }
	    Log.i(TAG, "===========\n");

	    Log.i(TAG, "ContactedPeers");
	    i = 1;
	    for (ContactedPeerRowDB contactedPeerRow : sqliteMngr.queryForContactedPeers()) {
		Log.i(TAG, "	contacted peer no.[" + (i++) + "]: " + contactedPeerRow.toString()
			+ "]");
	    }
	    Log.i(TAG, "===========\n");
	} finally {
	    // Close
	    sqliteMngr.close();
	}
    }

    public void updateCoordinator(String theCoordinator) {
	// Open
	sqliteMngr.open();

	try {
	    synchronized (coordinatorTableSync) {
		sqliteMngr.setCoordinator(theCoordinator);
	    }

	} finally {
	    // Close
	    sqliteMngr.close();
	}
    }

    public String getCoordinator() {
	// Open
	sqliteMngr.open();

	try {
	    synchronized (coordinatorTableSync) {
		return sqliteMngr.queryForCoordinatorPeer();
	    }
	} finally {
	    // Close
	    sqliteMngr.close();
	}
    }

    protected abstract String getProtocol();

    protected String contactedPeersToString() {
	String contactedPeersAsString = "";
	StringBuffer sb = new StringBuffer();

	// Query for the contacted peers
	List<ContactedPeerRowDB> contactedPeers = sqliteMngr.queryForContactedPeers();

	// Append the contacted peers to the string
	for (ContactedPeerRowDB contactedPeer : contactedPeers) {
	    sb.append(contactedPeer.getContactedPeerID());
	    sb.append(",");
	}
	if (sb.length() > 0) {
	    contactedPeersAsString = sb.substring(0, sb.length() - ",".length());
	}
	return contactedPeersAsString;
    }

    protected String peersToString(Collection<PeerRowDB> peers, String excludePeerID) {
	String peersAsString = "";
	StringBuffer sb = new StringBuffer();

	for (PeerRowDB peer : peers) {
	    if (peer.getPeerID().equals(excludePeerID)) {
		continue;
	    }
	    sb.append(peer.getPeerID());
	    sb.append(",");
	}
	if (sb.length() > 0) {
	    peersAsString = sb.substring(0, sb.length() - ",".length());
	}
	return peersAsString;
    }

    protected PeerRowDB getLocalPeer() {
	// Query for the local one
	LocalPeerInfoRowDB localPeerInfo = sqliteMngr.queryLocalPeerInfo();

	// Query for the local peer
	PeerRowDB localPeer = sqliteMngr.queryPeerByPeerID(localPeerInfo.getLocalPeerID());

	return localPeer;
    }

    protected boolean checkIfPeerIDExistInList(Collection<PeerRowDB> peers, String peerID) {
	boolean exist = false;

	for (PeerRowDB peer : peers) {
	    if (peerID.equals(peer.getPeerID())) {
		exist = true;
		break;
	    }
	}

	return exist;
    }
}

/*
        Copyright 2007-2014 CNR-ISTI, http://isti.cnr.it
        Institute of Information Science and Technologies
        of the Italian National Research Council

        See the NOTICE file distributed with this work for additional
        information regarding copyright ownership

        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
 */
package org.universAAL.middleware.android.modules.impl;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.xml.XMLConstants;
import ae.javax.xml.bind.JAXBContext;
import ae.javax.xml.bind.JAXBException;
import ae.javax.xml.bind.Unmarshaller;
import android.util.Log;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.PeerRole;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceStatus;
import org.universAAL.middleware.interfaces.aalspace.Consts;
import org.universAAL.middleware.interfaces.aalspace.model.Aalspace;
import org.universAAL.middleware.interfaces.aalspace.model.Aalspace.CommunicationChannels;
import org.universAAL.middleware.interfaces.aalspace.model.Aalspace.PeeringChannel;
import org.universAAL.middleware.interfaces.aalspace.model.Aalspace.SpaceDescriptor;
import org.universAAL.middleware.managers.aalspace.MatchingResultImpl;
import org.universAAL.middleware.managers.aalspace.util.AALSpaceSchemaEventHandler;
import org.universAAL.middleware.managers.api.AALSpaceEventHandler;
import org.universAAL.middleware.managers.api.AALSpaceListener;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.managers.api.MatchingResult;
import org.xml.sax.SAXException;

/**
 * The implementation of the AALSpaceManager and AALSpaceEventHandler
 *
 * @author <a href="mailto:michele.girolami@isti.cnr.it">Michele Girolami</a>
 * @author <a href="mailto:stefano.lenzi@isti.cnr.it">Stefano Lenzi</a>
 * @author <a href="mailto:giancarlo.riolo@isti.cnr.it">Giancarlo Riolo</a>
 * @version $LastChangedRevision$ ( $LastChangedDate$ )
 */
public class AndroidAALSpaceManagerImpl implements AALSpaceEventHandler,
        AALSpaceManager, SharedObjectListener {
	private static final String TAG = "AndroidAALSpaceManagerImpl";
    private ModuleContext context;
    private AndroidControlBroker controlBroker;
    private boolean initialized = false;
    // data structure for the MW
    /**
     * The AALSpace to which the MW is connected. Currently the MW can join to
     * only one AAL space
     */
    private AALSpaceDescriptor currentAALSpace;
    private PeerCard myPeerCard;
    private PeerRole peerRole;
    private ChannelDescriptor peeringChannel;
    /**
     * The list of AALSpace discovered by the MW
     */
    private Set<AALSpaceCard> foundAALSpaces;

    /**
     * The set of peers joining to my AAL Space
     */
    private Map<String, PeerCard> peers;

    /**
     * A map of AALSpaces managed from this MW instance
     */
    private Map<String, AALSpaceDescriptor> managedAALspaces;
    private Boolean pendingAALSpace = new Boolean(false);
    private String spaceExtension;
    private Aalspace aalSpaceDefaultConfiguration;

    // thread
    private AndroidJoiner joiner;
    private ScheduledFuture joinerFuture;

    private AndroidCheckPeerThread checkPeerThread;
    private ScheduledFuture checkerFuture;

    private AndroidRefreshAALSpaceThread refreshAALSpaceThread;
    private ScheduledFuture refreshFuture;

    private String aalSpaceConfigurationPath;
    private JAXBContext jc;
    private Unmarshaller unmarshaller;
    private boolean aalSpaceValidation;
    private String aalSpaceSchemaURL;
    private String aalSpaceSchemaName;
    private int aalSpaceLifeTime;
    private long waitBeforeClosingChannels;
    private long waitAfterJoinRequest;
    private String altConfigDir;

    private List<AALSpaceListener> listeners;

    private long TIMEOUT;

    // scheduler
    private final ScheduledExecutorService scheduler = Executors
            .newScheduledThreadPool(10);

	public AndroidAALSpaceManagerImpl(ModuleContext context, String altConfigDir) {
		this.context = context;
		this.altConfigDir = altConfigDir;
		try {
			jc = JAXBContext.newInstance("org.universAAL.middleware.interfaces.aalspace.model", this.getClass().getClassLoader());
			unmarshaller = jc.createUnmarshaller();
			managedAALspaces = new Hashtable<String, AALSpaceDescriptor>();
			foundAALSpaces = Collections
					.synchronizedSet(new HashSet<AALSpaceCard>());
			peers = new HashMap<String, PeerCard>();
			listeners = new ArrayList<AALSpaceListener>();
		} catch (JAXBException e) {
            Log.e(TAG,  "Error during AALSpace parser intialization: ",e);
        }
        try {
            TIMEOUT = Long.parseLong(System.getProperty(
                    AALSpaceManager.COMUNICATION_TIMEOUT_KEY,
                    AALSpaceManager.COMUNICATION_TIMEOUT_VALUE));
        } catch (Exception ex) {
            Log.e(TAG, "intalization timeout, falling back to default value: "
                            + AALSpaceManager.COMUNICATION_TIMEOUT_VALUE );
            TIMEOUT = Long
                    .parseLong(AALSpaceManager.COMUNICATION_TIMEOUT_VALUE);
        }
    }

    public Map<String, AALSpaceDescriptor> getManagedAALSpaces() {
        return managedAALspaces;
    }

    public Map<String, PeerCard> getPeers() {
        return peers;
    }

    public Aalspace getAalSpaceDefaultConfiguration() {
        return aalSpaceDefaultConfiguration;
    }

    public Boolean getPendingAALSpace() {
        return this.pendingAALSpace;
    }

    public long getWaitAfterJoinRequest() {
        return waitAfterJoinRequest;
    }

    public AALSpaceDescriptor getAALSpaceDescriptor() {
        return currentAALSpace;
    }

    public PeerCard getMyPeerCard() {
        return myPeerCard;
    }

    public Set<AALSpaceCard> getAALSpaces() {
        synchronized (foundAALSpaces) {
            return foundAALSpaces;
        }
    }

    public synchronized boolean init() {
        if (!initialized) {

            Log.d(TAG,  "Creating the PeerCard..." );
            // to fix empty fields
            myPeerCard = new PeerCard(peerRole, "", "");
            myPeerCard.setRole(peerRole);
            Log.i(TAG,  "--->PeerCard created: "
                            + myPeerCard.toString() );

            // fetching the services
            Log.d(TAG,  "Fetching the ContextBroker..." );
            Object[] cBrokers = context.getContainer().fetchSharedObject(
                    context,
                    new Object[] { AndroidControlBroker.class.getName().toString() },
                    this);
            if (cBrokers != null) {
                Log.d(TAG,  "Found  ContextBrokers..." );
                if (cBrokers[0] instanceof AndroidControlBroker)
                    controlBroker = (AndroidControlBroker) cBrokers[0];
                else {
                    initialized = false;
                    return initialized;
                }
            } else {
                Log.w(TAG, "No ContextBroker found" );
                initialized = false;
                return initialized;
            }

            // XML Schema validation
            if (aalSpaceValidation && aalSpaceConfigurationPath != null
                    && aalSpaceSchemaName != null) {
                Log.d(TAG,  "Initialize AALSpace schema validation" );
                SchemaFactory sf = SchemaFactory
                        .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                try {
                    File aalSpaceSchemaFile = new File(aalSpaceSchemaURL
                            + File.separatorChar + aalSpaceSchemaName);
                    Schema aalSpaceSchema = null;
                    if (aalSpaceSchemaFile.canRead()) {
                        aalSpaceSchema = sf.newSchema(aalSpaceSchemaFile);
                        unmarshaller.setSchema(aalSpaceSchema);
                        unmarshaller
                                .setEventHandler(new AALSpaceSchemaEventHandler(
                                        context));
                    } else
                        Log.w(TAG,  "Unable to read AALSpace Scham from path: "
                                        + aalSpaceSchemaFile.getAbsolutePath() );

                } catch (SAXException e) {
                    Log.e(TAG,  "Error during AALSpace schema initialization: ",e);
                } catch (NullPointerException e) {
                    Log.e(TAG, "Error during AALSpace schema initialization: ",e);
                } catch (JAXBException e) {
                    Log.e(TAG,  "Error during AALSpace Schema Event handler initialization: ",e );
                }
                initialized = true;
            }

            // start the threads
            // Joiner -> AALSapce joiner
            joiner = new AndroidJoiner(this/*, context*/);
            joinerFuture = scheduler.scheduleAtFixedRate(joiner, 0, 1,
                    TimeUnit.SECONDS);

            // Configure the AAL Space
            if (aalSpaceConfigurationPath == null
                    || aalSpaceConfigurationPath.length() == 0) {
                Log.w(TAG,  "AALSpace default configurations are null" );
                initialized = true;
            } else {
                Log.d(TAG, "Parse the AALSpace default configurations" );
                aalSpaceDefaultConfiguration = readAALSpaceDefaultConfigurations();
                initAALSpace(aalSpaceDefaultConfiguration);
                initialized = true;
            }
        }
        return initialized;
    }

    /**
     * Private method to manage the creation of a new AALSpace starting from the
     * default configurations
     *
     * @param aalSpaceDefaultConfiguration
     *            Default AAL Space configurations
     * @return true if the creation succeeded, false otherwise
     */
    public synchronized void initAALSpace(Aalspace aalSpaceDefaultConfiguration) {
        // configure the MW with the space configurations
        try {
            if (currentAALSpace == null && aalSpaceDefaultConfiguration != null) {

                Log.d(TAG,  "AALSpace default configuration found" );
                // first look for existing AALSpace with the same name as the
                // one reported in the default config.file
                List<AALSpaceCard> spaceCards = controlBroker
                        .discoverAALSpace(buildAALSpaceFilter(aalSpaceDefaultConfiguration));
                if (spaceCards != null && spaceCards.size() > 0) {
                    Log.d(TAG, "Default AALSpace found" );
                    synchronized (foundAALSpaces) {
                        this.foundAALSpaces.addAll(spaceCards);
                    }
                } else {
                    if (myPeerCard.getRole().equals(PeerRole.COORDINATOR)) {

                        Log.i(TAG,  "No default AALSpace found...creating it ");

                        List<org.universAAL.middleware.interfaces.ChannelDescriptor> communicationChannels = new ArrayList<org.universAAL.middleware.interfaces.ChannelDescriptor>();
                        // fetch the communication channels
                        communicationChannels = getChannels(aalSpaceDefaultConfiguration
                                .getCommunicationChannels()
                                .getChannelDescriptor());
                        // fetch the peering channel
                        org.universAAL.middleware.interfaces.ChannelDescriptor peeringChannel = getChannel(aalSpaceDefaultConfiguration
                                .getPeeringChannel().getChannelDescriptor());
                        // configure the MW channels
                        if (controlBroker != null) {
                            controlBroker.configurePeeringChannel(
                                    peeringChannel, myPeerCard.getPeerID());
                            controlBroker.configureChannels(
                                    communicationChannels,
                                    myPeerCard.getPeerID());

                            // create the new AALSpace
                            AALSpaceCard myAALSpace = new AALSpaceCard(
                                    getAALSpaceProperties(aalSpaceDefaultConfiguration));
                            myAALSpace.setAalSpaceLifeTime(aalSpaceLifeTime);
                            currentAALSpace = new AALSpaceDescriptor(
                                    myAALSpace, communicationChannels);
                            // since coordinator and deployCoordinator matches,
                            // configure the space Descriptor
                            currentAALSpace.setDeployManager(myPeerCard);

                            // announce the AAL Space
                            controlBroker.buildAALSpace(myAALSpace);

                            // strat thread
                            refreshAALSpaceThread = new AndroidRefreshAALSpaceThread(
                                    context);
                            refreshFuture = scheduler.scheduleAtFixedRate(
                                    refreshAALSpaceThread, 0,
                                    aalSpaceLifeTime - 1, TimeUnit.SECONDS);

                            // start the thread for management of AALSpace
                            checkPeerThread = new AndroidCheckPeerThread(context);
                            checkerFuture = scheduler.scheduleAtFixedRate(
                                    checkPeerThread, 0, 1, TimeUnit.SECONDS);

                            // add the AALSpace created to the list of managed
                            // AAL spaces
                            managedAALspaces.put(myAALSpace.getSpaceID(),
                                    currentAALSpace);

                            // notify to all the listeners a new AAL Space has
                            // been joined
                            for (AALSpaceListener spaceListener : listeners) {
                                spaceListener.aalSpaceJoined(currentAALSpace);
                            }
                            peers.put(myPeerCard.getPeerID(), myPeerCard);

                            // init the control broker
                            Log.i(TAG, "New AALSpace created!" );

                        } else {
                            Log.w(TAG,  "Control Broker is not initialize" );
                        }

                    } else {
                        Log.i(TAG,  "No default AALSpace found...waiting to join an AALSpace as :"
                                        + myPeerCard.getRole());
                    }
                }
            } else {
                if (currentAALSpace != null)
                    Log.d(TAG, "The MW belongs to: "
                                    + currentAALSpace.getSpaceCard().toString());
                else
                    Log.d(TAG,  "No AALSpace default configuration found on the path: "
                                    + aalSpaceConfigurationPath );
            }
        } catch (Exception e) {
            Log.e(TAG,  "Error during AALSpace initialization: ",e);
        }

    }

    public void join(AALSpaceCard spaceCard) {
        if (currentAALSpace != null) {
            Log.w(TAG,  "Cannot join to multiple AALSpace. First leave the current AALSpace " );

        }
        if (init()) {
            synchronized (pendingAALSpace) {

                pendingAALSpace = true;
                Log.i(TAG, "--->Start the join phase to AALSpace: "
                                + spaceCard.toString());
                Log.d(TAG,  "Configure the peering channel..." );

                // fetch the default peering channel
                org.universAAL.middleware.interfaces.ChannelDescriptor defaultPeeringChannel = getChannel(aalSpaceDefaultConfiguration
                        .getPeeringChannel().getChannelDescriptor());
                // fetch the default peering channel URL
                String peeringChannelSerialized = spaceCard.getPeeringChannel();

                // If the default peering channel URL from the SpaceCard matches
                // with the default peering channel URL from the local
                // configuration file, then I use the defaultPeeringChannel
                // channel descriptor
                if (defaultPeeringChannel != null
                        && peeringChannelSerialized
                                .equals(defaultPeeringChannel
                                        .getChannelDescriptorFileURL())) {
                    peeringChannel = defaultPeeringChannel;
                } else {
                    peeringChannel = new ChannelDescriptor(
                            spaceCard.getPeeringChannelName(), "", null);
                    peeringChannel
                            .setChannelDescriptorFileURL(peeringChannelSerialized);
                }
                try {

                    if (peeringChannelSerialized != null) {
                        // InputStream channelValue = new
                        // ByteArrayInputStream(peeringChannelSerialized.getBytes());
                        // ChannelDescriptor peeringChannelD =
                        // (ChannelDescriptor)unmarshaller.unmarshal(channelValue);
                        // org.universAAL.middleware.interfaces.ChannelDescriptor
                        // peeringChannel = getChannel(peeringChannelD);

                        controlBroker.configurePeeringChannel(peeringChannel,
                                myPeerCard.getPeerID());
                        Log.i(TAG, "--->Peering channel configured!" );
                    } else {
                        Log.w(TAG,  "Peering channel is null not able to join the AALSpace" );
                    }
                    Log.i(TAG, "--->Sending join request..." );
                    PeerCard spaceCoordinator = new PeerCard(
                            spaceCard.getCoordinatorID(), PeerRole.COORDINATOR);
                    controlBroker.join(spaceCoordinator, spaceCard);

                } catch (Exception e) {
                    Log.e(TAG,  "Error during AALSpace join: "
                                    + spaceCard.toString() );
                    pendingAALSpace = false;
                }
            }
        } else {
            Log.w(TAG, "AALSpace Manager not initialized" );
        }

    }

    public void cleanUpJoinRequest() {
        synchronized (pendingAALSpace) {
            List<ChannelDescriptor> pendingPC = new ArrayList<ChannelDescriptor>();
            pendingPC.add(peeringChannel);
            controlBroker.resetModule(pendingPC);
            pendingAALSpace = false;
        }

    }

    public void aalSpaceJoined(AALSpaceDescriptor descriptor) {
        if (init()) {
            Log.d(TAG,  "Joining to AALSpace: "
                            + descriptor.getSpaceCard().toString() );

            synchronized (pendingAALSpace) {
                currentAALSpace = descriptor;
                pendingAALSpace = false;

                Log.i(TAG,  "--->AALSpace Joined!" );
                try {
                    pendingAALSpace.notifyAll();
                } catch (Exception e) {
                    Log.e(TAG, "Error during notify: "
                                    + e.toString());
                }
            }
            // creating AALSpace channels
            List<ChannelDescriptor> communicationChannels = currentAALSpace
                    .getBrokerChannels();
            if (communicationChannels != null)
                controlBroker.configureChannels(communicationChannels,
                        myPeerCard.getPeerID());
            // start checking for members peers in the AALSpace
            checkPeerThread = new AndroidCheckPeerThread(context);
            checkerFuture = scheduler.scheduleAtFixedRate(checkPeerThread, 0,
                    1, TimeUnit.SECONDS);
            // add myself to the list of peers
            peerFound(myPeerCard);
            controlBroker.newPeerAdded(currentAALSpace.getSpaceCard(),
                    myPeerCard);

            Log.i(TAG,  "--->Announced my presence!" );

            for (AALSpaceListener spaceListener : listeners) {
                spaceListener.aalSpaceJoined(currentAALSpace);
            }

        } else {
            Log.w(TAG,  "AALSpace Manager is not initialized aborting." );
            pendingAALSpace = false;
        }

    }

    private Dictionary<String, String> buildAALSpaceFilter(Aalspace space) {
        Dictionary<String, String> filters = new Hashtable<String, String>();
        if (space != null) {
            try {
                filters.put(Consts.AALSPaceID, space.getSpaceDescriptor()
                        .getSpaceId());
                Log.d(TAG, "Filter created" );
            } catch (NullPointerException e) {
                Log.e(TAG, "Error while building AALSpace filter...returning empty filter",e);
                return filters;
            } catch (Exception e) {
                Log.e(TAG,  "Error while building AALSpace filter...returning empty filter",e);
                return filters;
            }
        }
        return filters;
    }

    private List<ChannelDescriptor> getChannels(
            List<org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor> channels) {
        List<ChannelDescriptor> theChannels = new ArrayList<ChannelDescriptor>();

        for (org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor channel : channels) {
            ChannelDescriptor singleChannel = new ChannelDescriptor(
                    channel.getChannelName(), channel.getChannelURL(),
                    channel.getChannelValue());
            theChannels.add(singleChannel);
        }
        return theChannels;

    }

    private ChannelDescriptor getChannel(
            org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor channel) {
        ChannelDescriptor singleChannel = new ChannelDescriptor(
                channel.getChannelName(), channel.getChannelURL(),
                channel.getChannelValue());
        return singleChannel;

    }

    /**
     * This method collects in a dictionary the properties associated with a new
     * AAL Space in order to announce them. The properties are read from the
     * data structure AalSpace. The properties added to the AALSpace card are
     * the name,id,description and coordinator ID and the peering channel
     * serialized as XML string
     *
     * @param space
     * @return
     */
    private Dictionary<String, String> getAALSpaceProperties(Aalspace space) {
        Dictionary<String, String> properties = new Hashtable<String, String>();
        try {

            // general purpose properties
            properties.put(Consts.AALSPaceName, space.getSpaceDescriptor()
                    .getSpaceName());
            properties.put(Consts.AALSPaceID, space.getSpaceDescriptor()
                    .getSpaceId());
            properties.put(Consts.AALSPaceDescription, space
                    .getSpaceDescriptor().getSpaceDescription());
            properties.put(Consts.AALSpaceCoordinator, myPeerCard.getPeerID());
            // URL where to fetch the peering channel
            properties
                    .put(Consts.AALSpacePeeringChannelURL, space
                            .getPeeringChannel().getChannelDescriptor()
                            .getChannelURL());
            properties.put(Consts.AALSpacePeeringChannelName, space
                    .getPeeringChannel().getChannelDescriptor()
                    .getChannelName());
            properties.put(Consts.AALSPaceProfile, space.getSpaceDescriptor()
                    .getProfile());

        } catch (NullPointerException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
        return properties;

    }

    private String[] getFileList(String aalSpaceConfigurationPath) {
        File spaceConfigDirectory = new File(aalSpaceConfigurationPath);
        if (!spaceConfigDirectory.canRead()) {
            Log.w(TAG,  "File: " + aalSpaceConfigurationPath
                            + " cannot be read." );
            return null;
        }
        String[] spaces = spaceConfigDirectory.list(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return (name.endsWith(spaceExtension));
            }
        });
        return spaces;
    }

    public Aalspace readAALSpaceDefaultConfigurations() {
	Log.d(TAG,  "Reading AALSpace configuration." );
	try {
	    String aalSpaceConfigurationPath = this.aalSpaceConfigurationPath;
	    File spaceConfigDirectory = new File(aalSpaceConfigurationPath);

	    // debug output: log the current path
	    String currPath = "";
	    try {
		currPath = new java.io.File(".").getCanonicalPath();
	    } catch (IOException e) {
	    }
	    Log.d(TAG, 
			    "Reading AALSpace configuration from directory: "+
			    spaceConfigDirectory.toString()+
			    " The current path is: "+ currPath );

	    // get the list of config files
	    String[] spaces = getFileList(aalSpaceConfigurationPath);
	    if (spaces == null || spaces.length == 0) {
                Log.w(TAG, "File: "+ aalSpaceConfigurationPath+
                                " cannot be read, trying alternative: "+
                                altConfigDir );

                aalSpaceConfigurationPath = altConfigDir;
                spaces = getFileList(aalSpaceConfigurationPath);
	    }

	    String value = "<![CDATA[<config xmlns=\"urn:org:jgroups\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:org:jgroups http://www.jgroups.org/schema/JGroups-3.0.xsd\"> <UDP mcast_port=\"${jgroups.udp.mcast_port:45588}\" tos=\"8\" ucast_recv_buf_size=\"20M\" ucast_send_buf_size=\"640K\" mcast_recv_buf_size=\"25M\" mcast_send_buf_size=\"640K\" loopback=\"true\" discard_incompatible_packets=\"true\" max_bundle_size=\"64K\" max_bundle_timeout=\"30\" ip_ttl=\"${jgroups.udp.ip_ttl:8}\" enable_bundling=\"true\" enable_diagnostics=\"false\" thread_naming_pattern=\"cl\" timer_type=\"new\" timer.min_threads=\"4\" timer.max_threads=\"10\" timer.keep_alive_time=\"3000\" timer.queue_max_size=\"500\" thread_pool.enabled=\"true\" thread_pool.min_threads=\"2\" thread_pool.max_threads=\"8\" thread_pool.keep_alive_time=\"5000\" thread_pool.queue_enabled=\"true\" thread_pool.queue_max_size=\"10000\" thread_pool.rejection_policy=\"discard\" oob_thread_pool.enabled=\"true\" oob_thread_pool.min_threads=\"1\" oob_thread_pool.max_threads=\"8\" oob_thread_pool.keep_alive_time=\"5000\" oob_thread_pool.queue_enabled=\"false\" oob_thread_pool.queue_max_size=\"100\" oob_thread_pool.rejection_policy=\"Run\"/> <PING timeout=\"2000\" num_initial_members=\"3\"/> <MERGE2 max_interval=\"30000\" min_interval=\"10000\"/> <FD_SOCK/> <FD_ALL/> <VERIFY_SUSPECT timeout=\"1500\" /> <BARRIER /> <pbcast.NAKACK exponential_backoff=\"300\" xmit_stagger_timeout=\"200\" use_mcast_xmit=\"false\" discard_delivered_msgs=\"true\"/> <UNICAST /> <pbcast.STABLE stability_delay=\"1000\" desired_avg_gossip=\"50000\" max_bytes=\"4M\"/> <pbcast.GMS print_local_addr=\"true\" join_timeout=\"3000\" view_bundling=\"true\"/> <UFC max_credits=\"2M\" min_threshold=\"0.4\"/> <MFC max_credits=\"2M\" min_threshold=\"0.4\"/> <FRAG2 frag_size=\"60K\" /> <pbcast.STATE_TRANSFER /> <pbcast.FLUSH /> </config>]]>";
	    String url="file:/mnt/sdcard/data/felix-conf-1.3.3/conf/etc/udp.xml";
	    //TODO Locate url elsehow
	    // evaluate the list of config files
	    if (spaces != null && spaces.length > 0) {
		Log.d(TAG,  "Found: "
				+ spaces.length
				+ " space configurations...picking up the default one" );
		// Currently only one space is read from the file system
		File defaultSpaceConfiguration = new File(
			aalSpaceConfigurationPath + File.separatorChar
				+ spaces[0]);
		if (defaultSpaceConfiguration.canRead()) {
		    Aalspace space = (Aalspace) unmarshaller
			    .unmarshal(defaultSpaceConfiguration);
		    // PATCH: HARDCODED CONFIGURATION (unmarshaller dont work)
		    space=new Aalspace();
		    space.setAdmin("admin");
		    space.setOwner("owner");
		    space.setSecurity("security");
		    SpaceDescriptor sd = new SpaceDescriptor();
		    sd.setSpaceName("myHome3");
		    sd.setProfile("HomeSpace");
		    sd.setSpaceId("8888");
		    sd.setSpaceDescription("Super Domestic Home");
		    space.setSpaceDescriptor(sd);
		    PeeringChannel pc = new PeeringChannel();
		    org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor cd = new org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor();
		    cd.setChannelName("mw.modules.aalspace.osgi");
		    cd.setChannelURL(url);
		    cd.setChannelValue(value);
		    pc.setChannelDescriptor(cd);
		    space.setPeeringChannel(pc);
		    CommunicationChannels ccs = new CommunicationChannels();
		    org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor cd1 = new org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor();
		    cd1.setChannelName("mw.brokers.control.osgi"); // ONLY NAMES
								   // ARE NEEDED
		    cd1.setChannelURL(url);
		    cd1.setChannelValue(value);
		    org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor cd2 = new org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor();
		    cd2.setChannelName("mw.bus.context.osgi");
		    cd2.setChannelURL(url);
		    cd2.setChannelValue(value);
		    org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor cd3 = new org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor();
		    cd3.setChannelName("mw.bus.service.osgi");
		    cd3.setChannelURL(url);
		    cd3.setChannelValue(value);
		    org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor cd4 = new org.universAAL.middleware.interfaces.aalspace.model.ChannelDescriptor();
		    cd4.setChannelName("mw.bus.ui.osgi");
		    cd4.setChannelURL(url);
		    cd.setChannelValue(value);
		    ccs.getChannelDescriptor().add(cd1);
		    ccs.getChannelDescriptor().add(cd2);
		    ccs.getChannelDescriptor().add(cd3);
		    ccs.getChannelDescriptor().add(cd4);
		    space.setCommunicationChannels(ccs);
		    if (space != null) {
			return space;
		    } else {
			Log.w(TAG,  "Unable to parse default AALSpace configuration" );
			return null;
		    }
		} else {
		    Log.w(TAG, "Directory were files are located is not accessible");
		    return null;
		}
	    } else {
		Log.w(TAG, "No default AALSpaces found");
		return null;
	    }
	} catch (JAXBException e) {
	    Log.e(TAG,  "Error during JAXB initialization: ",e);
	    return null;
	}

    }

    public void loadConfigurations(Dictionary configurations) {
        Log.d(TAG,  "Updating AALSpaceManager properties" );
        if (configurations == null) {
            Log.w(TAG,  "AALSpaceManager properties are null!!!" );
            return;
        } else {
            Log.d(TAG, "Fetching the PeerRole" );
            String role = (String) configurations
                    .get(org.universAAL.middleware.managers.aalspace.util.Consts.PEER_ROLE);
            String roleOverride = System
                    .getProperty(org.universAAL.middleware.managers.aalspace.util.Consts.PEER_ROLE);
            if (roleOverride != null)
                role = roleOverride;
            if (role != null) {
                try {
                    peerRole = PeerRole.valueOf(role);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Unable to initialize the peer with the role: "
                                    + role );
                    Log.e(TAG,  "...configuring as regular PEER: "
                                    + role);
                    peerRole = PeerRole.PEER;
                }
            } else {
                Log.w(TAG,  "The role is null...configuring as regular PEER: "
                                + role );
                peerRole = PeerRole.PEER;
            }

            Log.d(TAG,  "Fetching AALSpace default configurations" );
            aalSpaceConfigurationPath = (String) configurations
                    .get(org.universAAL.middleware.managers.aalspace.util.Consts.AAL_SPACE_CONFIGURATION_PATH);
            if (aalSpaceConfigurationPath != null)
                Log.d(TAG,  "AALSpace default configurations fetched: "
                                + aalSpaceConfigurationPath );
            else
                Log.w(TAG,  "AALSpace default configurations are null!" );
            Log.d(TAG,  "Fetching AALSpace extension" );
            spaceExtension = (String) configurations
                    .get(org.universAAL.middleware.managers.aalspace.util.Consts.SPACE_EXTENSION);
            aalSpaceValidation = Boolean
                    .parseBoolean((String) configurations
                            .get(org.universAAL.middleware.managers.aalspace.util.Consts.AAL_SPACE_VALIDATION));
            aalSpaceSchemaURL = (String) configurations
                    .get(org.universAAL.middleware.managers.aalspace.util.Consts.AAL_SPACE_SCHEMA_URL);
            aalSpaceLifeTime = Integer
                    .parseInt((String) configurations
                            .get(org.universAAL.middleware.managers.aalspace.util.Consts.AAL_SPACE_LIFETIME));

            aalSpaceSchemaName = (String) configurations
                    .get(org.universAAL.middleware.managers.aalspace.util.Consts.AAL_SPACE_SCHEMA_NAME);
            waitBeforeClosingChannels = Long
                    .parseLong((String) configurations
                            .get(org.universAAL.middleware.managers.aalspace.util.Consts.WAIT_BEFEORE_CLOSING_CHANNEL));
            waitAfterJoinRequest = Long
                    .parseLong((String) configurations
                            .get(org.universAAL.middleware.managers.aalspace.util.Consts.WAIT_BEFEORE_CLOSING_CHANNEL));

        }
    }

    public void joinRequest(AALSpaceCard spaceCard, PeerCard peer) {
        if (init()) {
            if (spaceCard != null && peer != null
                    && spaceCard.getSpaceID() != null) {
                Log.i(TAG,  "---> Peer:"
                                + peer.getPeerID().toString()
                                + " requests to join to the AAL Space: ");
                if (!managedAALspaces.containsKey(spaceCard.getSpaceID())) {
                    Log.w(TAG,  "Received a join request to an AALSpace not managed: my AALSpace: "
                                    + currentAALSpace.getSpaceCard()
                                            .getSpaceID()
                                    + " while received: "
                                    + spaceCard.getSpaceID() );

                } else {
                    // send unicast message to the peer with the space
                    // descriptor
                    Log.d(TAG,  "Sending the space descriptor..." );
                    // update the peers
                    // add the new peer to the map of peers
                    peerFound(peer);

                    controlBroker.addNewPeer(currentAALSpace, peer);
                    Log.d(TAG,  "Space descriptor sent!" );
                    // newPeerJoined(peer)

                    // announce bcast the new peer
                    Log.d(TAG,  "Announcing the new peer...");
                }

            } else
                Log.d(TAG,  "Invalid join request parameter" );
        } else {
            Log.w(TAG, "AALSpace Manager not initialized" );
        }

    }

    public synchronized void newAALSpacesFound(Set<AALSpaceCard> spaceCards) {
        boolean result = false;
        if (spaceCards != null) {
            synchronized (foundAALSpaces) {
                foundAALSpaces = spaceCards;
            }
            if (foundAALSpaces.size() > 0) {
                Log.v(TAG,  "--->The list of AAL Spaces has been updated:"
                                + foundAALSpaces.toString() );
            }
        }
    }

    public synchronized void peerFound(PeerCard peer) {
        if (peer != null && !peers.containsKey(peer.getPeerID())) {
            Log.i(TAG,  "--->The Peer: "
                            + peer.getPeerID().toString()
                            + " joins the AALSpace: " );
            peers.put(peer.getPeerID(), peer);
            for (AALSpaceListener list : listeners) {
                list.newPeerJoined(peer);

        }

    }

    }

    public synchronized void peerLost(PeerCard peer) {
        if (peer != null) {
            Log.i(TAG,  "--->Peer +" + peer.getPeerID()
                            + " left the AALSpace" );
            peers.remove(peer.getPeerID());
            for (AALSpaceListener list : listeners) {
                list.peerLost(peer);

            }

        }
    }

    public void sharedObjectAdded(Object sharedObj, Object removeHook) {
        if (sharedObj instanceof AndroidControlBroker) {
            Log.d(TAG,  "ControlBroker service added");
            this.controlBroker = (AndroidControlBroker) sharedObj;
        }
    }

    public void sharedObjectRemoved(Object removeHook) {
        if (removeHook instanceof AndroidControlBroker) {
            Log.d(TAG, "ControlBroker service removed");
            this.controlBroker = null;
            initialized = false;
        }
    }

    public synchronized void leaveRequest(AALSpaceDescriptor spaceDescriptor) {
        if (spaceDescriptor != null) {

            // stop the management thread
            checkerFuture.cancel(true);
            if (refreshFuture != null)
                refreshFuture.cancel(true);

            ChannelDescriptor peeringChannel = new ChannelDescriptor(
                    spaceDescriptor.getSpaceCard().getPeeringChannelName(), "",
                    null);
            List<ChannelDescriptor> channels = new ArrayList<ChannelDescriptor>();
            channels.add(peeringChannel);
            channels.addAll(spaceDescriptor.getBrokerChannels());
            Log.i(TAG,  "--->Leaving the AALSpace: "
                            + spaceDescriptor.getSpaceCard().getSpaceName());
            controlBroker.resetModule(channels);
            // we assume the current aal space is the only one
            currentAALSpace = null;
            peers.clear();
        }
    }

    public void leaveAALSpace(AALSpaceDescriptor spaceDescriptor) {
        if (init()) {
            if (spaceDescriptor != null
                    && managedAALspaces.containsKey(spaceDescriptor
                            .getSpaceCard().getSpaceID())) {
                Log.i(TAG,  "--->Leaving a managed AALSpace: "
                                + spaceDescriptor.getSpaceCard().getSpaceName());
                closeManagedSpace(spaceDescriptor);
            } else if (spaceDescriptor.getSpaceCard().getSpaceID()
                    .equals(currentAALSpace.getSpaceCard().getSpaceID())) {
                // send a leave message
                Log.i(TAG,  "--->Leaving the AALSpace: "
                                + spaceDescriptor.getSpaceCard().getSpaceName());
                PeerCard spaceCoordinator = new PeerCard(spaceDescriptor
                        .getSpaceCard().getCoordinatorID(),
                        PeerRole.COORDINATOR);
                controlBroker.leaveAALSpace(spaceCoordinator,
                        spaceDescriptor.getSpaceCard());
                Log.d(TAG,  "Leave message sent!" );

                // stop the management thread
                checkerFuture.cancel(true);
                if (refreshFuture != null)
                    refreshFuture.cancel(true);

                ChannelDescriptor peeringChannel = new ChannelDescriptor(
                        spaceDescriptor.getSpaceCard().getPeeringChannelName(),
                        "", null);
                List<ChannelDescriptor> channels = new ArrayList<ChannelDescriptor>();
                channels.add(peeringChannel);
                channels.addAll(spaceDescriptor.getBrokerChannels());
                controlBroker.resetModule(channels);
                // we assume the current aal space is the only one
                currentAALSpace = null;
                // reset list of peers
                peers.clear();

            }

            for (AALSpaceListener elem : listeners) {

                elem.aalSpaceLost(spaceDescriptor);
            }

        } else {
            Log.w(TAG,  "AALSpace Manager not initialized" );
        }
    }

    public void dispose() {
        // remove me as listener
        context.getContainer().removeSharedObjectListener(this);
        // workaround waiting for
        // http://forge.universaal.org/gf/project/middleware/tracker/?action=TrackerItemEdit&tracker_item_id=270
        controlBroker.sharedObjectRemoved(this);
        scheduler.shutdownNow();
        scheduler.shutdown();
        if (init()) {

            if (!managedAALspaces.isEmpty()) {
                Log.i(TAG, "Closing all the managed AAL Spaces" );
                try {
                    for (String spaceID : managedAALspaces.keySet()) {
                        closeManagedSpace(managedAALspaces.get(spaceID));
                    }
                } catch (Exception e) {
                    Log.e(TAG,  "Error during dispose: ",e );
                }
            } else {
                if (currentAALSpace != null)
                    leaveAALSpace(currentAALSpace);

            }
        } else {
            Log.w(TAG, "AALSpace Manager not initialized" );
        }
        currentAALSpace = null;
        managedAALspaces.clear();
        synchronized (foundAALSpaces) {
            foundAALSpaces.clear();
        }
        initialized = false;
    }

    /**
     * Destroy all the managed AALSpace
     *
     * @param spaceDescriptor
     */
    private void closeManagedSpace(AALSpaceDescriptor spaceDescriptor) {

        controlBroker.requestToLeave(spaceDescriptor);
        try {
            Thread.sleep(waitBeforeClosingChannels);
        } catch (InterruptedException e) {
            Log.e(TAG,  "Error during wait: ",e);
        }
        ChannelDescriptor peeringChannel = new ChannelDescriptor(
                spaceDescriptor.getSpaceCard().getPeeringChannelName(), "",
                null);
        List<ChannelDescriptor> channels = new ArrayList<ChannelDescriptor>();
        channels.add(peeringChannel);
        channels.addAll(spaceDescriptor.getBrokerChannels());
        controlBroker.resetModule(channels);
        controlBroker.destroyAALSpace(spaceDescriptor.getSpaceCard());

    }

    public void addAALSpaceListener(AALSpaceListener listener) {
        if (listener != null && !listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeAALSpaceListener(AALSpaceListener listener) {
        if (listener != null)
            listeners.remove(listener);
    }

    public void setListOfPeers(Map<String, PeerCard> peers) {

        // verify if among the peers the coordinator is present. If not the
        // coordinator crashed
        if (currentAALSpace != null
                && !peers.keySet().contains(
                        currentAALSpace.getSpaceCard().getCoordinatorID())) {
            // coordinator crashed, leave from the AAL Space

            leaveRequest(currentAALSpace);
        } else
            this.peers = peers;
    }

    public AALSpaceStatus getAALSpaceStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    public void mpaInstalled(AALSpaceDescriptor spaceDescriptor) {
        controlBroker.signalAALSpaceStatus(AALSpaceStatus.INSTALLED_UAAP,
                spaceDescriptor);

    }

    public void mpaInstalling(AALSpaceDescriptor spaceDescriptor) {
        // send a event notification to the AALSpace
        controlBroker.signalAALSpaceStatus(AALSpaceStatus.INSTALLING_UAAP,
                spaceDescriptor);
    }

    public void aalSpaceEvent(AALSpaceStatus newStatus) {
        Log.i(TAG, "--->New event from AALSpace: "
                        + newStatus.toString() );

        for (AALSpaceListener elem : listeners) {
            elem.aalSpaceStatusChanged(newStatus);
        }

    }

    public MatchingResult getMatchingPeers(Map<String, Serializable> filter) {
        final int limit = getPeers().size();
        final long timeout = TIMEOUT;
        final Map<PeerCard, Map<String, Serializable>> result = controlBroker
                .findMatchingPeers(filter, limit, (int) timeout);
        final MatchingResult response = new MatchingResultImpl(result);
        return response;
    }

    public Map<String, Serializable> getPeerAttributes(List<String> attributes,
            PeerCard target) {
        final int limit = 1;
        final long timeout = TIMEOUT;
        final Map<String, Serializable> result = controlBroker
                .requestPeerAttributes(attributes, target, limit, (int) timeout);
        return result;
    }

}

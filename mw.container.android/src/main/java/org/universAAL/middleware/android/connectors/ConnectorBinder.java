package org.universAAL.middleware.android.connectors;

import java.lang.ref.WeakReference;
import java.util.Dictionary;
import java.util.List;
import org.universAAL.middleware.connectors.ServiceListener;
import org.universAAL.middleware.connectors.exception.CommunicationConnectorException;
import org.universAAL.middleware.connectors.exception.DiscoveryConnectorException;
import org.universAAL.middleware.connectors.util.ChannelMessage;
import org.universAAL.middleware.interfaces.ChannelDescriptor;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceCard;

import android.os.Binder;

public class ConnectorBinder extends Binder {
	private WeakReference<ConnectorService> mService;

	public ConnectorBinder(ConnectorService connectorService) {
		mService = new WeakReference<ConnectorService>(connectorService);
	}

	// TODO Check nulls of mService.get(). ... ????

	// TODO Fix? All these are from Connector, which is superclass of both
	// interfaces. However these methods are not supposed to be called by other
	// modules, but from Activators, and therefore have nothing to do here.
	public void dispose() {
		return;
	}

	public String getDescription() {
		return "";
	}

	public String getName() {
		return "";
	}

	public String getProvider() {
		return "";
	}

	public String getVersion() {
		return "";
	}

	public boolean init() {
		return true;
	}

	public void loadConfigurations(Dictionary arg0) {
		return;
	}

	// These are from DiscoveryConnector
	public void addAALSpaceListener(ServiceListener arg0) {
		mService.get().getSlpDiscoveryConnector().addAALSpaceListener(arg0);
	}

	public void announceAALSpace(AALSpaceCard arg0)
			throws DiscoveryConnectorException {
		mService.get().getSlpDiscoveryConnector().announceAALSpace(arg0);
	}

	public void deregisterAALSpace(AALSpaceCard arg0)
			throws DiscoveryConnectorException {
		mService.get().getSlpDiscoveryConnector().deregisterAALSpace(arg0);
	}

	public List<AALSpaceCard> findAALSpace() throws DiscoveryConnectorException {
		// TODO Caution here, what to return for fooling when null?
		return mService.get().getSlpDiscoveryConnector().findAALSpace();
	}

	public List<AALSpaceCard> findAALSpace(Dictionary<String, String> arg0)
			throws DiscoveryConnectorException {
		// TODO Caution here, what to return for fooling when null?
		return mService.get().getSlpDiscoveryConnector().findAALSpace(arg0);
	}

	public String getSDPPRotocol() {
		// TODO Can we just put the string?
		return mService.get().getSlpDiscoveryConnector().getSDPPRotocol();
	}

	public void removeAALSpaceListener(ServiceListener arg0) {
		mService.get().getSlpDiscoveryConnector().removeAALSpaceListener(arg0);
	}

	// These are from CommunicationConnector
	public void configureConnector(List<ChannelDescriptor> arg0, String arg1)
			throws CommunicationConnectorException {
		mService.get().getJgroupsCommunicationConnector()
				.configureConnector(arg0, arg1);
	}

	public void dispose(List<ChannelDescriptor> arg0) {
		mService.get().getJgroupsCommunicationConnector().dispose(arg0);
	}

	public List<String> getGroupMembers(String arg0) {
		// TODO Caution here, what to return for fooling when null?
		return mService.get().getJgroupsCommunicationConnector()
				.getGroupMembers(arg0);
	}

	public boolean hasChannel(String arg0) {
		// TODO Caution here, what to return for fooling when null?
		return mService.get().getJgroupsCommunicationConnector()
				.hasChannel(arg0);
	}

	public void multicast(ChannelMessage arg0)
			throws CommunicationConnectorException {
		mService.get().getJgroupsCommunicationConnector().multicast(arg0);
	}

	public void multicast(ChannelMessage arg0, List<PeerCard> arg1)
			throws CommunicationConnectorException {
		mService.get().getJgroupsCommunicationConnector().multicast(arg0, arg1);
	}

	public void unicast(ChannelMessage arg0, String arg1) {
		mService.get().getJgroupsCommunicationConnector().unicast(arg0, arg1);
	}

}

package org.camunda.bpm.cycle.connector.ibo;

import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;

import javax.inject.Inject;

import org.camunda.bpm.cycle.configuration.CycleConfiguration;
import org.camunda.bpm.cycle.connector.Connector;
import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;
import org.camunda.bpm.cycle.connector.ContentInformation;
import org.camunda.bpm.cycle.connector.Secured;
import org.camunda.bpm.cycle.entity.ConnectorConfiguration;

public class IboConnector extends Connector {

	// custom config properties
	public final static String CONFIG_KEY_IBO_BASE_URL = "iboBaseUrl";
	public final static String CONFIG_KEY_PROXY_URL = "proxyUrl";
	public final static String CONFIG_KEY_PROXY_USERNAME = "proxyUsername";
	public final static String CONFIG_KEY_PROXY_PASSWORD = "proxyPassword";

	private IboClient iboClient;

	private boolean isLoggedIn = false;

	@Inject
	private CycleConfiguration cycleConfiguration;

	@Override
	public void login(String userName, String password) {

		if (getIboClient() == null) {
			ConnectorConfiguration connectorConfiguration = getConfiguration();
			init(connectorConfiguration);
		}

		isLoggedIn = getIboClient().login(userName, password);
	}

	@Override
	public void init(ConnectorConfiguration config) {
		try {
			String defaultCommitMessage = getDefaultCommitMessage();
			iboClient = new IboClient(getConfiguration().getName(), getConfiguration().getProperties().get(
					CONFIG_KEY_IBO_BASE_URL), getConfiguration().getProperties().get(CONFIG_KEY_PROXY_URL),
					getConfiguration().getProperties().get(CONFIG_KEY_PROXY_USERNAME), getConfiguration().getProperties()
							.get(CONFIG_KEY_PROXY_PASSWORD), defaultCommitMessage);
			isLoggedIn = false;
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}

	@Secured
	@Override
	public List<ConnectorNode> getChildren(ConnectorNode parent) {
		String childrenStr = getIboClient().getChildren(parent);
		return IboJson.getConnectorNodeList(childrenStr, getId());
	}

	@Secured
	@Override
	public ConnectorNode getRoot() {
		String rootNodeStr = getIboClient().getRootNode();
		ConnectorNode rootNode = IboJson.getConnectorNode(rootNodeStr);
		rootNode.setConnectorId(getId());
		return rootNode;
	}

	@Override
	public ConnectorNode getNode(String id) {
		return null;
	}

	@Override
	@Secured
	public InputStream getContent(ConnectorNode node) {
		switch (node.getType()) {
			case PNG_FILE:
				InputStream pngStream = getIboClient().getPngFile(node.getId());
				return pngStream;
			default:
				InputStream xmlStream = getIboClient().getXmlFile(node.getId());
				return xmlStream;
		}
	}

	@Secured
	@Override
	public ContentInformation getContentInformation(ConnectorNode node) {
		String contentInfoStr = getIboClient().getContentInformation(node);
		ContentInformation contentInformation = IboJson.getContentInformation(contentInfoStr);
		return contentInformation;
	}

	@Override
	public ConnectorNode createNode(String parentId, String label, ConnectorNodeType type, String message) {
		if (type == ConnectorNodeType.FOLDER) {
			String connectorNodeStr = getIboClient().createFolder(parentId, label, message);
			return IboJson.getConnectorNode(connectorNodeStr);
		} else if (type == ConnectorNodeType.BPMN_FILE) {
			String connectorNodeStr = getIboClient().createFile(parentId, label, message);
			return IboJson.getConnectorNode(connectorNodeStr);
		}
		return null;
	}

	@Secured
	@Override
	public void deleteNode(ConnectorNode node, String message) {
		if (node.getType() == ConnectorNodeType.FOLDER) {
			getIboClient().deleteFolder(node.getId(), message);
		} else if (node.getType() == ConnectorNodeType.BPMN_FILE) {
			getIboClient().deleteFile(node.getId(), message);
		}
	}

	@Secured
	@Override
	public ContentInformation updateContent(ConnectorNode node, InputStream newContent, String message) throws Exception {
		String contentInfoStr = getIboClient().updateContent(node, newContent, message);
		return IboJson.getContentInformation(contentInfoStr);
	}

	@Override
	public boolean needsLogin() {
		return !isLoggedIn;
	}

	@Override
	public boolean isSupportsCommitMessage() {
		return true;
	}

	@Override
	public void dispose() {
		if (getIboClient() != null) {
			getIboClient().endSession();
			getIboClient().dispose();
			iboClient = null;
			isLoggedIn = false;
		}
	}

	protected String getDefaultCommitMessage() {
		if (cycleConfiguration != null) {
			return cycleConfiguration.getDefaultCommitMessage();
		} else {
			return "";
		}
	}

	private IboClient getIboClient() {
		return iboClient;
	}

}
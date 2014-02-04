package org.camunda.bpm.cycle.connector.ibo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;
import org.camunda.bpm.cycle.connector.ContentInformation;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class IboJson {
	public static boolean extractSuccess(JSONObject json) throws JSONException {
		return json.getBoolean("success");
	}

	public static String extractSessionId(JSONObject json) throws JSONException {
		return json.getString("sessionId");
	}

	public static ConnectorNode getConnectorNode(String strData) {
		try {
			return getConnectorNode(new JSONObject(strData));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static List<ConnectorNode> getConnectorNodeList(String strData, Long connectorId) {
		List<ConnectorNode> connectorNodeList = new ArrayList<ConnectorNode>();

		try {
			JSONArray arr = new JSONArray(strData);
			for (int i = 0; i < arr.length(); i++) {
				JSONObject obj = arr.getJSONObject(i);
				ConnectorNode node = getConnectorNode(obj);
				node.setConnectorId(connectorId);
				connectorNodeList.add(node);
			}
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}

		return connectorNodeList;
	}

	private static ConnectorNode getConnectorNode(JSONObject obj) throws JSONException {
		ConnectorNode connectorNode = new ConnectorNode();

		if (obj.has("connectorNodeType")) {
			connectorNode.setType(ConnectorNodeType.valueOf(obj.getString("connectorNodeType")));
		}
		if (obj.has("id")) {
			connectorNode.setId(obj.getString("id"));
		}
		if (obj.has("label")) {
			connectorNode.setLabel(obj.getString("label"));
		}
		if (obj.has("message")) {
			connectorNode.setMessage(obj.getString("message"));
		}
		if (obj.has("created")) {
			connectorNode.setCreated(new Date(obj.getLong("created")));
		}
		if (obj.has("lastModified")) {
			connectorNode.setLastModified(new Date(obj.getLong("lastModified")));
		}
		return connectorNode;
	}

	public static ContentInformation getContentInformation(String contentInfoStr) {
		try {
			JSONObject obj = new JSONObject(contentInfoStr);
			Date lastModified = null;
			if (obj.has("lastModified")) {
				lastModified = new Date(obj.getLong("lastModified"));
			}

			Boolean isAvailable = false;
			if (obj.has("isAvailable")) {
				isAvailable = obj.getBoolean("isAvailable");
			}

			return new ContentInformation(isAvailable, lastModified);
		} catch (JSONException e) {
			return null;
		}
	}
}

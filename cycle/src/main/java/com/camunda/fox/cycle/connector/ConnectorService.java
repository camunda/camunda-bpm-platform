package com.camunda.fox.cycle.connector;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import com.camunda.fox.cycle.api.connector.Connector;
import com.camunda.fox.cycle.api.connector.ConnectorNode;
import com.camunda.fox.cycle.api.connector.ConnectorNode.ConnectorNodeType;
import com.camunda.fox.cycle.web.dto.ConnectorDTO;

@Path("secured/connector")
public class ConnectorService {
  
  @Inject
  ConnectorRegistry connectorRegistry;
  
  @GET
  @Path("/list")
  @Produces("application/json")
  public List<ConnectorDTO> list() {
    ArrayList<ConnectorDTO> result = new ArrayList<ConnectorDTO>();
    for (Connector c : connectorRegistry.getConnectors()) {
      result.add(new ConnectorDTO(c));
    }
    return result;
  }
  
  @GET
  @Path("{id}/tree/root")
  @Produces("application/json")
  public List<ConnectorNode> tree(@PathParam("id") String connectorId) {
    ArrayList<ConnectorNode> rootList = new ArrayList<ConnectorNode>();
    ConnectorNode rootNode = new ConnectorNode("/", "/");
    rootNode.setLabel(connectorId);
    rootNode.setType(ConnectorNodeType.FOLDER);
    rootList.add(rootNode);
    return rootList;
  }
  
  @POST
  @Path("{id}/tree/children")
  @Produces("application/json")
  public List<ConnectorNode> children(@PathParam("id") String connectorId, @FormParam("parent") String parent, @FormParam("parentPath") String parentPath) {
    Connector connector = connectorRegistry.getSessionConnectorMap().get(connectorId);
    return connector.getChildren(new ConnectorNode(parentPath, parent));
  }
  
}

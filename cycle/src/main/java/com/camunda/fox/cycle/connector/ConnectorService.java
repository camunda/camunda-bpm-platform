package com.camunda.fox.cycle.connector;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import com.camunda.fox.cycle.api.connector.Connector;
import com.camunda.fox.cycle.web.dto.ConnectorDTO;

@Path("/connector")
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
  public ArrayNode tree(@PathParam("id") String connectorId) {
    JsonNodeFactory factory = JsonNodeFactory.instance;
    ArrayNode resultList = factory.arrayNode();
    ObjectNode rootNode = factory.objectNode();
    rootNode.put("id", "root");
    rootNode.put("name", connectorId);
    rootNode.put("path", "/");
    rootNode.put("type", "folder");
    resultList.add(rootNode);
    return resultList;
  }
  
  @GET
  @Path("{id}/tree/{parentId}/children")
  @Produces("application/json")
  public ArrayNode children(@PathParam("id") String connectorId, @PathParam("parentId") String parentId) {
    JsonNodeFactory factory = JsonNodeFactory.instance;
    ArrayNode resultList = factory.arrayNode();
    ObjectNode rootNode = factory.objectNode();
    rootNode.put("id", parentId+"child");
    rootNode.put("name", parentId+"child");
    rootNode.put("path", parentId+"/child");
    rootNode.put("type", "folder");
    resultList.add(rootNode);
    return resultList;
  }
  
}

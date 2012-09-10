package com.camunda.fox.cycle.web.service.resource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.camunda.fox.cycle.api.connector.Connector;
import com.camunda.fox.cycle.api.connector.Connector.ConnectorContentType;
import com.camunda.fox.cycle.api.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorRegistry;
import com.camunda.fox.cycle.web.dto.ConnectorDTO;
import com.camunda.fox.cycle.web.dto.ConnectorNodeDTO;

@Path("secured/resource/connector")
public class ConnectorService {
  
  @Inject
  protected ConnectorRegistry connectorRegistry;
  
  @GET
  @Path("/list")
  @Produces("application/json")
  public List<ConnectorDTO> list() {
    ArrayList<ConnectorDTO> result = new ArrayList<ConnectorDTO>();
    for (Connector c : connectorRegistry.getSessionConnectors()) {
      result.add(new ConnectorDTO(c));
    }
    return result;
  }
  
  @GET
  @Path("{id}/tree/root")
  @Produces("application/json")
  public List<ConnectorNodeDTO> root(@PathParam("id") Long connectorId) {
    Connector connector = connectorRegistry.getSessionConnectorMap().get(connectorId);
    List<ConnectorNode> rootList = new ArrayList<ConnectorNode>();
    rootList.add(connector.getRoot());
    return ConnectorNodeDTO.wrapAll(rootList);
  }
  
  @POST
  @Path("{id}/tree/children")
  @Produces("application/json")
  public List<ConnectorNodeDTO> children(@PathParam("id") Long connectorId, @FormParam("parent") String parent) {
    Connector connector = connectorRegistry.getSessionConnectorMap().get(connectorId);
    return ConnectorNodeDTO.wrapAll(connector.getChildren(new ConnectorNode(parent)));
  }
  
  @POST
  @Path("{id}/tree/content")
  @Produces("application/xml")
  public String content(@PathParam("id") Long connectorId, @FormParam("nodeId") String nodeId) {
    Connector connector = connectorRegistry.getSessionConnectorMap().get(connectorId);
    try {
      return new java.util.Scanner(connector.getContent(new ConnectorNode(nodeId))).useDelimiter("\\A").next();
    } catch (java.util.NoSuchElementException e) {
      return "";
    }
  }
  
  @GET
  @Path("{id}/content/{type}")
  public Response getTypedContent(@PathParam("id") Long connectorId, @QueryParam("nodeId") String nodeId, @PathParam("type") ConnectorContentType type) {
    Connector connector = connectorRegistry.getSessionConnectorMap().get(connectorId);
    InputStream content = connector.getContent(new ConnectorNode(nodeId), type);
    return Response.ok(content)
            .header("Content-Type", connector.getMimeType(type))
            .build();
  }
  
  @GET
  @Path("{id}/content/{type}/available")
  public boolean isContentAvailable(@PathParam("id") Long connectorId, @QueryParam("nodeId") String nodeId, @PathParam("type") ConnectorContentType type) {
    Connector connector = connectorRegistry.getSessionConnectorMap().get(connectorId);
    return connector.isContentAvailable(new ConnectorNode(nodeId), type);
  }
  
}

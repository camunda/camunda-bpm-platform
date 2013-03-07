package org.camunda.bpm.cycle.web.service.resource;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.cycle.connector.Connector;
import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;
import org.camunda.bpm.cycle.connector.ConnectorRegistry;
import org.camunda.bpm.cycle.connector.ContentInformation;
import org.camunda.bpm.cycle.util.IoUtil;
import org.camunda.bpm.cycle.web.dto.ConnectorDTO;
import org.camunda.bpm.cycle.web.dto.ConnectorNodeDTO;
import org.camunda.bpm.cycle.web.jaxrs.ext.JaxRsUtil;
import org.camunda.bpm.cycle.web.service.AbstractRestService;


@Path("secured/resource/connector")
public class ConnectorService extends AbstractRestService {
  
  @Inject
  protected ConnectorRegistry connectorRegistry;
  
  @GET
  @Path("list")
  @Produces("application/json")
  public List<ConnectorDTO> list() {
    ArrayList<ConnectorDTO> result = new ArrayList<ConnectorDTO>();
    for (Connector c : connectorRegistry.getConnectors()) {
      result.add(new ConnectorDTO(c));
    }
    return result;
  }
  
  @GET
  @Path("{connectorId}/root")
  @Produces("application/json")
  public List<ConnectorNodeDTO> root(@PathParam("connectorId") long connectorId) {
    Connector connector = getConnector(connectorId);
    
    List<ConnectorNode> rootList = new ArrayList<ConnectorNode>();
    rootList.add(connector.getRoot());
    return ConnectorNodeDTO.wrapAll(rootList);
  }
  
  @GET
  @Path("{connectorId}/children")
  @Produces("application/json")
  public List<ConnectorNodeDTO> children(@PathParam("connectorId") long connectorId, @QueryParam("nodeId") String nodeId, @QueryParam("type") List<ConnectorNodeType> connectorNodeTypes) {
    Connector connector = getConnector(connectorId);
    
    // Filter by content type
    List<ConnectorNode> children = connector.getChildren(new ConnectorNode(nodeId));
    Iterator<ConnectorNode> i = children.iterator();
    while (i.hasNext()) {
      ConnectorNode next = i.next();
      if (!next.getType().isAnyOf(connectorNodeTypes.toArray(new ConnectorNodeType[0]))) {
        i.remove();
      }
    }
    
    return ConnectorNodeDTO.wrapAll(children);
  }
  
  @Path("{connectorId}/contents")
  @Produces("application/xml")
  public String getXmlContent(@PathParam("connectorId") long connectorId, @QueryParam("nodeId") String nodeId) {
    Connector connector = getConnector(connectorId);
    try {
      return new java.util.Scanner(connector.getContent(new ConnectorNode(nodeId))).useDelimiter("\\A").next();
    } catch (java.util.NoSuchElementException e) {
      return "";
    }
  }
  
  @GET
  @Path("{connectorId}/contents")
  public Response getTypedContent(@PathParam("connectorId") long connectorId, @QueryParam("nodeId") String nodeId, @QueryParam("type") ConnectorNodeType type) {
    Connector connector = getConnector(connectorId);
    InputStream content = connector.getContent(new ConnectorNode(nodeId, null, type));
    
    if (content == null) {
      return JaxRsUtil.createResponse().status(Response.Status.NOT_FOUND).build();
    }
    
    // nre: TODO: Why not guess by extension?
    try {
      return JaxRsUtil.createResponse().status(Status.OK).entity(IoUtil.readInputStream(content, connectorId + "-" + nodeId + "-content-stream"))
              .header("Content-Type", type.getMimeType())
              .build();
    } finally {
      IoUtil.closeSilently(content);
    }
  }

  @GET
  @Path("{connectorId}/contents/info")
  public ContentInformation getContentInfo(
    @PathParam("connectorId") long connectorId, 
    @QueryParam("nodeId") String nodeId, 
    @QueryParam("type") @DefaultValue("ANY_FILE") ConnectorNodeType type) {
    
    Connector connector = getConnector(connectorId);
    return connector.getContentInformation(new ConnectorNode(nodeId, null, type));
  }

  protected Connector getConnector(long connectorId) {
    Connector connector = connectorRegistry.getConnector(connectorId);
    
    if (connector == null) {
      throw notFound("connector not found");
    }
    
    return connector;
  }
}

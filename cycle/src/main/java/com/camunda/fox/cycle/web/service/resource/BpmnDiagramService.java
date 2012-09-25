package com.camunda.fox.cycle.web.service.resource;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorRegistry;
import com.camunda.fox.cycle.connector.ContentInformation;
import com.camunda.fox.cycle.entity.BpmnDiagram;
import com.camunda.fox.cycle.entity.BpmnDiagram.Status;
import com.camunda.fox.cycle.repository.BpmnDiagramRepository;
import com.camunda.fox.cycle.web.dto.BpmnDiagramDTO;
import com.camunda.fox.cycle.web.dto.BpmnDiagramStatusDTO;
import com.camunda.fox.cycle.web.dto.ConnectorNodeDTO;

/**
 *
 * @author nico.rehwaldt
 */
@Path("secured/resource/diagram")
public class BpmnDiagramService {

  private static Logger log = Logger.getLogger(BpmnDiagramService.class.getName());
  
  @Inject
  private BpmnDiagramRepository bpmnDiagramRepository;
  
  @Inject
  private ConnectorRegistry connectorRegistry;
  
  
//  @GET
//  public List<BpmnDiagramDTO> list() {
//    // NOTE: No such thing as list for diagrams
//  }

  @GET
  @Path("{id}")
  public BpmnDiagramDTO get(@PathParam("id") long id) {
    return BpmnDiagramDTO.wrap(bpmnDiagramRepository.findById(id));
  }

  @POST
  @Path("{id}")
  @Transactional
  public BpmnDiagramDTO update(BpmnDiagramDTO data) {
    return BpmnDiagramDTO.wrap(createOrUpdate(data));
  }

  @POST
  public BpmnDiagramDTO create(BpmnDiagramDTO data) {
    return BpmnDiagramDTO.wrap(createOrUpdate(data));
  }
  
  /**
   * Create or update the bpmn diagram from the given data
   * 
   * @param data
   * @return 
   */
  @Transactional
  public BpmnDiagram createOrUpdate(BpmnDiagramDTO data) {
    BpmnDiagram diagram;
    
    if (data.getId() == null) {
      diagram = new BpmnDiagram();
      diagram.setStatus(Status.UNSPECIFIED);
    } else {
      diagram = bpmnDiagramRepository.findById(data.getId());
      if (diagram == null) {
        throw new IllegalArgumentException("Not found");
      }
    }
    
    update(diagram, data);
    return bpmnDiagramRepository.saveAndFlush(diagram);
  }
  
  /**
   * Update a diagram with the given data
   * 
   * @param diagram
   * @param data
   */
  private void update(BpmnDiagram diagram, BpmnDiagramDTO data) {
    diagram.setModeler(data.getModeler());

    ConnectorNodeDTO node = data.getConnectorNode();
    diagram.setLabel(node.getLabel());
    
    diagram.setConnectorId(node.getConnectorId());
    diagram.setDiagramPath(node.getId());
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("modelerNames")
  public List<String> getModelerNames() {
    return bpmnDiagramRepository.findAllModelerNames();
  }
  
  @GET
  @Transactional
  @Path("{id}/syncStatus")
  public BpmnDiagramStatusDTO synchronizationStatus(@PathParam("id") long id) {

    BpmnDiagram diagram = bpmnDiagramRepository.findById(id);
    if (diagram == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }

    ConnectorNode node = diagram.getConnectorNode();
    Connector connector = connectorRegistry.getConnector(node.getConnectorId());

    Status status = Status.UNSPECIFIED;
    Date lastModified = null;

    ContentInformation contentInfo = connector.getContentInformation(node);
    if (!contentInfo.exists()) {
      status = Status.UNAVAILABLE;
    } else {
      lastModified = contentInfo.getLastModified();

      if (lastModified != null && diagram.getLastSync() != null) {
        if (lastModified.getTime() <= diagram.getLastSync().getTime()) {
          status = Status.SYNCED;
        }

        if (lastModified.getTime() > diagram.getLastSync().getTime()) {
          status = Status.OUT_OF_SYNC;
        }
      } else {
        status = Status.UNSPECIFIED;
      }
    }
    
    
    BpmnDiagramStatusDTO statusDTO = new BpmnDiagramStatusDTO(diagram.getId(), status, lastModified);
    statusDTO.setLastUpdated(new Date());
    return statusDTO;
  }
}

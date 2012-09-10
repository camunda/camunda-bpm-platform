package com.camunda.fox.cycle.web.service.resource;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.api.connector.Connector;
import com.camunda.fox.cycle.api.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorRegistry;
import com.camunda.fox.cycle.entity.BpmnDiagram;
import com.camunda.fox.cycle.entity.BpmnDiagram.Status;
import com.camunda.fox.cycle.entity.Roundtrip;
import com.camunda.fox.cycle.repository.BpmnDiagramRepository;
import com.camunda.fox.cycle.web.dto.BpmnDiagramDTO;

/**
 *
 * @author nico.rehwaldt
 */
@Path("secured/resource/diagram")
public class BpmnDiagramService {

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

    diagram.setLabel(data.getLabel());
    diagram.setConnectorId(data.getConnectorId());
    diagram.setDiagramPath(data.getDiagramPath());
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("modelerNames")
  public List<String> getModelerNames() {
    return bpmnDiagramRepository.findAllModelerNames();
  }
  
  public BpmnDiagramDTO isDiagramInSync(BpmnDiagramDTO bpmnDiagramDTO, Roundtrip roundtrip) {
    Connector diagramConnector = this.connectorRegistry.getSessionConnectorMap().get(bpmnDiagramDTO.getConnectorId());
    ConnectorNode connectorNode = new ConnectorNode(bpmnDiagramDTO.getDiagramPath(), bpmnDiagramDTO.getLabel());
    Date lastModifiedDate = diagramConnector.getLastModifiedDate(connectorNode);
    
    if (lastModifiedDate != null && roundtrip.getLastSync() != null) {
      if (diagramConnector.getConfiguration().getLabel().equals("VFS Connector")) {
  
        if (lastModifiedDate.before(roundtrip.getLastSync()) || lastModifiedDate.equals(roundtrip.getLastSync())) {
          bpmnDiagramDTO.setStatus(Status.SYNCED);
        }
  
        if (lastModifiedDate.after(roundtrip.getLastSync())) {
          bpmnDiagramDTO.setStatus(Status.OUT_OF_SYNC);
        }
      }
    
    } else {
      bpmnDiagramDTO.setStatus(Status.UNSPECIFIED);
    }
    
    return bpmnDiagramDTO;
  }
}

package org.camunda.bpm.cycle.web.service.resource;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.camunda.bpm.cycle.connector.Connector;
import org.camunda.bpm.cycle.connector.ConnectorNode;
import org.camunda.bpm.cycle.connector.ConnectorNodeType;
import org.camunda.bpm.cycle.connector.ConnectorRegistry;
import org.camunda.bpm.cycle.connector.ContentInformation;
import org.camunda.bpm.cycle.entity.BpmnDiagram;
import org.camunda.bpm.cycle.entity.BpmnDiagram.Status;
import org.camunda.bpm.cycle.repository.BpmnDiagramRepository;
import org.camunda.bpm.cycle.web.dto.BpmnDiagramDTO;
import org.camunda.bpm.cycle.web.dto.BpmnDiagramStatusDTO;
import org.camunda.bpm.cycle.web.dto.ConnectorNodeDTO;
import org.camunda.bpm.cycle.web.service.AbstractRestService;
import org.springframework.transaction.annotation.Transactional;


/**
 *
 * @author nico.rehwaldt
 */
@Path("secured/resource/diagram")
public class BpmnDiagramService extends AbstractRestService {

  private static final int OFFSET = 5000;

  private static Logger log = Logger.getLogger(BpmnDiagramService.class.getName());
  
  @Inject
  private BpmnDiagramRepository bpmnDiagramRepository;

  @Inject
  private ConnectorService connectorService;

  private ConnectorRegistry connectorRegistry;

  @Inject
  public void setConnectorRegistry(ConnectorRegistry connectorRegistry) {
    this.connectorRegistry = connectorRegistry;
  }

//  @GET
//  public List<BpmnDiagramDTO> list() {
//    // NOTE: No such thing as list for diagrams
//  }

  @GET
  @Path("{id}")
  public BpmnDiagramDTO get(@PathParam("id") long id) {
    return BpmnDiagramDTO.wrap(getDiagramById(id));
  }

  @GET
  @Path("{id}/image")
  public Response getImage(@PathParam("id") long id) {
    
    // we do offer the functionality to serve images here, rather than relying on the connector service directly
    // because we need to add the out of date logic which is not the connector services' concern

    BpmnDiagram diagram = getDiagramById(id);
    ConnectorNode node = diagram.getConnectorNode();

    ContentInformation imageInformation = connectorService.getContentInfo(node.getConnectorId(), node.getId(), ConnectorNodeType.PNG_FILE);

    if (!imageInformation.exists()) {
      throw notFound("no image");
    }

    Date diagramLastModified = diagram.getLastModified();
    if (diagramLastModified != null) {
      
      Date imageLastModified = imageInformation.getLastModified();
      if (imageLastModified != null) {
        // need to do comparison based on timestamp to ignore time zones
        if ((imageLastModified.getTime() + OFFSET) < diagramLastModified.getTime()) {
          // diagram is younger than the image --> image out of date
          throw notFound("no up to date image");
        }
      }
    }
    
    // everything ok --> serve image data
    return connectorService.getTypedContent(node.getConnectorId(), node.getId(), ConnectorNodeType.PNG_FILE);
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
      diagram = getDiagramById(data.getId());
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

    BpmnDiagram diagram = getDiagramById(id);

    ConnectorNode node = diagram.getConnectorNode();
    Connector connector = connectorRegistry.getConnector(node.getConnectorId());
    if (connector == null) {
      BpmnDiagramStatusDTO notFoundStatus = new BpmnDiagramStatusDTO(diagram.getId(), Status.UNAVAILABLE, null);
      notFoundStatus.setLastUpdated(new Date());
      return notFoundStatus;
    }

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
        } else {
          status = Status.OUT_OF_SYNC;
        }
      } else {
        status = Status.UNSPECIFIED;
      }
    }
    
    // update last modified diagram status
    diagram.setLastModified(lastModified);
    
    BpmnDiagramStatusDTO statusDTO = new BpmnDiagramStatusDTO(diagram.getId(), status, diagram.getLastModified());
    statusDTO.setLastUpdated(new Date());
    return statusDTO;
  }

  protected BpmnDiagram getDiagramById(long id) {
    BpmnDiagram diagram = bpmnDiagramRepository.findById(id);
    if (diagram == null) {
      throw notFound("diagram not found");
    }
    
    return diagram;
  }
}

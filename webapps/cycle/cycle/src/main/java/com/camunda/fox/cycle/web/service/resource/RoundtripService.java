package com.camunda.fox.cycle.web.service.resource;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorNodeType;
import com.camunda.fox.cycle.connector.ConnectorRegistry;
import com.camunda.fox.cycle.connector.ContentInformation;
import com.camunda.fox.cycle.connector.signavio.SignavioConnector;
import com.camunda.fox.cycle.entity.BpmnDiagram;
import com.camunda.fox.cycle.entity.Roundtrip;
import com.camunda.fox.cycle.entity.Roundtrip.SyncMode;
import com.camunda.fox.cycle.exception.CycleException;
import com.camunda.fox.cycle.repository.RoundtripRepository;
import com.camunda.fox.cycle.roundtrip.SynchronizationService;
import com.camunda.fox.cycle.util.IoUtil;
import com.camunda.fox.cycle.web.dto.BpmnDiagramDTO;
import com.camunda.fox.cycle.web.dto.ConnectorNodeDTO;
import com.camunda.fox.cycle.web.dto.RoundtripDTO;
import com.camunda.fox.cycle.web.dto.SynchronizationResultDTO;
import com.camunda.fox.cycle.web.dto.SynchronizationResultDTO.SynchronizationStatus;
import com.camunda.fox.cycle.web.service.AbstractRestService;

/**
 * This is the main roundtrip rest controller which exposes roundtrip
 * <code>list</code>,
 * <code>get</code>,
 * <code>create</code> and<code>update</code> methods as well as some utilities to the cycle client application.
 *
 * The arrangement of methods is compatible with angular JS
 * <code>$resource</code>.
 *
 * @author nico.rehwaldt
 */
@Path("secured/resource/roundtrip")
public class RoundtripService extends AbstractRestService {

  @Inject
  private RoundtripRepository roundtripRepository;
  
  @Inject
  private ConnectorRegistry connectorRegistry;
  
  @Inject
  private BpmnDiagramService bpmnDiagramController;
  
  @Inject
  private SynchronizationService synchronizationService;


  // roundtrip $resource //////////////////////////////////////////////
  
  @GET
  public List<RoundtripDTO> list() {
    return RoundtripDTO.wrapAll(roundtripRepository.findAll());
  }

  @GET
  @Path("{id}")
  public RoundtripDTO get(@PathParam("id") long id) {
    return RoundtripDTO.wrap(roundtripRepository.findById(id));
  }

  @POST
  @Path("{id}")
  @Transactional
  public RoundtripDTO update(RoundtripDTO data) {
    long id = data.getId();

    Roundtrip roundtrip = roundtripRepository.findById(id);
    if (roundtrip == null) {
      throw notFound("roundtrip not found");
    }

    update(roundtrip, data);
    return RoundtripDTO.wrap(roundtrip);
  }

  @POST
  public RoundtripDTO create(RoundtripDTO data) {
    Roundtrip roundtrip = new Roundtrip();
    update(roundtrip, data);
    return RoundtripDTO.wrap(roundtripRepository.saveAndFlush(roundtrip));
  }
  
  @DELETE
  @Path("{id}")
  @Transactional
  public void delete(@PathParam("id") long id) {
    Roundtrip roundtrip = roundtripRepository.findById(id);
    if (roundtrip == null) {
      throw notFound("roundtrip not found");
    }
    
    roundtripRepository.delete(roundtrip);
  }

  // roundtrip details $resource //////////////////////////////////////////////
  
  @GET
  @Transactional
  @Path("{id}/details")
  public RoundtripDTO getDetails(@PathParam("id") long id) {
    Roundtrip roundtrip = roundtripRepository.findById(id);

    RoundtripDTO roundtripDTO = new RoundtripDTO(roundtrip);

    if (roundtrip.getLeftHandSide() != null) {
      roundtripDTO.setLeftHandSide(BpmnDiagramDTO.wrap(roundtrip.getLeftHandSide()));
    } else {
      roundtripDTO.setLeftHandSide(null);
    }

    if (roundtrip.getRightHandSide() != null) {
      roundtripDTO.setRightHandSide(BpmnDiagramDTO.wrap(roundtrip.getRightHandSide()));
    } else {
      roundtripDTO.setRightHandSide(null);
    }

    // TODO: Fetch eager
    return roundtripDTO;
  }

  @POST
  @Path("{id}/details")
  @Transactional
  public RoundtripDTO updateDetails(RoundtripDTO data) {
    long id = data.getId();

    Roundtrip roundtrip = roundtripRepository.findById(id);
    if (roundtrip == null) {
      throw notFound("roundtrip not found");
    }

    if (data.getLeftHandSide() != null) {
      BpmnDiagramDTO leftHandSideDTO = data.getLeftHandSide();
      BpmnDiagram leftHandSide = bpmnDiagramController.createOrUpdate(leftHandSideDTO);
      roundtrip.setLeftHandSide(leftHandSide);
    } else {
      roundtrip.setLeftHandSide(null);
    }

    if (data.getRightHandSide() != null) {
      BpmnDiagramDTO rightHandSideDTO = data.getRightHandSide();
      BpmnDiagram rightHandSide = bpmnDiagramController.createOrUpdate(rightHandSideDTO);
      roundtrip.setRightHandSide(rightHandSide);
    } else {
      roundtrip.setRightHandSide(null);
    }

    Roundtrip saved = roundtripRepository.saveAndFlush(roundtrip);
    return new RoundtripDTO(saved, saved.getLeftHandSide(), saved.getRightHandSide());
  }

  // querying ////////////////////////////////////////////////
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("isNameAvailable")
  public boolean isNameAvailable(@QueryParam("name") String name) {
    return roundtripRepository.isNameAvailable(name);
  }

  @POST
  @Path("{id}/sync")
  @Transactional
  public SynchronizationResultDTO doSynchronize(@QueryParam("syncMode") SyncMode syncMode, @PathParam("id") long roundtripId, @QueryParam("message") String message) {
    try {
      Roundtrip roundtrip = roundtripRepository.findById(roundtripId);
      
      if (roundtrip == null) {
        throw notFound("roundtrip not found");
      }
      
      synchronizeModels(roundtrip.getLeftHandSide(), roundtrip.getRightHandSide(), syncMode, message);
      
      roundtrip.setLastSync(new Date());
      roundtrip.setLastSyncMode(syncMode);
      
      return new SynchronizationResultDTO(SynchronizationStatus.SYNC_SUCCESS);
    } catch (Exception e) {
      return new SynchronizationResultDTO(SynchronizationStatus.SYNC_FAILED, e.getMessage());
    }
  }
  
  @POST
  @Path("{id}/create")
  @Transactional
  public RoundtripDTO create(@PathParam("id") long roundtripId, @QueryParam("diagramlabel") String diagramLabel, @QueryParam("syncMode") SyncMode syncMode, 
                             @QueryParam("modeler") String modeler, @QueryParam("connectorId") Long connectorId, @QueryParam("parentFolderId") String parentFolderId,
                             @QueryParam("message") String message) {
    
    Roundtrip roundtrip = roundtripRepository.findById(roundtripId);
    if (roundtrip == null) {
      throw notFound("Roundtrip not found");
    }
    
    if (roundtrip.getLeftHandSide() == null && roundtrip.getRightHandSide() == null) {
      throw new CycleException("No model exists in roundtrip '" + roundtrip.getName() + "'. It is not possible to create a diagram model.");
    }
    
    Connector connector = connectorRegistry.getConnector(connectorId);
    if (!(connector instanceof SignavioConnector)) {
      if (!diagramLabel.endsWith(".xml") && !diagramLabel.endsWith(".bpmn")) {
        diagramLabel = diagramLabel.concat(".bpmn");
      }
    }
    
    BpmnDiagramDTO bpmnDiagramDTO = new BpmnDiagramDTO();
    bpmnDiagramDTO.setLabel(diagramLabel);
    bpmnDiagramDTO.setModeler(modeler);
    
    ConnectorNode newConnectorNode = connector.createNode(parentFolderId, diagramLabel, ConnectorNodeType.BPMN_FILE, message);
    bpmnDiagramDTO.setConnectorNode(new ConnectorNodeDTO(newConnectorNode));

    BpmnDiagram bpmnDiagram = bpmnDiagramController.createOrUpdate(bpmnDiagramDTO);
    bpmnDiagram.setConnectorId(connectorId);
    bpmnDiagram.setLastModified(new Date());
    
    switch (syncMode) {
    case LEFT_TO_RIGHT:
      synchronizeModels(roundtrip.getLeftHandSide(), bpmnDiagram, SyncMode.LEFT_TO_RIGHT, message);
      roundtrip.setRightHandSide(bpmnDiagram);
      break;
    case RIGHT_TO_LEFT:
      // create left hand side model from right hand side model
      // it's only a copy from the right hand side model, no poolextraction or something else is necessary
      ConnectorNode rhsNode = roundtrip.getRightHandSide().getConnectorNode();
      Connector rhsConnector = connectorRegistry.getConnector(rhsNode.getConnectorId());
      InputStream rhsInputStream = rhsConnector.getContent(rhsNode);
      
      try {
        ContentInformation contentInformation = connector.updateContent(newConnectorNode, rhsInputStream, message);
        bpmnDiagram.setLastSync(contentInformation.getLastModified());
        IoUtil.closeSilently(rhsInputStream);
        
        ContentInformation otherSideInfo = rhsConnector.getContentInformation(rhsNode);
        roundtrip.getRightHandSide().setLastSync(otherSideInfo.getLastModified());
        
      } catch (Exception e) {
        throw new CycleException("Creation of left hand side model failed.", e);
      }
      
      roundtrip.setLeftHandSide(bpmnDiagram);
      break;
    }
    
    roundtrip.setLastSync(new Date());
    roundtrip.setLastSyncMode(syncMode);
    
    RoundtripDTO roundtripDTO = RoundtripDTO.wrapIncludeDiagrams(roundtrip);
    
    return roundtripDTO;
  }

  /**
   * Updates the roundtrip with the given data
   *
   * @param roundtrip
   * @param data
   */
  private void update(Roundtrip roundtrip, RoundtripDTO data) {
    roundtrip.setName(data.getName());
  }

  private void synchronizeModels(BpmnDiagram lhs, BpmnDiagram rhs, SyncMode syncMode, String message) {

    if (lhs == null) {
      throw new IllegalArgumentException("Left hand side model is null");
    }

    if (rhs == null) {
      throw new IllegalArgumentException("Right hand side model is null");
    }

    ConnectorNode lhsNode = lhs.getConnectorNode();
    ConnectorNode rhsNode = rhs.getConnectorNode();

    // perform actual synchronization
    Connector lhsConnector = connectorRegistry.getConnector(lhsNode.getConnectorId());
    Connector rhsConnector = connectorRegistry.getConnector(rhsNode.getConnectorId());

    InputStream lhsInputStream = lhsConnector.getContent(lhsNode);
    InputStream rhsInputStream = rhsConnector.getContent(rhsNode);

    try {
      ContentInformation resultInfo;
      ContentInformation otherSideInfo;
      InputStream resultStream = null;
      switch (syncMode) {
        case LEFT_TO_RIGHT:
          resultStream = synchronizationService.syncLeftToRight(lhsInputStream, rhsInputStream);
          IoUtil.closeSilently(lhsInputStream, rhsInputStream);
          resultInfo = rhsConnector.updateContent(rhsNode, resultStream, message);
          rhs.setLastSync(resultInfo.getLastModified());
          
          otherSideInfo = lhsConnector.getContentInformation(lhsNode);
          lhs.setLastSync(otherSideInfo.getLastModified());
          break;
        case RIGHT_TO_LEFT:
          resultStream = synchronizationService.syncRightToLeft(lhsInputStream, rhsInputStream);
          IoUtil.closeSilently(lhsInputStream, rhsInputStream);
          resultInfo = lhsConnector.updateContent(lhsNode, resultStream, message);
          lhs.setLastSync(resultInfo.getLastModified());
          
          otherSideInfo = rhsConnector.getContentInformation(rhsNode);
          rhs.setLastSync(otherSideInfo.getLastModified());
          break;
      }
      
      IoUtil.closeSilently(resultStream);
    } catch (Exception e) {
      if (e instanceof CycleException) {
        throw (CycleException) e;
      }
      throw new CycleException("Synchronization failed: " + e.getMessage(), e);
    }
  }
}

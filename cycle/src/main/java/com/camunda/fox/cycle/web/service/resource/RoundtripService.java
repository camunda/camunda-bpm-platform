package com.camunda.fox.cycle.web.service.resource;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.util.IOUtil;
import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.api.connector.Connector;
import com.camunda.fox.cycle.api.connector.ConnectorNode;
import com.camunda.fox.cycle.api.connector.ConnectorNode.ConnectorNodeType;
import com.camunda.fox.cycle.connector.ConnectorRegistry;
import com.camunda.fox.cycle.entity.BpmnDiagram;
import com.camunda.fox.cycle.entity.Roundtrip;
import com.camunda.fox.cycle.exception.CycleException;
import com.camunda.fox.cycle.repository.RoundtripRepository;
import com.camunda.fox.cycle.service.roundtrip.BpmnProcessModelUtil;
import com.camunda.fox.cycle.util.IoUtil;
import com.camunda.fox.cycle.web.dto.BpmnDiagramDTO;
import com.camunda.fox.cycle.web.dto.RoundtripDTO;
import com.camunda.fox.cycle.web.service.AbstractRestService;

/**
 * This is the main roundtrip rest controller which exposes roundtrip 
 * <code>list</code>, <code>get</code>, <code>create</code> and<code>update</code> 
 * methods as well as some utilities to the cycle client application.
 * 
 * The arrangement of methods is compatible with angular JS <code>$resource</code>. 
 * 
 * @author nico.rehwaldt
 */
@Path("secured/resource/roundtrip")
public class RoundtripService extends AbstractRestService {
  
  public enum SyncMode {
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT
  }
  
	@Inject
	private RoundtripRepository roundtripRepository;
	
	@Inject
	private ConnectorRegistry connectorRegistry;
	
	@Inject
	private BpmnProcessModelUtil bpmnProcessModelUtil;
  
  @Inject
  private BpmnDiagramService bpmnDiagramController;
  
  /**
   * $resource specific methods
   */
  
  @GET
  public List<RoundtripDTO> list() {
    return RoundtripDTO.wrapAll(getRoundtripRepository().findAll());
  }
  
  @GET
  @Path("{id}")
  public RoundtripDTO get(@PathParam("id") long id) {
    return RoundtripDTO.wrap(getRoundtripRepository().findById(id));
  }
  
  @POST
  @Path("{id}")
  @Transactional
  public RoundtripDTO update(RoundtripDTO data) {
    long id = data.getId();
    
    Roundtrip roundtrip = getRoundtripRepository().findById(id);
    if (roundtrip == null) {
      throw new IllegalArgumentException("Not found");
    }
    
    update(roundtrip, data);
    return RoundtripDTO.wrap(roundtrip);
  }
  
  @POST
  public RoundtripDTO create(RoundtripDTO data) {
    Roundtrip roundtrip = new Roundtrip();
    update(roundtrip, data);
    return RoundtripDTO.wrap(getRoundtripRepository().saveAndFlush(roundtrip));
  }
  
  /**
   * Non $resource specific methods
   */
  
  @GET
  @Transactional
  @Path("{id}/details")
  public RoundtripDTO getDetails(@PathParam("id") long id) {
    Roundtrip roundtrip = getRoundtripRepository().findById(id);
    
    RoundtripDTO roundtripDTO = new RoundtripDTO(roundtrip);
    
    if (roundtrip.getLeftHandSide() != null) {
      roundtripDTO.setLeftHandSide(bpmnDiagramController.isDiagramInSync(BpmnDiagramDTO.wrap(roundtrip.getLeftHandSide()), roundtrip));
    } else {
      roundtripDTO.setLeftHandSide(null);
    }
    
    if (roundtrip.getRightHandSide() != null) {
      roundtripDTO.setRightHandSide(bpmnDiagramController.isDiagramInSync(BpmnDiagramDTO.wrap(roundtrip.getRightHandSide()), roundtrip));
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
    
    Roundtrip roundtrip = getRoundtripRepository().findById(id);
    if (roundtrip == null) {
      throw new IllegalArgumentException("Not found");
    }
    
    if (data.getLeftHandSide() != null) {
      BpmnDiagramDTO leftHandSideDTO = bpmnDiagramController.isDiagramInSync(data.getLeftHandSide(), roundtrip);
      BpmnDiagram leftHandSide = bpmnDiagramController.createOrUpdate(leftHandSideDTO);
      roundtrip.setLeftHandSide(leftHandSide);
    } else {
      roundtrip.setLeftHandSide(null);
    }
    
    if (data.getRightHandSide() != null) {
      BpmnDiagramDTO rightHandSideDTO = bpmnDiagramController.isDiagramInSync(data.getRightHandSide(), roundtrip);
      BpmnDiagram rightHandSide = bpmnDiagramController.createOrUpdate(rightHandSideDTO);
      roundtrip.setRightHandSide(rightHandSide);
    } else {
      roundtrip.setRightHandSide(null);
    }
    
    Roundtrip saved = getRoundtripRepository().saveAndFlush(roundtrip); 
    return new RoundtripDTO(saved, saved.getLeftHandSide(), saved.getRightHandSide());
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("isNameValid")
  public boolean isNameValid(@QueryParam("name") String name) {
    return getRoundtripRepository().isNameValid(name);
  }
  
  @POST
  @Path("{id}/sync")
  @Transactional
  public RoundtripDTO doSynchronize(@QueryParam("syncMode") SyncMode syncMode, @PathParam("id") long id) {
    Roundtrip roundtrip = this.getRoundtripRepository().findById(id);
    
    BpmnDiagram leftHandSide = roundtrip.getLeftHandSide();
    
    Connector leftHandSideConnector = this.connectorRegistry.getSessionConnectorMap().get(leftHandSide.getConnectorId());
    ConnectorNode leftHandSideModelNode = new ConnectorNode(leftHandSide.getDiagramPath(), leftHandSide.getLabel());
    leftHandSideModelNode.setType(ConnectorNodeType.FILE);
    InputStream leftHandSideModelContent = leftHandSideConnector.getContent(leftHandSideModelNode);
    
    BpmnDiagram rightHandSide = roundtrip.getRightHandSide();
    
    Connector rightHandSideConnector = this.connectorRegistry.getSessionConnectorMap().get(rightHandSide.getConnectorId());
    ConnectorNode rightHandSideModelNode = new ConnectorNode(rightHandSide.getDiagramPath(), rightHandSide.getLabel());
    rightHandSideModelNode.setType(ConnectorNodeType.FILE);
    InputStream rightHandSideModelContent = rightHandSideConnector.getContent(rightHandSideModelNode);
    
    try {
      
      switch (syncMode) {
        case LEFT_TO_RIGHT:
          IoUtil.closeSilently(rightHandSideModelContent);
          rightHandSideConnector.updateContent(rightHandSideModelNode,  this.bpmnProcessModelUtil.extractExecutablePool(leftHandSideModelContent));
          IoUtil.closeSilently(leftHandSideModelContent);
          break;
          
        case RIGHT_TO_LEFT:
          String result = this.bpmnProcessModelUtil.importChangesFromExecutableBpmnModel(IOUtil.toString(rightHandSideModelContent, "UTF-8"), IOUtil.toString(leftHandSideModelContent, "UTF-8"));
          IoUtil.closeSilently(leftHandSideModelContent);
          IoUtil.closeSilently(rightHandSideModelContent);
          InputStream resultStream = IOUtils.toInputStream(result, "UTF-8");
          leftHandSideConnector.updateContent(leftHandSideModelNode, resultStream);
          IoUtil.closeSilently(resultStream);
          break;
      }
      
      roundtrip.setLastSync(new Date());
      roundtrip.setLastSyncMode(syncMode);
      
      
    } catch (Exception e) {
      throw new CycleException(e);
    }
    
    BpmnDiagramDTO leftHandSideDTO = bpmnDiagramController.isDiagramInSync(BpmnDiagramDTO.wrap(roundtrip.getLeftHandSide()), roundtrip);
    BpmnDiagramDTO rightHandSideDTO = bpmnDiagramController.isDiagramInSync(BpmnDiagramDTO.wrap(roundtrip.getRightHandSide()), roundtrip);
    
    RoundtripDTO roundtripDTO = new RoundtripDTO(roundtrip);
    roundtripDTO.setLeftHandSide(leftHandSideDTO);
    roundtripDTO.setRightHandSide(rightHandSideDTO);
    
    return roundtripDTO;
  }
  
  /**
   * Updates the roundtrip with the given data
   * @param roundtrip
   * @param data 
   */
  private void update(Roundtrip roundtrip, RoundtripDTO data) {
    roundtrip.setName(data.getName());
  }

  public RoundtripRepository getRoundtripRepository() {
    return roundtripRepository;
  }

  public void setRoundtripRepository(RoundtripRepository roundtripRepository) {
    this.roundtripRepository = roundtripRepository;
  }

}

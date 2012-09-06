package com.camunda.fox.cycle.web.service.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
      throw new IllegalArgumentException("Not found");
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
  
  /**
   * Non $resource specific methods
   */
  
  @GET
  @Transactional
  @Path("{id}/details")
  public RoundtripDTO getDetails(@PathParam("id") long id) {
    Roundtrip roundtrip = roundtripRepository.findById(id);
    // TODO: Fetch eager
    return new RoundtripDTO(roundtrip, roundtrip.getLeftHandSide(), roundtrip.getRightHandSide());
  }
  
  @POST
  @Path("{id}/details")
  @Transactional
  public RoundtripDTO updateDetails(RoundtripDTO data) {
    long id = data.getId();
    
    Roundtrip roundtrip = roundtripRepository.findById(id);
    if (roundtrip == null) {
      throw new IllegalArgumentException("Not found");
    }
    
    if (data.getLeftHandSide() != null) {
      BpmnDiagram leftHandSide = bpmnDiagramController.createOrUpdate(data.getLeftHandSide());
      roundtrip.setLeftHandSide(leftHandSide);
    } else {
      roundtrip.setLeftHandSide(null);
    }
    
    if (data.getRightHandSide() != null) {
      BpmnDiagram rightHandSide = bpmnDiagramController.createOrUpdate(data.getRightHandSide());
      roundtrip.setRightHandSide(rightHandSide);
    } else {
      roundtrip.setRightHandSide(null);
    }
    
    Roundtrip saved = roundtripRepository.saveAndFlush(roundtrip); 
    return new RoundtripDTO(saved, saved.getLeftHandSide(), saved.getRightHandSide());
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("isNameValid")
  public boolean isNameValid(@QueryParam("name") String name) {
    return roundtripRepository.isNameValid(name);
  }
  
  @POST
  @Path("{id}/sync")
  @Transactional
  public RoundtripDTO doSynchronize(@QueryParam("syncMode") SyncMode syncMode, @PathParam("id") long id) {
    Roundtrip roundtrip = this.roundtripRepository.findById(id);
    
    BpmnDiagram leftHandSide = roundtrip.getLeftHandSide();
    
    Connector leftHandSideConnector = this.connectorRegistry.getSessionConnectorMap().get(leftHandSide.getConnectorId());
    ConnectorNode leftHandSideModelNode = new ConnectorNode(leftHandSide.getDiagramPath(), leftHandSide.getLabel());
    leftHandSideModelNode.setType(ConnectorNodeType.FILE);
    String leftHandSideModelContent = this.asString(leftHandSideConnector.getContent(leftHandSideModelNode));
    
    BpmnDiagram rightHandSide = roundtrip.getLeftHandSide();
    
    Connector rightHandSideConnector = this.connectorRegistry.getSessionConnectorMap().get(rightHandSide.getConnectorId());
    ConnectorNode rightHandSideModelNode = new ConnectorNode(rightHandSide.getDiagramPath(), rightHandSide.getLabel());
    rightHandSideModelNode.setType(ConnectorNodeType.FILE);
    String rightHandSideModelContent = this.asString(rightHandSideConnector.getContent(rightHandSideModelNode));
    
    String result = null;
    try {
      
      switch (syncMode) {
        case LEFT_TO_RIGHT:
          result = this.bpmnProcessModelUtil.extractExecutablePool(leftHandSideModelContent);
          rightHandSideConnector.updateContent(rightHandSideModelNode, result);
          break;
          
        case RIGHT_TO_LEFT:
          result = this.bpmnProcessModelUtil.importChangesFromExecutableBpmnModel(rightHandSideModelContent, leftHandSideModelContent);
          leftHandSideConnector.updateContent(leftHandSideModelNode, result);
          break;
      }
      
      roundtrip.setLastSync(new Date());
      
    } catch (Exception e) {
      throw new CycleException(e);
    }
    
    return new RoundtripDTO(roundtrip, roundtrip.getLeftHandSide(), roundtrip.getRightHandSide());
  }
  
  /**
   * Updates the roundtrip with the given data
   * @param roundtrip
   * @param data 
   */
  private void update(Roundtrip roundtrip, RoundtripDTO data) {
    roundtrip.setName(data.getName());
  }
  
  private String asString(InputStream inputStream) {
    try {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
      StringBuilder stringBuilder = new StringBuilder();
      String line = null;
      while ((line = bufferedReader.readLine()) != null) {
        stringBuilder.append(line + "\n");
      }
      bufferedReader.close();
      return stringBuilder.toString();
    } catch (IOException e) {
      throw new CycleException("The content of the assigned file could not be read.", e);
    }
  }
}

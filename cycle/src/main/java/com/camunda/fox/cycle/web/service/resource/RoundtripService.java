package com.camunda.fox.cycle.web.service.resource;

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

import com.camunda.fox.cycle.entity.BpmnDiagram;
import com.camunda.fox.cycle.entity.Roundtrip;
import com.camunda.fox.cycle.web.service.AbstractRestService;
import com.camunda.fox.cycle.web.dto.RoundtripDTO;
import com.camunda.fox.cycle.repository.RoundtripRepository;

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

	@Inject
	private RoundtripRepository roundtripRepository;
  
  @Inject
  private BpmnDiagramService bpmnDiagramController;
  
  @GET
  public List<RoundtripDTO> list() {
    return RoundtripDTO.wrapAll(roundtripRepository.findAll());
  }
  
  @GET
  @Path("{id}")
  public RoundtripDTO get(@PathParam("id") long id) {
    return RoundtripDTO.wrap(roundtripRepository.findById(id));
  }
  
  @GET
  @Transactional
  @Path("{id}/details")
  public RoundtripDTO getDetails(@PathParam("id") long id) {
    Roundtrip roundtrip = roundtripRepository.findById(id);
    // TODO: Fetch eager
    return new RoundtripDTO(roundtrip, roundtrip.getLeftHandSide(), roundtrip.getRightHandSide());
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
  
  @POST
  public RoundtripDTO create(RoundtripDTO data) {
    Roundtrip roundtrip = new Roundtrip();
    update(roundtrip, data);
    return RoundtripDTO.wrap(roundtripRepository.saveAndFlush(roundtrip));
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("isNameValid")
  public boolean isNameValid(@QueryParam("name") String name) {
    return roundtripRepository.isNameValid(name);
  }
  
  /**
   * Updates the roundtrip with the given data
   * @param roundtrip
   * @param data 
   */
  private void update(Roundtrip roundtrip, RoundtripDTO data) {
    roundtrip.setName(data.getName());
  }
}

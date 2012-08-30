package com.camunda.fox.cycle.web.service.resource;

import com.camunda.fox.cycle.repository.BpmnDiagramRepository;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.entity.BpmnDiagram;
import com.camunda.fox.cycle.web.dto.BpmnDiagramDTO;

/**
 *
 * @author nico.rehwaldt
 */
@Path("secured/resource/diagram")
public class BpmnDiagramService {

  @Inject
  private BpmnDiagramRepository bpmnDiagramRepository;

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
    diagram.setDiagramPath(data.getDiagramPath());
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("modelerNames")
  public List<String> getModelerNames() {
    return bpmnDiagramRepository.findAllModelerNames();
  }
}

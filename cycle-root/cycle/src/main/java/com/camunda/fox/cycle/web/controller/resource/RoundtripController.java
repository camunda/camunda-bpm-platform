package com.camunda.fox.cycle.web.controller.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.camunda.fox.cycle.entity.Roundtrip;
import com.camunda.fox.cycle.web.controller.AbstractController;
import com.camunda.fox.cycle.web.dto.RoundtripDTO;
import com.camunda.fox.cycle.web.repository.RoundtripRepository;

/**
 * This is the main roundtrip rest controller which exposes roundtrip 
 * <code>list</code>, <code>get</code>, <code>create</code> and<code>update</code> 
 * methods as well as some utilities to the cycle client application.
 * 
 * The arrangement of methods is compatible with angular JS <code>$resource</code>. 
 * 
 * @author nico.rehwaldt
 */
@Path("secured/resources/roundtrip")
public class RoundtripController extends AbstractController {

	@Inject
	private RoundtripRepository roundtripRepository;
  
  private long currentId = 0;
  
  private TreeMap<Long, RoundtripDTO> roundTrips = new TreeMap<Long, RoundtripDTO>();
  
  @PostConstruct
  private void postConstruct() {
    roundTrips.put(++currentId, new RoundtripDTO(currentId, "MyRoundtrip", "1", "2"));
    roundTrips.put(++currentId, new RoundtripDTO(currentId, "MyRoundtrip 2", "1", "2"));
  }
  
  @GET
  public List<RoundtripDTO> list() {
    return new ArrayList<RoundtripDTO>(roundTrips.values());
  }
  
  @GET
  @Path("{id}")
  public RoundtripDTO get(@PathParam("id") long id) {
    return roundTrips.get(id);
  }
  
  @POST
  @Path("{id}")
  public RoundtripDTO update(RoundtripDTO data) {
    long id = data.getId();
    
    RoundtripDTO roundTrip = roundTrips.get(id);
    if (roundTrip == null) {
      throw new IllegalArgumentException("Not found");
    }
    
    data.setId(id);
    roundTrips.put(id, data);
    
    return data;
  }
  
  @POST
  public RoundtripDTO create(RoundtripDTO data) {
    Roundtrip r = roundtripRepository.save(new Roundtrip(data.getName()));
    return new RoundtripDTO(r);
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("isNameValid")
  public boolean isNameValid(@QueryParam("name") String name) {
    for (RoundtripDTO r: roundTrips.values()) {
      if (r.getName().equals(name)) {
        return false;
      }
    }
    
    return true;
  }
}

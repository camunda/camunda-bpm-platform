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

import org.springframework.transaction.annotation.Transactional;

import com.camunda.fox.cycle.connector.Connector;
import com.camunda.fox.cycle.connector.ConnectorNode;
import com.camunda.fox.cycle.connector.ConnectorRegistry;
import com.camunda.fox.cycle.connector.ContentInformation;
import com.camunda.fox.cycle.entity.BpmnDiagram;
import com.camunda.fox.cycle.entity.Roundtrip;
import com.camunda.fox.cycle.entity.Roundtrip.SyncMode;
import com.camunda.fox.cycle.exception.CycleException;
import com.camunda.fox.cycle.repository.RoundtripRepository;
import com.camunda.fox.cycle.roundtrip.SynchronizationService;
import com.camunda.fox.cycle.util.IoUtil;
import com.camunda.fox.cycle.web.dto.BpmnDiagramDTO;
import com.camunda.fox.cycle.web.dto.RoundtripDTO;
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
      throw new IllegalArgumentException("Not found");
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

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("isNameValid")
  public boolean isNameValid(@QueryParam("name") String name) {
    return roundtripRepository.isNameValid(name);
  }

  @POST
  @Path("{id}/sync")
  @Transactional
  public RoundtripDTO doSynchronize(@QueryParam("syncMode") SyncMode syncMode, @PathParam("id") long roundtripId) {
    Roundtrip roundtrip = roundtripRepository.findById(roundtripId);

    if (roundtrip == null) {
      throw new IllegalArgumentException("Roundtrip not found");
    }

    synchronizeModels(roundtrip.getLeftHandSide(), roundtrip.getRightHandSide(), syncMode);

    roundtrip.setLastSync(new Date());
    roundtrip.setLastSyncMode(syncMode);

    RoundtripDTO roundtripDTO = new RoundtripDTO(roundtrip, roundtrip.getLeftHandSide(), roundtrip.getRightHandSide());
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

  private void synchronizeModels(BpmnDiagram lhs, BpmnDiagram rhs, SyncMode syncMode) {

    if (lhs == null) {
      throw new IllegalArgumentException("Left hand side model is null");
    }

    if (rhs == null) {
      throw new IllegalArgumentException("RIght hand side model is null");
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
          resultInfo = rhsConnector.updateContent(rhsNode, resultStream);
          rhs.setLastSync(resultInfo.getLastModified());
          
          otherSideInfo = lhsConnector.getContentInformation(lhsNode);
          lhs.setLastSync(otherSideInfo.getLastModified());
          break;
        case RIGHT_TO_LEFT:
          resultStream = synchronizationService.syncRightToLeft(lhsInputStream, rhsInputStream);
          IoUtil.closeSilently(lhsInputStream, rhsInputStream);
          resultInfo = lhsConnector.updateContent(lhsNode, resultStream);
          lhs.setLastSync(resultInfo.getLastModified());
          
          otherSideInfo = rhsConnector.getContentInformation(rhsNode);
          rhs.setLastSync(otherSideInfo.getLastModified());
          break;
      }
      
      IoUtil.closeSilently(resultStream);
    } catch (Exception e) {
      throw new CycleException("Synchronization failed", e);
    }

    // TODO: Update all synchronization states?
    // And to which date? 
    // update synchronization status
    // lhs.setLastSync(lhsConnector.getLastModifiedDate(lhsNode));
    // rhs.setLastSync(rhsConnector.getLastModifiedDate(rhsNode));
  }
}

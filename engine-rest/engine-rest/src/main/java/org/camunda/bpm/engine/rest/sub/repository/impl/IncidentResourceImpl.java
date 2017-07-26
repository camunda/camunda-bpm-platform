package org.camunda.bpm.engine.rest.sub.repository.impl;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.rest.dto.runtime.IncidentDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.runtime.IncidentResource;
import org.camunda.bpm.engine.runtime.Incident;

import com.fasterxml.jackson.databind.ObjectMapper;

public class IncidentResourceImpl implements IncidentResource {

  protected ProcessEngine engine;
  protected String incidentId;
  protected ObjectMapper objectMapper;

  public IncidentResourceImpl(ProcessEngine engine, String incidentId, ObjectMapper objectMapper) {
    this.engine = engine;
    this.incidentId = incidentId;
    this.objectMapper = objectMapper;
  }

  public IncidentDto getIncident() {
    Incident incident = engine.getRuntimeService().createIncidentQuery().incidentId(incidentId).singleResult();
    if (incident == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "No matching incident with id " + incidentId);
    }
    return IncidentDto.fromIncident(incident);
  }

  public void resolveIncident() {
    try {
      engine.getRuntimeService().resolveIncident(incidentId);
    } catch (NotFoundException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e.getMessage());
    } catch (BadUserRequestException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e.getMessage());
    }
  }
}

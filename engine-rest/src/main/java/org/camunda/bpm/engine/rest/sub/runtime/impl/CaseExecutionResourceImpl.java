package org.camunda.bpm.engine.rest.sub.runtime.impl;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.rest.dto.runtime.CaseExecutionDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.runtime.CaseExecutionResource;
import org.camunda.bpm.engine.runtime.CaseExecution;

public class CaseExecutionResourceImpl implements CaseExecutionResource {

  protected ProcessEngine engine;
  protected String caseExecutionId;

  public CaseExecutionResourceImpl(ProcessEngine engine, String caseExecutionId) {
    this.engine = engine;
    this.caseExecutionId = caseExecutionId;
  }

  public CaseExecutionDto getCaseExecution() {
    CaseService caseService = engine.getCaseService();

    CaseExecution execution = caseService
        .createCaseExecutionQuery()
        .caseExecutionId(caseExecutionId)
        .singleResult();

    if (execution == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Case execution with id " + caseExecutionId + " does not exist.");
    }

    CaseExecutionDto result = CaseExecutionDto.fromCaseExecution(execution);
    return result;
  }

}

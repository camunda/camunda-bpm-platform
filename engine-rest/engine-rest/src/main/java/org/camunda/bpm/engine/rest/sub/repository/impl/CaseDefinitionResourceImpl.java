/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.sub.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.NotAllowedException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.rest.CaseInstanceRestService;
import org.camunda.bpm.engine.rest.dto.HistoryTimeToLiveDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.repository.CaseDefinitionDiagramDto;
import org.camunda.bpm.engine.rest.dto.repository.CaseDefinitionDto;
import org.camunda.bpm.engine.rest.dto.runtime.CaseInstanceDto;
import org.camunda.bpm.engine.rest.dto.runtime.CreateCaseInstanceDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.repository.CaseDefinitionResource;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.variable.VariableMap;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;

/**
 *
 * @author Roman Smirnov
 *
 */
public class CaseDefinitionResourceImpl implements CaseDefinitionResource {

  protected ProcessEngine engine;
  protected String caseDefinitionId;
  protected String rootResourcePath;
  protected ObjectMapper objectMapper;

  public CaseDefinitionResourceImpl(ProcessEngine engine, String caseDefinitionId, String rootResourcePath, ObjectMapper objectMapper) {
    this.engine = engine;
    this.caseDefinitionId = caseDefinitionId;
    this.rootResourcePath = rootResourcePath;
    this.objectMapper = objectMapper;
  }

  @Override
  public CaseDefinitionDto getCaseDefinition() {
    RepositoryService repositoryService = engine.getRepositoryService();

    CaseDefinition definition = null;

    try {
      definition = repositoryService.getCaseDefinition(caseDefinitionId);

    } catch (NotFoundException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, e.getMessage());

    } catch (NotValidException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, e.getMessage());

    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e);

    }

    return CaseDefinitionDto.fromCaseDefinition(definition);
  }

  @Override
  public CaseDefinitionDiagramDto getCaseDefinitionCmmnXml() {
    InputStream caseModelInputStream = null;
    try {
      caseModelInputStream = engine.getRepositoryService().getCaseModel(caseDefinitionId);

      byte[] caseModel = IoUtil.readInputStream(caseModelInputStream, "caseModelCmmnXml");
      return CaseDefinitionDiagramDto.create(caseDefinitionId, new String(caseModel, "UTF-8"));

    } catch (NotFoundException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, e.getMessage());

    } catch (NotValidException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, e.getMessage());

    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e);

    } catch (UnsupportedEncodingException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e);

    } finally {
      IoUtil.closeSilently(caseModelInputStream);
    }
  }

  public CaseInstanceDto createCaseInstance(UriInfo context, CreateCaseInstanceDto parameters) {
    CaseService caseService = engine.getCaseService();

    CaseInstance instance = null;
    try {

      String businessKey = parameters.getBusinessKey();
      VariableMap variables = VariableValueDto.toMap(parameters.getVariables(), engine, objectMapper);

      instance = caseService
          .withCaseDefinition(caseDefinitionId)
          .businessKey(businessKey)
          .setVariables(variables)
          .create();

    } catch (RestException e) {
      String errorMessage = String.format("Cannot instantiate case definition %s: %s", caseDefinitionId, e.getMessage());
      throw new InvalidRequestException(e.getStatus(), e, errorMessage);

    } catch (NotFoundException e) {
      String errorMessage = String.format("Cannot instantiate case definition %s: %s", caseDefinitionId, e.getMessage());
      throw new InvalidRequestException(Status.NOT_FOUND, e, errorMessage);

    } catch (NotValidException e) {
      String errorMessage = String.format("Cannot instantiate case definition %s: %s", caseDefinitionId, e.getMessage());
      throw new InvalidRequestException(Status.BAD_REQUEST, e, errorMessage);

    } catch (NotAllowedException e) {
      String errorMessage = String.format("Cannot instantiate case definition %s: %s", caseDefinitionId, e.getMessage());
      throw new InvalidRequestException(Status.FORBIDDEN, e, errorMessage);

    } catch (ProcessEngineException e) {
      String errorMessage = String.format("Cannot instantiate case definition %s: %s", caseDefinitionId, e.getMessage());
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);

    }

    CaseInstanceDto result = CaseInstanceDto.fromCaseInstance(instance);

    URI uri = context.getBaseUriBuilder()
      .path(rootResourcePath)
      .path(CaseInstanceRestService.PATH)
      .path(instance.getId())
      .build();

    result.addReflexiveLink(uri, HttpMethod.GET, "self");

    return result;
  }

  @Override
  public Response getCaseDefinitionDiagram() {
    CaseDefinition definition = engine.getRepositoryService().getCaseDefinition(caseDefinitionId);
    InputStream caseDiagram = engine.getRepositoryService().getCaseDiagram(caseDefinitionId);
    if (caseDiagram == null) {
      return Response.noContent().build();
    } else {
      String fileName = definition.getDiagramResourceName();
      return Response.ok(caseDiagram).header("Content-Disposition", "attachment; filename=" + fileName)
          .type(ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix(fileName)).build();
    }
  }

  @Override
  public void updateHistoryTimeToLive(HistoryTimeToLiveDto historyTimeToLiveDto) {
    engine.getRepositoryService().updateCaseDefinitionHistoryTimeToLive(caseDefinitionId, historyTimeToLiveDto.getHistoryTimeToLive());
  }

}

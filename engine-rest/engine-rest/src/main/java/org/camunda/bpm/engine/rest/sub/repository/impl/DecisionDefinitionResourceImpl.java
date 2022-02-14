/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.rest.sub.repository.impl;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.camunda.bpm.dmn.engine.DmnDecisionResult;
import org.camunda.bpm.dmn.engine.DmnDecisionResultEntries;
import org.camunda.bpm.dmn.engine.DmnEngineException;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.rest.dto.HistoryTimeToLiveDto;
import org.camunda.bpm.engine.rest.dto.VariableValueDto;
import org.camunda.bpm.engine.rest.dto.dmn.EvaluateDecisionDto;
import org.camunda.bpm.engine.rest.dto.repository.DecisionDefinitionDiagramDto;
import org.camunda.bpm.engine.rest.dto.repository.DecisionDefinitionDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.repository.DecisionDefinitionResource;
import org.camunda.bpm.engine.rest.util.URLEncodingUtil;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.TypedValue;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DecisionDefinitionResourceImpl implements DecisionDefinitionResource {

  protected ProcessEngine engine;
  protected String decisionDefinitionId;
  protected String rootResourcePath;
  protected ObjectMapper objectMapper;

  public DecisionDefinitionResourceImpl(ProcessEngine engine, String decisionDefinitionId, String rootResourcePath, ObjectMapper objectMapper) {
    this.engine = engine;
    this.decisionDefinitionId = decisionDefinitionId;
    this.rootResourcePath = rootResourcePath;
    this.objectMapper = objectMapper;
  }

  @Override
  public DecisionDefinitionDto getDecisionDefinition() {
    RepositoryService repositoryService = engine.getRepositoryService();

    DecisionDefinition definition = null;

    try {
      definition = repositoryService.getDecisionDefinition(decisionDefinitionId);

    } catch (NotFoundException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, e.getMessage());

    } catch (NotValidException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, e.getMessage());

    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e);

    }

    return DecisionDefinitionDto.fromDecisionDefinition(definition);
  }

  @Override
  public DecisionDefinitionDiagramDto getDecisionDefinitionDmnXml() {
    InputStream decisionModelInputStream = null;
    try {
      decisionModelInputStream = engine.getRepositoryService().getDecisionModel(decisionDefinitionId);

      byte[] decisionModel = IoUtil.readInputStream(decisionModelInputStream, "decisionModelDmnXml");
      return DecisionDefinitionDiagramDto.create(decisionDefinitionId, new String(decisionModel, "UTF-8"));

    } catch (NotFoundException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, e.getMessage());

    } catch (NotValidException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, e.getMessage());

    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e);

    } catch (UnsupportedEncodingException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e);

    } finally {
      IoUtil.closeSilently(decisionModelInputStream);
    }
  }

  @Override
  public Response getDecisionDefinitionDiagram() {
    DecisionDefinition definition = engine.getRepositoryService().getDecisionDefinition(decisionDefinitionId);
    InputStream decisionDiagram = engine.getRepositoryService().getDecisionDiagram(decisionDefinitionId);
    if (decisionDiagram == null) {
      return Response.noContent().build();
    } else {
      String fileName = definition.getDiagramResourceName();
      return Response.ok(decisionDiagram).header("Content-Disposition",URLEncodingUtil.buildAttachmentValue(fileName))
          .type(ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix(fileName)).build();
    }
  }

  @Override
  public List<Map<String, VariableValueDto>> evaluateDecision(UriInfo context, EvaluateDecisionDto parameters) {
    DecisionService decisionService = engine.getDecisionService();

    Map<String, Object> variables = VariableValueDto.toMap(parameters.getVariables(), engine, objectMapper);

    try {
      DmnDecisionResult decisionResult = decisionService
          .evaluateDecisionById(decisionDefinitionId)
          .variables(variables)
          .evaluate();

      return createDecisionResultDto(decisionResult);

    }
    catch (AuthorizationException e) {
      throw e;
    }
    catch (NotFoundException e) {
      String errorMessage = String.format("Cannot evaluate decision %s: %s", decisionDefinitionId, e.getMessage());
      throw new InvalidRequestException(Status.NOT_FOUND, e, errorMessage);
    }
    catch (NotValidException e) {
      String errorMessage = String.format("Cannot evaluate decision %s: %s", decisionDefinitionId, e.getMessage());
      throw new InvalidRequestException(Status.BAD_REQUEST, e, errorMessage);
    }
    catch (ProcessEngineException e) {
      String errorMessage = String.format("Cannot evaluate decision %s: %s", decisionDefinitionId, e.getMessage());
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);
    }
    catch (DmnEngineException e) {
      String errorMessage = String.format("Cannot evaluate decision %s: %s", decisionDefinitionId, e.getMessage());
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e, errorMessage);
    }
  }

  @Override
  public void updateHistoryTimeToLive(HistoryTimeToLiveDto historyTimeToLiveDto) {
    engine.getRepositoryService().updateDecisionDefinitionHistoryTimeToLive(decisionDefinitionId, historyTimeToLiveDto.getHistoryTimeToLive());
  }

  protected List<Map<String, VariableValueDto>> createDecisionResultDto(DmnDecisionResult decisionResult) {
    List<Map<String, VariableValueDto>> dto = new ArrayList<>();

    for (DmnDecisionResultEntries entries : decisionResult) {
      Map<String, VariableValueDto> resultEntriesDto = createResultEntriesDto(entries);
      dto.add(resultEntriesDto);
    }

    return dto;
  }

  protected Map<String, VariableValueDto> createResultEntriesDto(DmnDecisionResultEntries entries) {
    VariableMap variableMap = Variables.createVariables();

    for(String key : entries.keySet()) {
      TypedValue typedValue = entries.getEntryTyped(key);
      variableMap.putValueTyped(key, typedValue);
    }

    return VariableValueDto.fromMap(variableMap);
  }

}

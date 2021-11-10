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

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.rest.dto.repository.DecisionRequirementsDefinitionDto;
import org.camunda.bpm.engine.rest.dto.repository.DecisionRequirementsDefinitionXmlDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.repository.DecisionRequirementsDefinitionResource;
import org.camunda.bpm.engine.rest.util.URLEncodingUtil;

/**
 * 
 * @author Deivarayan Azhagappan
 *
 */
public class DecisionRequirementsDefinitionResourceImpl implements DecisionRequirementsDefinitionResource {

  protected ProcessEngine engine;
  protected String decisionRequirementsDefinitionId;
 
  public DecisionRequirementsDefinitionResourceImpl(ProcessEngine engine, String decisionDefinitionId) {
    this.engine = engine;
    this.decisionRequirementsDefinitionId = decisionDefinitionId;
  }

  @Override
  public DecisionRequirementsDefinitionDto getDecisionRequirementsDefinition() {
    RepositoryService repositoryService = engine.getRepositoryService();

    DecisionRequirementsDefinition definition = null;

    try {
      definition = repositoryService.getDecisionRequirementsDefinition(decisionRequirementsDefinitionId);

    } catch (NotFoundException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, e.getMessage());

    } catch (NotValidException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, e.getMessage());

    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e);

    }

    return DecisionRequirementsDefinitionDto.fromDecisionRequirementsDefinition(definition);
  }

  @Override
  public DecisionRequirementsDefinitionXmlDto getDecisionRequirementsDefinitionDmnXml() {
    InputStream decisionRequirementsModelInputStream = null;
    try {
      decisionRequirementsModelInputStream = engine.getRepositoryService().getDecisionRequirementsModel(decisionRequirementsDefinitionId);

      byte[] decisionRequirementsModel = IoUtil.readInputStream(decisionRequirementsModelInputStream, "decisionRequirementsModelDmnXml");
      return DecisionRequirementsDefinitionXmlDto.create(decisionRequirementsDefinitionId, new String(decisionRequirementsModel, "UTF-8"));

    } catch (NotFoundException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, e.getMessage());

    } catch (NotValidException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, e.getMessage());

    } catch (ProcessEngineException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e);

    } catch (UnsupportedEncodingException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e);

    } finally {
      IoUtil.closeSilently(decisionRequirementsModelInputStream);
    }
  }

  @Override
  public Response getDecisionRequirementsDefinitionDiagram() {
    DecisionRequirementsDefinition definition = engine.getRepositoryService().getDecisionRequirementsDefinition(decisionRequirementsDefinitionId);
    InputStream decisionRequirementsDiagram = engine.getRepositoryService().getDecisionRequirementsDiagram(decisionRequirementsDefinitionId);
    if (decisionRequirementsDiagram == null) {
      return Response.noContent().build();
    } else {
      String fileName = definition.getDiagramResourceName();
      return Response.ok(decisionRequirementsDiagram).header("Content-Disposition", URLEncodingUtil.buildAttachmentValue(fileName))
          .type(ProcessDefinitionResourceImpl.getMediaTypeForFileSuffix(fileName)).build();
    }
  }
}

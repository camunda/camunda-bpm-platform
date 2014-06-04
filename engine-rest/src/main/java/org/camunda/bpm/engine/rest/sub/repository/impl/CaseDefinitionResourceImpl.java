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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.rest.dto.repository.CaseDefinitionDiagramDto;
import org.camunda.bpm.engine.rest.dto.repository.CaseDefinitionDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.exception.RestException;
import org.camunda.bpm.engine.rest.sub.repository.CaseDefinitionResource;

import javax.ws.rs.core.Response.Status;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 *
 * @author Roman Smirnov
 *
 */
public class CaseDefinitionResourceImpl implements CaseDefinitionResource {

  private ProcessEngine engine;
  private String caseDefinitionId;

  public CaseDefinitionResourceImpl(ProcessEngine engine, String caseDefinitionId) {
    this.engine = engine;
    this.caseDefinitionId = caseDefinitionId;
  }

  @Override
  public CaseDefinitionDto getCaseDefinition() {
    RepositoryService repositoryService = engine.getRepositoryService();

    CaseDefinition definition;

    try {
      definition = repositoryService.getCaseDefinition(caseDefinitionId);

    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e, "No matching definition with id " + caseDefinitionId);
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

    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.BAD_REQUEST, e, "No matching definition with id " + caseDefinitionId);

    } catch (UnsupportedEncodingException e) {
      throw new RestException(Status.INTERNAL_SERVER_ERROR, e);

    } finally {
      IoUtil.closeSilently(caseModelInputStream);
    }
  }

}

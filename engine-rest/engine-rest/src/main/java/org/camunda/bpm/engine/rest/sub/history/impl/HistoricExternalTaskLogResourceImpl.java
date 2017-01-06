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
package org.camunda.bpm.engine.rest.sub.history.impl;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricExternalTaskLog;
import org.camunda.bpm.engine.rest.dto.history.HistoricExternalTaskLogDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.history.HistoricExternalTaskLogResource;

import javax.ws.rs.core.Response.Status;

public class HistoricExternalTaskLogResourceImpl implements HistoricExternalTaskLogResource {

  protected String id;
  protected ProcessEngine engine;

  public HistoricExternalTaskLogResourceImpl(String id, ProcessEngine engine) {
    this.id = id;
    this.engine = engine;
  }

  @Override
  public HistoricExternalTaskLogDto getHistoricExternalTaskLog() {
    HistoryService historyService = engine.getHistoryService();
    HistoricExternalTaskLog historicExternalTaskLog = historyService
      .createHistoricExternalTaskLogQuery()
      .logId(id)
      .singleResult();

    if (historicExternalTaskLog == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Historic external task log with id " + id + " does not exist");
    }

    return HistoricExternalTaskLogDto.fromHistoricExternalTaskLog(historicExternalTaskLog);
  }

  @Override
  public String getErrorDetails() {
    try {
      HistoryService historyService = engine.getHistoryService();
      return historyService.getHistoricExternalTaskLogErrorDetails(id);
    } catch (AuthorizationException e) {
      throw e;
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e.getMessage());
    }
  }
}

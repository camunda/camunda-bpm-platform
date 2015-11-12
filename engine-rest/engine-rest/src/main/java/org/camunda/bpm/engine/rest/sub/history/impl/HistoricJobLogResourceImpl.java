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

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.history.HistoricJobLog;
import org.camunda.bpm.engine.rest.dto.history.HistoricJobLogDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.history.HistoricJobLogResource;

/**
 * @author Roman Smirnov
 *
 */
public class HistoricJobLogResourceImpl implements HistoricJobLogResource {

  protected String id;
  protected ProcessEngine engine;

  public HistoricJobLogResourceImpl(String id, ProcessEngine engine) {
    this.id = id;
    this.engine = engine;
  }

  public HistoricJobLogDto getHistoricJobLog() {
    HistoryService historyService = engine.getHistoryService();
    HistoricJobLog historicJobLog = historyService
        .createHistoricJobLogQuery()
        .logId(id)
        .singleResult();

    if (historicJobLog == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Historic job log with id " + id + " does not exist");
    }

    return HistoricJobLogDto.fromHistoricJobLog(historicJobLog);
  }

  public String getStacktrace() {
    try {
      HistoryService historyService = engine.getHistoryService();
      String stacktrace = historyService.getHistoricJobLogExceptionStacktrace(id);
      return stacktrace;
    } catch (AuthorizationException e) {
      throw e;
    } catch (ProcessEngineException e) {
      throw new InvalidRequestException(Status.NOT_FOUND, e.getMessage());
    }
  }

}

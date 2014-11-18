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

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.rest.dto.history.HistoricCaseInstanceDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.history.HistoricCaseInstanceResource;

import javax.ws.rs.core.Response.Status;

public class HistoricCaseInstanceResourceImpl implements HistoricCaseInstanceResource {

  private ProcessEngine engine;
  private String caseInstanceId;

  public HistoricCaseInstanceResourceImpl(ProcessEngine engine, String caseInstanceId) {
    this.engine = engine;
    this.caseInstanceId = caseInstanceId;
  }

  public HistoricCaseInstanceDto getHistoricCaseInstance() {
    HistoryService historyService = engine.getHistoryService();
    HistoricCaseInstance instance = historyService.createHistoricCaseInstanceQuery().caseInstanceId(caseInstanceId).singleResult();

    if (instance == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Historic case instance with id '" + caseInstanceId + "' does not exist");
    }

    return HistoricCaseInstanceDto.fromHistoricCaseInstance(instance);
  }

}

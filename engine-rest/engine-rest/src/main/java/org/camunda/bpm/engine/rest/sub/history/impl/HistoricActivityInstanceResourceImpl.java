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
package org.camunda.bpm.engine.rest.sub.history.impl;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.rest.dto.history.HistoricActivityInstanceDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.history.HistoricActivityInstanceResource;

import javax.ws.rs.core.Response.Status;

public class HistoricActivityInstanceResourceImpl implements HistoricActivityInstanceResource {

  private ProcessEngine engine;
  private String activityInstanceId;

  public HistoricActivityInstanceResourceImpl(ProcessEngine engine, String activityInstanceId) {
    this.engine = engine;
    this.activityInstanceId = activityInstanceId;
  }

  public HistoricActivityInstanceDto getHistoricActivityInstance() {
    HistoryService historyService = engine.getHistoryService();
    HistoricActivityInstance instance = historyService.createHistoricActivityInstanceQuery()
      .activityInstanceId(activityInstanceId).singleResult();

    if (instance == null) {
      throw new InvalidRequestException(Status.NOT_FOUND, "Historic activity instance with id '" + activityInstanceId + "' does not exist");
    }

    final HistoricActivityInstanceDto historicActivityInstanceDto = new HistoricActivityInstanceDto();
    HistoricActivityInstanceDto.fromHistoricActivityInstance(historicActivityInstanceDto, instance);
    return historicActivityInstanceDto;
  }

}

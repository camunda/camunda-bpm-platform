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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.ws.rs.core.Response.Status;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.camunda.bpm.engine.rest.exception.InvalidRequestException;
import org.camunda.bpm.engine.rest.sub.history.HistoricVariableInstanceResource;

/**
 * @author Daniel Meyer
 *
 */
public class HistoricVariableInstanceResourceImpl implements HistoricVariableInstanceResource {

  protected String variableId;
  protected ProcessEngine engine;

  public HistoricVariableInstanceResourceImpl(String variableId, ProcessEngine engine) {
    this.variableId = variableId;
    this.engine = engine;
  }

  public HistoricVariableInstanceDto getVariable() {
    HistoricVariableInstance variableInstance = baseQuery()
      .disableBinaryFetching()
      .singleResult();
    if(variableInstance != null) {
      return HistoricVariableInstanceDto.fromHistoricVariableInstance(variableInstance);

    } else {
      throw new InvalidRequestException(Status.NOT_FOUND, "Variable instance with Id '"+variableId + "' does not exist.");

    }
  }

  public InputStream getBinaryVariable() {
    HistoricVariableInstance variableInstance = baseQuery()
        .singleResult();
    if(variableInstance != null) {

      Object value = variableInstance.getValue();
      if(value instanceof byte[]) {
        return new ByteArrayInputStream((byte[]) value);

      } else {
        throw new InvalidRequestException(Status.BAD_REQUEST, "Variable instance with Id '"+variableId + "' is not a binary variable.");

      }

    } else {
      throw new InvalidRequestException(Status.NOT_FOUND, "Variable instance with Id '"+variableId + "' does not exist.");
    }
  }

  protected HistoricVariableInstanceQuery baseQuery() {
    return engine.getHistoryService()
        .createHistoricVariableInstanceQuery()
        .variableId(variableId);
  }

}

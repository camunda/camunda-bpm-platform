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

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.rest.dto.history.HistoricVariableInstanceDto;
import org.camunda.bpm.engine.rest.sub.AbstractResourceProvider;
import org.camunda.bpm.engine.rest.sub.history.HistoricVariableInstanceResource;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Daniel Meyer
 * @author Ronny Bräunlich
 *
 */
public class HistoricVariableInstanceResourceImpl extends
    AbstractResourceProvider<HistoricVariableInstanceQuery, HistoricVariableInstance, HistoricVariableInstanceDto> implements HistoricVariableInstanceResource {

  public HistoricVariableInstanceResourceImpl(String variableId, ProcessEngine engine) {
    super(variableId, engine);
  }

  protected HistoricVariableInstanceQuery baseQuery() {
    return getEngine().getHistoryService().createHistoricVariableInstanceQuery().variableId(getId());
  }

  @Override
  protected Query<HistoricVariableInstanceQuery, HistoricVariableInstance> baseQueryForBinaryVariable() {
    return baseQuery().disableCustomObjectDeserialization();
  }

  @Override
  protected Query<HistoricVariableInstanceQuery, HistoricVariableInstance> baseQueryForVariable(boolean deserializeObjectValue) {
    HistoricVariableInstanceQuery query = baseQuery().disableBinaryFetching();

    if (!deserializeObjectValue) {
      query.disableCustomObjectDeserialization();
    }
    return query;
  }

  @Override
  protected TypedValue transformQueryResultIntoTypedValue(HistoricVariableInstance queryResult) {
    return queryResult.getTypedValue();
  }

  @Override
  protected HistoricVariableInstanceDto transformToDto(HistoricVariableInstance queryResult) {
    return HistoricVariableInstanceDto.fromHistoricVariableInstance(queryResult);
  }

  @Override
  protected String getResourceNameForErrorMessage() {
    return "Historic variable instance";
  }

}

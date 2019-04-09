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
package org.camunda.bpm.engine.rest.sub.runtime.impl;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.query.Query;
import org.camunda.bpm.engine.rest.dto.runtime.VariableInstanceDto;
import org.camunda.bpm.engine.rest.sub.AbstractResourceProvider;
import org.camunda.bpm.engine.rest.sub.runtime.VariableInstanceResource;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.runtime.VariableInstanceQuery;
import org.camunda.bpm.engine.variable.value.TypedValue;

/**
 * @author Daniel Meyer
 * @author Ronny Bräunlich
 *
 */
public class VariableInstanceResourceImpl extends AbstractResourceProvider<VariableInstanceQuery, VariableInstance, VariableInstanceDto> implements
    VariableInstanceResource {

  public VariableInstanceResourceImpl(String variableId, ProcessEngine engine) {
    super(variableId, engine);
  }

  protected VariableInstanceQuery baseQuery() {
    return getEngine().getRuntimeService().createVariableInstanceQuery().variableId(getId());
  }

  @Override
  protected Query<VariableInstanceQuery, VariableInstance> baseQueryForBinaryVariable() {
    return baseQuery().disableCustomObjectDeserialization();
  }

  @Override
  protected Query<VariableInstanceQuery, VariableInstance> baseQueryForVariable(boolean deserializeObjectValue) {
    VariableInstanceQuery baseQuery = baseQuery();

    // do not fetch byte arrays
    baseQuery.disableBinaryFetching();

    if (!deserializeObjectValue) {
      baseQuery.disableCustomObjectDeserialization();
    }
    return baseQuery;
  }

  @Override
  protected TypedValue transformQueryResultIntoTypedValue(VariableInstance queryResult) {
    return queryResult.getTypedValue();
  }

  @Override
  protected VariableInstanceDto transformToDto(VariableInstance queryResult) {
    return VariableInstanceDto.fromVariableInstance(queryResult);
  }

  @Override
  protected String getResourceNameForErrorMessage() {
    return "Variable instance";
  }

}

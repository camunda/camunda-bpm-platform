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
package org.camunda.bpm.engine.impl.core.variable.mapping.value;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.delegate.VariableScope;

/**
 * @author Daniel Meyer
 *
 */
public class ListValueProvider implements ParameterValueProvider {

  protected List<ParameterValueProvider> providerList;

  public ListValueProvider(List<ParameterValueProvider> providerList) {
    this.providerList = providerList;
  }

  public Object getValue(VariableScope variableScope) {
    List<Object> valueList = new ArrayList<Object>();
    for (ParameterValueProvider provider : providerList) {
      valueList.add(provider.getValue(variableScope));
    }
    return valueList;
  }

  public List<ParameterValueProvider> getProviderList() {
    return providerList;
  }

  public void setProviderList(List<ParameterValueProvider> providerList) {
    this.providerList = providerList;
  }

  /**
   * @return this method currently always returns false, but that might change in the future.
   */
  @Override
  public boolean isDynamic() {
    // This implementation is currently not needed and therefore returns a defensive default value
    return true;
  }

}

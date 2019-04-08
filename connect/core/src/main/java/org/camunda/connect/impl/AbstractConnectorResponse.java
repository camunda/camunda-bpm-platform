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
package org.camunda.connect.impl;

import java.util.HashMap;
import java.util.Map;

import org.camunda.connect.spi.ConnectorResponse;

/**
 * @author Daniel Meyer
 *
 */
public abstract class AbstractConnectorResponse implements ConnectorResponse {

  protected Map<String, Object> responseParameters;

  public Map<String, Object> getResponseParameters() {
    if(responseParameters == null) {
      responseParameters = new HashMap<String, Object>();
      collectResponseParameters(responseParameters);
    }
    return responseParameters;
  }

  @SuppressWarnings("unchecked")
  public <V> V getResponseParameter(String name) {
    return (V) getResponseParameters().get(name);
  }

  /**
   * To be implemented by subclasses for collecting the generic response parameters of a response.
   * @param responseParameters a map to save the response parameters in
   */
  protected abstract void collectResponseParameters(Map<String, Object> responseParameters);

}

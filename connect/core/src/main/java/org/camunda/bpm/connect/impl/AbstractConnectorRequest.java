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
package org.camunda.bpm.connect.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.camunda.bpm.connect.ConnectorRequest;
import org.camunda.bpm.connect.ConnectorResponse;

/**
 * @author Daniel Meyer
 *
 */
public abstract class AbstractConnectorRequest<R extends ConnectorResponse> implements ConnectorRequest<R> {

  protected AbstractConnector<AbstractConnectorRequest<R>,R> connector;

  protected Map<String, Object> requestParameters = new HashMap<String, Object>();

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public AbstractConnectorRequest(AbstractConnector connector) {
    this.connector = connector;
  }

  public R execute() {
    if(!isRequestValid()) {
      throw new RuntimeException();
    }
    return connector.execute(this);
  }

  /**
   * Allows subclasses to provide custom validation logic of the
   * request parameters.
   * @return true if the request parameters are valid.
   */
  protected boolean isRequestValid() {
    return true;
  }

  public void setRequestParameters(Map<String, Object> params) {
    for (Entry<String, Object> param : params.entrySet()) {
      setRequestParameter(param.getKey(), param.getValue());
    }
  }

  /**
   * Sets a request parameter on the request
   * @param name the name of the parameter
   * @param value the value of the parameter
   */
  public void setRequestParameter(String name, Object value) {
    requestParameters.put(name, value);
  }

  /**
   * @return the parameters as handed in to the request.
   */
  public Map<String, Object> getRequestParameters() {
    return requestParameters;
  }

  @SuppressWarnings("unchecked")
  public <V> V getRequestParameter(String name) {
    return (V) requestParameters.get(name);
  }

}

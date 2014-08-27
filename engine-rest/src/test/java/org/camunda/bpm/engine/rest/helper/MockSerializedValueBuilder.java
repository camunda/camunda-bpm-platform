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
package org.camunda.bpm.engine.rest.helper;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.delegate.SerializedVariableValue;

/**
 * @author Thorben Lindhauer
 *
 */
public class MockSerializedValueBuilder {

  protected Map<String, Object> configuration = new HashMap<String, Object>();
  protected Object value;

  public MockSerializedValueBuilder value(Object value) {
    this.value = value;
    return this;
  }

  public MockSerializedValueBuilder configuration(String key, Object value) {
    configuration.put(key, value);
    return this;
  }

  public SerializedVariableValue build() {
    SerializedVariableValue mock = mock(SerializedVariableValue.class);
    when(mock.getValue()).thenReturn(value);
    when(mock.getConfig()).thenReturn(configuration);

    return mock;
  }

}

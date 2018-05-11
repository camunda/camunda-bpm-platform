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
package org.camunda.bpm.client.variable.impl.value;

import org.camunda.bpm.client.impl.EngineClient;
import org.camunda.bpm.client.impl.EngineClientException;
import org.camunda.bpm.client.impl.ExternalTaskClientLogger;
import org.camunda.bpm.engine.variable.impl.value.FileValueImpl;
import org.camunda.bpm.engine.variable.type.PrimitiveValueType;
import org.camunda.bpm.engine.variable.value.FileValue;

/**
 * @author Tassilo Weidner
 */
public class DeferredFileValue extends FileValueImpl {

  protected static final ExternalTaskClientLogger LOG = ExternalTaskClientLogger.CLIENT_LOGGER;

  protected boolean isLoaded;

  protected String variableName;
  protected String processInstanceId;
  protected EngineClient engineClient;

  public DeferredFileValue(FileValue fileValue, String variableName, String processInstanceId, EngineClient engineClient) {
    super(PrimitiveValueType.FILE, fileValue.getFilename());

    setValue(null); // deferred
    setEncoding(fileValue.getEncoding());
    setMimeType(fileValue.getMimeType());

    this.isLoaded = false;

    this.variableName = variableName;
    this.processInstanceId = processInstanceId;
    this.engineClient = engineClient;
  }

  public void load() {
    try {
      byte[] bytes = engineClient.getLocalBinaryVariable(variableName, processInstanceId);
      setValue(bytes);

      this.isLoaded = true;

    } catch (EngineClientException e) {
      throw LOG.cannotLoadDeferedFileValueException(variableName, e);
    }
  }

  public boolean isLoaded() {
    return isLoaded;
  }

}

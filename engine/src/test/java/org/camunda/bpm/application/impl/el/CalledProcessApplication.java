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
package org.camunda.bpm.application.impl.el;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.impl.EmbeddedProcessApplication;
import org.camunda.bpm.engine.impl.el.ReadOnlyMapELResolver;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;

/**
 * @author Thorben Lindhauer
 *
 */
@ProcessApplication(
    value="called-app",
    deploymentDescriptors={"org/camunda/bpm/application/impl/el/called-process-app.xml"}
)
public class CalledProcessApplication extends EmbeddedProcessApplication {

  public static final String STRING_VARIABLE_VALUE = "aVariableValue";

  protected ELResolver initProcessApplicationElResolver() {
    Map<Object, Object> resolvableValues = new HashMap<Object, Object>();
    resolvableValues.put("aStringValue", STRING_VARIABLE_VALUE);

    return new ReadOnlyMapELResolver(resolvableValues);
  }
}

/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package org.camunda.bpm.model.dmn.impl.instance;

import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN11_NS;
import static org.camunda.bpm.model.dmn.impl.DmnModelConstants.DMN_ELEMENT_CALLED_FUNCTION;

import org.camunda.bpm.model.dmn.instance.CalledFunction;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

public class CalledFunctionImpl extends DmnModelElementInstanceImpl implements CalledFunction {

  public CalledFunctionImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CalledFunction.class, DMN_ELEMENT_CALLED_FUNCTION)
      .namespaceUri(DMN11_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CalledFunction>() {
        public CalledFunction newInstance(ModelTypeInstanceContext instanceContext) {
          return new CalledFunctionImpl(instanceContext);
        }
      });

    typeBuilder.build();
  }

}

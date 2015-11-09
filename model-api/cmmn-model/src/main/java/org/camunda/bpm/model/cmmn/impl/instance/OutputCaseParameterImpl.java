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
package org.camunda.bpm.model.cmmn.impl.instance;

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN11_NS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_OUTPUT;

import org.camunda.bpm.model.cmmn.instance.CaseParameter;
import org.camunda.bpm.model.cmmn.instance.OutputCaseParameter;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class OutputCaseParameterImpl extends CaseParameterImpl implements OutputCaseParameter {

  public OutputCaseParameterImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(OutputCaseParameter.class, CMMN_ELEMENT_OUTPUT)
      .namespaceUri(CMMN11_NS)
      .extendsType(CaseParameter.class)
      .instanceProvider(new ModelElementTypeBuilder.ModelTypeInstanceProvider<OutputCaseParameter>() {
        public OutputCaseParameter newInstance(ModelTypeInstanceContext instanceContext) {
          return new OutputCaseParameterImpl(instanceContext);
        }
      });

    typeBuilder.build();
  }

}

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
package org.camunda.bpm.model.cmmn.impl.instance.camunda;

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_SOURCE;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_SOURCE_EXPRESSION;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_TARGET;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_VARIABLES;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ELEMENT_OUT;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_NS;

import org.camunda.bpm.model.cmmn.impl.instance.CmmnModelElementInstanceImpl;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaOut;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;

/**
 * @author Sebastian Menski
 * @author Roman Smirnov
 *
 */
public class CamundaOutImpl extends CmmnModelElementInstanceImpl implements CamundaOut {

  protected static Attribute<String> camundaSourceAttribute;
  protected static Attribute<String> camundaSourceExpressionAttribute;
  protected static Attribute<String> camundaVariablesAttribute;
  protected static Attribute<String> camundaTargetAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CamundaOut.class, CAMUNDA_ELEMENT_OUT)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CamundaOut>() {
        public CamundaOut newInstance(ModelTypeInstanceContext instanceContext) {
          return new CamundaOutImpl(instanceContext);
        }
      });

    camundaSourceAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_SOURCE)
      .namespace(CAMUNDA_NS)
      .build();

    camundaSourceExpressionAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_SOURCE_EXPRESSION)
      .namespace(CAMUNDA_NS)
      .build();

    camundaVariablesAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_VARIABLES)
      .namespace(CAMUNDA_NS)
      .build();

    camundaTargetAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_TARGET)
      .namespace(CAMUNDA_NS)
      .build();

    typeBuilder.build();
  }

  public CamundaOutImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getCamundaSource() {
    return camundaSourceAttribute.getValue(this);
  }

  public void setCamundaSource(String camundaSource) {
    camundaSourceAttribute.setValue(this, camundaSource);
  }

  public String getCamundaSourceExpression() {
    return camundaSourceExpressionAttribute.getValue(this);
  }

  public void setCamundaSourceExpression(String camundaSourceExpression) {
    camundaSourceExpressionAttribute.setValue(this, camundaSourceExpression);
  }

  public String getCamundaVariables() {
    return camundaVariablesAttribute.getValue(this);
  }

  public void setCamundaVariables(String camundaVariables) {
    camundaVariablesAttribute.setValue(this, camundaVariables);
  }

  public String getCamundaTarget() {
    return camundaTargetAttribute.getValue(this);
  }

  public void setCamundaTarget(String camundaTarget) {
    camundaTargetAttribute.setValue(this, camundaTarget);
  }

}

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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_IMPLEMENTATION_TYPE;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ATTRIBUTE_NAME;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_DECISION;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.Decision;
import org.camunda.bpm.model.cmmn.instance.InputDecisionParameter;
import org.camunda.bpm.model.cmmn.instance.OutputDecisionParameter;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class DecisionImpl extends CmmnElementImpl implements Decision {

  protected static Attribute<String> nameAttribute;
  protected static Attribute<String> implementationTypeAttribute;

  protected static ChildElementCollection<InputDecisionParameter> inputCollection;
  protected static ChildElementCollection<OutputDecisionParameter> outputCollection;

  public DecisionImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getName() {
    return nameAttribute.getValue(this);
  }

  public void setName(String name) {
    nameAttribute.setValue(this, name);
  }

  public String getImplementationType() {
    return implementationTypeAttribute.getValue(this);
  }

  public void setImplementationType(String implementationType) {
    implementationTypeAttribute.setValue(this, implementationType);
  }

  public Collection<InputDecisionParameter> getInputs() {
    return inputCollection.get(this);
  }

  public Collection<OutputDecisionParameter> getOutputs() {
    return outputCollection.get(this);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Decision.class, CMMN_ELEMENT_DECISION)
        .extendsType(CmmnElement.class)
        .namespaceUri(CMMN11_NS)
        .instanceProvider(new ModelTypeInstanceProvider<Decision>() {
          public Decision newInstance(ModelTypeInstanceContext instanceContext) {
            return new DecisionImpl(instanceContext);
          }
        });

    nameAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_NAME)
        .build();

    implementationTypeAttribute = typeBuilder.stringAttribute(CMMN_ATTRIBUTE_IMPLEMENTATION_TYPE)
        .defaultValue("http://www.omg.org/spec/CMMN/DecisionType/Unspecified")
        .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    inputCollection = sequenceBuilder.elementCollection(InputDecisionParameter.class)
        .build();

    outputCollection = sequenceBuilder.elementCollection(OutputDecisionParameter.class)
        .build();

    typeBuilder.build();
  }

}

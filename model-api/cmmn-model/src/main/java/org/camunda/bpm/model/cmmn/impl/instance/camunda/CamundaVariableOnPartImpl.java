package org.camunda.bpm.model.cmmn.impl.instance.camunda;

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ATTRIBUTE_VARIABLE_NAME;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ELEMENT_VARIABLE_ON_PART;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_NS;

import org.camunda.bpm.model.cmmn.VariableTransition;
import org.camunda.bpm.model.cmmn.impl.instance.CmmnModelElementInstanceImpl;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaVariableOnPart;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaVariableTransitionEvent;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.attribute.Attribute;
import org.camunda.bpm.model.xml.type.child.ChildElement;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

public class CamundaVariableOnPartImpl extends CmmnModelElementInstanceImpl implements CamundaVariableOnPart {

  protected static Attribute<String> camundaVariableNameAttribute;
  protected static ChildElement<CamundaVariableTransitionEvent> camundaVariableEventChild; 
  
  public CamundaVariableOnPartImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public static void registerType(ModelBuilder modelBuilder) {

    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CamundaVariableOnPart.class, CAMUNDA_ELEMENT_VARIABLE_ON_PART)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CamundaVariableOnPart>() {
        public CamundaVariableOnPart newInstance(ModelTypeInstanceContext instanceContext) {
          return new CamundaVariableOnPartImpl(instanceContext);
      }
    });

    camundaVariableNameAttribute = typeBuilder.stringAttribute(CAMUNDA_ATTRIBUTE_VARIABLE_NAME)
      .namespace(CAMUNDA_NS)
      .build();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    camundaVariableEventChild = sequenceBuilder.element(CamundaVariableTransitionEvent.class)
      .build();

    typeBuilder.build();
  }

  public String getVariableName() {
    return camundaVariableNameAttribute.getValue(this);
  }

  public void setVariableName(String name) {
    camundaVariableNameAttribute.setValue(this, name);
  }


  public VariableTransition getVariableEvent() {
    CamundaVariableTransitionEvent child = camundaVariableEventChild.getChild(this);
    return child.getValue();
  }

  public void setVariableEvent(VariableTransition variableTransition) {
    CamundaVariableTransitionEvent child = camundaVariableEventChild.getChild(this);
    child.setValue(variableTransition);
  }

}

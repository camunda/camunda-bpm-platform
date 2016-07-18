package org.camunda.bpm.model.cmmn.impl.instance.camunda;

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_ELEMENT_VARIABLE_EVENT;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CAMUNDA_NS;

import org.camunda.bpm.model.cmmn.VariableTransition;
import org.camunda.bpm.model.cmmn.impl.instance.CmmnModelElementInstanceImpl;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaVariableTransitionEvent;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

public class CamundaVariableTransitionEventImpl  extends CmmnModelElementInstanceImpl implements CamundaVariableTransitionEvent {

  public CamundaVariableTransitionEventImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CamundaVariableTransitionEvent.class, CAMUNDA_ELEMENT_VARIABLE_EVENT)
      .namespaceUri(CAMUNDA_NS)
      .instanceProvider(new ModelTypeInstanceProvider<CamundaVariableTransitionEvent>() {
        public CamundaVariableTransitionEvent newInstance(ModelTypeInstanceContext instanceContext) {
          return new CamundaVariableTransitionEventImpl(instanceContext);
      }
    });

    typeBuilder.build();
  }

  public VariableTransition getValue() {
    String variableEvent = getTextContent().trim();
    return Enum.valueOf(VariableTransition.class, variableEvent);
  }

  public void setValue(VariableTransition value) {
    setTextContent(value.toString());
  }
}

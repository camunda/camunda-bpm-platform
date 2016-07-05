package org.camunda.bpm.dmn.engine.impl.transform;

import org.camunda.bpm.dmn.engine.impl.DmnDecisionRequirementsGraphImpl;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformContext;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformHandler;
import org.camunda.bpm.model.dmn.instance.Definitions;

public class DmnDecisionRequirementsGraphTransformHandler implements DmnElementTransformHandler<Definitions, DmnDecisionRequirementsGraphImpl> {

  public DmnDecisionRequirementsGraphImpl handleElement(DmnElementTransformContext context, Definitions definitions) {
    return createFromDefinitions(context, definitions);
  }

  protected DmnDecisionRequirementsGraphImpl createFromDefinitions(DmnElementTransformContext context, Definitions definitions) {
    DmnDecisionRequirementsGraphImpl drd = createDmnElement();

    drd.setKey(definitions.getId());
    drd.setName(definitions.getName());

    return drd;
  }

  protected DmnDecisionRequirementsGraphImpl createDmnElement() {
    return new DmnDecisionRequirementsGraphImpl();
  }

}

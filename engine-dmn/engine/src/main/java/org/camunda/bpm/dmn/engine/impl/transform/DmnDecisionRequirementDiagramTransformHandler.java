package org.camunda.bpm.dmn.engine.impl.transform;

import org.camunda.bpm.dmn.engine.impl.DmnDecisionRequirementDiagramImpl;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformContext;
import org.camunda.bpm.dmn.engine.impl.spi.transform.DmnElementTransformHandler;
import org.camunda.bpm.model.dmn.instance.Definitions;

public class DmnDecisionRequirementDiagramTransformHandler implements DmnElementTransformHandler<Definitions, DmnDecisionRequirementDiagramImpl> {

  public DmnDecisionRequirementDiagramImpl handleElement(DmnElementTransformContext context, Definitions definitions) {
    return createFromDefinitions(context, definitions);
  }

  protected DmnDecisionRequirementDiagramImpl createFromDefinitions(DmnElementTransformContext context, Definitions definitions) {
    DmnDecisionRequirementDiagramImpl drd = createDmnElement();

    drd.setKey(definitions.getId());
    drd.setName(definitions.getName());

    return drd;
  }

  protected DmnDecisionRequirementDiagramImpl createDmnElement() {
    return new DmnDecisionRequirementDiagramImpl();
  }

}

package org.camunda.bpm.engine.runtime;

import java.io.InputStream;

import org.camunda.bpm.engine.impl.cmmn.entity.runtime.CaseExecutionEntity;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.model.cmmn.Cmmn;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.camunda.bpm.model.cmmn.impl.instance.CaseImpl;
import org.camunda.bpm.model.cmmn.impl.instance.CaseTaskImpl;
import org.camunda.bpm.model.cmmn.impl.instance.EventListenerImpl;
import org.camunda.bpm.model.cmmn.impl.instance.HumanTaskImpl;
import org.camunda.bpm.model.cmmn.impl.instance.MilestoneImpl;
import org.camunda.bpm.model.cmmn.impl.instance.PlanItemDefinitionImpl;
import org.camunda.bpm.model.cmmn.impl.instance.PlanItemImpl;
import org.camunda.bpm.model.cmmn.impl.instance.ProcessTaskImpl;
import org.camunda.bpm.model.cmmn.impl.instance.StageImpl;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * Handles identification of the execution type for given case execution.
 * @author Simon Zambrovski
 */
public class CaseExecutionModelExtender {

  private static final String DEFINITION_REF = "definitionRef";

  private Type type;
  private String description;

  /**
   * Retrieves the type.
   * @return type of case execution.
   */
  public String getType() {
    return this.type.label;
  }

  /**
   * Retrieves description.
   * @return description of the definition element.
   */
  public String getDescription() {
    return this.description;
  }

  /**
   * Factory method constructing the
   * @param caseExecutionEntity
   * @throws IllegalArgumentException if some unexpected case execution entity is used.
   * @return ready case execution extender instance.
   */
  public static CaseExecutionModelExtender from(final CaseExecutionEntity caseExecutionEntity) {

    final CaseExecutionModelExtender caseExecutionType = new CaseExecutionModelExtender();
    final CmmnModelInstance modelInstance = caseExecutionEntity.getCmmnModelInstance();
    
    final ModelElementInstance plainItemModel = modelInstance.getModelElementById(caseExecutionEntity.getActivityId());
    if (plainItemModel instanceof PlanItemImpl) {
      final PlanItemImpl planItem = (PlanItemImpl) plainItemModel;
      final String attributeValue = planItem.getAttributeValue(DEFINITION_REF);
      final ModelElementInstance element = modelInstance.getModelElementById(attributeValue);
      caseExecutionType.type = Type.of(element);

      if (element instanceof PlanItemDefinitionImpl) {
        final PlanItemDefinitionImpl pid = (PlanItemDefinitionImpl) element;
        caseExecutionType.description = pid.getDescription();
      }
    }

    return caseExecutionType;
  }

  /**
   * Possible case execution types.
   */
  static enum Type {
    HUMANTASK("human-task"), CASETASK("case-task"), PROCESSTASK("process-task"), STAGE("stage"), CASE("case"), MILESTONE("milestone"), EVENTLISTENER(
        "event-listener");

    private String label;

    Type(String label) {
      this.label = label;
    }

    /**
     * Retrieves the type, based on the element class.
     * @param element to look for the type.
     * @return type.
     */
    public static Type of(final ModelElementInstance element) {
      if (element instanceof HumanTaskImpl) {
        return Type.HUMANTASK;
      } else if (element instanceof CaseTaskImpl) {
        return Type.CASETASK;
      } else if (element instanceof CaseImpl) {
        return Type.CASE;
      } else if (element instanceof StageImpl) {
        return Type.STAGE;
      } else if (element instanceof ProcessTaskImpl) {
        return Type.PROCESSTASK;
      } else if (element instanceof MilestoneImpl) {
        return Type.MILESTONE;
      } else if (element instanceof EventListenerImpl) {
        return Type.EVENTLISTENER;
      } else {
        // unknown type
        throw new IllegalArgumentException("Unknown model element of type " + element.getClass().getName() + " detected.");
      }
    }
  }
}

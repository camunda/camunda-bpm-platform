/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.impl.cmmn.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnIfPartDeclaration;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnOnPartDeclaration;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.camunda.bpm.engine.impl.cmmn.model.CmmnVariableOnPartDeclaration;
import org.camunda.bpm.engine.impl.cmmn.transformer.CmmnTransformerLogger;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.model.cmmn.PlanItemTransition;
import org.camunda.bpm.model.cmmn.Query;
import org.camunda.bpm.model.cmmn.VariableTransition;
import org.camunda.bpm.model.cmmn.instance.CaseFileItemOnPart;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.cmmn.instance.ConditionExpression;
import org.camunda.bpm.model.cmmn.instance.ExtensionElements;
import org.camunda.bpm.model.cmmn.instance.IfPart;
import org.camunda.bpm.model.cmmn.instance.OnPart;
import org.camunda.bpm.model.cmmn.instance.PlanItem;
import org.camunda.bpm.model.cmmn.instance.PlanItemOnPart;
import org.camunda.bpm.model.cmmn.instance.Sentry;
import org.camunda.bpm.model.cmmn.instance.camunda.CamundaVariableOnPart;
import org.camunda.bpm.model.xml.instance.ModelElementInstance;

/**
 * @author Roman Smirnov
 *
 */
public class SentryHandler extends CmmnElementHandler<Sentry, CmmnSentryDeclaration> {

  protected static final CmmnTransformerLogger LOG = ProcessEngineLogger.CMMN_TRANSFORMER_LOGGER;

  public CmmnSentryDeclaration handleElement(Sentry element, CmmnHandlerContext context) {

    String id = element.getId();
    Collection<OnPart> onParts = element.getOnParts();
    IfPart ifPart = element.getIfPart();
    List<CamundaVariableOnPart> variableOnParts = queryExtensionElementsByClass(element, CamundaVariableOnPart.class);

    if ((ifPart == null || ifPart.getConditions().isEmpty()) && variableOnParts.isEmpty()) {

      if (onParts == null || onParts.isEmpty()) {
        LOG.ignoredSentryWithMissingCondition(id);
        return null;
      } else {
        boolean atLeastOneOnPartsValid = false;

        for (OnPart onPart : onParts) {
          if (onPart instanceof PlanItemOnPart) {
            PlanItemOnPart planItemOnPart = (PlanItemOnPart) onPart;
            if (planItemOnPart.getSource() != null && planItemOnPart.getStandardEvent() != null) {
              atLeastOneOnPartsValid = true;
              break;
            }
          }
        }

        if (!atLeastOneOnPartsValid) {
          LOG.ignoredSentryWithInvalidParts(id);
          return null;
        }
      }
    }

    CmmnSentryDeclaration sentryDeclaration = new CmmnSentryDeclaration(id);

    // the ifPart will be initialized immediately
    initializeIfPart(ifPart, sentryDeclaration, context);

    // the variableOnParts will be initialized immediately as it does not have any dependency
    initializeVariableOnParts(element, sentryDeclaration, context, variableOnParts);
    
    // ...whereas the onParts will be initialized later because the
    // the reference to the plan items (sourceRef) and the reference
    // to the sentry (sentryRef) cannot be set in this step. To set
    // the corresponding reference (sourceRef or sentryRef) on the
    // transformed sentry all planned items and all sentries inside
    // the current stage should be already transformed.

    CmmnActivity parent = context.getParent();
    if (parent != null) {
      parent.addSentry(sentryDeclaration);
    }

    return sentryDeclaration;
  }

  public void initializeOnParts(Sentry sentry, CmmnHandlerContext context) {
    Collection<OnPart> onParts = sentry.getOnParts();
    for (OnPart onPart : onParts) {
      if (onPart instanceof PlanItemOnPart) {
        initializeOnPart((PlanItemOnPart) onPart, sentry, context);
      } else {
        initializeOnPart((CaseFileItemOnPart) onPart, sentry, context);
      }
    }
  }

  protected void initializeOnPart(PlanItemOnPart onPart, Sentry sentry, CmmnHandlerContext context) {
    CmmnActivity parent = context.getParent();
    String sentryId = sentry.getId();
    CmmnSentryDeclaration sentryDeclaration = parent.getSentry(sentryId);

    PlanItem source = onPart.getSource();
    PlanItemTransition standardEvent = onPart.getStandardEvent();

    if (source != null && standardEvent != null) {
      CmmnOnPartDeclaration onPartDeclaration = new CmmnOnPartDeclaration();

      // initialize standardEvent
      String standardEventName = standardEvent.name();
      onPartDeclaration.setStandardEvent(standardEventName);

      // initialize sourceRef
      String sourceId = source.getId();
      CmmnActivity sourceActivity = parent.findActivity(sourceId);

      if (sourceActivity != null) {
        onPartDeclaration.setSource(sourceActivity);
      }

      // initialize sentryRef
      Sentry sentryRef = onPart.getSentry();
      if (sentryRef != null) {
        String sentryRefId = sentryRef.getId();

        CmmnSentryDeclaration sentryRefDeclaration = parent.getSentry(sentryRefId);
        onPartDeclaration.setSentry(sentryRefDeclaration);
      }

      // add onPartDeclaration to sentryDeclaration
      sentryDeclaration.addOnPart(onPartDeclaration);
    }

  }

  protected void initializeOnPart(CaseFileItemOnPart onPart, Sentry sentry, CmmnHandlerContext context) {
    // not yet implemented
    String id = sentry.getId();
    LOG.ignoredUnsupportedAttribute("onPart", "CaseFileItem", id);
  }

  protected void initializeIfPart(IfPart ifPart, CmmnSentryDeclaration sentryDeclaration, CmmnHandlerContext context) {
    if (ifPart == null) {
      return;
    }

    Collection<ConditionExpression> conditions = ifPart.getConditions();

    if (conditions.size() > 1) {
      String id = sentryDeclaration.getId();
      LOG.multipleIgnoredConditions(id);
    }

    ExpressionManager expressionManager = context.getExpressionManager();
    ConditionExpression condition = conditions.iterator().next();
    Expression conditionExpression = expressionManager.createExpression(condition.getText());

    CmmnIfPartDeclaration ifPartDeclaration = new CmmnIfPartDeclaration();
    ifPartDeclaration.setCondition(conditionExpression);
    sentryDeclaration.setIfPart(ifPartDeclaration);
  }

  protected void initializeVariableOnParts(CmmnElement element, CmmnSentryDeclaration sentryDeclaration, 
    CmmnHandlerContext context, List<CamundaVariableOnPart> variableOnParts) {
    for(CamundaVariableOnPart variableOnPart: variableOnParts) {
      initializeVariableOnPart(variableOnPart, sentryDeclaration, context);
    }
  }

  protected void initializeVariableOnPart(CamundaVariableOnPart variableOnPart, CmmnSentryDeclaration sentryDeclaration, CmmnHandlerContext context) {
    VariableTransition variableTransition;

    try {
      variableTransition = variableOnPart.getVariableEvent();
    } catch(IllegalArgumentException illegalArgumentexception) {
      throw LOG.nonMatchingVariableEvents(sentryDeclaration.getId());
    } catch(NullPointerException nullPointerException) {
      throw LOG.nonMatchingVariableEvents(sentryDeclaration.getId());
    }

    String variableName = variableOnPart.getVariableName();
    String variableEventName = variableTransition.name();

    if (variableName != null) {
      if (!sentryDeclaration.hasVariableOnPart(variableEventName, variableName)) {
        CmmnVariableOnPartDeclaration variableOnPartDeclaration = new CmmnVariableOnPartDeclaration();
        variableOnPartDeclaration.setVariableEvent(variableEventName);
        variableOnPartDeclaration.setVariableName(variableName);
        sentryDeclaration.addVariableOnParts(variableOnPartDeclaration);
      } 
    } else {
      throw LOG.emptyVariableName(sentryDeclaration.getId());
    }
  }

  protected <V extends ModelElementInstance> List<V> queryExtensionElementsByClass(CmmnElement element, Class<V> cls) {
    ExtensionElements extensionElements = element.getExtensionElements();

    if (extensionElements != null) {
      Query<ModelElementInstance> query = extensionElements.getElementsQuery();
      return query.filterByType(cls).list();

    } else {
      return new ArrayList<V>();
    }
  }
}

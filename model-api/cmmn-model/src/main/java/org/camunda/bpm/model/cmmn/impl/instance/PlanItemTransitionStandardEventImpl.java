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
package org.camunda.bpm.model.cmmn.impl.instance;

import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN11_NS;
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_STANDARD_EVENT;

import org.camunda.bpm.model.cmmn.PlanItemTransition;
import org.camunda.bpm.model.cmmn.instance.PlanItemTransitionStandardEvent;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class PlanItemTransitionStandardEventImpl extends CmmnModelElementInstanceImpl implements PlanItemTransitionStandardEvent {

  public PlanItemTransitionStandardEventImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(PlanItemTransitionStandardEvent.class, CMMN_ELEMENT_STANDARD_EVENT)
      .namespaceUri(CMMN11_NS)
      .instanceProvider(new ModelElementTypeBuilder.ModelTypeInstanceProvider<PlanItemTransitionStandardEvent>() {
        public PlanItemTransitionStandardEvent newInstance(ModelTypeInstanceContext instanceContext) {
          return new PlanItemTransitionStandardEventImpl(instanceContext);
        }
      });

    typeBuilder.build();
  }

  public PlanItemTransition getValue() {
    String standardEvent = getTextContent().trim();
    return Enum.valueOf(PlanItemTransition.class, standardEvent);
  }

  public void setValue(PlanItemTransition value) {
    setTextContent(value.toString());
  }

}

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
package org.camunda.bpm.model.bpmn.impl.instance;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_POTENTIAL_OWNER;

import org.camunda.bpm.model.bpmn.instance.HumanPerformer;
import org.camunda.bpm.model.bpmn.instance.PotentialOwner;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

/**
 * The BPMN potentialOwner element
 *
 * @author Dario Campagna
 */
public class PotentialOwnerImpl extends HumanPerformerImpl implements PotentialOwner {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(PotentialOwner.class, BPMN_ELEMENT_POTENTIAL_OWNER)
      .namespaceUri(BPMN20_NS)
      .extendsType(HumanPerformer.class)
      .instanceProvider(new ModelElementTypeBuilder.ModelTypeInstanceProvider<PotentialOwner>() {
        public PotentialOwner newInstance(ModelTypeInstanceContext instanceContext) {
          return new PotentialOwnerImpl(instanceContext);
        }
      });
    typeBuilder.build();
  }

  public PotentialOwnerImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

}

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

import org.camunda.bpm.model.bpmn.instance.DataAssociation;
import org.camunda.bpm.model.bpmn.instance.DataInputAssociation;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;

import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN20_NS;
import static org.camunda.bpm.model.bpmn.impl.BpmnModelConstants.BPMN_ELEMENT_DATA_INPUT_ASSOCIATION;
import static org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;

/**
 * The BPMN dataInputAssociation element
 *
 * @author Sebastian Menski
 */
public class DataInputAssociationImpl extends DataAssociationImpl implements DataInputAssociation {

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(DataInputAssociation.class, BPMN_ELEMENT_DATA_INPUT_ASSOCIATION)
      .namespaceUri(BPMN20_NS)
      .extendsType(DataAssociation.class)
      .instanceProvider(new ModelTypeInstanceProvider<DataInputAssociation>() {
        public DataInputAssociation newInstance(ModelTypeInstanceContext instanceContext) {
          return new DataInputAssociationImpl(instanceContext);
        }
      });

    typeBuilder.build();
  }

  public DataInputAssociationImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }
}

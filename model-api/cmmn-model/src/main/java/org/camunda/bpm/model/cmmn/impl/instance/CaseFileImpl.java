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
import static org.camunda.bpm.model.cmmn.impl.CmmnModelConstants.CMMN_ELEMENT_CASE_FILE;

import java.util.Collection;

import org.camunda.bpm.model.cmmn.instance.CaseFile;
import org.camunda.bpm.model.cmmn.instance.CaseFileItem;
import org.camunda.bpm.model.cmmn.instance.CmmnElement;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder;
import org.camunda.bpm.model.xml.type.ModelElementTypeBuilder.ModelTypeInstanceProvider;
import org.camunda.bpm.model.xml.type.child.ChildElementCollection;
import org.camunda.bpm.model.xml.type.child.SequenceBuilder;

/**
 * @author Roman Smirnov
 *
 */
public class CaseFileImpl extends CmmnElementImpl implements CaseFile {

  protected static ChildElementCollection<CaseFileItem> caseFileItemCollection;

  public CaseFileImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Collection<CaseFileItem> getCaseFileItems() {
    return caseFileItemCollection.get(this);
  }

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CaseFile.class, CMMN_ELEMENT_CASE_FILE)
        .namespaceUri(CMMN11_NS)
        .extendsType(CmmnElement.class)
        .instanceProvider(new ModelTypeInstanceProvider<CaseFile>() {
          public CaseFile newInstance(ModelTypeInstanceContext instanceContext) {
            return new CaseFileImpl(instanceContext);
          }
        });

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    caseFileItemCollection = sequenceBuilder.elementCollection(CaseFileItem.class)
        .build();

    typeBuilder.build();
  }

}

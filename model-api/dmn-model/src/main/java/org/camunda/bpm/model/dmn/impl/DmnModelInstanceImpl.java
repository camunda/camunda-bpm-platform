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
package org.camunda.bpm.model.dmn.impl;

import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.camunda.bpm.model.dmn.instance.Definitions;
import org.camunda.bpm.model.xml.ModelBuilder;
import org.camunda.bpm.model.xml.impl.ModelImpl;
import org.camunda.bpm.model.xml.impl.ModelInstanceImpl;
import org.camunda.bpm.model.xml.instance.DomDocument;


public class DmnModelInstanceImpl extends ModelInstanceImpl implements DmnModelInstance {

  public DmnModelInstanceImpl(ModelImpl model, ModelBuilder modelBuilder, DomDocument document) {
    super(model, modelBuilder, document);
  }

  public Definitions getDefinitions() {
    return (Definitions) getDocumentElement();
  }

  public void setDefinitions(Definitions definitions) {
    setDocumentElement(definitions);
  }

  @Override
  public DmnModelInstance clone() {
    return new DmnModelInstanceImpl(model, modelBuilder, document.clone());
  }

}

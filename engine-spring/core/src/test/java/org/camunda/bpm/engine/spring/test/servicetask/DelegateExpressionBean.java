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
package org.camunda.bpm.engine.spring.test.servicetask;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.el.FixedValue;

/**
 * @author Joram Barrez
 * @author Bernd Ruecker (camunda)
 */
public class DelegateExpressionBean implements JavaDelegate {

  private SentenceGenerator sentenceGenerator;

  private FixedValue someField;

  public void execute(DelegateExecution execution) throws Exception {
    execution.setVariable("myVar", sentenceGenerator.getSentence());
    if (someField != null) {
      execution.setVariable("fieldInjection", someField.getValue(execution));
    }
  }

  public void setSentenceGenerator(SentenceGenerator sentenceGenerator) {
    this.sentenceGenerator = sentenceGenerator;
  }

  public FixedValue getSomeField() {
    return someField;
  }

  public void setSomeField(FixedValue someField) {
    this.someField = someField;
  }

}

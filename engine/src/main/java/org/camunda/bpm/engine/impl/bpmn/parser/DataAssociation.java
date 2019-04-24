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
package org.camunda.bpm.engine.impl.bpmn.parser;

import org.camunda.bpm.engine.delegate.Expression;

/**
 * A data association (Input or Output) between a source and a target
 */
public class DataAssociation {

  protected String source;

  protected Expression sourceExpression;

  protected String target;

  protected String variables;

  protected Expression businessKeyExpression;

  protected DataAssociation(String source, String target) {
    this.source = source;
    this.target = target;
  }

  protected DataAssociation(Expression sourceExpression, String target) {
    this.sourceExpression = sourceExpression;
    this.target = target;
  }

  protected DataAssociation(String variables) {
    this.variables = variables;
  }

  protected DataAssociation(Expression businessKeyExpression) {
    this.businessKeyExpression = businessKeyExpression;
  }

  public String getSource() {
    return source;
  }

  public String getTarget() {
    return target;
  }


  public Expression getSourceExpression() {
    return sourceExpression;
  }

  public String getVariables() {
    return variables;
  }

  public Expression getBusinessKeyExpression() {
    return businessKeyExpression;
  }
}

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
package org.camunda.bpm.dmn.engine.impl;

import javax.script.CompiledScript;

import org.camunda.bpm.dmn.engine.impl.spi.el.ElExpression;
import org.camunda.bpm.dmn.engine.impl.spi.type.DmnTypeDefinition;

public class DmnExpressionImpl implements CachedCompiledScriptSupport, CachedExpressionSupport {

  protected String id;
  protected String name;

  protected DmnTypeDefinition typeDefinition;
  protected String expressionLanguage;
  protected String expression;

  protected CompiledScript cachedCompiledScript;
  protected ElExpression cachedExpression;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DmnTypeDefinition getTypeDefinition() {
    return typeDefinition;
  }

  public void setTypeDefinition(DmnTypeDefinition typeDefinition) {
    this.typeDefinition = typeDefinition;
  }

  public String getExpressionLanguage() {
    return expressionLanguage;
  }

  public void setExpressionLanguage(String expressionLanguage) {
    this.expressionLanguage = expressionLanguage;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  @Override
  public String toString() {
    return "DmnExpressionImpl{" +
      "id='" + id + '\'' +
      ", name='" + name + '\'' +
      ", typeDefinition=" + typeDefinition +
      ", expressionLanguage='" + expressionLanguage + '\'' +
      ", expression='" + expression + '\'' +
      '}';
  }

  public void cacheCompiledScript(CompiledScript compiledScript) {
    this.cachedCompiledScript = compiledScript;
  }

  public CompiledScript getCachedCompiledScript() {
    return this.cachedCompiledScript;
  }

  public ElExpression getCachedExpression() {
    return this.cachedExpression;
  }

  public void setCachedExpression(ElExpression expression) {
    this.cachedExpression = expression;
  }
}

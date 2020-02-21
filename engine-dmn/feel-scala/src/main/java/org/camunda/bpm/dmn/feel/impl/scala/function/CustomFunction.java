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
package org.camunda.bpm.dmn.feel.impl.scala.function;

import org.camunda.bpm.dmn.feel.impl.scala.function.builder.CustomFunctionBuilder;
import org.camunda.bpm.dmn.feel.impl.scala.function.builder.CustomFunctionBuilderImpl;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class CustomFunction {

  protected List<String> params;
  protected Function<List<Object>, Object> function;
  protected boolean hasVarargs;

  public CustomFunction() {
    params = Collections.emptyList();
  }

  /**
   * Creates a fluent builder to configure a custom function
   *
   * @return builder to apply configurations on
   */
  public static CustomFunctionBuilder create() {
    return new CustomFunctionBuilderImpl();
  }

  public List<String> getParams() {
    return params;
  }

  public void setParams(List<String> params) {
    this.params = params;
  }

  public Function<List<Object>, Object> getFunction() {
    return function;
  }

  public void setFunction(Function<List<Object>, Object> function) {
    this.function = function;
  }

  public boolean hasVarargs() {
    return hasVarargs;
  }

  public void setHasVarargs(boolean hasVarargs) {
    this.hasVarargs = hasVarargs;
  }

}

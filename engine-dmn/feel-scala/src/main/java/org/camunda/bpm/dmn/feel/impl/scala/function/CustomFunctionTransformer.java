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

import org.camunda.bpm.dmn.feel.impl.scala.ScalaFeelLogger;
import org.camunda.feel.context.JavaFunction;
import org.camunda.feel.context.JavaFunctionProvider;
import org.camunda.feel.syntaxtree.Val;
import org.camunda.feel.valuemapper.ValueMapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CustomFunctionTransformer extends JavaFunctionProvider {

  protected static final ScalaFeelLogger LOGGER = ScalaFeelLogger.LOGGER;

  protected Map<String, JavaFunction> functions;
  protected ValueMapper valueMapper;

  public CustomFunctionTransformer(List<FeelCustomFunctionProvider> functionProviders,
                                   ValueMapper valueMapper) {
    this.functions = new HashMap<>();
    this.valueMapper = valueMapper;

    if (functionProviders != null) {
      transformFunctions(functionProviders);
    }
  }

  protected void transformFunctions(List<FeelCustomFunctionProvider> functionProviders) {
    functionProviders.forEach(functionProvider -> {
      Collection<String> functionNames = functionProvider.getFunctionNames();

      functionNames.forEach(functionName -> {
        CustomFunction customFunction = functionProvider.resolveFunction(functionName)
            .orElseThrow(LOGGER::customFunctionNotFoundException);

        List<String> params = customFunction.getParams();

        Function<List<Val>, Val> function = transformFunction(customFunction);

        boolean hasVarargs = customFunction.hasVarargs();

        JavaFunction javaFunction = new JavaFunction(params, function, hasVarargs);

        this.functions.put(functionName, javaFunction);
      });
    });
  }

  protected Function<List<Val>, Val> transformFunction(CustomFunction function) {
    return args -> {

      List<Object> unpackedArgs = unpackVals(args);

      Function<List<Object>, Object> functionHandler = function.getFunction();
      Object result = functionHandler.apply(unpackedArgs);

      return toVal(result);
    };
  }

  protected List<Object> unpackVals(List<Val> args) {
    return args.stream()
      .map(this::unpackVal)
      .collect(Collectors.toList());
  }

  protected Val toVal(Object rawResult) {
    return valueMapper.toVal(rawResult);
  }

  protected Object unpackVal(Val arg) {
    return valueMapper.unpackVal(arg);
  }

  @Override
  public Optional<JavaFunction> resolveFunction(String functionName) {
    return Optional.ofNullable(functions.get(functionName));
  }

  @Override
  public Collection<String> getFunctionNames() {
    return functions.keySet();
  }

}

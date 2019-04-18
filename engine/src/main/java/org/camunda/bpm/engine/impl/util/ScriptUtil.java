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
package org.camunda.bpm.engine.impl.util;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureAtLeastOneNotNull;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotEmpty;
import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.scripting.ExecutableScript;
import org.camunda.bpm.engine.impl.scripting.ScriptFactory;
import org.camunda.bpm.engine.impl.scripting.engine.JuelScriptEngineFactory;

/**
 * @author Sebastian Menski
 */
public final class ScriptUtil {

  /**
   * Creates a new {@link ExecutableScript} from a source or resource. It excepts static and
   * dynamic sources and resources. Dynamic means that the source or resource is an expression
   * which will be evaluated during execution.
   *
   * @param language the language of the script
   * @param source the source code of the script or an expression which evaluates to the source code
   * @param resource the resource path of the script code or an expression which evaluates to the resource path
   * @param expressionManager the expression manager to use to generate the expressions of dynamic scripts
   * @return the newly created script
   * @throws NotValidException if language is null or empty or both of source and resource are null or empty
   */
  public static ExecutableScript getScript(String language, String source, String resource, ExpressionManager expressionManager) {
    return getScript(language, source, resource, expressionManager, getScriptFactory());
  }

  /**
   * Creates a new {@link ExecutableScript} from a source or resource. It excepts static and
   * dynamic sources and resources. Dynamic means that the source or resource is an expression
   * which will be evaluated during execution.
   *
   * @param language the language of the script
   * @param source the source code of the script or an expression which evaluates to the source code
   * @param resource the resource path of the script code or an expression which evaluates to the resource path
   * @param expressionManager the expression manager to use to generate the expressions of dynamic scripts
   * @param scriptFactory the script factory used to create the script
   * @return the newly created script
   * @throws NotValidException if language is null or empty or both of source and resource are invalid
   */
  public static ExecutableScript getScript(String language, String source, String resource, ExpressionManager expressionManager, ScriptFactory scriptFactory) {
    ensureNotEmpty(NotValidException.class, "Script language", language);
    ensureAtLeastOneNotNull(NotValidException.class, "No script source or resource was given", source, resource);
    if (resource != null && !resource.isEmpty()) {
      return getScriptFromResource(language, resource, expressionManager, scriptFactory);
    }
    else {
      return getScriptFormSource(language, source, expressionManager, scriptFactory);
    }
  }

  /**
   * Creates a new {@link ExecutableScript} from a source. It excepts static and dynamic sources.
   * Dynamic means that the source is an expression which will be evaluated during execution.
   *
   * @param language the language of the script
   * @param source the source code of the script or an expression which evaluates to the source code
   * @param expressionManager the expression manager to use to generate the expressions of dynamic scripts
   * @param scriptFactory the script factory used to create the script
   * @return the newly created script
   * @throws NotValidException if language is null or empty or source is null
   */
  public static ExecutableScript getScriptFormSource(String language, String source, ExpressionManager expressionManager, ScriptFactory scriptFactory) {
    ensureNotEmpty(NotValidException.class, "Script language", language);
    ensureNotNull(NotValidException.class, "Script source", source);
    if (isDynamicScriptExpression(language, source)) {
      Expression sourceExpression = expressionManager.createExpression(source);
      return getScriptFromSourceExpression(language, sourceExpression, scriptFactory);
    }
    else {
      return getScriptFromSource(language, source, scriptFactory);
    }
  }

  /**
   * Creates a new {@link ExecutableScript} from a static source.
   *
   * @param language the language of the script
   * @param source the source code of the script
   * @param scriptFactory the script factory used to create the script
   * @return the newly created script
   * @throws NotValidException if language is null or empty or source is null
   */
  public static ExecutableScript getScriptFromSource(String language, String source, ScriptFactory scriptFactory) {
    ensureNotEmpty(NotValidException.class, "Script language", language);
    ensureNotNull(NotValidException.class, "Script source", source);
    return scriptFactory.createScriptFromSource(language, source);
  }

  /**
   * Creates a new {@link ExecutableScript} from a dynamic source. Dynamic means that the source
   * is an expression which will be evaluated during execution.
   *
   * @param language the language of the script
   * @param sourceExpression the expression which evaluates to the source code
   * @param scriptFactory the script factory used to create the script
   * @return the newly created script
   * @throws NotValidException if language is null or empty or sourceExpression is null
   */
  public static ExecutableScript getScriptFromSourceExpression(String language, Expression sourceExpression, ScriptFactory scriptFactory) {
    ensureNotEmpty(NotValidException.class, "Script language", language);
    ensureNotNull(NotValidException.class, "Script source expression", sourceExpression);
    return scriptFactory.createScriptFromSource(language, sourceExpression);
  }

  /**
   * Creates a new {@link ExecutableScript} from a resource. It excepts static and dynamic resources.
   * Dynamic means that the resource is an expression which will be evaluated during execution.
   *
   * @param language the language of the script
   * @param resource the resource path of the script code or an expression which evaluates to the resource path
   * @param expressionManager the expression manager to use to generate the expressions of dynamic scripts
   * @param scriptFactory the script factory used to create the script
   * @return the newly created script
   * @throws NotValidException if language or resource are null or empty
   */
  public static ExecutableScript getScriptFromResource(String language, String resource, ExpressionManager expressionManager, ScriptFactory scriptFactory) {
    ensureNotEmpty(NotValidException.class, "Script language", language);
    ensureNotEmpty(NotValidException.class, "Script resource", resource);
    if (isDynamicScriptExpression(language, resource)) {
      Expression resourceExpression = expressionManager.createExpression(resource);
      return getScriptFromResourceExpression(language, resourceExpression, scriptFactory);
    }
    else {
      return getScriptFromResource(language, resource, scriptFactory);
    }
  }

  /**
   * Creates a new {@link ExecutableScript} from a static resource.
   *
   * @param language the language of the script
   * @param resource the resource path of the script code
   * @param scriptFactory the script factory used to create the script
   * @return the newly created script
   * @throws NotValidException if language or resource are null or empty
   */
  public static ExecutableScript getScriptFromResource(String language, String resource, ScriptFactory scriptFactory) {
    ensureNotEmpty(NotValidException.class, "Script language", language);
    ensureNotEmpty(NotValidException.class, "Script resource", resource);
    return scriptFactory.createScriptFromResource(language, resource);
  }

  /**
   * Creates a new {@link ExecutableScript} from a dynamic resource. Dynamic means that the source
   * is an expression which will be evaluated during execution.
   *
   * @param language the language of the script
   * @param resourceExpression the expression which evaluates to the resource path
   * @param scriptFactory the script factory used to create the script
   * @return the newly created script
   * @throws NotValidException if language is null or empty or resourceExpression is null
   */
  public static ExecutableScript getScriptFromResourceExpression(String language, Expression resourceExpression, ScriptFactory scriptFactory) {
    ensureNotEmpty(NotValidException.class, "Script language", language);
    ensureNotNull(NotValidException.class, "Script resource expression", resourceExpression);
    return scriptFactory.createScriptFromResource(language, resourceExpression);
  }

  /**
   * Checks if the value is an expression for a dynamic script source or resource.
   *
   * @param language the language of the script
   * @param value the value to check
   * @return true if the value is an expression for a dynamic script source/resource, otherwise false
   */
  public static boolean isDynamicScriptExpression(String language, String value) {
    return StringUtil.isExpression(value) && (language != null && !JuelScriptEngineFactory.names.contains(language.toLowerCase()));
  }

  /**
   * Returns the configured script factory in the context or a new one.
   */
  public static ScriptFactory getScriptFactory() {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();
    if (processEngineConfiguration != null) {
      return processEngineConfiguration.getScriptFactory();
    }
    else {
      return new ScriptFactory();
    }
  }
}

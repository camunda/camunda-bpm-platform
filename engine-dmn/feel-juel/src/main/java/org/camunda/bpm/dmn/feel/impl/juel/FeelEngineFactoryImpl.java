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
package org.camunda.bpm.dmn.feel.impl.juel;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;

import org.camunda.bpm.dmn.feel.impl.FeelEngine;
import org.camunda.bpm.dmn.feel.impl.FeelEngineFactory;
import org.camunda.bpm.dmn.feel.impl.juel.el.ElContextFactory;
import org.camunda.bpm.dmn.feel.impl.juel.el.FeelElContextFactory;
import org.camunda.bpm.dmn.feel.impl.juel.el.FeelTypeConverter;
import org.camunda.bpm.dmn.feel.impl.juel.transform.FeelToJuelFunctionTransformer;
import org.camunda.bpm.dmn.feel.impl.juel.transform.FeelToJuelTransform;
import org.camunda.bpm.dmn.feel.impl.juel.transform.FeelToJuelTransformImpl;
import org.camunda.commons.utils.cache.Cache;
import org.camunda.commons.utils.cache.ConcurrentLruCache;

import de.odysseus.el.ExpressionFactoryImpl;

public class FeelEngineFactoryImpl implements FeelEngineFactory {

  public static final FeelEngineLogger LOG = FeelLogger.ENGINE_LOGGER;

  public static final int DEFAULT_EXPRESSION_CACHE_SIZE = 1000;

  protected final FeelEngine feelEngine;

  protected final int expressionCacheSize;
  protected final List<FeelToJuelFunctionTransformer> customFunctionTransformers;

  public FeelEngineFactoryImpl() {
    this(DEFAULT_EXPRESSION_CACHE_SIZE);
  }

  public FeelEngineFactoryImpl(int expressionCacheSize) {
      this(expressionCacheSize, Collections.<FeelToJuelFunctionTransformer> emptyList());
  }

  public FeelEngineFactoryImpl(List<FeelToJuelFunctionTransformer> customFunctionTransformers) {
      this(DEFAULT_EXPRESSION_CACHE_SIZE, customFunctionTransformers);
  }

  public FeelEngineFactoryImpl(int expressionCacheSize, List<FeelToJuelFunctionTransformer> customFunctionTransformers) {
    this.expressionCacheSize = expressionCacheSize;
    this.customFunctionTransformers = customFunctionTransformers;

    feelEngine = createFeelEngine();
  }

  public FeelEngine createInstance() {
    return feelEngine;
  }

  protected FeelEngine createFeelEngine() {
    FeelToJuelTransform transform = createFeelToJuelTransform();
    ExpressionFactory expressionFactory = createExpressionFactory();
    ElContextFactory elContextFactory = createElContextFactory();
    Cache<TransformExpressionCacheKey, String> transformExpressionCache = createTransformExpressionCache();
    return new FeelEngineImpl(transform, expressionFactory, elContextFactory, transformExpressionCache);
  }

  protected FeelToJuelTransform createFeelToJuelTransform() {
    FeelToJuelTransformImpl transformer = new FeelToJuelTransformImpl();

    for (FeelToJuelFunctionTransformer functionTransformer : customFunctionTransformers) {
      transformer.addCustomFunctionTransformer(functionTransformer);
    }

    return transformer;
  }

  protected ExpressionFactory createExpressionFactory() {
    Properties properties = new Properties();
    properties.put(ExpressionFactoryImpl.PROP_CACHE_SIZE, String.valueOf(expressionCacheSize));

    try {
      return new ExpressionFactoryImpl(properties, createTypeConverter());
    }
    catch (ELException e) {
      throw LOG.unableToInitializeFeelEngine(e);
    }
  }

  protected FeelTypeConverter createTypeConverter() {
    return new FeelTypeConverter();
  }

  protected ElContextFactory createElContextFactory() {
    FeelElContextFactory factory = new FeelElContextFactory();

    for (FeelToJuelFunctionTransformer functionTransformer : customFunctionTransformers) {
      factory.addCustomFunction(functionTransformer.getName(), functionTransformer.getMethod());
    }

    return factory;
  }

  protected Cache<TransformExpressionCacheKey, String> createTransformExpressionCache() {
    return new ConcurrentLruCache<TransformExpressionCacheKey, String>(expressionCacheSize);
  }

}

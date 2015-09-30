/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionTableListener;
import org.camunda.bpm.dmn.engine.DmnEngine;
import org.camunda.bpm.dmn.engine.DmnEngineConfiguration;
import org.camunda.bpm.dmn.engine.DmnEngineMetricCollector;
import org.camunda.bpm.dmn.engine.DmnScriptEngineResolver;
import org.camunda.bpm.dmn.engine.context.DmnContextFactory;
import org.camunda.bpm.dmn.engine.handler.DmnElementHandlerRegistry;
import org.camunda.bpm.dmn.engine.hitpolicy.DmnHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.context.DmnContextFactoryImpl;
import org.camunda.bpm.dmn.engine.impl.handler.DmnElementHandlerRegistryImpl;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.AnyHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.CollectHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.FirstHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.OutputOrderHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.PriorityHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.RuleOrderHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.hitpolicy.UniqueHitPolicyHandler;
import org.camunda.bpm.dmn.engine.impl.transform.DmnTransformFactoryImpl;
import org.camunda.bpm.dmn.engine.impl.transform.DmnTransformerImpl;
import org.camunda.bpm.dmn.engine.impl.type.DefaultDataTypeTransformerFactory;
import org.camunda.bpm.dmn.engine.transform.DmnTransformFactory;
import org.camunda.bpm.dmn.engine.transform.DmnTransformListener;
import org.camunda.bpm.dmn.engine.transform.DmnTransformer;
import org.camunda.bpm.dmn.engine.type.DataTypeTransformerFactory;
import org.camunda.bpm.dmn.feel.FeelEngineProvider;
import org.camunda.bpm.dmn.feel.impl.FeelEngineProviderImpl;
import org.camunda.bpm.model.dmn.HitPolicy;
import org.camunda.bpm.model.dmn.impl.DmnModelConstants;

public class DmnEngineConfigurationImpl implements DmnEngineConfiguration {

  public static final String FEEL_EXPRESSION_LANGUAGE = DmnModelConstants.FEEL_NS;
  public static final String JUEL_EXPRESSION_LANGUAGE = "juel";

  protected DmnContextFactory contextFactory;

  protected DmnTransformer transformer;
  protected DmnTransformFactory transformFactory;
  protected DmnElementHandlerRegistry elementHandlerRegistry;
  protected DmnEngineMetricCollector engineMetricCollector;

  protected List<DmnTransformListener> customPreDmnTransformListeners = new ArrayList<DmnTransformListener>();
  protected List<DmnTransformListener> customPostDmnTransformListeners = new ArrayList<DmnTransformListener>();
  protected List<DmnDecisionTableListener> customPreDmnDecisionTableListeners = new ArrayList<DmnDecisionTableListener>();
  protected List<DmnDecisionTableListener> customDmnDecisionTableListeners = new ArrayList<DmnDecisionTableListener>();
  protected List<DmnDecisionTableListener> customPostDmnDecisionTableListeners = new ArrayList<DmnDecisionTableListener>();

  protected Map<HitPolicy, DmnHitPolicyHandler> hitPolicyHandlers;
  protected DmnScriptEngineResolver scriptEngineResolver;
  protected FeelEngineProvider feelEngineProvider;
  protected DataTypeTransformerFactory dataTypeTransformerFactory;

  protected String defaultAllowedValueExpressionLanguage = JUEL_EXPRESSION_LANGUAGE;
  protected String defaultInputEntryExpressionLanguage = FEEL_EXPRESSION_LANGUAGE;
  protected String defaultInputExpressionExpressionLanguage = JUEL_EXPRESSION_LANGUAGE;
  protected String defaultOutputEntryExpressionLanguage = JUEL_EXPRESSION_LANGUAGE;

  public DmnContextFactory getDmnContextFactory() {
    return contextFactory;
  }

  public void setDmnContextFactory(DmnContextFactory contextFactory) {
    this.contextFactory = contextFactory;
  }

  public DmnTransformer getTransformer() {
    return transformer;
  }

  public DmnTransformFactory getTransformFactory() {
    return transformFactory;
  }

  public void setTransformFactory(DmnTransformFactory transformFactory) {
    this.transformFactory = transformFactory;
  }

  public DmnElementHandlerRegistry getElementHandlerRegistry() {
    return elementHandlerRegistry;
  }

  public void setElementHandlerRegistry(DmnElementHandlerRegistry elementHandlerRegistry) {
    this.elementHandlerRegistry = elementHandlerRegistry;
  }

  public DmnEngineMetricCollector getEngineMetricCollector() {
    return engineMetricCollector;
  }

  public void setEngineMetricCollector(DmnEngineMetricCollector engineMetricCollector) {
    this.engineMetricCollector = engineMetricCollector;
  }

  public List<DmnTransformListener> getCustomPreDmnTransformListeners() {
    return customPreDmnTransformListeners;
  }

  public void setCustomPreDmnTransformListeners(List<DmnTransformListener> customPreDmnTransformListeners) {
    this.customPreDmnTransformListeners = customPreDmnTransformListeners;
  }

  public List<DmnTransformListener> getCustomPostDmnTransformListeners() {
    return customPostDmnTransformListeners;
  }

  public void setCustomPostDmnTransformListeners(List<DmnTransformListener> customPostDmnTransformListeners) {
    this.customPostDmnTransformListeners = customPostDmnTransformListeners;
  }

  public List<DmnDecisionTableListener> getCustomPreDmnDecisionTableListeners() {
    return customPreDmnDecisionTableListeners;
  }

  public List<DmnDecisionTableListener> getCustomDmnDecisionTableListeners() {
    return customDmnDecisionTableListeners;
  }

  public void setCustomPreDmnDecisionTableListeners(List<DmnDecisionTableListener> customPreDmnDecisionTableListeners) {
    this.customPreDmnDecisionTableListeners = customPreDmnDecisionTableListeners;
  }

  public void setCustomDmnDecisionTableListeners(List<DmnDecisionTableListener> customDmnDecisionTableListeners) {
    this.customDmnDecisionTableListeners = customDmnDecisionTableListeners;
  }

  public List<DmnDecisionTableListener> getCustomPostDmnDecisionTableListeners() {
    return customPostDmnDecisionTableListeners;
  }

  public void setCustomPostDmnDecisionTableListeners(List<DmnDecisionTableListener> customPostDmnDecisionTableListeners) {
    this.customPostDmnDecisionTableListeners = customPostDmnDecisionTableListeners;
  }

  public Map<HitPolicy, DmnHitPolicyHandler> getHitPolicyHandlers() {
    return hitPolicyHandlers;
  }

  public void setHitPolicyHandlers(Map<HitPolicy, DmnHitPolicyHandler> hitPolicyHandlers) {
    this.hitPolicyHandlers = hitPolicyHandlers;
  }

  public DmnScriptEngineResolver getScriptEngineResolver() {
    return scriptEngineResolver;
  }

  public void setScriptEngineResolver(DmnScriptEngineResolver scriptEngineResolver) {
    this.scriptEngineResolver = scriptEngineResolver;
  }

  public FeelEngineProvider getFeelEngineProvider() {
    return feelEngineProvider;
  }

  public void setFeelEngineProvider(FeelEngineProvider feelEngineProvider) {
    this.feelEngineProvider = feelEngineProvider;
  }

  public DataTypeTransformerFactory getDataTypeTransformerFactory() {
    return dataTypeTransformerFactory;
  }

  public void setDataTypeTransformerFactory(DataTypeTransformerFactory dataTypeTransformerFactory) {
    this.dataTypeTransformerFactory = dataTypeTransformerFactory;
  }

  public String getDefaultAllowedValueExpressionLanguage() {
    return defaultAllowedValueExpressionLanguage;
  }

  public void setDefaultAllowedValueExpressionLanguage(String defaultAllowedValueExpressionLanguage) {
    this.defaultAllowedValueExpressionLanguage = defaultAllowedValueExpressionLanguage;
  }

  public String getDefaultInputEntryExpressionLanguage() {
    return defaultInputEntryExpressionLanguage;
  }

  public void setDefaultInputEntryExpressionLanguage(String defaultInputEntryExpressionLanguage) {
    this.defaultInputEntryExpressionLanguage = defaultInputEntryExpressionLanguage;
  }

  public String getDefaultInputExpressionExpressionLanguage() {
    return defaultInputExpressionExpressionLanguage;
  }

  public void setDefaultInputExpressionExpressionLanguage(String defaultInputExpressionExpressionLanguage) {
    this.defaultInputExpressionExpressionLanguage = defaultInputExpressionExpressionLanguage;
  }

  public String getDefaultOutputEntryExpressionLanguage() {
    return defaultOutputEntryExpressionLanguage;
  }

  public void setDefaultOutputEntryExpressionLanguage(String defaultOutputEntryExpressionLanguage) {
    this.defaultOutputEntryExpressionLanguage = defaultOutputEntryExpressionLanguage;
  }

  public DmnEngine buildEngine() {
    init();
    return new DmnEngineImpl(this);
  }

  protected void init() {
    initContextFactory();
    initTransformFactory();
    initElementHandlerRegistry();
    initMetricCollector();
    initDataTypeTransformerFactory();
    initTransformer();
    initDmnDecisionTableListeners();
    initHitPolicyHandlers();
    initScriptEngineResolver();
    initFeelEngineProvider();
  }

  protected void initContextFactory() {
    if (contextFactory == null) {
      contextFactory = new DmnContextFactoryImpl();
    }
  }

  public void initTransformFactory() {
    if (transformFactory == null) {
      transformFactory = new DmnTransformFactoryImpl();
    }
  }

  protected void initElementHandlerRegistry() {
    if (elementHandlerRegistry == null) {
      elementHandlerRegistry = new DmnElementHandlerRegistryImpl();
    }
  }

  protected void initMetricCollector() {
    if (engineMetricCollector == null) {
      engineMetricCollector = new DmnEngineMetricCollectorImpl();
    }
  }

  protected void initTransformer() {
    transformer = new DmnTransformerImpl(transformFactory, elementHandlerRegistry, dataTypeTransformerFactory);
    if (customPreDmnTransformListeners != null) {
      transformer.getTransformListeners().addAll(customPreDmnTransformListeners);
    }
    transformer.getTransformListeners().addAll(getDefaultDmnTransformListeners());
    if (customPostDmnTransformListeners != null) {
      transformer.getTransformListeners().addAll(customPostDmnTransformListeners);
    }
  }

  protected List<DmnTransformListener> getDefaultDmnTransformListeners() {
    return Collections.emptyList();
  }

  protected void initDmnDecisionTableListeners() {
    List<DmnDecisionTableListener> listeners = new ArrayList<DmnDecisionTableListener>();
    if (customPreDmnDecisionTableListeners != null) {
      listeners.addAll(customPreDmnDecisionTableListeners);
    }
    listeners.addAll(getDefaultDmnDecisionTableListeners());
    if (customPostDmnDecisionTableListeners != null) {
      listeners.addAll(customPostDmnDecisionTableListeners);
    }
    setCustomDmnDecisionTableListeners(listeners);
  }

  protected List<DmnDecisionTableListener> getDefaultDmnDecisionTableListeners() {
    List<DmnDecisionTableListener> defaultListeners = new ArrayList<DmnDecisionTableListener>();
    defaultListeners.add(engineMetricCollector);
    return defaultListeners;
  }

  protected void initHitPolicyHandlers() {
    if (hitPolicyHandlers == null) {
      hitPolicyHandlers = getDefaultHitPolicyHandlers();
    }
  }

  protected Map<HitPolicy, DmnHitPolicyHandler> getDefaultHitPolicyHandlers() {
    Map<HitPolicy, DmnHitPolicyHandler> hitPolicyHandlers = new HashMap<HitPolicy, DmnHitPolicyHandler>();
    hitPolicyHandlers.put(HitPolicy.UNIQUE, new UniqueHitPolicyHandler());
    hitPolicyHandlers.put(HitPolicy.ANY, new AnyHitPolicyHandler());
    hitPolicyHandlers.put(HitPolicy.PRIORITY, new PriorityHitPolicyHandler());
    hitPolicyHandlers.put(HitPolicy.FIRST, new FirstHitPolicyHandler());
    hitPolicyHandlers.put(HitPolicy.OUTPUT_ORDER, new OutputOrderHitPolicyHandler());
    hitPolicyHandlers.put(HitPolicy.RULE_ORDER, new RuleOrderHitPolicyHandler());
    hitPolicyHandlers.put(HitPolicy.COLLECT, new CollectHitPolicyHandler());
    return hitPolicyHandlers;
  }

  protected void initScriptEngineResolver() {
    if (scriptEngineResolver == null) {
      scriptEngineResolver = new DefaultScriptEngineResolver();
    }
  }

  protected void initFeelEngineProvider() {
    if (feelEngineProvider == null) {
      feelEngineProvider = new FeelEngineProviderImpl();
    }
  }

  protected void initDataTypeTransformerFactory() {
    if (dataTypeTransformerFactory == null) {
      dataTypeTransformerFactory = new DefaultDataTypeTransformerFactory();
    }
  }

}

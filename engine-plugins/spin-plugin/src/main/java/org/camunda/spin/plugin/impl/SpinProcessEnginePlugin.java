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
package org.camunda.spin.plugin.impl;

import static org.camunda.spin.plugin.variable.type.SpinValueType.JSON;
import static org.camunda.spin.plugin.variable.type.SpinValueType.XML;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.el.ExpressionManager;
import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.camunda.bpm.engine.impl.variable.serializer.JavaObjectSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;
import org.camunda.bpm.engine.variable.type.ValueTypeResolver;
import org.camunda.spin.DataFormats;
import org.camunda.spin.Spin;

/**
 * @author Thorben Lindhauer
 *
 */
public class SpinProcessEnginePlugin extends SpinConfiguration {

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    // use classloader which loaded the plugin
    ClassLoader classloader = ClassLoaderUtil.getClassloader(SpinProcessEnginePlugin.class);

    // use Spin plugin configuration properties
    Map<String, Object> configurationOptions = new HashMap<>();
    configurationOptions.put(XXE_PROPERTY, isEnableXxeProcessing());
    configurationOptions.put(SP_PROPERTY, isEnableSecureXmlProcessing());

    DataFormats.loadDataFormats(classloader, configurationOptions);
  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    registerFunctionMapper(processEngineConfiguration);
    registerScriptResolver(processEngineConfiguration);
    registerSerializers(processEngineConfiguration);
    registerValueTypes(processEngineConfiguration);
    registerFallbackSerializer(processEngineConfiguration);
  }

  protected void registerFallbackSerializer(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.setFallbackSerializerFactory(new SpinFallbackSerializerFactory());
  }

  protected void registerSerializers(ProcessEngineConfigurationImpl processEngineConfiguration) {

    List<TypedValueSerializer<?>> spinDataFormatSerializers = lookupSpinSerializers();

    VariableSerializers variableSerializers = processEngineConfiguration.getVariableSerializers();

    int javaObjectSerializerIdx = variableSerializers.getSerializerIndexByName(JavaObjectSerializer.NAME);

    for (TypedValueSerializer<?> spinSerializer : spinDataFormatSerializers) {
      // add before java object serializer
      variableSerializers.addSerializer(spinSerializer, javaObjectSerializerIdx);
    }
  }

  protected List<TypedValueSerializer<?>> lookupSpinSerializers() {
    DataFormats globalFormats = DataFormats.getInstance();
    List<TypedValueSerializer<?>> serializers =
        SpinVariableSerializers.createObjectValueSerializers(globalFormats);
    serializers.addAll(SpinVariableSerializers.createSpinValueSerializers(globalFormats));

    return serializers;
  }

  protected void registerScriptResolver(ProcessEngineConfigurationImpl processEngineConfiguration) {
    processEngineConfiguration.getEnvScriptResolvers().add(new SpinScriptEnvResolver());
  }

  protected void registerFunctionMapper(ProcessEngineConfigurationImpl processEngineConfiguration) {
    ExpressionManager expressionManager = processEngineConfiguration.getExpressionManager();
    expressionManager.addFunction(SpinFunctions.S, 
        ReflectUtil.getMethod(Spin.class, SpinFunctions.S, Object.class));
    expressionManager.addFunction(SpinFunctions.XML,
        ReflectUtil.getMethod(Spin.class, SpinFunctions.XML, Object.class));
    expressionManager.addFunction(SpinFunctions.JSON,
        ReflectUtil.getMethod(Spin.class, SpinFunctions.JSON, Object.class));
  }

  protected void registerValueTypes(ProcessEngineConfigurationImpl processEngineConfiguration){
    ValueTypeResolver resolver = processEngineConfiguration.getValueTypeResolver();
    resolver.addType(JSON);
    resolver.addType(XML);
  }

}

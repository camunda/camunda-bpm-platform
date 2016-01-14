/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.List;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ClassLoaderUtil;
import org.camunda.bpm.engine.impl.variable.serializer.JavaObjectSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.TypedValueSerializer;
import org.camunda.bpm.engine.impl.variable.serializer.VariableSerializers;
import org.camunda.bpm.engine.variable.type.ValueTypeResolver;
import org.camunda.spin.DataFormats;

/**
 * @author Thorben Lindhauer
 *
 */
public class SpinProcessEnginePlugin extends AbstractProcessEnginePlugin {

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    // use classloader which loaded the plugin
    ClassLoader classloader = ClassLoaderUtil.getClassloader(SpinProcessEnginePlugin.class);
    DataFormats.loadDataFormats(classloader);
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
    processEngineConfiguration.getExpressionManager().addFunctionMapper(new SpinFunctionMapper());
  }

  protected void registerValueTypes(ProcessEngineConfigurationImpl processEngineConfiguration){
    ValueTypeResolver resolver = processEngineConfiguration.getValueTypeResolver();
    resolver.addType(JSON);
    resolver.addType(XML);
  }

}

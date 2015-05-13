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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.impl.variable.serializer.DefaultVariableSerializers;
import org.camunda.spin.DataFormats;
import org.camunda.spin.plugin.variable.type.JsonValueType;
import org.camunda.spin.plugin.variable.type.XmlValueType;
import org.mockito.Mockito;

/**
 * @author Ronny Bräunlich
 *
 */
public class SpinProcessEnginePluginTest extends PluggableProcessEngineTestCase {

  public void testPluginDoesNotRegisterXmlSerializerIfNotPresentInClasspath() throws IOException {
    ClassLoader mockClassloader = Mockito.mock(ClassLoader.class);
    Mockito.when(mockClassloader.getResources(Mockito.anyString())).thenReturn(Collections.enumeration(Collections.<URL>emptyList()));
    DataFormats.loadDataFormats(mockClassloader);
    ProcessEngineConfigurationImpl mockConfig = Mockito.mock(ProcessEngineConfigurationImpl.class);
    DefaultVariableSerializers serializers = new DefaultVariableSerializers();
    Mockito.when(mockConfig.getVariableSerializers()).thenReturn(serializers);
    new SpinProcessEnginePlugin().registerSerializers(mockConfig);

    assertTrue(serializers.getSerializerByName(XmlValueType.TYPE_NAME) == null);
  }

  public void testPluginDoesNotRegisterJsonSerializerIfNotPresentInClasspath() throws IOException {
    ClassLoader mockClassloader = Mockito.mock(ClassLoader.class);
    Mockito.when(mockClassloader.getResources(Mockito.anyString())).thenReturn(Collections.enumeration(Collections.<URL>emptyList()));
    DataFormats.loadDataFormats(mockClassloader);
    ProcessEngineConfigurationImpl mockConfig = Mockito.mock(ProcessEngineConfigurationImpl.class);
    DefaultVariableSerializers serializers = new DefaultVariableSerializers();
    Mockito.when(mockConfig.getVariableSerializers()).thenReturn(serializers);
    new SpinProcessEnginePlugin().registerSerializers(mockConfig);

    assertTrue(serializers.getSerializerByName(JsonValueType.TYPE_NAME) == null);
  }

  public void testPluginRegistersXmlSerializerIfPresentInClasspath(){
    DataFormats.loadDataFormats(null);
    ProcessEngineConfigurationImpl mockConfig = Mockito.mock(ProcessEngineConfigurationImpl.class);
    Mockito.when(mockConfig.getVariableSerializers()).thenReturn(processEngineConfiguration.getVariableSerializers());
    new SpinProcessEnginePlugin().registerSerializers(mockConfig);

    assertTrue(processEngineConfiguration.getVariableSerializers().getSerializerByName(XmlValueType.TYPE_NAME) instanceof XmlValueSerializer);
  }

  public void testPluginRegistersJsonSerializerIfPresentInClasspath(){
    DataFormats.loadDataFormats(null);
    ProcessEngineConfigurationImpl mockConfig = Mockito.mock(ProcessEngineConfigurationImpl.class);
    Mockito.when(mockConfig.getVariableSerializers()).thenReturn(processEngineConfiguration.getVariableSerializers());
    new SpinProcessEnginePlugin().registerSerializers(mockConfig);

    assertTrue(processEngineConfiguration.getVariableSerializers().getSerializerByName(JsonValueType.TYPE_NAME) instanceof JsonValueSerializer);
  }
}

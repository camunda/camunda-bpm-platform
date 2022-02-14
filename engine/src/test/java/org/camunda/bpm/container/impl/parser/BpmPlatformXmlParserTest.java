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
package org.camunda.bpm.container.impl.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.container.impl.metadata.BpmPlatformXmlParser;
import org.camunda.bpm.container.impl.metadata.spi.BpmPlatformXml;
import org.camunda.bpm.container.impl.metadata.spi.JobAcquisitionXml;
import org.camunda.bpm.container.impl.metadata.spi.JobExecutorXml;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEnginePluginXml;
import org.camunda.bpm.container.impl.metadata.spi.ProcessEngineXml;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>The testcases for the {@link BpmPlatformXmlParser}</p>
 *
 * @author Daniel Meyer
 *
 */
public class BpmPlatformXmlParserTest {

  private BpmPlatformXmlParser parser;

  @Before
  public void setUp() throws Exception {
    parser = new BpmPlatformXmlParser();
  }

  protected URL getStreamUrl(String filename) {
    return BpmPlatformXmlParserTest.class.getResource(filename);
  }

  @Test
  public void testParseBpmPlatformXmlNoEngine() {

    BpmPlatformXml bpmPlatformXml = parser.createParse()
      .sourceUrl(getStreamUrl("bpmplatform_xml_no_engine.xml"))
      .execute()
      .getBpmPlatformXml();

    assertNotNull(bpmPlatformXml);
    assertNotNull(bpmPlatformXml.getJobExecutor());
    assertEquals(0, bpmPlatformXml.getProcessEngines().size());

    JobExecutorXml jobExecutorXml = bpmPlatformXml.getJobExecutor();
    assertEquals(1, jobExecutorXml.getJobAcquisitions().size());

    JobAcquisitionXml jobAcquisitionXml = jobExecutorXml.getJobAcquisitions().get(0);
    assertEquals("default", jobAcquisitionXml.getName());
    assertEquals("org.camunda.bpm.engine.impl.jobexecutor.DefaultJobExecutor", jobAcquisitionXml.getJobExecutorClassName());

    assertEquals(2, jobAcquisitionXml.getProperties().size());

  }

  @Test
  public void testParseBpmPlatformXmlOneEngine() {

    BpmPlatformXml bpmPlatformXml = parser.createParse()
      .sourceUrl(getStreamUrl("bpmplatform_xml_one_engine.xml"))
      .execute()
      .getBpmPlatformXml();

    assertNotNull(bpmPlatformXml);
    assertNotNull(bpmPlatformXml.getJobExecutor());
    assertEquals(1, bpmPlatformXml.getProcessEngines().size());

    JobExecutorXml jobExecutorXml = bpmPlatformXml.getJobExecutor();
    assertEquals(1, jobExecutorXml.getJobAcquisitions().size());
    assertThat(jobExecutorXml.getProperties().size()).isEqualTo(2);

    JobAcquisitionXml jobAcquisitionXml = jobExecutorXml.getJobAcquisitions().get(0);
    assertEquals("default", jobAcquisitionXml.getName());
    assertEquals("org.camunda.bpm.engine.impl.jobexecutor.DefaultJobExecutor", jobAcquisitionXml.getJobExecutorClassName());

    assertEquals(2, jobAcquisitionXml.getProperties().size());

    ProcessEngineXml engineXml = bpmPlatformXml.getProcessEngines().get(0);
    assertEquals("engine1", engineXml.getName());
    assertEquals("default", engineXml.getJobAcquisitionName());

    Map<String, String> properties = engineXml.getProperties();
    assertNotNull(properties);
    assertEquals(0, properties.size());

    List<ProcessEnginePluginXml> plugins = engineXml.getPlugins();
    assertNotNull(plugins);
    assertEquals(0, plugins.size());

  }

  @Test
  public void testParseBpmPlatformXmlEnginePlugin() {

    BpmPlatformXml bpmPlatformXml = parser.createParse()
      .sourceUrl(getStreamUrl("bpmplatform_xml_engine_plugin.xml"))
      .execute()
      .getBpmPlatformXml();

    assertNotNull(bpmPlatformXml);
    assertEquals(1, bpmPlatformXml.getProcessEngines().size());

    ProcessEngineXml engineXml = bpmPlatformXml.getProcessEngines().get(0);
    assertEquals("engine1", engineXml.getName());
    assertEquals("default", engineXml.getJobAcquisitionName());

    List<ProcessEnginePluginXml> plugins = engineXml.getPlugins();
    assertEquals(1, plugins.size());

    ProcessEnginePluginXml plugin1 = plugins.get(0);
    assertNotNull(plugin1);

    assertEquals("org.camunda.bpm.MyAwesomePlugin", plugin1.getPluginClass());

    Map<String, String> properties = plugin1.getProperties();
    assertNotNull(properties);
    assertEquals(2, properties.size());

    String val1 = properties.get("prop1");
    assertNotNull(val1);
    assertEquals("val1", val1);

    String val2 = properties.get("prop2");
    assertNotNull(val2);
    assertEquals("val2", val2);

  }

  @Test
  public void testParseBpmPlatformXmlMultipleEnginePlugins() {

    BpmPlatformXml bpmPlatformXml = parser.createParse()
      .sourceUrl(getStreamUrl("bpmplatform_xml_multiple_engine_plugins.xml"))
      .execute()
      .getBpmPlatformXml();

    assertNotNull(bpmPlatformXml);
    assertEquals(1, bpmPlatformXml.getProcessEngines().size());

    ProcessEngineXml engineXml = bpmPlatformXml.getProcessEngines().get(0);
    assertEquals("engine1", engineXml.getName());
    assertEquals("default", engineXml.getJobAcquisitionName());

    List<ProcessEnginePluginXml> plugins = engineXml.getPlugins();
    assertEquals(2, plugins.size());

  }

  @Test
  public void testParseProcessesXmlAntStyleProperties() {

    BpmPlatformXml platformXml = parser.createParse()
        .sourceUrl(getStreamUrl("bpmplatform_xml_ant_style_properties.xml"))
        .execute()
        .getBpmPlatformXml();

    assertNotNull(platformXml);

    ProcessEngineXml engineXml = platformXml.getProcessEngines().get(0);

    assertEquals(1, engineXml.getPlugins().size());
    ProcessEnginePluginXml pluginXml = engineXml.getPlugins().get(0);

    Map<String, String> properties = pluginXml.getProperties();
    assertEquals(2, properties.size());

    // these two system properties are guaranteed to be set
    assertEquals(System.getProperty("java.version"), properties.get("prop1"));
    assertEquals("prefix-" + System.getProperty("os.name"), properties.get("prop2"));
  }

}

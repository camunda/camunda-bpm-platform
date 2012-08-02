/**
 * Copyright (C) 2011, 2012 camunda services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.camunda.fox.client.impl.parser;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.camunda.fox.client.impl.parser.DefaultProcessesXmlParser;
import com.camunda.fox.client.impl.parser.spi.ProcessesXmlParser;
import com.camunda.fox.client.impl.schema.ProcessesXml;

/**
 * 
 * @author Daniel Meyer
 */
public class TestDefaultProcessesXmlParser {
  
  private ProcessesXmlParser parser;

  @Before
  public void setup() {
    parser = new DefaultProcessesXmlParser();
  }
  
  @Test
  public void testParseEmptyProcessesXml() {
    ProcessesXml processesXml = parser.parseProcessesXml("com/camunda/fox/processarchive/parser/emptyProcesses.xml").get(0);
    Assert.assertNotNull(processesXml);
    Assert.assertNotNull(processesXml.processArchives);
    Assert.assertEquals(1, processesXml.processArchives.size());
  }
  
  @Test
  public void testSingleProcessArchive() {
    ProcessesXml processesXml = parser.parseProcessesXml("com/camunda/fox/processarchive/parser/singleProcessArchive.xml").get(0);
    Assert.assertNotNull(processesXml);
    Assert.assertNotNull(processesXml.processArchives);
    Assert.assertEquals(1, processesXml.processArchives.size());
    Assert.assertEquals("ProcessArchive1", processesXml.processArchives.get(0).name);
  }
  
  @Test
  public void testConfiguration() {
    ProcessesXml processesXml = parser.parseProcessesXml("com/camunda/fox/processarchive/parser/configuration.xml").get(0);
    Assert.assertNotNull(processesXml);
    Assert.assertNotNull(processesXml.processArchives);
    Assert.assertEquals(1, processesXml.processArchives.size());
    Assert.assertEquals("ProcessArchive1", processesXml.processArchives.get(0).name);
    Assert.assertNotNull(processesXml.processArchives.get(0).configuration);
  }
  
  @Test
  public void testListedProcesses() {
    ProcessesXml processesXml = parser.parseProcessesXml("com/camunda/fox/processarchive/parser/processesListed.xml").get(0);
    Assert.assertNotNull(processesXml);
    Assert.assertNotNull(processesXml.processArchives);
    Assert.assertEquals(1, processesXml.processArchives.size());
    Assert.assertEquals(2, processesXml.processArchives.get(0).processes.size());
    Assert.assertEquals("test1.bpmn", processesXml.processArchives.get(0).processes.get(0).resourceName);
    Assert.assertEquals("test2.bpmn", processesXml.processArchives.get(0).processes.get(1).resourceName);
  }
  
  @Test
  public void testMultipleProcessArchvies() {
    ProcessesXml processesXml = parser.parseProcessesXml("com/camunda/fox/processarchive/parser/multipleProcessArchives.xml").get(0);
    Assert.assertNotNull(processesXml);
    Assert.assertNotNull(processesXml.processArchives);
    Assert.assertEquals(2, processesXml.processArchives.size());    
  }
  
  @Test
  public void testDeprecatedSyntax() {
    ProcessesXml processesXml = parser.parseProcessesXml("com/camunda/fox/processarchive/parser/deprecated.xml").get(0);
    Assert.assertNotNull(processesXml);
    Assert.assertNotNull(processesXml.processArchives);
    Assert.assertEquals(1, processesXml.processArchives.size());    
  }

}

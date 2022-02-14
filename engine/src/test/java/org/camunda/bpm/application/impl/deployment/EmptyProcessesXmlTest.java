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
package org.camunda.bpm.application.impl.deployment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;

import org.camunda.bpm.application.impl.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.metadata.spi.ProcessesXml;
import org.camunda.bpm.engine.repository.ResumePreviousBy;
import org.junit.Test;

/**
 * <p>Testcase verifying the default properties in the empty processes.xml</p>
 *
 * @author Daniel Meyer
 *
 */
public class EmptyProcessesXmlTest {

  @Test
  public void testDefaultValues() {

    ProcessesXml emptyProcessesXml = ProcessesXml.EMPTY_PROCESSES_XML;
    assertNotNull(emptyProcessesXml);

    assertNotNull(emptyProcessesXml.getProcessEngines());
    assertEquals(0, emptyProcessesXml.getProcessEngines().size());

    assertNotNull(emptyProcessesXml.getProcessArchives());
    assertEquals(1, emptyProcessesXml.getProcessArchives().size());

    ProcessArchiveXml processArchiveXml = emptyProcessesXml.getProcessArchives().get(0);

    assertNull(processArchiveXml.getName());
    assertNull(processArchiveXml.getProcessEngineName());

    assertNotNull(processArchiveXml.getProcessResourceNames());
    assertEquals(0, processArchiveXml.getProcessResourceNames().size());

    Map<String, String> properties = processArchiveXml.getProperties();

    assertNotNull(properties);
    assertEquals(4, properties.size());

    String isDeleteUponUndeploy = properties.get(ProcessArchiveXml.PROP_IS_DELETE_UPON_UNDEPLOY);
    assertNotNull(isDeleteUponUndeploy);
    assertEquals(Boolean.FALSE.toString(), isDeleteUponUndeploy);

    String isScanForProcessDefinitions = properties.get(ProcessArchiveXml.PROP_IS_SCAN_FOR_PROCESS_DEFINITIONS);
    assertNotNull(isScanForProcessDefinitions);
    assertEquals(Boolean.TRUE.toString(), isScanForProcessDefinitions);

    String isDeployChangedOnly = properties.get(ProcessArchiveXml.PROP_IS_DEPLOY_CHANGED_ONLY);
    assertNotNull(isDeployChangedOnly);
    assertEquals(Boolean.FALSE.toString(), isDeployChangedOnly);

    String resumePreviousBy = properties.get(ProcessArchiveXml.PROP_RESUME_PREVIOUS_BY);
    assertThat(resumePreviousBy).isNotNull();
    assertThat(resumePreviousBy).isSameAs(ResumePreviousBy.RESUME_BY_PROCESS_DEFINITION_KEY);
  }

}

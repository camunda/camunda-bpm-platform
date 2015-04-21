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
package org.camunda.bpm.application.impl.deployment;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.camunda.bpm.application.impl.metadata.spi.ProcessArchiveXml;
import org.camunda.bpm.application.impl.metadata.spi.ProcessesXml;
import org.camunda.bpm.engine.repository.ResumePreviousBy;

import junit.framework.TestCase;

/**
 * <p>Testcase verifying the default properties in the empty processes.xml</p>
 *
 * @author Daniel Meyer
 *
 */
public class EmptyProcessesXmlTest extends TestCase {

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
    assertThat(resumePreviousBy, is(notNullValue()));
    assertThat(resumePreviousBy, is(ResumePreviousBy.RESUME_BY_PROCESS_DEFINITION_KEY));
  }

}

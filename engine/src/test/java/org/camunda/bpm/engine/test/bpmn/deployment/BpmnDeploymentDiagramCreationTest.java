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
package org.camunda.bpm.engine.test.bpmn.deployment;

import java.io.IOException;
import java.io.InputStream;

import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;

/**
 * @author Thorben Lindhauer
 *
 */
public class BpmnDeploymentDiagramCreationTest extends ResourceProcessEngineTestCase {

  public BpmnDeploymentDiagramCreationTest() {
    super("org/camunda/bpm/engine/test/bpmn/deployment/deploy.diagram.camunda.cfg.xml");
  }

  /**
   * Just assures that diagram creation actually creates something and does not crash.
   * No qualitative evaluation of created diagram.
   * @throws IOException
   */
  public void testProcessDiagramCreation() throws IOException {
    String deploymentId = processEngine.getRepositoryService().createDeployment()
      .addClasspathResource("org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramCreation.bpmn20.xml")
      .deploy().getId();

    ProcessDefinition definition = processEngine.getRepositoryService().createProcessDefinitionQuery().singleResult();
    String expectedDiagramName = "org/camunda/bpm/engine/test/bpmn/deployment/BpmnDeploymentTest.testProcessDiagramCreation.processDiagramProcess.png";
    assertEquals(expectedDiagramName, definition.getDiagramResourceName());

    InputStream diagramStream = processEngine.getRepositoryService().getProcessDiagram(definition.getId());
    assertNotNull(diagramStream);
    diagramStream.close();

    // clean db
    processEngine.getRepositoryService().deleteDeployment(deploymentId);
  }
}

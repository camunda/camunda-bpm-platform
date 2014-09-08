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
package org.camunda.bpm.engine.test.bpmn.exclusive;

import org.camunda.bpm.engine.test.Deployment;

/**
 * @author Daniel Meyer
 *
 */
public class ExclusiveTaskReuseCacheTest extends ExclusiveTaskTest {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    processEngineConfiguration.setDbEntityCacheReuseEnabled(true);
  }

  @Override
  protected void tearDown() throws Exception {
    processEngineConfiguration.setDbEntityCacheReuseEnabled(false);
    super.tearDown();
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/exclusive/ExclusiveTaskTest.testNonExclusiveService.bpmn20.xml"})
  public void testNonExclusiveService() {
    super.testNonExclusiveService();
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/exclusive/ExclusiveTaskTest.testExclusiveService.bpmn20.xml"})
  public void testExclusiveService() {
    super.testExclusiveService();
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/exclusive/ExclusiveTaskTest.testExclusiveServiceConcurrent.bpmn20.xml"})
  public void testExclusiveServiceConcurrent() {
    super.testExclusiveServiceConcurrent();
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/exclusive/ExclusiveTaskTest.testExclusiveSequence2.bpmn20.xml"})
  public void testExclusiveSequence2() {
    super.testExclusiveSequence2();
  }

  @Deployment(resources={"org/camunda/bpm/engine/test/bpmn/exclusive/ExclusiveTaskTest.testExclusiveSequence3.bpmn20.xml"})
  public void testExclusiveSequence3() {
    super.testExclusiveSequence3();
  }

}

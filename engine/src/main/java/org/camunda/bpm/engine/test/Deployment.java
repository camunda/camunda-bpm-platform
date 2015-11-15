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

package org.camunda.bpm.engine.test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for a test method or class to create and delete a deployment around a test method.
 *
 * <p>Usage - Example 1 (method-level annotation):</p>
 * <pre>
 * package org.example;
 *
 * ...
 *
 * public class ExampleTest {
 *
 *   &#64;Deployment
 *   public void testForADeploymentWithASingleResource() {
 *     // a deployment will be available in the engine repository
 *     // containing the single resource <b>org/example/ExampleTest.testForADeploymentWithASingleResource.bpmn20.xml</b>
 *   }
 *
 *   &#64;Deployment(resources = {
 *     "org/example/processOne.bpmn20.xml",
 *     "org/example/processTwo.bpmn20.xml",
 *     "org/example/some.other.resource" })
 *   public void testForADeploymentWithASingleResource() {
 *     // a deployment will be available in the engine repository
 *     // containing the three resources
 *   }
 * </pre>
 *
 * <p>Usage - Example 2 (class-level annotation):</p>
 * <pre>
 * package org.example;
 *
 * ...
 *
 * &#64;Deployment
 * public class ExampleTest2 {
 *
 *   public void testForADeploymentWithASingleResource() {
 *     // a deployment will be available in the engine repository
 *     // containing the single resource <b>org/example/ExampleTest2.bpmn20.xml</b>
 *   }
 *
 *   &#64;Deployment(resources = "org/example/process.bpmn20.xml")
 *   public void testForADeploymentWithASingleResource() {
 *     // the method-level annotation overrides the class-level annotation
 *   }
 * </pre>
 *
 * @author Dave Syer
 * @author Tom Baeyens
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Deployment {

  /** Specify resources that make up the process definition. */
  public String[] resources() default {};

}

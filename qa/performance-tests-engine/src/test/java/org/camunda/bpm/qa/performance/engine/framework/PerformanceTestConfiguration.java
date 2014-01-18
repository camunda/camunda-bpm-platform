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
package org.camunda.bpm.qa.performance.engine.framework;

import java.util.Properties;


/**
 * Configuration of a performance test
 *
 * @author Daniel Meyer
 *
 */
public class PerformanceTestConfiguration {

  protected int numberOfThreads = 2;
  protected int numberOfRuns = 1000;

  public PerformanceTestConfiguration(Properties properties) {
    numberOfRuns = Integer.parseInt(properties.getProperty("numberOfRuns"));
    numberOfThreads =  Integer.parseInt(properties.getProperty("numberOfThreads"));
  }

  public PerformanceTestConfiguration() {
  }

  public int getNumberOfThreads() {
    return numberOfThreads;
  }
  public void setNumberOfThreads(int numberOfThreads) {
    this.numberOfThreads = numberOfThreads;
  }
  public int getNumberOfRuns() {
    return numberOfRuns;
  }
  public void setNumberOfRuns(int numberOfExecutions) {
    this.numberOfRuns = numberOfExecutions;
  }

}

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

/**
 * @author Daniel Meyer
 *
 */
public class PerformanceTestResults {

  protected String testName;

  // global state
  protected long duration;
  protected long numberOfRuns;
  protected int numberOfThreads;

  // getter / setters ////////////////////////////

  public String getTestName() {
    return testName;
  }
  public void setTestName(String testName) {
    this.testName = testName;
  }
  public long getDuration() {
    return duration;
  }
  public void setDuration(long duration) {
    this.duration = duration;
  }
  public long getNumberOfRuns() {
    return numberOfRuns;
  }
  public void setNumberOfRuns(long numberOfRuns) {
    this.numberOfRuns = numberOfRuns;
  }
  public int getNumberOfThreads() {
    return numberOfThreads;
  }
  public void setNumberOfThreads(int numberOfThreads) {
    this.numberOfThreads = numberOfThreads;
  }

  @Override
  public String toString() {
    return testName + " Completed " + numberOfRuns + " runs using " + numberOfThreads +" threads. Took " + duration + "ms.";
  }

}

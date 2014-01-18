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
package org.camunda.bpm.qa.performance.engine;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.qa.performance.engine.framework.PerformanceTestResults;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * JUnit rule recording the performance test result
 *
 * @author Daniel Meyer
 *
 */
public class PerformanceTestResultRecorderRule extends TestWatcher {

  public static final Logger LOG = Logger.getLogger(PerformanceTestResultRecorderRule.class.getName());

  protected PerformanceTestResults results;

  @Override
  protected void succeeded(Description description) {
    if(results != null) {
      results.setTestName(description.getDisplayName());
      LOG.log(Level.INFO, results.toString());
    }
  }

  public void setResults(PerformanceTestResults results) {
    this.results = results;
  }

}

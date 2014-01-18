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

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.camunda.bpm.engine.impl.util.IoUtil;
import org.camunda.bpm.qa.performance.engine.framework.PerformanceTestConfiguration;
import org.camunda.bpm.qa.performance.engine.framework.PerformanceTestException;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * JUnit rule allowing to load the performance test configuration from a file
 *
 * @author Daniel Meyer
 *
 */
public class PerformanceTestConfigurationRule extends TestWatcher {

  private static final String PROPERTY_FILE_NAME = "PerformaceTestConfiguration.properties";

  static PerformanceTestConfiguration performanceTestConfiguration;

  @Override
  protected void starting(Description description) {
    if(performanceTestConfiguration == null) {

      File file = IoUtil.getFile(PROPERTY_FILE_NAME);
      if(!file.exists()) {
        throw new PerformanceTestException("Cannot load file '"+PROPERTY_FILE_NAME+"': file does not exist.");
      }
      FileInputStream propertyInputStream = null;
      try {
        propertyInputStream = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(propertyInputStream);
        performanceTestConfiguration = new PerformanceTestConfiguration(properties);
      } catch(Exception e) {
        throw new PerformanceTestException("Cannot load properties from file "+PROPERTY_FILE_NAME+": "+e);

      } finally {
        IoUtil.closeSilently(propertyInputStream);
      }
    }
  }

  public PerformanceTestConfiguration getPerformanceTestConfiguration() {
    return performanceTestConfiguration;
  }

}

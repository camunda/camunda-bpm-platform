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
package org.camunda.bpm.engine.impl.metrics;

import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngine;

/**
 * @author Thorben Lindhauer
 *
 */
public class SimpleIpBasedProvider implements MetricsReporterIdProvider {

  private static Logger log = Logger.getLogger(SimpleIpBasedProvider.class.getName());

  public String provideId(ProcessEngine processEngine) {
    String localIp = "";
    try {
      localIp = InetAddress.getLocalHost().getHostAddress();
    } catch (Exception e) {
      // do not throw an exception; failure to determine an IP should not prevent from using the engine
      if (log.isLoggable(Level.WARNING)) {
        log.log(Level.WARNING, "Could not determine local IP address for generating an engine id", e);
      }
    }

    return createId(localIp, processEngine.getName());
  }

  public static final String createId(String ip, String engineName) {
    return ip + "$" + engineName;
  }
}

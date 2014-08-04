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
package org.camunda.spin.spi;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.camunda.spin.impl.util.RewindableInputStream;
import org.camunda.spin.logging.SpinCoreLogger;

/**
 * Can be used as a base class to determine whether an input stream
 * is readable by applying regular expression matching.
 * 
 * @author Lindhauer
 */
public abstract class TextBasedDataFormatReader implements DataFormatReader {

  private static final SpinCoreLogger LOG = SpinCoreLogger.CORE_LOGGER;

  public boolean canRead(RewindableInputStream input) {
    int inputSize = input.getCurrentRewindableCapacity();
    
    byte[] firstBytes = new byte[inputSize];
    
    try {
      input.read(firstBytes, 0, inputSize);
    } catch (IOException e) {
      throw LOG.unableToReadInputStream(e);
    }
    
    
    Pattern pattern = getInputDetectionPattern();

    return pattern.matcher(new String(firstBytes, Charset.forName("UTF-8"))).find();
  }
  
  protected abstract Pattern getInputDetectionPattern();
}

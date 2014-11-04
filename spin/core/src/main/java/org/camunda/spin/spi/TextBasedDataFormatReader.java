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
import java.io.Reader;
import java.util.regex.Pattern;

import org.camunda.spin.impl.logging.SpinCoreLogger;

/**
 * Can be used as a base class to determine whether an input reader
 * is readable by applying regular expression matching.
 *
 * @author Lindhauer
 */
public abstract class TextBasedDataFormatReader implements DataFormatReader {

  private static final SpinCoreLogger LOG = SpinCoreLogger.CORE_LOGGER;

  public boolean canRead(Reader input, int readLimit) {
    char[] firstCharacters = new char[readLimit];

    try {
      input.read(firstCharacters, 0, readLimit);
    } catch (IOException e) {
      throw LOG.unableToReadFromReader(e);
    }


    Pattern pattern = getInputDetectionPattern();

    return pattern.matcher(new String(firstCharacters)).find();
  }

  protected abstract Pattern getInputDetectionPattern();
}

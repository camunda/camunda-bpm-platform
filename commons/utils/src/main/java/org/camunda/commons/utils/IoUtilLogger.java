/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.commons.utils;

import java.io.IOException;

/**
 * @author Sebastian Menski
 */
public class IoUtilLogger extends UtilsLogger {

  public IoUtilException unableToReadInputStream(IOException cause) {
    return new IoUtilException(exceptionMessage("001", "Unable to read input stream"), cause);
  }

  public IoUtilException fileNotFoundException(String filename, Exception cause) {
    return new IoUtilException(exceptionMessage("002", "Unable to find file with path '{}'", filename), cause);
  }

  public IoUtilException fileNotFoundException(String filename) {
    return fileNotFoundException(filename, null);
  }

  public IoUtilException nullParameter(String parameter) {
    return new IoUtilException(exceptionMessage("003", "Parameter '{}' can not be null", parameter));
  }

  public IoUtilException unableToReadFromReader(Throwable cause) {
    return new IoUtilException(exceptionMessage("004", "Unable to read from reader"), cause);
  }

}

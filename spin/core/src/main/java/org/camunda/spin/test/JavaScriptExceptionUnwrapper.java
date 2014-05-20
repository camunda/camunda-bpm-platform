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

package org.camunda.spin.test;

import org.camunda.spin.SpinRuntimeException;
import org.camunda.spin.impl.xml.dom.SpinXmlDomAttributeException;
import org.camunda.spin.impl.xml.dom.SpinXmlDomElementException;

/**
 * @author Sebastian Menski
 * TODO: Remove after the future of javascript is decided
 */
public class JavaScriptExceptionUnwrapper {

  public static SpinRuntimeException unwrap(Throwable cause) {
    if (cause.getMessage().contains("SpinXmlDomElementException")) {
      return new SpinXmlDomElementException(cause.getMessage());
    }
    else if (cause.getMessage().contains("SpinXmlDomAttributeException")) {
      return new SpinXmlDomAttributeException(cause.getMessage());
    }
    else {
      return SpinTestLogger.TEST_LOGGER.unableToUnwrapRhinoJsWrappedException(cause.getMessage());
    }
  }
}

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

import org.camunda.spin.logging.SpinLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Sebastian Menski
 */
public class JavaScriptVariableUnwrapper {

  private final static SpinTestLogger LOG = SpinLogger.TEST_LOGGER;

  private static Method unwrap = null;

  public static Object unwrap(String name, Object variable) {
    if (variable.getClass().getName().equals("sun.org.mozilla.javascript.internal.NativeJavaObject")) {
        if (unwrap == null) {
          try {
            unwrap = variable.getClass().getMethod("unwrap");
          } catch (NoSuchMethodException e) {
            e.printStackTrace();
            throw LOG.unableToFindUnwrapMethod(e);
          }
        }
      try {
        variable = unwrap.invoke(variable);
      } catch (IllegalAccessException e) {
        throw LOG.unableToUnwrapRhinoJsVariable(name, e);
      } catch (InvocationTargetException e) {
        throw LOG.unableToUnwrapRhinoJsVariable(name, e);
      }
    }
    return variable;

  }

}

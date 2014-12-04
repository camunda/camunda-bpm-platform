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
package org.camunda.spin.impl.util;

import org.camunda.spin.impl.logging.SpinCoreLogger;
import org.camunda.spin.spi.DataFormat;

/**
 * @author Daniel Meyer
 *
 */
public class SpinReflectUtil {

  private final static SpinCoreLogger LOG = SpinCoreLogger.CORE_LOGGER;

  /**
   * Used by dataformats if they need to load a class
   *
   * @param classname the name of the
   * @param dataFormat
   * @return
   */
  public static Class<?> loadClass(String classname, DataFormat<?> dataFormat) {

    // first try context classoader
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    if(cl != null) {
      LOG.tryLoadingClass(classname, cl);
      try {
        return cl.loadClass(classname);
      }
      catch(Exception e) {
        // ignore
      }
    }

    // else try the classloader which loaded the dataformat
    cl = dataFormat.getClass().getClassLoader();
    try {
      LOG.tryLoadingClass(classname, cl);
      return cl.loadClass(classname);
    }
    catch (ClassNotFoundException e) {
      throw LOG.classNotFound(classname, e);
    }

  }

}

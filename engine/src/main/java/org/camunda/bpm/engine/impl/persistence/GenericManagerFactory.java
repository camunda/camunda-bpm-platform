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

package org.camunda.bpm.engine.impl.persistence;

import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.db.EnginePersistenceLogger;
import org.camunda.bpm.engine.impl.interceptor.Session;
import org.camunda.bpm.engine.impl.interceptor.SessionFactory;
import org.camunda.bpm.engine.impl.util.ReflectUtil;


/**
 * @author Tom Baeyens
 */
public class GenericManagerFactory implements SessionFactory {

  protected static final EnginePersistenceLogger LOG = ProcessEngineLogger.PERSISTENCE_LOGGER;

  protected Class<? extends Session> managerImplementation;
  
  public GenericManagerFactory(Class< ? extends Session> managerImplementation) {
    this.managerImplementation = managerImplementation;
  }
  
  @SuppressWarnings("unchecked")
  public GenericManagerFactory(String classname) {
    managerImplementation = (Class<? extends Session>) ReflectUtil.loadClass(classname);    
  }

  public Class< ? > getSessionType() {
    return managerImplementation;
  }

  public Session openSession() {
    try {
      return managerImplementation.newInstance();
    } catch (Exception e) {
      throw LOG.instantiateSessionException(managerImplementation.getName(), e);
    }
  }
}

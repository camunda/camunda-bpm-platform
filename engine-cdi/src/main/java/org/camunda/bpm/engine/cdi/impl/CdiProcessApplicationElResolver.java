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
package org.camunda.bpm.engine.cdi.impl;

import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationElResolver;
import org.camunda.bpm.engine.cdi.impl.el.CdiResolver;
import org.camunda.bpm.engine.impl.javax.el.ELResolver;

/**
 * <p>Exposes the CdiResolver in a multiple-applications, shared process engine context.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class CdiProcessApplicationElResolver implements ProcessApplicationElResolver {

  public Integer getPrecedence() {
    return ProcessApplicationElResolver.CDI_RESOLVER;
  }
  
  public ELResolver getElResolver(AbstractProcessApplication processApplication) {
    return new CdiResolver();
  }

}

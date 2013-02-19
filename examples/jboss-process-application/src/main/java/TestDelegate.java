import javax.naming.InitialContext;

import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.context.Context;

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

/**
 * @author meyerd
 *
 */
public class TestDelegate implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    System.out.println("Execution:" +execution.getId());
    System.out.println("Deployment:" +Context.getExecutionContext().getDeployment().getId());
    System.out.println("Application:" +Context.getCurrentProcessApplication());
    
    InitialContext.doLookup("java:module/Starter");
    
  }

}

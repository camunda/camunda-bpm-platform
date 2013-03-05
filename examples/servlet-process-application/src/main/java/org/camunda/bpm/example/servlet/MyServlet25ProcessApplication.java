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
package org.camunda.bpm.example.servlet;

import java.util.concurrent.Callable;

import org.camunda.bpm.application.ProcessApplication;
import org.camunda.bpm.application.ProcessApplicationExecutionException;
import org.camunda.bpm.application.impl.ServletProcessApplication;

/**
 * @author Daniel Meyer
 *
 */
@ProcessApplication("MyApp1")
public class MyServlet25ProcessApplication extends ServletProcessApplication {

  public <T> T execute(Callable<T> callable) throws ProcessApplicationExecutionException {
    
    // TODO Auto-generated method stub
    return super.execute(callable);
  }
  
}

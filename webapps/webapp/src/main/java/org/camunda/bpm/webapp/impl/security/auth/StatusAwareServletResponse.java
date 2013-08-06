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
package org.camunda.bpm.webapp.impl.security.auth;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * <p>Hacking around fact that Servlet 2.5 Response does not expose Status.</p>
 * 
 * @author Daniel Meyer
 *
 */
public class StatusAwareServletResponse extends HttpServletResponseWrapper {
  
  protected int httpStatus;

  public StatusAwareServletResponse(HttpServletResponse response) {
    super(response);
  }
    
  public void sendError(int sc) throws IOException {
      httpStatus = sc;
      super.sendError(sc);
  }

  public void sendError(int sc, String msg) throws IOException {
      httpStatus = sc;
      super.sendError(sc, msg);
  }


  public void setStatus(int sc) {
      httpStatus = sc;
      super.setStatus(sc);
  }

  public int getStatus() {
      return httpStatus;
  }

}

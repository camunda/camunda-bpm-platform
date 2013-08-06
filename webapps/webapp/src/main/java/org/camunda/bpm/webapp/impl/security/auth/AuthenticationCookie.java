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

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.NewCookie;


/**
 * <p>Util class allowing to obtain a Cookie from the current Authentications.</p>
 * 
 * <p>List of authentications is written as JSON object</p>
 * 
 * @author Daniel Meyer
 *
 */
public class AuthenticationCookie {
  
  public static final String AUTH_COOKIE_NAME = "CAM-AUTH";
  
  public static void updateCookie(HttpServletResponse response, HttpSession session) {
    Cookie cookie = new Cookie(AUTH_COOKIE_NAME, getCockieValue(Authentications.getCurrent()));
    cookie.setPath(getCookiePath(session));
    cookie.setMaxAge(Integer.MAX_VALUE);
    response.addCookie(cookie);
  }
  
  public static NewCookie fromAuthentications(Authentications authentications, HttpServletRequest request) {
    
    String cookiePath = getCookiePath(request.getSession());    
    
    String value = getCockieValue(authentications);
        
    return new NewCookie(AUTH_COOKIE_NAME, value, cookiePath, null, null, Integer.MAX_VALUE, false);
  }

  protected static String getCookiePath(HttpSession session) {    
    return session.getServletContext().getContextPath();    
  }

  private static String getCockieValue(Authentications authentications) {
    List<Authentication> authList = authentications.getAuthentications();
    StringBuilder cookieWriter = new StringBuilder();
    cookieWriter.append("{");    
    for(int i = 0; i < authList.size(); i++) {
      UserAuthentication auth = (UserAuthentication) authList.get(i);
      if(i > 0) {
        cookieWriter.append(",");  
      }
      cookieWriter.append("\"");
      cookieWriter.append(auth.getProcessEngineName());
      cookieWriter.append("\"");
      cookieWriter.append(":");
      cookieWriter.append("{");
      
      cookieWriter.append("\"");
      cookieWriter.append("userId");
      cookieWriter.append("\"");      
      cookieWriter.append(":");      
      cookieWriter.append("\"");
      cookieWriter.append(auth.getIdentityId());
      cookieWriter.append("\",");
      
      cookieWriter.append("\"");
      cookieWriter.append("cockpit");
      cookieWriter.append("\"");      
      cookieWriter.append(":");      
      cookieWriter.append(auth.isCockpitAuthorized());  
      cookieWriter.append(",");      
      
      cookieWriter.append("\"");
      cookieWriter.append("tasklist");
      cookieWriter.append("\"");      
      cookieWriter.append(":");      
      cookieWriter.append(auth.isTasklistAuthorized());      
      
      cookieWriter.append("}");
    }    
    cookieWriter.append("}");
    return cookieWriter.toString();
  }

}

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

package org.camunda.bpm.engine.impl.cmd;

import java.io.Serializable;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.identity.Picture;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.UserEntity;


/**
 * @author Tom Baeyens
 */
public class SetUserPictureCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String userId;
  protected Picture picture;
  
  
  public SetUserPictureCmd(String userId, Picture picture) {
    this.userId = userId;
    this.picture = picture;
  }

  public Object execute(CommandContext commandContext) {
    if(userId == null) {
      throw new ProcessEngineException("userId is null");
    }
    UserEntity user = (UserEntity) commandContext
      .getUserManager()
      .findUserById(userId);
    if(user == null) {
      throw new ProcessEngineException("user "+userId+" doesn't exist");
    }
    user.setPicture(picture);
    return null;
  }

}

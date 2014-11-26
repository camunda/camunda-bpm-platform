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

import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityInfoEntity;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

/**
 * @author Daniel Meyer
 *
 */
public class DeleteUserPictureCmd implements Command<Void> {
  
  protected String userId;

  public DeleteUserPictureCmd(String userId) {
    this.userId = userId;
  }

  public Void execute(CommandContext commandContext) {
    ensureNotNull("UserId", userId);

    IdentityInfoEntity infoEntity = commandContext.getIdentityInfoManager()
      .findUserInfoByUserIdAndKey(userId, "picture");
    
    if(infoEntity != null) {
      String byteArrayId = infoEntity.getValue();
      if(byteArrayId != null) {
        commandContext.getByteArrayManager()
          .deleteByteArrayById(byteArrayId);
      }
      commandContext.getIdentityInfoManager()
        .delete(infoEntity);
    }
    
    
    return null;
  }

}

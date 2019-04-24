/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.impl.cmd;

import java.io.Serializable;
import org.camunda.bpm.engine.identity.Picture;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.camunda.bpm.engine.impl.persistence.entity.IdentityInfoEntity;
import org.camunda.bpm.engine.repository.ResourceTypes;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;


/**
 * @author Daniel Meyer
 * @author Tom Baeyens
 */
public class SetUserPictureCmd implements Command<Void>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String userId;
  protected Picture picture;


  public SetUserPictureCmd(String userId, Picture picture) {
    this.userId = userId;
    this.picture = picture;
  }

  public Void execute(CommandContext commandContext) {
    ensureNotNull("userId", userId);

    IdentityInfoEntity pictureInfo = commandContext.getIdentityInfoManager()
      .findUserInfoByUserIdAndKey(userId, "picture");

    if (pictureInfo != null) {
      String byteArrayId = pictureInfo.getValue();
      if (byteArrayId != null) {
        commandContext.getByteArrayManager()
          .deleteByteArrayById(byteArrayId);
      }

    } else {
      pictureInfo = new IdentityInfoEntity();
      pictureInfo.setUserId(userId);
      pictureInfo.setKey("picture");
      commandContext.getDbEntityManager().insert(pictureInfo);
    }

    ByteArrayEntity byteArrayEntity = new ByteArrayEntity(picture.getMimeType(), picture.getBytes(), ResourceTypes.REPOSITORY);

    commandContext.getByteArrayManager()
      .insertByteArray(byteArrayEntity);

    pictureInfo.setValue(byteArrayEntity.getId());

    return null;
  }

}

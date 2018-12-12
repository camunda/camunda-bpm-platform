/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.camunda.bpm.engine.test.standalone.authentication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.AuthenticationException;
import org.camunda.bpm.engine.identity.User;
import org.camunda.bpm.engine.impl.test.ResourceProcessEngineTestCase;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.junit.Test;

public class LoginAttemptsTest extends ResourceProcessEngineTestCase {

  private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

  public LoginAttemptsTest() {
    super("org/camunda/bpm/engine/test/standalone/authentication/camunda.cfg.xml");
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    ClockUtil.setCurrentTime(new Date());
  }

  @Test
  public void testUsuccessfulAttemptsResultInException() throws ParseException {
    User user = identityService.newUser("johndoe");
    user.setPassword("xxx");
    identityService.saveUser(user);

    Date now = sdf.parse("2000-01-24T13:00:00");
    ClockUtil.setCurrentTime(now);
    try {
      for (int i = 0; i <= 6; i++) {
        assertFalse(identityService.checkPassword("johndoe", "invalid pwd"));
        now = DateUtils.addSeconds(now, 5);
        ClockUtil.setCurrentTime(now);
      }
      fail("expected exception");
    } catch (AuthenticationException e) {
      assertTrue(e.getMessage().contains("The user with id 'johndoe' is locked."));
    }

    identityService.deleteUser(user.getId());
  }
}

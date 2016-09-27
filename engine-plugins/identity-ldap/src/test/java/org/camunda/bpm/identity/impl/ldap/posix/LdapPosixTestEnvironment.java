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
package org.camunda.bpm.identity.impl.ldap.posix;

import java.io.File;
import org.apache.commons.lang.StringUtils;
import org.apache.directory.api.ldap.model.entry.Entry;

import org.apache.directory.api.ldap.model.entry.Modification;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.ModificationOperation;
import org.apache.directory.api.ldap.model.name.Dn;
import org.camunda.bpm.identity.impl.ldap.LdapTestEnvironment;

import java.util.ArrayList;
import java.util.List;
import org.apache.directory.api.ldap.model.entry.DefaultModification;

/**
 * <p>
 * LDAP test setup for posix groups using apache directory</p>
 *
 * @author Tom Crossland
 */
public class LdapPosixTestEnvironment extends LdapTestEnvironment {

  public LdapPosixTestEnvironment() {
    super();
    // overwrite the name of the directory to use
    workingDirectory = new File(System.getProperty("java.io.tmpdir") + "/ldap-posix-work");
  }

  @Override
  public void init() throws Exception {
    initializeDirectory();

    // Enable POSIX groups in ApacheDS
    Dn nis = new Dn("cn=nis,ou=schema");
    if (service.getAdminSession().exists(nis)) {
      Entry entry = service.getAdminSession().lookup(nis);
      Attribute nisDisabled = entry.get("m-disabled");
      if (null != nisDisabled && StringUtils.equalsIgnoreCase(nisDisabled.getString(), "TRUE")) {
        nisDisabled.remove("TRUE");
        nisDisabled.add("FALSE");
        List<Modification> modifications = new ArrayList<Modification>();
        modifications.add(new DefaultModification(ModificationOperation.REPLACE_ATTRIBUTE, nisDisabled));
        service.getAdminSession().modify(nis, modifications);
        service.shutdown();
        initializeDirectory(); // Note: This instantiates service again for schema modifications to take effect.
      }
    }

    startServer();

    createGroup("office-berlin");
    createUserUid("daniel", "office-berlin", "Daniel", "Meyer", "daniel@camunda.org");

    createGroup("people");
    createUserUid("ruecker", "people", "Bernd", "Ruecker", "ruecker@camunda.org");
    createUserUid("monster", "people", "Cookie", "Monster", "monster@camunda.org");
    createUserUid("fozzie", "people", "Bear", "Fozzie", "fozzie@camunda.org");

    createGroup("groups");
    createPosixGroup("1", "posix-group-without-members");
    createPosixGroup("2", "posix-group-with-members", "fozzie", "monster", "ruecker");
  }

  protected void createPosixGroup(String gid, String name, String... memberUids) throws Exception {
    Dn dn = new Dn("cn=" + name + ",ou=groups,o=camunda,c=org");
    if (!service.getAdminSession().exists(dn)) {
      Entry entry = service.newEntry(dn);
      entry.add("objectClass", "top", "posixGroup");
      entry.add("cn", name);
      entry.add("gidNumber", gid);
      for (String memberUid : memberUids) {
        entry.add("memberUid", memberUid);
      }
      service.getAdminSession().add(entry);
    }
  }
}

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

import org.apache.commons.lang.StringUtils;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.entry.ServerModification;
import org.apache.directory.shared.ldap.entry.EntryAttribute;
import org.apache.directory.shared.ldap.entry.Modification;
import org.apache.directory.shared.ldap.entry.ModificationOperation;
import org.apache.directory.shared.ldap.name.LdapDN;
import org.camunda.bpm.identity.impl.ldap.LdapTestEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>LDAP test setup for posix groups using apache directory</p>
 *
 * @author Tom Crossland
 */
public class LdapPosixTestEnvironment extends LdapTestEnvironment {

  public LdapPosixTestEnvironment() {
    super();
  }

  @Override
  public void init() throws Exception {
    initializeDirectory("target/ldap-posix-work");

    // Enable POSIX groups in ApacheDS
    LdapDN nis = new LdapDN("cn=nis,ou=schema");
    if (service.getAdminSession().exists(nis)) {
      ServerEntry entry = service.getAdminSession().lookup(nis);
      EntryAttribute nisDisabled = entry.get("m-disabled");
      if (null != nisDisabled && StringUtils.equalsIgnoreCase(nisDisabled.getString(), "TRUE")) {
        nisDisabled.remove("TRUE");
        nisDisabled.put("FALSE");
        List<Modification> modifications = new ArrayList<Modification>();
        modifications.add(new ServerModification(ModificationOperation.REPLACE_ATTRIBUTE, nisDisabled));
        service.getAdminSession().modify(nis, modifications);
        service.shutdown();
        initializeDirectory("target/ldap-posix-work"); // Note: This instantiates service again for schema modifications to take effect.
      }
    }

    ldapService.start();

    createGroup("office-berlin");
    String dnDaniel = createUserUid("daniel", "office-berlin", "Daniel", "Meyer", "daniel@camunda.org");

    createGroup("people");
    createUserUid("ruecker", "people", "Bernd", "Ruecker", "ruecker@camunda.org");
    createUserUid("monster", "people", "Cookie", "Monster", "monster@camunda.org");
    createUserUid("fozzie", "people", "Bear", "Fozzie", "fozzie@camunda.org");

    createGroup("groups");
    createPosixGroup("1", "posix-group-without-members");
    createPosixGroup("2", "posix-group-with-members", "fozzie", "monster", "ruecker");
  }

  protected void createPosixGroup(String gid, String name, String... memberUids) throws Exception {
    LdapDN dn = new LdapDN("cn=" + name + ",ou=groups,o=camunda,c=org");
    if (!service.getAdminSession().exists(dn)) {
      ServerEntry entry = service.newEntry(dn);
      entry.add("objectClass", "top", "posixGroup");
      entry.add("cn", name);
      entry.add("gidNumber", gid);
      for (String memberUid : memberUids) {
        entry.add("memberUid", memberUid);
      }
      service.getAdminSession().add(entry);
      System.out.println("created entry: " + dn.toNormName());
    }
  }
}

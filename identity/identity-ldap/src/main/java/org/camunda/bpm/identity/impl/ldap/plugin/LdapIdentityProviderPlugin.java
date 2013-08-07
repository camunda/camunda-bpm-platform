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
package org.camunda.bpm.identity.impl.ldap.plugin;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.identity.impl.ldap.LdapConfiguration;
import org.camunda.bpm.identity.impl.ldap.LdapIdentityProviderFactory;
import org.camunda.bpm.identity.impl.ldap.util.CertificateHelper;

/**
 * <p>{@link ProcessEnginePlugin} providing Ldap Identity Provider support</p>
 * 
 * <p>This class extends {@link LdapConfiguration} such that the configuration properties 
 * can be set directly on this class vie the <code>&lt;properties .../&gt;</code> element
 * in bpm-platform.xml / processes.xml</p>
 * 
 * @author Daniel Meyer
 *
 */
public class LdapIdentityProviderPlugin extends LdapConfiguration implements ProcessEnginePlugin {
  
  protected Logger LOG = Logger.getLogger(LdapIdentityProviderPlugin.class.getName());

  protected boolean acceptUntrustedCertificates = false;

  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    
    LOG.log(Level.INFO, "PLUGIN {0} activated on process engine {1}", new String[]{getClass().getSimpleName(), processEngineConfiguration.getProcessEngineName()});
    
    if(acceptUntrustedCertificates) {
      CertificateHelper.acceptUntrusted();
      LOG.log(Level.WARNING, "Enabling accept of untrusted certificates. Use at own risk.");
    }
    
    LdapIdentityProviderFactory ldapIdentityProviderFactory = new LdapIdentityProviderFactory();
    ldapIdentityProviderFactory.setLdapConfiguration(this);
    processEngineConfiguration.setIdentityProviderSessionFactory(ldapIdentityProviderFactory);
    
  }

  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    // nothing to do    
  }
  
  public void postProcessEngineBuild(ProcessEngine processEngine) {
    // nothing to do
  }
  
  public void setAcceptUntrustedCertificates(boolean acceptUntrustedCertificates) {
    this.acceptUntrustedCertificates = acceptUntrustedCertificates;
  }
  
  public boolean isAcceptUntrustedCertificates() {
    return acceptUntrustedCertificates;
  }

}

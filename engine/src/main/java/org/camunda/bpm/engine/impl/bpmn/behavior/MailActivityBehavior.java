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

package org.camunda.bpm.engine.impl.bpmn.behavior;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.mail.SimpleEmail;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.ProcessEngineLogger;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;

/**
 * @author Joram Barrez
 * @author Frederik Heremans
 */
public class MailActivityBehavior extends AbstractBpmnActivityBehavior {

  protected static final BpmnBehaviorLogger LOG = ProcessEngineLogger.BPMN_BEHAVIOR_LOGGER;

  protected Expression to;
  protected Expression from;
  protected Expression cc;
  protected Expression bcc;
  protected Expression subject;
  protected Expression text;
  protected Expression html;
  protected Expression charset;

  public void execute(ActivityExecution execution) {
    String toStr = getStringFromField(to, execution);
    String fromStr = getStringFromField(from, execution);
    String ccStr = getStringFromField(cc, execution);
    String bccStr = getStringFromField(bcc, execution);
    String subjectStr = getStringFromField(subject, execution);
    String textStr = getStringFromField(text, execution);
    String htmlStr = getStringFromField(html, execution);
    String charSetStr = getStringFromField(charset, execution);

    Email email = createEmail(textStr, htmlStr);

    addTo(email, toStr);
    setFrom(email, fromStr);
    addCc(email, ccStr);
    addBcc(email, bccStr);
    setSubject(email, subjectStr);
    setMailServerProperties(email);
    setCharset(email, charSetStr);

    try {
      email.send();
    } catch (EmailException e) {
      throw LOG.sendingEmailException(toStr, e);
    }
    leave(execution);
  }

  protected Email createEmail(String text, String html) {
    if (html != null) {
      return createHtmlEmail(text, html);
    } else if (text != null) {
      return createTextOnlyEmail(text);
    } else {
      throw LOG.emailFormatException();
    }
  }

  protected HtmlEmail createHtmlEmail(String text, String html) {
    HtmlEmail email = new HtmlEmail();
    try {
      email.setHtmlMsg(html);
      if (text != null) { // for email clients that don't support html
        email.setTextMsg(text);
      }
      return email;
    } catch (EmailException e) {
      throw LOG.emailCreationException("HTML", e);
    }
  }

  protected SimpleEmail createTextOnlyEmail(String text) {
    SimpleEmail email = new SimpleEmail();
    try {
      email.setMsg(text);
      return email;
    } catch (EmailException e) {
      throw LOG.emailCreationException("text-only", e);
    }
  }

  protected void addTo(Email email, String to) {
    String[] tos = splitAndTrim(to);
    if (tos != null) {
      for (String t : tos) {
        try {
          email.addTo(t);
        } catch (EmailException e) {
          throw LOG.addRecipientException(t, e);
        }
      }
    } else {
      throw LOG.missingRecipientsException();
    }
  }

  protected void setFrom(Email email, String from) {
    String fromAddress = null;

    if (from != null) {
      fromAddress = from;
    } else { // use default configured from address in process engine config
      fromAddress = Context.getProcessEngineConfiguration().getMailServerDefaultFrom();
    }

    try {
      email.setFrom(fromAddress);
    } catch (EmailException e) {
      throw LOG.addSenderException(from, e);
    }
  }

  protected void addCc(Email email, String cc) {
    String[] ccs = splitAndTrim(cc);
    if (ccs != null) {
      for (String c : ccs) {
        try {
          email.addCc(c);
        } catch (EmailException e) {
          throw LOG.addCcException(c, e);
        }
      }
    }
  }

  protected void addBcc(Email email, String bcc) {
    String[] bccs = splitAndTrim(bcc);
    if (bccs != null) {
      for (String b : bccs) {
        try {
          email.addBcc(b);
        } catch (EmailException e) {
          throw LOG.addBccException(b, e);
        }
      }
    }
  }

  protected void setSubject(Email email, String subject) {
    email.setSubject(subject != null ? subject : "");
  }

  protected void setMailServerProperties(Email email) {
    ProcessEngineConfigurationImpl processEngineConfiguration = Context.getProcessEngineConfiguration();

    String host = processEngineConfiguration.getMailServerHost();
    ensureNotNull("Could not send email: no SMTP host is configured", "host", host);
    email.setHostName(host);

    int port = processEngineConfiguration.getMailServerPort();
    email.setSmtpPort(port);

    email.setTLS(processEngineConfiguration.getMailServerUseTLS());

    String user = processEngineConfiguration.getMailServerUsername();
    String password = processEngineConfiguration.getMailServerPassword();
    if (user != null && password != null) {
      email.setAuthentication(user, password);
    }
  }
  
  protected void setCharset(Email email, String charSetStr) {
    if (charset != null) {
      email.setCharset(charSetStr);
    }
  }
  
  protected String[] splitAndTrim(String str) {
    if (str != null) {
      String[] splittedStrings = str.split(",");
      for (int i = 0; i < splittedStrings.length; i++) {
        splittedStrings[i] = splittedStrings[i].trim();
      }
      return splittedStrings;
    }
    return null;
  }

  protected String getStringFromField(Expression expression, DelegateExecution execution) {
    if(expression != null) {
      Object value = expression.getValue(execution);
      if(value != null) {
        return value.toString();
      }
    }
    return null;
  }

}

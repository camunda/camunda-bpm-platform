package org.camunda.bpm.cycle.web;

import org.thymeleaf.Arguments;
import org.thymeleaf.messageresolver.IMessageResolver;
import org.thymeleaf.messageresolver.MessageResolution;
import org.xnap.commons.i18n.I18n;

public class I18nResolver implements IMessageResolver {

  private I18n i18n;

  public I18nResolver(I18n i18n) {
    this.i18n = i18n;
  }

  public MessageResolution resolveMessage(Arguments arguments, String key, Object[] messageParameters) {
    return new MessageResolution(i18n.tr(key, messageParameters));
  }

  public void initialize() {
  }

  public Integer getOrder() {
    return 1;
  }

  public String getName() {
    return "gettext:org.xnap.commons.i18n";
  }
}

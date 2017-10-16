package org.camunda.bpm.engine.impl.cmd;

import java.io.InputStream;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

/**
 * 
 * @author Anna Pazola
 *
 */
public abstract class AbstractGetDeployedFormCmd implements Command<InputStream> {

  public InputStream execute(final CommandContext commandContext) {
    final FormData formData = getFormData(commandContext);

    if (formData.getFormKey() == null) {
      return null;
    }
    String formKey = formData.getFormKey();
    final String resourceName = getResourceName(formKey);

    checkAuthorization(commandContext);
    InputStream deployedFormData = commandContext.runWithoutAuthorization(new Callable<InputStream>() {

      @Override
      public InputStream call() throws Exception {
        return new GetDeploymentResourceCmd(formData.getDeploymentId(), resourceName).execute(commandContext);
      }
    });
    return deployedFormData;
  }

  protected abstract FormData getFormData(CommandContext commandContext);

  protected abstract void checkAuthorization(CommandContext commandContext);

  protected String getResourceName(String formKey) {
    String[] splittedFormKey = formKey.split(":");
    return splittedFormKey[splittedFormKey.length - 1];
  }
}

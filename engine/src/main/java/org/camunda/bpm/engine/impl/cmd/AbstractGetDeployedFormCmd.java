package org.camunda.bpm.engine.impl.cmd;

import java.io.InputStream;
import java.util.concurrent.Callable;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.exception.DeploymentResourceNotFoundException;
import org.camunda.bpm.engine.exception.NotFoundException;
import org.camunda.bpm.engine.form.FormData;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

/**
 * 
 * @author Anna Pazola
 *
 */
public abstract class AbstractGetDeployedFormCmd implements Command<InputStream> {

  public InputStream execute(final CommandContext commandContext) {
    final FormData formData = getFormData(commandContext);
    String formKey = formData.getFormKey();

    checkAuthorization(commandContext);

    if (formKey == null) {
      throw new NotFoundException("The form key is not set.");
    }

    checkKeyFormat(formKey);
    final String resourceName = getResourceName(formKey);

    InputStream deployedFormData = null;
    try {
      deployedFormData = commandContext.runWithoutAuthorization(new Callable<InputStream>() {

        @Override
        public InputStream call() throws Exception {
          return new GetDeploymentResourceCmd(formData.getDeploymentId(), resourceName).execute(commandContext);
        }
      });
    } catch (DeploymentResourceNotFoundException e) {
      throw new NotFoundException("The resource '" + resourceName + "' cannot be found.", e);
    }

    return deployedFormData;
  }

  protected void checkKeyFormat(String formKey) {
    boolean rightFormat = formKey.matches("^embedded:deployment:.+") || formKey.matches("^deployment:.+");
    if (!rightFormat) {
      throw new BadUserRequestException("The form key '" + formKey + "' has wrong format. It does not refer to the deployment.");
    }
  }

  protected abstract FormData getFormData(CommandContext commandContext);

  protected abstract void checkAuthorization(CommandContext commandContext);

  protected String getResourceName(String formKey) {
    String[] splittedFormKey = formKey.split(":");
    return splittedFormKey[splittedFormKey.length - 1];
  }
}

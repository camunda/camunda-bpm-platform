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

  protected static String EMBEDDED_KEY = "embedded:";
  protected static int EMBEDDED_KEY_LENGTH = EMBEDDED_KEY.length();

  protected static String DEPLOYMENT_KEY = "deployment:";
  protected static int DEPLOYMENT_KEY_LENGTH = DEPLOYMENT_KEY.length();

  public InputStream execute(final CommandContext commandContext) {
    checkAuthorization(commandContext);

    final FormData formData = getFormData(commandContext);
    String formKey = formData.getFormKey();

    if (formKey == null) {
      throw new BadUserRequestException("The form key is not set.");
    }

    final String resourceName = getResourceName(formKey);

    try {
      return commandContext.runWithoutAuthorization(new Callable<InputStream>() {
        @Override
        public InputStream call() throws Exception {
          return new GetDeploymentResourceCmd(formData.getDeploymentId(), resourceName).execute(commandContext);
        }
      });
    }
    catch (DeploymentResourceNotFoundException e) {
      throw new NotFoundException("The form with the resource name '" + resourceName + "' cannot be found in deployment.", e);
    }
  }

  protected String getResourceName(String formKey) {
    String resourceName = formKey;

    if (resourceName.startsWith(EMBEDDED_KEY)) {
      resourceName = resourceName.substring(EMBEDDED_KEY_LENGTH, resourceName.length());
    }

    if (!resourceName.startsWith(DEPLOYMENT_KEY)) {
      throw new BadUserRequestException("The form key '" + formKey + "' does not reference a deployed form.");
    }

    resourceName = resourceName.substring(DEPLOYMENT_KEY_LENGTH, resourceName.length());

    return resourceName;
  }

  protected abstract FormData getFormData(CommandContext commandContext);

  protected abstract void checkAuthorization(CommandContext commandContext);

}

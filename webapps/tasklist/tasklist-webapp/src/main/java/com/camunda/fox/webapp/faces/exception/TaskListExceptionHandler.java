package com.camunda.fox.webapp.faces.exception;

import java.util.Iterator;

import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.application.NavigationHandler;
import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerWrapper;
import javax.faces.context.FacesContext;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;

public class TaskListExceptionHandler extends ExceptionHandlerWrapper {

  private ExceptionHandler wrapped;

  public TaskListExceptionHandler(ExceptionHandler wrapped) {
    super();
    this.wrapped = wrapped;
  }

  @Override
  public ExceptionHandler getWrapped() {
    return this.wrapped;
  }

  @Override
  public void handle() throws FacesException {

    Iterator<ExceptionQueuedEvent> itr = getUnhandledExceptionQueuedEvents().iterator();
    while (itr.hasNext()) {

      ExceptionQueuedEvent event = itr.next();
      ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
      Throwable throwable = context.getException();

      FacesMessage message = null;

      if (throwable instanceof FacesException) {
        // Handle failed log-in
        while (throwable.getCause() != null) {
          throwable = throwable.getCause();
          if (throwable instanceof TaskListAuthenticationFailedException) {
            message = new FacesMessage(throwable.getMessage());
          }
        }
        FacesContext fc = FacesContext.getCurrentInstance();
        NavigationHandler nav = fc.getApplication().getNavigationHandler();
        try {
          // ...handle all other exceptions
          if (message == null) {
            message = new FacesMessage("Oops, something went wrong. Please log in and try again.");
          }
          fc.addMessage(null, message);
          nav.handleNavigation(fc, null, "signin.jsf");
          fc.renderResponse();
        } finally {
          itr.remove();
        }
      }
    }
    getWrapped().handle();
  }
}

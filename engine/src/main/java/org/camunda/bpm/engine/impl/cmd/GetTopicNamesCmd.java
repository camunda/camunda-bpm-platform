package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.impl.ExternalTaskQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.util.EnsureUtil;

import java.io.Serializable;
import java.util.List;

public class GetTopicNamesCmd implements Command<List<String>>, Serializable {

  protected ExternalTaskQuery externalTaskQuery;

  public GetTopicNamesCmd() {
  }

  public GetTopicNamesCmd(ExternalTaskQuery externalTaskQuery) {
    this.externalTaskQuery = externalTaskQuery;
  }

  @Override public List<String> execute(CommandContext commandContext) {
    EnsureUtil.ensureNotNull("externalTaskQuery", externalTaskQuery);
    ExternalTaskQueryImpl externalTaskQueryImpl = (ExternalTaskQueryImpl) externalTaskQuery;
    return commandContext.getExternalTaskManager().selectTopicNamesByQuery(externalTaskQueryImpl);
  }
}

package org.camunda.bpm.engine.impl.cmd;

import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.impl.ExternalTaskQueryImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;

import java.io.Serializable;
import java.util.List;

public class GetTopicNamesCmd implements Command<List<String>>, Serializable {

    ExternalTaskQuery externalTaskQuery;

    private GetTopicNamesCmd(){}

    public GetTopicNamesCmd(ExternalTaskQuery externalTaskQuery){
        this.externalTaskQuery = externalTaskQuery;
    }

    @Override
    public List<String> execute(CommandContext commandContext) {
        return commandContext
                .getExternalTaskManager()
                .selectTopicNamesByQuery((ExternalTaskQueryImpl) externalTaskQuery);
    }
}

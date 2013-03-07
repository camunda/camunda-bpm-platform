package org.camunda.bpm.engine;


/**
 * This exception is thrown when you try to claim a task that is already claimed
 * by someone else.
 * 
 * @author Jorg Heymans
 * @author Falko Menge 
 */
public class TaskAlreadyClaimedException extends ProcessEngineException {
    
    private static final long serialVersionUID = 1L;

    /** the id of the task that is already claimed */
    private String taskId;
    
    /** the assignee of the task that is already claimed */
    private String taskAssignee;
    
    public TaskAlreadyClaimedException(String taskId, String taskAssignee) {
        super("Task '" + taskId + "' is already claimed by someone else.");
        this.taskId = taskId;
        this.taskAssignee = taskAssignee;
    }
    
    public String getTaskId() {
        return this.taskId;
    }

    public String getTaskAssignee(){
        return this.taskAssignee;
    }

}

def eventName = caseExecution.eventName
def eventCounterName = eventName + "EventCounter"

caseExecution.setVariable(eventName, true)

def eventCounter = caseExecution.getVariable(eventCounterName)

if (eventCounter == null) {
  eventCounter = 0
}

caseExecution.setVariable(eventCounterName, eventCounter + 1)

def counter = caseExecution.getVariable("eventCounter")

if (counter == null) {
  counter = 0
}

caseExecution.setVariable("eventCounter", counter + 1)
caseExecution.setVariable(eventName+"OnCaseExecutionId", caseExecution.getId())
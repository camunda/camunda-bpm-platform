def eventName = task.eventName
def eventCounterName = eventName + "EventCounter"

task.setVariable(eventName, true)

def eventCounter = task.getVariable(eventCounterName)

if (eventCounter == null) {
  eventCounter = 0
}

task.setVariable(eventCounterName, eventCounter + 1)

def counter = task.getVariable("eventCounter")

if (counter == null) {
  counter = 0
}

task.setVariable("eventCounter", counter + 1)

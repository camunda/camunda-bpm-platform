// helper
def isInputMapping() {
  binding.variables.containsKey('execution')
}

def isOutputMapping() {
  binding.variables.containsKey('connector')
}

// logic
if (isInputMapping() && throwInMapping == 'in') {
  throw exception
}

if (isOutputMapping() != null && throwInMapping == 'out') {
  throw exception
}

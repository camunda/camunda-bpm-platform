node = S(input, "application/json")

oldValue = node.prop("order").stringValue()

node.prop("order", 42.00)
newValue = node.prop("order").numberValue()
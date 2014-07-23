node = JSON(input)

oldValue = node.prop("order").stringValue()

node.prop("order", False)
newValue = node.prop("order").boolValue()
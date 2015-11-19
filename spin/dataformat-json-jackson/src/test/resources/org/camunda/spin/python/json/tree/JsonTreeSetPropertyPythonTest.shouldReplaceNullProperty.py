node = S(input, "application/json")
node.prop("order", None)

propertyNode = node.prop("order")
newValue = node.prop("order").value()
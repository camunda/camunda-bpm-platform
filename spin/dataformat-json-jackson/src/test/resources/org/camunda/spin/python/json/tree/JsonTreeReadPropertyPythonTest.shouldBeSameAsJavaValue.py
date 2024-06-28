node = S(input, "application/json")
childNode = node.prop("orderDetails")

property1 = node.prop("order")
property2 = childNode.prop("price")
property3 = node.prop("active")

stringValue = property1.stringValue()
numberValue = property2.numberValue()
boolValue = property3.boolValue()

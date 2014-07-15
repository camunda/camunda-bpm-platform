node = JSON(input)
childNode = node.prop("orderDetails")

property1 = node.prop("order")
property2 = childNode.prop("price")
property3 = node.prop("active")

stringValue = property1.value()
numberValue = property2.numberValue()
boolValue = property3.boolValue()

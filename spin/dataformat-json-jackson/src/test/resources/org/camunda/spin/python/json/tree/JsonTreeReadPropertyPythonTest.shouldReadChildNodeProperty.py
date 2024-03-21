node = S(input, "application/json")
childNode = node.prop("orderDetails")
childNode2 = childNode.prop("currencies")
list = childNode2.elements()

property1 = childNode.prop("roundedPrice")
property2 = list.get(1)

value1 = property1.numberValue()
value2 = property2.stringValue()


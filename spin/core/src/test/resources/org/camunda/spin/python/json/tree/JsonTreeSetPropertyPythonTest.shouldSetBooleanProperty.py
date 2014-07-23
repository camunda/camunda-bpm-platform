node = JSON(input)
node.prop("comment", False)

propertyNode = node.prop("comment")
value = propertyNode.boolValue()
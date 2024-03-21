node = S(input, "application/json")
node.prop("comment", None)

propertyNode = node.prop("comment")
newValue = node.prop("comment").value()
node = S(input, "application/json")
list = [
    "test",
    "test2"
]
node.prop("comment", list)

propertyNode = node.prop("comment")
value = propertyNode.elements().get(1).stringValue()
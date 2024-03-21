node = S(input, "application/json")
object = {
    "name": "test",
    "comment": "42!"
}
node.prop("comment", object)

propertyNode = node.prop("comment")
value = propertyNode.prop("comment").stringValue()
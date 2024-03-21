node = S(input, "application/json")
node.prop("comment", False)

propertyNode = node.prop("comment")

# Known jython issue. Boolean and boolean values are casted to long
# if a matching method is found first.
value = bool(propertyNode.value())

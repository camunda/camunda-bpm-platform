node = S(input, "application/json")

oldValue = node.prop("order").stringValue()

node.prop("order", False)

# Known jython issue. Boolean and boolean values are casted to long
# if a matching method is found first.
newValue = bool(node.prop("order").value())

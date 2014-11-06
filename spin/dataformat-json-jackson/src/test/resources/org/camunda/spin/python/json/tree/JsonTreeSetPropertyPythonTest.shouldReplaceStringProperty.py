node = JSON(input)

oldValue = node.prop("order").stringValue()

node.prop("order", "new Order")
newValue = node.prop("order").stringValue()
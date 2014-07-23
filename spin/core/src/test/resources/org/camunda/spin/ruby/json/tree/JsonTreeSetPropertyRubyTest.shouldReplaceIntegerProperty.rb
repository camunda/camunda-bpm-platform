node = JSON($input)

$oldValue = node.prop("order").stringValue()

node.prop("order", 42)

$newValue = node.prop("order").numberValue()
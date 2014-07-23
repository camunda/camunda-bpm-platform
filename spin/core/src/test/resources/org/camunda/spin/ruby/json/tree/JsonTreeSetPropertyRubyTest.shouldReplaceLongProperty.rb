node = JSON($input)

$oldValue = node.prop("order").stringValue()

node.prop("order", 4200000000)
$newValue = node.prop("order").numberValue()
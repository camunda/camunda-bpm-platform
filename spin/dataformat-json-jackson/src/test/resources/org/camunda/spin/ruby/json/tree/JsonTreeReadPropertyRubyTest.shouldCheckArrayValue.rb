node = S($input, "application/json")

property1 = node.prop('order')
property2 = node.prop('id')
property3 = node.prop('customers')
property4 = node.prop('orderDetails')
property5 = node.prop('active')

$value1 = property1.isArray()
$value2 = property2.isArray()
$value3 = property3.isArray()
$value4 = property4.isArray()
$value5 = property5.isArray()
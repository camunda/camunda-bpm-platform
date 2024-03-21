node = S($input, "application/json")

property1 = node.prop('order')
property2 = node.prop('id')
property3 = node.prop('customers')
property4 = node.prop('orderDetails')
property5 = node.prop('active')

$value1 = property1.isString()
$value2 = property2.isString()
$value3 = property3.isString()
$value4 = property4.isString()
$value5 = property5.isString()
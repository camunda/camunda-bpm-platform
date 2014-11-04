node = JSON($input)
customers = node.prop("customers")

customers.remove(nil)
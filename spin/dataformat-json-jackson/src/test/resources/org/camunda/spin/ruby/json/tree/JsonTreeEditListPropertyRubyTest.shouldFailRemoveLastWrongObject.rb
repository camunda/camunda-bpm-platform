require 'date'

node = S($input, "application/json")
customers = node.prop("customers")

customers.removeLast(Date.today)

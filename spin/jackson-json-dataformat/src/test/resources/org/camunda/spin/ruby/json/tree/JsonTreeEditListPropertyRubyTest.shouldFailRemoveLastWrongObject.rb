require 'date'

node = JSON($input)
customers = node.prop("customers")

customers.removeLast(Date.today)

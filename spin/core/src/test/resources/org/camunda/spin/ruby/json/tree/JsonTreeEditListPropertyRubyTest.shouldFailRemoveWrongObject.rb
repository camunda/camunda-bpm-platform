require 'date'

node = JSON($input)
customers = node.prop("customers")

customers.remove(Date.today)

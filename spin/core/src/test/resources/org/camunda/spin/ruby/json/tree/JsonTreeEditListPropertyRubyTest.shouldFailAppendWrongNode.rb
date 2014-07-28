require 'date'

node = JSON($input)
customers = node.prop("customers")

customers.append(Date.today)
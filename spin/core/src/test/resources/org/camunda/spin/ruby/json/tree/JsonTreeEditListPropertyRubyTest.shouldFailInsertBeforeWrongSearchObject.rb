require 'date'

node = JSON($input)
currencies = node.prop("orderDetails").prop("currencies")

currencies.insertBefore(Date.today, "test")

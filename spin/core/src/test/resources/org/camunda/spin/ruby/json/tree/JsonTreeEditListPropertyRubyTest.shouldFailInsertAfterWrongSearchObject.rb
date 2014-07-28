require 'date'

node = JSON($input)
currencies = node.prop("orderDetails").prop("currencies")

currencies.insertAfter(Date.today, "test")
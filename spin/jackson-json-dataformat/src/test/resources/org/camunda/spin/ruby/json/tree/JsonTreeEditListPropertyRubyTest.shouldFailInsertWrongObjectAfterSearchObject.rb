require 'date'

node = JSON($input)
currencies = node.prop("orderDetails").prop("currencies")

currencies.insertAfter("euro", Date.today)

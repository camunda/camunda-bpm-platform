# camDate filter

## Configuration:

If you need an additional format variant, you can

````
ngModule.config([
'camDateFormatProvider',
function(
camDateFormatProvider
) {
  // default format
  camDateFormatProvider.setDateFormat('LLLL');
  // short format
  camDateFormatProvider.setDateFormat('LL', 'short');
  // custom format
  camDateFormatProvider.setDateFormat('LL', 'foo');
}]);
````


## Useage:

Possible arguments are:
 - short
 - long
 - normal 
 - or any custom registered format (see configuration)

````
{{ dateString | camDate }}
````

````
{{ dateString | camDate:'long' }}
````

Custom format (as in configuration)

````
{{ dateString | camDate:'foo' }}
````


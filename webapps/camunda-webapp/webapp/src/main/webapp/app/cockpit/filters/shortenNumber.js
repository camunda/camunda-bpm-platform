ngDefine('cockpit.filters.shorten.number', function(module) {

  var ShortenNumberFilter = function() {
    return function(number, decimal) {
      if (number < 950) {
        return number;
      }
      if (!decimal) {
        decimal = 1;
      }
      return m(number,decimal);
    };
    
    function m(number,decimal){
      var x = number.toString().length,
          pow = Math.pow,
          decimal = pow(10, decimal);
      
      x -= x % 3;
      
      return Math.round(number * decimal / pow(10,x)) / decimal +" kMGTPE"[x/3];
    };
  };

  module.filter('shortenNumber', ShortenNumberFilter);
});
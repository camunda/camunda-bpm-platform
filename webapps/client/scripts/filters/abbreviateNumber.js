'use strict';

  var AbbreviateNumberFilter = function() {
    return function(number, decimal) {

      if (!number) {
        return;
      }

      if (number < 950) {
        return number;
      }

      if (!decimal) {
        decimal = 1;
      }

      return abbreviateNumber(number,decimal);
    };

    function abbreviateNumber(number, decimal) {
      // 2 decimal places => 100, 3 => 1000, etc
      decimal = Math.pow(10, decimal);

      // Enumerate number abbreviations
      var abbreviations = [ "k", "m", "b", "t" ];

      // Go through the array backwards, so we do the largest first
      for (var i = abbreviations.length - 1; i >= 0; i--) {
        // Convert array index to "1000", "1000000", etc
        var size = Math.pow(10, (i + 1) * 3);

        // If the number is bigger or equal do the abbreviation
        if(size <= number) {
          // Here, we multiply by decimal, round, and then divide by decPlaces.
          // This gives us nice rounding to a particular decimal place.
          number = Math.round(number * decimal / size) / decimal;

          // Handle special case where we round up to the next abbreviation
          if(number == 1000 && i < abbreviations.length - 1) {
            number = 1;
            i++;
          }

          // Add the letter for the abbreviation
          number += abbreviations[i];

          // We are done... stop
          return number;
        }
      }
      return number;
    }
  };
  module.exports = AbbreviateNumberFilter;

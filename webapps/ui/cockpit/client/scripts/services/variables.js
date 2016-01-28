  'use strict';

  var VariablesFactory = [ function() {

    // variable specific stuff //////////////

    function reverse(hash) {
      var result = {};

      for (var key in hash) {
        result[hash[key]] = key;
      }

      return result;
    }

    function keys(hash) {
      var keys = [];

      for (var key in hash) {
        keys.push(key);
      }

      return keys;
    }

    var OPS = {
      eq: '=',
      neq: '!=',
      gt : '>',
      gteq : '>=',
      lt : '<',
      lteq : '<=',
      like: ' like '
    };

    var SYM_TO_OPS = reverse(OPS);

    function operatorName(op) {
      return OPS[op];
    }

    var PATTERN = new RegExp('^(\\w+)\\s*(' + keys(SYM_TO_OPS).join('|') + ')\\s*([^!=<>]+)$');

    /**
     * Tries to guess the type of the input string
     * and returns the appropriate representation
     * in the guessed type.
     *
     * @param value {string}
     * @return value {string|boolean|number} the interpolated value
     */
    function typed(value) {

      // is a string ( "asdf" )
      if (/^".*"\s*$/.test(value)) {
        return value.substring(1, value.length - 1);
      }

      if ((parseFloat(value) + '') === value) {
        return parseFloat(value);
      }

      if (value === 'true' || value === 'false') {
        return value === 'true';
      }

      throw new Error('Cannot infer type of value ' + value);
    }

    function typedString(value) {

      if (!value) {
        return value;
      }

      if (typeof value === 'string') {
        return '"' + value + '"';
      }

      if (typeof value === 'boolean') {
        return value ? 'true' : 'false';
      }

      if (typeof value === 'number') {
        return value;
      }


      throw new Error('Cannot infer type of value ' + value);
    }

    /**
     * Public API of Variables utility
     */
    return {

      /**
       * Parse a string into a variableFilter { name: ..., operator: ..., value: ... }
       * @param  {string} str the string to parse
       * @return {object}     the parsed variableFilter object
       */
      parse: function(str) {

        var match = PATTERN.exec(str),
            value;

        if (!match) {
          throw new Error('Invalid variable syntax: ' + str);
        }

        value = typed(match[3]);

        return {
          name: match[1],
          operator: SYM_TO_OPS[match[2]],
          value: value
        };
      },

      toString: function(variable) {
        if (!variable) {
          return '';
        }

        return variable.name + operatorName(variable.operator) + typedString(variable.value);
      },

      operators: keys(SYM_TO_OPS)
    };
  }];
  module.exports = VariablesFactory;

  'use strict';

  var angular = require('camunda-commons-ui/vendor/angular');

  module.exports = [ 'ViewsProvider', function(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.decisionInstance.table', {
      id: 'realInput',
      initialize: function(data) {
        var realInput, dataEl;
        var inputHeaders = angular.element('th[data-col-id]');

        inputHeaders && inputHeaders.each(function(idx, inputHeader) {
          dataEl = data.decisionInstance.inputs.filter(function(inputObject) {
            return inputObject.clauseId === inputHeader.getAttribute('data-col-id');
          })[0];
          
          if(dataEl) {
            realInput = document.createElement('span');
            if (dataEl.type !== 'Object' &&
            dataEl.type !== 'Bytes' &&
            dataEl.type !== 'File') {

              realInput.className = 'dmn-input';
              realInput.textContent = ' = ' + dataEl.value;

            } else {
              realInput.className = 'dmn-input-object';
              realInput.setAttribute('title', 'Variable value of type ' + dataEl.type + ' is not shown');
              realInput.textContent = ' = [' + dataEl.type + ']';
            }
            inputHeader.firstChild.appendChild(realInput);
          }
        });
      }
    });
  }];

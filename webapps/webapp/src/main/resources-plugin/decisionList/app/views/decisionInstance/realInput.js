/* global define: false */
define([],
function() {
  'use strict';

  return [ 'ViewsProvider', function(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.decisionInstance.table', {
      id: 'realInput',
      initialize: function(data) {

        var viewer = data.tableControl.getViewer();

        var elementRegistry = viewer.get('elementRegistry');

        var clauseRow = elementRegistry.filter(function(element) {
          return element.isClauseRow;
        })[0];

        viewer.get('eventBus').on('cell.render', function(event) {

          var inputIdx = data.decisionInstance.inputs.map(function(inputObject) {
           return inputObject.clauseId;
          }).indexOf(event.data.column.id);

          if(event.data.row === clauseRow &&
              inputIdx !== -1) {
            var realInput = document.createElement('span');
            realInput.className = 'dmn-input';
            realInput.textContent = ' = ' + data.decisionInstance.inputs[inputIdx].value;
            event.gfx.firstChild.appendChild(realInput);
          }
        });

        viewer.get('graphicsFactory').update('row', clauseRow, elementRegistry.getGraphics(clauseRow.id));
      }
    });
  }];
});

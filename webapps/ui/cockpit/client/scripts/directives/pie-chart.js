'use strict';
var throttle = require('lodash').throttle;


function PieChart(options) {
  this.resize(options.width, options.height);

  this.rulersColor = options.rulersColor || '#666';

  this.lineWidth = options.lineWidth || 1;

  this.missingData = [{color: '#959595', label: 'No Data', value: 1}];

  this.setData(this.missingData);
}

var proto = PieChart.prototype;


proto.resize = function(width, height) {
  this.canvas = this.canvas || document.createElement('canvas');
  this.canvas.width = width;
  this.canvas.height = height;
  this.ctx = this.canvas.getContext('2d');

  this.offCanvas = this.offCanvas || document.createElement('canvas');
  this.offCanvas.width = width;
  this.offCanvas.height = height;
  this.offCtx = this.offCanvas.getContext('2d');
  this.x = this.canvas.width * 0.5;
  this.y = this.canvas.height * 0.5;

  this.radius = Math.max(20, Math.min(this.x - 15, this.y - 15));

  return this;
};






proto.setData = function(data) {
  this.data = data;
  if (!this.data.length || !this.data[0].value) {
    this.data = this.missingData;
  }

  this.total = this.data.reduce(function(prev, curr) {
    return prev + curr.value;
  }, 0);

  return this.draw();
};


proto.punchPie = function(ctx) {
  ctx.strokeStyle = ctx.fillStyle = '#fff';
  ctx.moveTo(this.x, this.y);
  ctx.beginPath();
  ctx.arc(this.x, this.y, this.radius * 0.75, 0, Math.PI * 2);
  ctx.closePath();
  ctx.stroke();
  ctx.fill();
};


proto.hoveredSlice = function(px, py) {
  var ctx = this.offCtx;
  ctx.font = this.fontSize + 'px sans-serif';
  var data = this.data || this.missingData;

  var x = this.x;
  var y = this.y;
  var r = this.radius;
  var tt = this.total;
  var prev = Math.PI;
  var rad = Math.PI * 2;


  this.punchPie(ctx);
  if (ctx.isPointInPath(px, py)) {
    return false;
  }

  for (var d in data) {
    var item = data[d];
    var angle = ((item.value / tt) * rad);
    ctx.beginPath();

    ctx.moveTo(x, y);
    ctx.arc(x, y, r, prev, prev + angle);

    ctx.closePath();
    ctx.stroke();
    if (ctx.isPointInPath(px, py)) {
      return item;
    }

    prev += angle;
  }
  return false;
};


proto.draw = function() {
  var ctx = this.ctx;
  var data = this.data || this.missingData;

  ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);

  var x = this.x;
  var y = this.y;
  var r = this.radius;
  var tt = this.total;
  var prev = Math.PI;
  var rad = Math.PI * 2;
  data.forEach(function(item) {
    var angle = ((item.value / tt) * rad);
    console.info('item', item.value, r, angle, prev, x, y);

    ctx.fillStyle = item.color;
    ctx.strokeStyle = item.color;
    ctx.beginPath();

    ctx.moveTo(x, y);
    ctx.arc(x, y, r, prev, prev + angle);

    ctx.closePath();
    ctx.fill();
    ctx.stroke();

    prev += angle;
  });


  this.punchPie(ctx);

  return this;
};

module.exports = ['$location', function($location) {
  return {
    restrict: 'A',

    scope: {
      values: '='
    },

    link: function($scope, $element) {
      var container = $element[0].querySelector('.canvas-holder') || $element[0];
      var win = container.ownerDocument.defaultView;

      var pieChart = new PieChart({
        width: container.clientWidth,
        height: container.clientHeight,
        lineColors: $scope.colors
      });

      container.appendChild(pieChart.canvas);
      function getMousePos(evt) {
        var rect = pieChart.canvas.getBoundingClientRect();
        return {
          x: evt.clientX - rect.left,
          y: evt.clientY - rect.top
        };
      }

      pieChart.canvas.addEventListener('mousemove', function(evt) {
        $scope.$apply(function() {
          var pos = getMousePos(evt);
          $scope.hoveredSlice = pieChart.hoveredSlice(pos.x, pos.y);
          pieChart.canvas.style.cursor = $scope.hoveredSlice && $scope.hoveredSlice.url ? 'pointer' : 'default';
        });
      });

      pieChart.canvas.addEventListener('click', function(evt) {
        $scope.$apply(function() {
          var pos = getMousePos(evt);
          var slice = pieChart.hoveredSlice(pos.x, pos.y);
          if (slice.url) {
            $location.path(slice.url);
          }
        });
      });

      $scope.$watch('values', function() {
        if (!$scope.values || !$scope.values.length) { return; }
        pieChart.setData($scope.values);
      });

      function resize() {
        var height = Math.min(Math.max(container.clientWidth * 0.75, 180), 220);
        pieChart.resize(container.clientWidth, height).draw();
      }
      resize();

      var _resize = throttle(resize, 100);
      win.addEventListener('resize', _resize);

      $scope.$on('$destroy', function() {
        win.removeEventListener('resize', _resize);
      });
    },

    template: '<div class="pie-chart">' +
                '<div class="canvas-holder"></div>' +

                '<div class="legend-holder help-block text-center" ng-if="values[0].label !== \'No Data\'">' +
                  '<span ng-if="hoveredSlice.value" ng-style="{color: hoveredSlice.color}">{{ hoveredSlice.label }} ({{ hoveredSlice.value | abbreviateNumber }})</span>' +
                  '<span ng-if="!hoveredSlice.value">Hover the chart for details</span>' +
                '</div>' +

                '<div class="legend-holder help-block text-center" ng-if="values[0].label === \'No Data\'">' +
                  'No Data' +
                '</div>' +
              '</div>'
  };
}];
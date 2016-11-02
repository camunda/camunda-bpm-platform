'use strict';
var abbreviateNumber = require('./../filters/abbreviateNumber.js')();
var throttle = require('lodash').throttle;


function PieChart(options) {
  this.resize(options.width, options.height);

  this.rulersColor = options.rulersColor || '#666';

  this.fontSize = options.fontSize || 12;

  this.lineWidth = options.lineWidth || 1;

  this.labelMaxChars = options.labelMaxChars || 10;

  this.missingData = [{color: '#959595', label: 'No Data', value: 1}];

  this.setData(this.missingData);
}

var proto = PieChart.prototype;


proto.calculateLabelsMaxWidth = function() {
  this.labelsMaxWidth = 0;
  var ctx = this.ctx;
  ctx.font = this.fontSize + 'px sans-serif';

  (this.data || []).forEach(function(item) {
    var label = item.label.slice(0, this.labelMaxChars);
    label = label === item.label ? label : label + '…';
    this.labelsMaxWidth = Math.max(ctx.measureText(abbreviateNumber(item.value) + ' ' + label).width, this.labelsMaxWidth);
  }, this);

  return this;
};


proto.resize = function(width, height) {
  this.canvas = this.canvas || document.createElement('canvas');
  this.canvas.width = width;
  this.canvas.height = height;
  this.ctx = this.canvas.getContext('2d');

  this.offCanvas = this.offCanvas || document.createElement('canvas');
  this.offCanvas.width = width;
  this.offCanvas.height = height;
  this.offCtx = this.offCanvas.getContext('2d');
  var x = this.x = Math.round(this.ctx.canvas.width * 0.5);
  var y = this.y = Math.round(this.ctx.canvas.height * 0.5);

  this.calculateLabelsMaxWidth();

  var widthMinusLabels = x - this.labelsMaxWidth;
  var heightMinusLabels = y - (this.fontSize * 2);
  this.radius = Math.max(20, Math.min(widthMinusLabels, heightMinusLabels));

  return this;
};






proto.setData = function(data) {
  this.data = data;
  if (!this.data.length) {
    this.data = this.missingData;
  }

  this.total = this.data.reduce(function(prev, curr) {
    return prev + curr.value;
  }, 0);

  this.calculateLabelsMaxWidth();

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

  ctx.font = this.fontSize + 'px sans-serif';


  var x = this.x;
  var y = this.y;
  var r = this.radius;
  var tt = this.total;
  var prev = Math.PI;
  var rad = Math.PI * 2;

  var lx, ly;
  var lr = r + this.fontSize; // always the hypotenuse
  var lc = this.rulersColor;
  var labelMaxChars = this.labelMaxChars;

  data.forEach(function(item) {
    var angle = ((item.value / tt) * rad);

    ctx.fillStyle = item.color;
    ctx.strokeStyle = item.color;
    ctx.beginPath();

    ctx.moveTo(x, y);
    ctx.arc(x, y, r, prev, prev + angle);

    ctx.closePath();
    ctx.fill();
    ctx.stroke();
    if (data.length === 1) { return; }

    lx = Math.round(x + Math.cos(prev + (angle * 0.5)) * lr);
    ly = Math.round(y + Math.sin(prev + (angle * 0.5)) * lr);

    var label = item.label.slice(0, labelMaxChars);
    label = label === item.label ? label : label + '…';
    label = abbreviateNumber(item.value) + ' ' + label;

    ctx.fillStyle = lc;
    ctx.textAlign = 'center';
    if (lx + 3 < x) {
      ctx.textAlign = 'right';
    }
    else if (lx - 3 > x) {
      ctx.textAlign = 'left';
    }

    ctx.textBaseline = 'middle';
    ctx.fillText(label, lx, ly);

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

                '<div class="legend-holder help-block text-center" ng-if="values.length > 1">' +
                  '<span ng-if="hoveredSlice.value" ng-style="{color: hoveredSlice.color}">{{ hoveredSlice.label }}</span>' +
                  '<span ng-if="!hoveredSlice.value">Hover the chart for details</span>' +
                '</div>' +

                '<div class="legend-holder help-block text-center" ng-if="values.length < 2">' +
                  'No Data' +
                '</div>' +
              '</div>'
  };
}];
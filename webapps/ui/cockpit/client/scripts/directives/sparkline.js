'use strict';

var throttle = require('lodash').throttle;
var moment = require('moment');





function roundUp(v, x) {
  var stepWidth = Math.ceil(v / x);
  var stepWidthStr = '' + stepWidth;
  stepWidth = (parseInt(stepWidthStr[0], 10) + 1) * Math.pow(10, stepWidthStr.length - 1);
  return stepWidth * x;
}



function Sparkline(options) {
  this.resize(options.width, options.height);

  this.lineColors = options.lineColors;

  this.rulersColor = options.rulersColor || '#666';

  this.fontSize = options.fontSize || 12;

  this.lineWidth = options.lineWidth || 1;

  this.valueLabelsCount = 8;

  this.timespan = options.timespan || 'day';

  this.timestampFormat = 'YYYY-MM-DDTHH:mm:ss';

  this.timeLabelFormats = {
    day: 'HH:mm',
    week: 'dd DD',
    month: 'DD MMM'
  };
}

var proto = Sparkline.prototype;

proto.resize = function(width, height) {
  this.canvas = this.canvas || document.createElement('canvas');
  this.canvas.width = width;
  this.canvas.height = height;

  this.ctx = this.canvas.getContext('2d');

  return this;
};

proto.max = function(index) {
  var self = this;

  var val = 0;
  if (!arguments.length) {
    (self.data || []).forEach(function(set, i) {
      val = Math.max(val, self.max(i));
    });
    return val;
  }

  (self.data[index] || []).forEach(function(d) {
    val = Math.max(d.value, val);
  });

  return val;
};

proto.min = function(index) {
  var self = this;

  var val = self.max();
  if (!arguments.length) {
    (self.data || []).forEach(function(set, i) {
      val = Math.min(val, self.min(i));
    });
    return val;
  }

  val = self.max(index);
  (self.data[index] || []).forEach(function(d) {
    val = Math.min(d.value, val);
  });

  return val;
};







proto.setData = function(data, newTimespan) {
  if (newTimespan) {
    this.timespan = newTimespan;
  }
  var emptyDate = moment();
  var defaultData = [[
    {
      value: 0,
      timestamp: emptyDate.format(this.dateformat)
    }
  ]];
  if (!data || !data.length || !data[0]) {
    data = defaultData;
  }
  this.data = data;


  var timespan = this.timespan;
  var labelsStart;
  var timestampFormat = this.timestampFormat;
  var max = this.max();
  var rounded = roundUp(max, this.valueLabelsCount);
  var timeLabelFormats = this.timeLabelFormats;


  this.valueLabels = [];
  for (var l = this.valueLabelsCount; l >= 0; l--) {
    this.valueLabels.push((l * rounded) / this.valueLabelsCount);
  }


  this.timeLabels = [];
  if (data.length && data[0] && data[0].length && data[0][0].timestamp) {
    var to = moment();

    var labelTo = this.labelTo = to.clone();
    if (timespan === 'day') {
      labelTo.startOf('hour').add(1, 'hour');
    }
    else if (timespan === 'week') {
      labelTo.startOf('day').add(1, 'day');
    }
    else if (timespan === 'month') {
      labelTo.startOf('week').add(1, 'week');
    }
    var labelFrom = this.labelFrom = labelTo.clone().subtract(1, timespan);

    var count;
    var unit;
    var unitCount = 1;
    if (timespan === 'day') {
      count = 12;
      unit = 'hour';
      unitCount = 2;
    }
    else if (timespan === 'week') {
      count = 7;
      unit = 'day';
    }
    else if (timespan === 'month') {
      count = 4;
      unit = 'week';
    }

    for (var c = 0; c <= count; c++) {
      this.timeLabels.push(labelFrom.clone().add(c * unitCount, unit).format(timeLabelFormats[timespan]));
    }
  }


  this.data = data.map(function(set) {
    if (!set || !set.length) {
      set = [{value: 0}];
    }

    if (set.length === 1) {
      set = [set[0], set[0]];
    }

    var to = moment(set[set.length - 1].timestamp, timestampFormat);
    var milliDiff = to - labelsStart;

    return set.map(function(item) {
      var millis = moment(item.timestamp, timestampFormat);
      item.positionPercent = (to - millis) / milliDiff;
      return item;
    });
  });

  return this.draw();
};


proto.draw = function() {
  var self = this;
  var lineWidth = self.lineWidth;
  var ctx = self.ctx;
  var padding = Math.max(2 * lineWidth, 10);

  var timeLabels = this.timeLabels;
  var valueLabels = this.valueLabels;
  var fontSize = this.fontSize;

  var textPadding = 3;
  var verticalScaleX = 0;
  var horizontalScaleY = 0;
  var tickSize = 10;

  var step;
  var c;

  ctx.clearRect(0, 0, ctx.canvas.width, ctx.canvas.height);


  ctx.strokeStyle = this.rulersColor;
  ctx.fillStyle = this.rulersColor;
  ctx.lineWidth = 1;
  ctx.font = fontSize + 'px Arial';

  valueLabels.forEach(function(l) {
    verticalScaleX = Math.max(verticalScaleX, ctx.measureText(l).width + (textPadding * 2) + tickSize);
  });
  verticalScaleX = Math.round(Math.max(verticalScaleX, tickSize + textPadding)) + 0.5;

  var innerW = ctx.canvas.width - (padding + verticalScaleX);
  var innerH = ctx.canvas.height - padding;

  var tt = 0;
  var tm = 0;
  timeLabels.forEach(function(l) {
    tt += ctx.measureText(l).width + (textPadding * 2);
    tm = Math.max(tm, ctx.measureText(l).width + (textPadding * 2));
  });


  var vertLabel = tt > innerW;
  if (vertLabel) {
    timeLabels.forEach(function(l) {
      horizontalScaleY = Math.max(horizontalScaleY, ctx.measureText(l).width + (textPadding * 2) + tickSize);
    });
    horizontalScaleY = Math.round(Math.max(horizontalScaleY, tickSize + textPadding)) + 0.5;
  }
  else {
    horizontalScaleY = fontSize + (textPadding * 2) + tickSize;
    innerW -= tm / 2;
  }
  innerH -= horizontalScaleY;





  // draw horizontal (time) scale
  var t = ctx.canvas.height - horizontalScaleY;
  ctx.beginPath();
  ctx.moveTo(verticalScaleX - tickSize, t);
  ctx.lineTo(verticalScaleX + innerW, t);
  ctx.stroke();



  // draw vertical (value) scale
  ctx.beginPath();
  ctx.moveTo(verticalScaleX, padding);
  ctx.lineTo(verticalScaleX, t + tickSize);
  ctx.stroke();




  if (vertLabel) {
    ctx.textAlign = 'right';
    ctx.textBaseline = 'bottom';
  }
  else {
    ctx.textAlign = 'center';
    ctx.textBaseline = 'top';
  }

  timeLabels.forEach(function(label, l) {
    var tx = verticalScaleX + (l * (innerW / (timeLabels.length - 1)));

    ctx.beginPath();
    ctx.moveTo(tx, t);
    ctx.lineTo(tx, t + tickSize);
    ctx.stroke();

    if (vertLabel) {
      ctx.save();
      ctx.translate(tx, ctx.canvas.height - (horizontalScaleY - (tickSize + textPadding)));
      ctx.rotate(-Math.PI / 2);
      ctx.fillText(timeLabels[l], 0, fontSize / 2);
      ctx.restore();
    }
    else {
      ctx.fillText(timeLabels[l], tx, ctx.canvas.height - (horizontalScaleY - (tickSize + textPadding)));
    }
  });



  step = innerH / (valueLabels.length - 1);
  ctx.textAlign = 'right';
  ctx.textBaseline = 'middle';
  for (c = 0; c < valueLabels.length; c++) {
    var ty = Math.round(padding + (step * c)) - 0.5;
    ctx.fillText(valueLabels[c], verticalScaleX - (tickSize + textPadding), ty);

    if (c < valueLabels.length - 1) {
      ctx.beginPath();
      ctx.moveTo(verticalScaleX - tickSize, ty);
      ctx.lineTo(verticalScaleX, ty);
      ctx.stroke();
    }
  }



  var rounded = this.valueLabels[0];
  function toPx(val) {
    return (innerH - ((innerH / rounded) * val)) + padding;
  }

  var labelFrom = this.labelFrom;
  var labelTo = this.labelTo;
  var labelDiff = labelTo - labelFrom;


  // draw the data
  this.data.forEach(function(set, index) {
    var right;
    var top;
    var skipped;
    var color = self.lineColors[index];

    ctx.lineWidth = lineWidth;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
    ctx.strokeStyle = color;

    ctx.beginPath();
    set.forEach(function(d) {
      var mom = moment(d.timestamp);
      if (mom <= labelFrom) {
        skipped = d;
        return;
      }

      if (skipped) {
        right = verticalScaleX;
        top = toPx(skipped.value);
        ctx.lineTo(right, top);
        skipped = null;
      }

      right = verticalScaleX + ((mom - labelFrom) / (labelDiff)) * innerW;
      top = toPx(d.value);
      ctx.lineTo(right, top);
    });
    ctx.stroke();

    // draw the starting point
    if (set.length >= 3) {
      ctx.beginPath();
      ctx.fillStyle = color;
      ctx.arc(right, top, lineWidth * 2, 0, 2 * Math.PI);
      ctx.fill();
      ctx.closePath();
    }
  });

  return self;
};


module.exports = function() {
  return {
    restrict: 'A',

    scope: {
      values: '=',
      colors: '=?',
      timespan: '=?'
    },

    link: function($scope, $element) {
      $scope.timespan = $scope.timespan || 'day';

      var container = $element[0];
      var win = container.ownerDocument.defaultView;

      var sparkline = $scope.sparkline = new Sparkline({
        width: container.clientWidth,
        height: container.clientHeight,
        lineColors: $scope.colors
      });

      $scope.$watch('values', function() {
        var cn = container.className.replace('no-data', '');
        if (!$scope.values.length || !$scope.values[0] || !$scope.values[0].length) {
          cn += ' no-data';
        }
        container.className = cn;
        sparkline.setData($scope.values, $scope.timespan);
      });

      container.appendChild(sparkline.canvas);

      var resize = throttle(function() {
        sparkline.resize(container.clientWidth, container.clientHeight).draw();
      }, 100);

      win.addEventListener('resize', resize);

      $scope.$on('$destroy', function() {
        win.removeEventListener('resize', resize);
      });
    },

    template: '<!-- keule!! pech jehabt! -->'
  };
};
/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

function roundUp(v, x) {
  var stepWidth = Math.ceil(v / x);
  var stepWidthStr = '' + stepWidth;
  stepWidth =
    (parseInt(stepWidthStr[0], 10) + 1) * Math.pow(10, stepWidthStr.length - 1);
  return stepWidth * x;
}

function noop() {}

function log10(x) {
  return Math.log(x) / Math.log(10);
}

function LineChart(options) {
  this.moment = options.moment;
  this.abbreviateNumber = options.abbreviateNumber;

  this.resize(options.width, options.height, options.disableSelection);

  this.lineColors = options.lineColors;

  this.rulersColor = options.rulersColor || '#666';

  this.selectingColor = options.selectingColor || 'rgba(0,0,64,0.1)';

  this.unselectedColor = options.unselectedColor || 'rgba(0,0,0,0.1)';

  this.handleColorHover = options.handleColorHover || '#999';

  this.handleColor = options.handleColor || '#aaa';

  this.fontSize = options.fontSize || 12;

  this.fontFamily = options.fontFamily || 'Arial';

  this.lineWidth = options.lineWidth || 1;

  this.isLogScale = options.isLogScale || false;

  this.valueLabelsCount = options.valueLabelsCount || 8;

  this.timespan = options.timespan || 'day';

  this.interval = options.interval || 900;

  this.handleWidth = options.handleWidth || 4;

  this.timestampFormat = options.timestampFormat || 'YYYY-MM-DDTHH:mm:ss';

  this.timeLabelFormats = options.timeLabelFormats || {
    day: 'HH:mm',
    week: 'dd DD',
    month: 'DD MMM'
  };

  this.tickSize = options.tickSize || 10;

  this.textPadding = options.textPadding || 3;

  this.onselection = options.onselection || noop;
}

module.exports = LineChart;

var proto = LineChart.prototype;

proto._mouseIsDown = false;

proto._selectedIn = null;
proto._selectedOut = null;

proto._eventHandlers = {};

proto._eventHandlers.mouseout = function(evt) {
  this.drawMouseHint().drawSelection(evt);
};

proto._eventHandlers.mousemove = function(evt) {
  var offset = this.cursorPosition(evt);

  this._hoveredSelectionHandle = this.hoveredSelectionHandle(evt);

  this.canvas.style.cursor = this._hoveredSelectionHandle
    ? 'ew-resize'
    : 'default';

  if (this._grabbedSelectionHandle === 'in') {
    this._selectedIn = offset.left;
  } else if (this._grabbedSelectionHandle === 'out') {
    this._selectedOut = offset.left;
  }

  this.drawMouseHint(offset.left, offset.top).drawSelection(evt);
};

proto._eventHandlers.mousedown = function(evt) {
  var pos = this.cursorPosition(evt);
  var verticalScaleX = this.verticalScaleX();
  var innerW = this.innerW();

  this._hoveredSelectionHandle = this.hoveredSelectionHandle(evt);

  this.canvas.style.cursor = this._hoveredSelectionHandle
    ? 'ew-resize'
    : 'default';

  if (!this._hoveredSelectionHandle) {
    if (!this._mouseIsDown) {
      this._selectedIn = Math.min(
        Math.max(pos.left, verticalScaleX),
        verticalScaleX + innerW
      );
      this._selectedOut = null;
    }
    this._mouseIsDown = true;
  } else {
    this._grabbedSelectionHandle = this._hoveredSelectionHandle;
  }

  this.drawMouseHint(pos.left, pos.top).drawSelection(evt);
};

proto._eventHandlers.mouseup = function(evt) {
  var pos = this.cursorPosition(evt);
  var verticalScaleX = this.verticalScaleX();
  var innerW = this.innerW();

  if (this._grabbedSelectionHandle) {
    this._grabbedSelectionHandle = false;
  }

  if (this._mouseIsDown) {
    this._selectedOut = Math.max(
      Math.min(pos.left, verticalScaleX + innerW),
      verticalScaleX
    );
  }
  this._mouseIsDown = false;

  if (Math.abs(this._selectedIn - this._selectedOut) <= 1) {
    this._selectedIn = this._selectedOut = null;

    this.onselection({
      start: null,
      end: null,
      in: null,
      out: null
    });
  }

  this.drawMouseHint(pos.left, pos.top).drawSelection(evt);

  if (this._selectedIn && this._selectedOut) {
    this.onselection({
      start: this.momentAtX(this._selectedIn),
      end: this.momentAtX(this._selectedOut),
      in: this._selectedIn,
      out: this._selectedOut
    });
  }
};

proto._eventHandlers.wheel = function(evt) {
  if (!this._selectedIn || !this._selectedOut) {
    return;
  }
  evt.preventDefault();
  var pos = this.cursorPosition(evt);

  var ctx = this.ctx;
  ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

  var speed = Math.max(
    Math.round(Math.abs(this._selectedOut - this._selectedIn) / 10),
    1
  );
  if (evt.deltaY > 0) {
    this._selectedIn += speed;
    this._selectedOut -= speed;
  } else {
    this._selectedIn -= speed;
    this._selectedOut += speed;
  }

  this.drawMouseHint(pos.left, pos.top).drawSelection(evt);

  this.onselection({
    start: this.momentAtX(this._selectedIn),
    end: this.momentAtX(this._selectedOut),
    in: this._selectedIn,
    out: this._selectedOut
  });
};

proto.cursorPosition = function(evt) {
  var rect = this.canvas.getBoundingClientRect();
  return {
    left: evt.clientX - rect.left,
    top: evt.clientY - rect.top
  };
};

proto.bindEvents = function() {
  Object.keys(proto._eventHandlers).forEach(function(evtName) {
    this.canvas.addEventListener(
      evtName,
      proto._eventHandlers[evtName].bind(this),
      false
    );
  }, this);
  return this;
};

proto.unbindEvents = function() {
  Object.keys(proto._eventHandlers).forEach(function(evtName) {
    this.canvas.removeEventListener(
      evtName,
      proto._eventHandlers[evtName].bind(this),
      false
    );
  }, this);
  return this;
};

proto._clearCache = function() {
  this._verticalLabels = null;
  this._verticalScaleX = null;
  this._horizontalScaleY = null;
  this._innerW = null;
  this._innerH = null;
  this._selectedIn = null;
  this._selectedOut = null;
};

proto.resize = function(width, height, disableSelection) {
  this._clearCache();

  if (!this.canvas) {
    this.canvas = document.createElement('canvas');
    this.offCanvas = document.createElement('canvas');
    !disableSelection && this.bindEvents();
  }

  this.canvas.width = this.offCanvas.width = width;
  this.canvas.height = this.offCanvas.height = height;

  this.ctx = this.canvas.getContext('2d');
  this.offCtx = this.offCanvas.getContext('2d');

  this._selectedIn = null;
  this._selectedOut = null;

  if (typeof this.onselection === 'function') {
    this.onselection({
      start: null,
      end: null,
      in: null,
      out: null
    });
  }
  return this;
};

proto.max = function(index) {
  var val = 0;
  if (!arguments.length) {
    (this.data || []).forEach(function(set, i) {
      val = Math.max(val, this.max(i));
    }, this);
    return val;
  }

  (this.data[index] || []).forEach(function(d) {
    val = Math.max(d.value, val);
  }, this);

  return val;
};

proto.min = function(index) {
  var val = this.max();
  if (!arguments.length) {
    (this.data || []).forEach(function(set, i) {
      val = Math.min(val, this.min(i));
    }, this);
    return val;
  }

  val = this.max(index);
  (this.data[index] || []).forEach(function(d) {
    val = Math.min(d.value, val);
  }, this);

  return val;
};

proto.momentAtX = function(x) {
  var moment = this.moment;
  var labelFrom = this.labelFrom;
  var labelTo = this.labelTo;
  var labelDiff = labelTo - labelFrom;
  var msPerPx = labelDiff / this.innerW();
  return moment(
    new Date(msPerPx * (x - this.verticalScaleX()) + this.labelFrom),
    moment.ISO_8601
  );
};

proto.valueAtY = function(y) {
  return y;
};

proto.setData = function(data, newTimespan, newInterval) {
  this._clearCache();
  var moment = this.moment;
  var abbreviateNumber = this.abbreviateNumber;
  this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
  this.offCtx.clearRect(0, 0, this.offCanvas.width, this.offCanvas.height);

  if (newTimespan) {
    this.timespan = newTimespan;
  }
  if (newInterval) {
    this.interval = newInterval;
  }

  var emptyDate = moment();
  var defaultData = [
    [
      {
        value: 0,
        timestamp: emptyDate.format(this.dateformat)
      }
    ]
  ];
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
  var labelValue;

  // if it's a log scale, the number of value labels is recalculated
  if (this.isLogScale) {
    this.valueLabelsCount = this.maxLog() + 1;
  }

  this.valueLabels = [];
  for (var l = this.valueLabelsCount; l >= 0; l--) {
    // set label value based on scale type
    labelValue = this.isLogScale
      ? l && Math.pow(10, l - 1)
      : abbreviateNumber((l * rounded) / this.valueLabelsCount) || 0;

    this.valueLabels.push(labelValue);
  }

  this.timeLabels = [];
  if (data.length && data[0] && data[0].length && data[0][0].timestamp) {
    var to = moment();

    var labelTo = (this.labelTo = to.clone());
    if (timespan === 'day') {
      labelTo.startOf('hour').add(1, 'hour');
    } else if (timespan === 'week') {
      labelTo.startOf('day').add(1, 'day');
    } else if (timespan === 'month') {
      labelTo.startOf('week').add(1, 'week');
    }
    var labelFrom = (this.labelFrom = labelTo.clone().subtract(1, timespan));

    var count;
    var unit;
    var unitCount = 1;
    if (timespan === 'day') {
      count = 12;
      unit = 'hour';
      unitCount = 2;
    } else if (timespan === 'week') {
      count = 7;
      unit = 'day';
    } else if (timespan === 'month') {
      count = 4;
      unit = 'week';
    }

    for (var c = 0; c <= count; c++) {
      this.timeLabels.push(
        labelFrom
          .clone()
          .add(c * unitCount, unit)
          .format(timeLabelFormats[timespan])
      );
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

proto._verticalLabels = null;
proto.verticalLabels = function() {
  if (this._verticalLabels) {
    return this._verticalLabels;
  }
  var ctx = this.ctx;
  var timeLabels = this.timeLabels;
  var textPadding = this.textPadding;
  var tt = 0;
  var innerW = this.innerW();

  timeLabels.forEach(function(l) {
    tt += ctx.measureText(l).width + textPadding * 2;
  });

  this._verticalLabels = tt > innerW;

  return this._verticalLabels;
};

proto._innerW = null;
proto.innerW = function() {
  if (this._innerW) {
    return this._innerW;
  }
  var lineWidth = this.lineWidth;
  var padding = Math.max(2 * lineWidth, 10);
  var ctx = this.ctx;
  var width = ctx.canvas.width;

  var tm = 0;
  var textPadding = this.textPadding;
  var timeLabels = this.timeLabels;
  timeLabels.forEach(function(l) {
    tm = Math.max(tm, ctx.measureText(l).width + textPadding * 2);
  });

  this._innerW = width - (padding + this.verticalScaleX());
  if (!this.verticalLabels()) {
    this._innerW -= tm / 2;
  }
  return this._innerW;
};

proto._innerH = null;
proto.innerH = function() {
  if (this._innerH) {
    return this._innerH;
  }
  var lineWidth = this.lineWidth;
  var padding = Math.max(2 * lineWidth, 10);
  var ctx = this.ctx;
  var height = ctx.canvas.height;

  this._innerH = height - (padding + this.horizontalScaleY());

  return this._innerH;
};

proto._verticalScaleX = null;
proto.verticalScaleX = function() {
  if (this._verticalScaleX) {
    return this._verticalScaleX;
  }
  var verticalScaleX = 0;
  var ctx = this.ctx;
  var valueLabels = this.valueLabels;
  var textPadding = this.textPadding;
  var tickSize = this.tickSize;

  valueLabels.forEach(function(l) {
    verticalScaleX = Math.max(
      verticalScaleX,
      ctx.measureText(l || '0').width + textPadding * 4 + tickSize
    );
  });
  verticalScaleX =
    Math.round(Math.max(verticalScaleX, tickSize + textPadding)) + 0.5;

  this._verticalScaleX = verticalScaleX;

  return verticalScaleX;
};

proto._horizontalScaleY = null;
proto.horizontalScaleY = function() {
  if (this._horizontalScaleY) {
    return this._horizontalScaleY;
  }

  var ctx = this.ctx;
  var timeLabels = this.timeLabels;
  var fontSize = this.fontSize;
  var tickSize = this.tickSize;
  var textPadding = this.textPadding;
  var vertLabel = this.verticalLabels();

  var horizontalScaleY = 0;

  if (vertLabel) {
    timeLabels.forEach(function(l) {
      horizontalScaleY = Math.max(
        horizontalScaleY,
        ctx.measureText(l).width + textPadding * 4 + tickSize
      );
    });
    horizontalScaleY = Math.round(
      Math.max(horizontalScaleY, tickSize + textPadding)
    );
  } else {
    horizontalScaleY = fontSize + textPadding * 2 + tickSize;
  }

  this._horizontalScaleY = horizontalScaleY;

  return horizontalScaleY;
};

proto.drawMouseHint = function(x, y) {
  var ctx = this.ctx;
  var innerW = this.innerW();
  var height = ctx.canvas.height;
  var width = ctx.canvas.width;
  var padding = Math.max(2 * this.lineWidth, 10);
  var verticalScaleX = this.verticalScaleX();
  var horizontalScaleY = this.horizontalScaleY();
  var tickSize = this.tickSize;

  ctx.strokeStyle = this.rulersColor;
  ctx.fillStyle = this.rulersColor;
  ctx.lineWidth = 1;
  ctx.lineCap = 'round';
  ctx.lineJoin = 'round';
  ctx.font = this.fontSize + 'px ' + this.fontFamily;

  ctx.clearRect(0, 0, width, height);
  ctx.drawImage(this.offCanvas, 0, 0, width, height, 0, 0, width, height);

  if (x && x > verticalScaleX && x <= verticalScaleX + innerW) {
    var text = this.momentAtX(x).format(this.timestampFormat);
    var tw = ctx.measureText(text).width + padding * 2;
    var tx = x + tw > width - padding ? width - (padding + tw) : x;
    var ty = y > padding ? y : padding;
    ctx.fillText(text, tx, ty);

    ctx.beginPath();
    ctx.moveTo(x + 0.5, height - (horizontalScaleY + tickSize));
    ctx.lineTo(x + 0.5, height - horizontalScaleY);
    ctx.stroke();
    ctx.closePath();
  }

  return this;
};

proto.drawSelection = function(evt) {
  var ctx = this.ctx;
  var innerH = this.innerH();
  var innerW = this.innerW();
  var verticalScaleX = this.verticalScaleX();
  var padding = Math.max(2 * this.lineWidth, 10);
  var offset = this.cursorPosition(evt);
  var handleWidth = this.handleWidth;
  var selectingColor = this.selectingColor;
  var unselectedColor = this.unselectedColor;

  var _fillStyle = ctx.fillStyle;

  if (this._mouseIsDown) {
    ctx.fillStyle = selectingColor;

    // selecting from left to right
    if (this._selectedIn < offset.left) {
      ctx.fillRect(
        this._selectedIn,
        padding,
        Math.min(
          offset.left - this._selectedIn,
          verticalScaleX + innerW - this._selectedIn
        ),
        innerH
      );
    }

    // selecting from right to left
    else {
      ctx.fillRect(
        Math.max(offset.left, verticalScaleX),
        padding,
        Math.min(this._selectedIn - offset.left, innerW),
        innerH
      );
    }

    ctx.fillStyle = _fillStyle;
    return this;
  }

  if (this._selectedIn && this._selectedOut) {
    var _lineWidth = ctx.lineWidth;
    var _strokeStyle = ctx.strokeStyle;

    if (this._selectedIn < verticalScaleX) {
      this._selectedIn = verticalScaleX;
    }

    if (this._selectedOut > verticalScaleX + innerW) {
      this._selectedOut = verticalScaleX + innerW;
    }

    ctx.fillStyle = unselectedColor;

    if (this._selectedOut && this._selectedIn > this._selectedOut) {
      var s = this._selectedOut;
      this._selectedOut = this._selectedIn;
      this._selectedIn = s;
    }

    // left rect
    ctx.fillRect(
      verticalScaleX,
      padding,
      this._selectedIn - verticalScaleX,
      innerH
    );
    // right rect
    ctx.fillRect(
      this._selectedOut,
      padding,
      innerW + verticalScaleX - this._selectedOut,
      innerH
    );

    ctx.beginPath();
    ctx.moveTo(this._selectedIn + 0.5, innerH + padding);
    ctx.lineTo(this._selectedIn + 0.5, padding + 0.5);
    ctx.lineTo(this._selectedOut + 0.5, padding + 0.5);
    ctx.lineTo(this._selectedOut + 0.5, innerH + padding);
    ctx.stroke();

    ctx.lineWidth = handleWidth + 2;
    ctx.strokeStyle = this.rulersColor;
    ctx.beginPath();
    ctx.moveTo(this._selectedIn, padding + 10);
    ctx.lineTo(this._selectedIn, 80);
    ctx.stroke();
    ctx.closePath();

    ctx.lineWidth = handleWidth;
    ctx.strokeStyle =
      this._hoveredSelectionHandle === 'in'
        ? this.handleColorHover
        : this.handleColor;
    ctx.beginPath();
    ctx.moveTo(this._selectedIn, padding + 10);
    ctx.lineTo(this._selectedIn, 80);
    ctx.stroke();

    ctx.lineWidth = handleWidth + 2;
    ctx.strokeStyle = '#333';
    ctx.beginPath();
    ctx.moveTo(this._selectedOut, padding + 10);
    ctx.lineTo(this._selectedOut, 80);
    ctx.stroke();
    ctx.closePath();

    ctx.lineWidth = handleWidth;
    ctx.strokeStyle =
      this._hoveredSelectionHandle === 'out'
        ? this.handleColorHover
        : this.handleColor;
    ctx.beginPath();
    ctx.moveTo(this._selectedOut, padding + 10);
    ctx.lineTo(this._selectedOut, 80);
    ctx.stroke();

    ctx.lineWidth = _lineWidth;
    ctx.fillStyle = _fillStyle;
    ctx.strokeStyle = _strokeStyle;
  }

  return this;
};

proto.hoveredSelectionHandle = function(evt) {
  if (!this._selectedIn || !this._selectedOut) return false;
  var offset = this.cursorPosition(evt);
  var ctx = this.ctx;
  var padding = Math.max(2 * this.lineWidth, 10);
  var returned = false;
  var handleWidth = this.handleWidth + 4;

  var _lineWidth = ctx.lineWidth;
  var _strokeStyle = ctx.strokeStyle;
  ctx.lineWidth = 1;
  ctx.strokeStyle = 'rgba(0,0,0,0)';

  ctx.beginPath();
  ctx.rect(this._selectedIn - handleWidth / 2, padding + 10, handleWidth, 80);
  ctx.stroke();
  ctx.closePath();
  if (ctx.isPointInPath(offset.left, offset.top)) {
    returned = 'in';
  }

  ctx.beginPath();
  ctx.rect(this._selectedOut - handleWidth / 2, padding + 10, handleWidth, 80);
  ctx.stroke();
  ctx.closePath();
  if (ctx.isPointInPath(offset.left, offset.top)) {
    returned = 'out';
  }

  ctx.lineWidth = _lineWidth;
  ctx.strokeStyle = _strokeStyle;
  return returned;
};

/**
 * returns the smallest power of 10 that is greater than max
 */
proto.maxLog = function() {
  var max = this.max() || 1;
  return Math.ceil(log10(max));
};

proto.drawRulers = function() {
  var ctx = this.offCtx; // for compositing with mouse interaction, draw on the canvas which is not in the DOM
  var lineWidth = this.lineWidth;
  var padding = Math.max(2 * lineWidth, 10);
  var abbreviateNumber = this.abbreviateNumber;

  var timeLabels = this.timeLabels;
  var valueLabels = this.valueLabels;
  var height = ctx.canvas.height;

  var textPadding = this.textPadding;
  var tickSize = this.tickSize;
  var verticalScaleX = this.verticalScaleX();
  var horizontalScaleY = this.horizontalScaleY();
  var innerW = this.innerW();
  var innerH = this.innerH();
  var vertLabel = this.verticalLabels();

  var step;
  var index;
  var valueLabel;
  var maxLog;
  var transformedVal;
  var yPosition;

  ctx.strokeStyle = this.rulersColor;
  ctx.fillStyle = this.rulersColor;
  ctx.lineWidth = 1;
  ctx.lineCap = 'round';
  ctx.lineJoin = 'round';
  ctx.font = this.fontSize + 'px ' + this.fontFamily;

  // draw horizontal (time) scale
  var t = height - horizontalScaleY + 0.5;
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
    ctx.textBaseline = 'middle';
  } else {
    ctx.textAlign = 'center';
    ctx.textBaseline = 'top';
  }

  timeLabels.forEach(function(label, l) {
    var tx = verticalScaleX + l * (innerW / (timeLabels.length - 1));

    ctx.beginPath();
    ctx.moveTo(tx, t);
    ctx.lineTo(tx, t + tickSize);
    ctx.stroke();

    if (vertLabel) {
      ctx.save();
      ctx.translate(tx, height - (horizontalScaleY - (tickSize + textPadding)));
      ctx.rotate(-Math.PI / 2);
      ctx.fillText(timeLabels[l], 0, 0);
      ctx.restore();
    } else {
      ctx.fillText(
        timeLabels[l],
        tx,
        height - (horizontalScaleY - (tickSize + textPadding))
      );
    }
  });

  step = innerH / (valueLabels.length - 1);
  maxLog = this.maxLog();
  ctx.textAlign = 'right';
  ctx.textBaseline = 'middle';
  for (index = 0; index < valueLabels.length; index++) {
    valueLabel = valueLabels[index];
    if (this.isLogScale) {
      transformedVal =
        valueLabel && (innerH / (maxLog + 1)) * (log10(valueLabel) + 1);
      yPosition = innerH - transformedVal + padding;
    } else {
      yPosition = Math.round(padding + step * index) - 0.5;
    }

    ctx.fillText(
      abbreviateNumber(valueLabel) || 0,
      verticalScaleX - (tickSize + textPadding),
      yPosition
    );

    if (index < valueLabels.length - 1) {
      ctx.beginPath();
      ctx.moveTo(verticalScaleX - tickSize, yPosition);
      ctx.lineTo(verticalScaleX, yPosition);
      ctx.stroke();
    }
  }

  return this;
};

proto.draw = function() {
  var ctx = this.offCtx; // for compositing with mouse interaction, draw on the canvas which is not in the DOM
  var lineWidth = this.lineWidth;
  var padding = Math.max(2 * lineWidth, 10);

  var width = ctx.canvas.width;
  var height = ctx.canvas.height;

  var verticalScaleX = this.verticalScaleX();
  var horizontalScaleY = this.horizontalScaleY();
  var innerW = this.innerW();
  var innerH = this.innerH();

  ctx.clearRect(0, 0, width, height);

  var labelFrom = this.labelFrom;
  var labelTo = this.labelTo;
  var labelDiff = labelTo - labelFrom;
  var interval = this.interval;
  var t = height - horizontalScaleY + 0.5;
  var isLogScale = this.isLogScale;

  var max = this.max();
  var maxLog = this.maxLog();
  var rounded = roundUp(max, this.valueLabelsCount);
  function pxFromTop(val) {
    if (!val) return t;
    var transformedVal = val && (innerH / (maxLog + 1)) * (log10(val) + 1);
    return isLogScale
      ? innerH - transformedVal + padding
      : innerH - (innerH / rounded) * val + padding;
  }
  function pxFromLeft(mom) {
    return verticalScaleX + ((mom - labelFrom) / labelDiff) * innerW;
  }

  // draw the data
  this.data.forEach(function(set, index) {
    var right;
    var top;
    var mom;
    var skipped;
    var moment = this.moment;
    var color = this.lineColors[index];

    ctx.lineWidth = lineWidth;
    ctx.strokeStyle = color;

    ctx.beginPath();
    set.forEach(function(item, i) {
      mom = moment(item.timestamp, moment.ISO_8601);
      // record is older than the from label
      if (mom <= labelFrom) {
        skipped = item;
        return;
      }
      // first record is after label from, draw a line at 0 until (mom - interval)
      else if (i === 0 && mom > labelFrom) {
        ctx.moveTo(verticalScaleX, height - horizontalScaleY);
        ctx.lineTo(
          pxFromLeft(mom.clone().subtract(interval, 'seconds')),
          height - horizontalScaleY
        );
      }

      if (skipped) {
        right = verticalScaleX;
        top = pxFromTop(skipped.value);
        ctx.lineTo(right, top);
        skipped = null;
      }

      right = pxFromLeft(mom);
      top = pxFromTop(item.value);
      ctx.lineTo(right, top);
    });

    if (moment() - mom >= interval * 1000) {
      right = pxFromLeft(mom.clone().add(interval, 'seconds'));
      top = height - horizontalScaleY;
      ctx.lineTo(right, top);

      right = pxFromLeft(moment());
      ctx.lineTo(right, top);
    }

    ctx.stroke();
    ctx.closePath();

    // draw the starting point
    if (set.length >= 1) {
      ctx.beginPath();
      ctx.fillStyle = color;
      ctx.arc(right, top, lineWidth * 2, 0, 2 * Math.PI);
      ctx.fill();
      ctx.closePath();
    }
  }, this);

  this.ctx.drawImage(this.offCanvas, 0, 0, width, height, 0, 0, width, height);

  return this.drawRulers().drawMouseHint();
};

proto.remove = function() {
  this.unbindEvents();
  this.canvas.parentNode.removeChild(this.canvas);
};

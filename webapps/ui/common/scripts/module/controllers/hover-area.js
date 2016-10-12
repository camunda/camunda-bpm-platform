'use strict';

module.exports = HoverArea;

function HoverArea() {
  this._hovered = null;
  this._listeners = [];
}

HoverArea.prototype.hoverTitle = function(title) {
  this._hovered = title;

  this._fireListeners();
};

HoverArea.prototype.cleanHover = function() {
  this._hovered = null;

  this._fireListeners();
};

HoverArea.prototype.addHoverListener = function(title, listener) {
  var entry = {
    title: title,
    listener: listener
  };

  this._listeners.push(entry);
  this._fireEntry(entry);

  return (function() {
    this._listeners = this._listeners.filter(function(entry) {
      return entry.listener !== listener;
    });
  }).bind(this);
};

HoverArea.prototype._fireListeners = function() {
  this._listeners.forEach(
    this._fireEntry.bind(this)
  );
};

HoverArea.prototype._fireEntry = function(entry) {
  entry.listener(entry.title === this._hovered);
};

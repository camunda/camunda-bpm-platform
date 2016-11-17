'use strict';

module.exports = ['$scope', 'exposeScopeProperties', CamSearchAbleArea];

function CamSearchAbleArea($scope, exposeScopeProperties) {
  exposeScopeProperties($scope, this, [
    'config',
    'total',
    'arrayTypes',
    'variableTypes',
    'onSearchChange',
    'loadingError',
    'loadingState',
    'textEmpty'
  ]);
}

CamSearchAbleArea.prototype.onQueryChange = function(query) {
  this.query = query;

  this.triggerSearchChange();
};

CamSearchAbleArea.prototype.onPaginationChange = function(pages) {
  this.pages = pages;

  this.triggerSearchChange();
};

CamSearchAbleArea.prototype.triggerSearchChange = function() {
  if (this.query && this.pages) {
    this.onSearchChange({
      query: this.query,
      pages: this.pages
    });
  }
};

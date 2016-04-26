'use strict';

var PAGE_SIZE = 10;

var Batch = function(camAPI, eventBus) {
  this._sdk = camAPI;
  this._eventBus = eventBus;

  this._batches = {
    runtime: {
      state: 'INITIAL',
      currentPage: 1,
      count: 0,
      data: null
    },
    history: {
      state: 'INITIAL',
      currentPage: 1,
      count: 0,
      data: null
    },
    selection: {
      state: 'INITIAL',
      type: null,
      data: {}
    }
  };

  this._jobs = {
    state: 'INITIAL',
    currentPage: 1,
    count: 0,
    data: []
  };
};

Batch.prototype.getProgressPercentage = function(batch, type) {
  switch(type) {
    case 'success': return 100 * batch.completedJobs / (batch.completedJobs + batch.remainingJobs);
    case 'failed': return 100 * batch.failedJobs / (batch.completedJobs + batch.remainingJobs);
    case 'remaining': return 100 * (batch.remainingJobs - batch.failedJobs) / (batch.completedJobs + batch.remainingJobs);
  }
};

Batch.prototype.getProgressAbsolute = function(batch, type) {
  switch(type) {
    case 'success': return batch.completedJobs;
    case 'failed': return batch.failedJobs;
    case 'remaining': return batch.remainingJobs - batch.failedJobs;
  }
};

Batch.prototype.isSelected = function(batch) {
  return this._batches.selection.data.id === batch.id;
};

Batch.prototype.getJobs = function() {
  return this._jobs.data;
};

Batch.prototype.getStacktraceUrl = function(job) {
  return this._sdk.baseUrl + '/job/' + job.id + '/stacktrace';
};

Batch.prototype.getJobCount = function() {
  return this._jobs.count;
};

Batch.prototype.getLoadingState = function(type) {
  if(type === 'jobs') {
    return this._jobs.state;
  }
  return this._batches[type].state;
};

Batch.prototype.getBatches = function(type) {
  return this._batches[type].data;
};

Batch.prototype.getSelection = function() {
  return this._batches.selection.data;
};

Batch.prototype.getSelectionType = function() {
  return this._batches.selection.type;
};

Batch.prototype.getBatchCount = function(type) {
  return this._batches[type].count;
};

Batch.prototype.getPageSize = function() {
  return PAGE_SIZE;
};

Batch.prototype.getCurrentPage = function(type) {
  if(type === 'jobs') {
    return this._jobs.currentPage;
  }
  return this._batches[type].currentPage;
};

Batch.prototype.updatePage = function(type) {
  if(type === 'job') {
    return this._loadFailedJobs(this.getSelection());
  }
  this._load(type);
};

Batch.prototype.load = function() {
  this._load('runtime');
  this._load('history');
};

Batch.prototype.loadPeriodically = function(interval) {
  var self = this;
  this.load();
  this.intervalHandle = window.setInterval(function() {
    self.load();
  }, interval);
};

Batch.prototype.stopLoadingPeriodically = function() {
  window.clearInterval(this.intervalHandle);
};

Batch.prototype.loadDetails = function(id, type) {
  var eventBus = this._eventBus;
  var obj = this._batches.selection;
  obj.state = 'LOADING';
  obj.type = type;
  var self = this;

  var cb = (function(err, data) {
    if(err || typeof data.length !== 'undefined' && data.length === 0) {
      // if the runtime version of the batch was requested,
      // try again with history (it may have finished in the meantime)
      if(type === 'runtime') {
        eventBus.emit('details:switchToHistory');
        self.loadDetails(id, 'history');
      } else {
        eventBus.emit('load:details:failed');
        obj.data = err.message;
        obj.state = 'ERROR';
      }
    } else {
      obj.data = data.length ? data[0] : data;
      obj.state = 'LOADED';
      eventBus.emit('load:details:completed');
      if(type === 'runtime') {
        this._loadFailedJobs(obj.data);
      }
    }
  }).bind(this);

  switch(type) {
    case 'runtime':
      return this._sdk.resource('batch').statistics({batchId: id}, cb);
    case 'history':
      return this._sdk.resource('history').singleBatch(id, cb);
  }
};

Batch.prototype._loadFailedJobs = function(data) {
  var eventBus = this._eventBus;
  var jobId = data.batchJobDefinitionId;
  var obj = this._jobs;

  obj.state = 'LOADING';

  var params = {
    jobDefinitionId: jobId,
    withException: true,
    firstResult: (obj.currentPage - 1) * PAGE_SIZE,
    maxResults: PAGE_SIZE
  };

  this._sdk.resource('job').list(params, (function(err, data) {
    if(err) {
      obj.data = err.message;
      obj.state = 'ERROR';
    } else {
      obj.data = data;
      this._sdk.resource('job').count(params, function(err, data) {
        obj.state = data ? 'LOADED' : 'EMPTY';
        obj.count = data;
        eventBus.emit('load:jobs:completed');
      });
    }
  }).bind(this));
};

Batch.prototype._load = function(type) {
  var eventBus = this._eventBus;
  var obj = this._batches[type];

  if(!obj.data) {
    obj.state = 'LOADING';
  }

  var params = {
    firstResult: (obj.currentPage - 1) * PAGE_SIZE,
    maxResults: PAGE_SIZE
  };
  var countCb = function(err, data) {
    obj.state = data.count ? 'LOADED' : 'EMPTY';
    obj.count = data.count;
    eventBus.emit('load:'+type+':completed');
  };
  var cb = function(err, data) {
    obj.data = data.items || data;
    if(typeof data.count !== 'undefined') {
      countCb(err,data);
    } else {
      switch(type) {
        case 'runtime':
          return this._sdk.resource('batch').statisticsCount(params, countCb);
        case 'history':
          return this._sdk.resource('history').batchCount(params, countCb);
      }
    }
  }.bind(this);

  switch(type) {
    case 'runtime':
      return this._sdk.resource('batch').statistics(params, cb);
    case 'history':
      params.completed = true;
      params.sortBy = 'startTime';
      params.sortOrder = 'desc';
      return this._sdk.resource('history').batch(params, cb);
  }
};

module.exports = Batch;

//>>built
define("dojox/charting/plot2d/Stacked",["dojo/_base/declare","./Default","./commonStacked"],function(_1,_2,_3){
return _1("dojox.charting.plot2d.Stacked",_2,{getSeriesStats:function(){
var _4=_3.collectStats(this.series);
this._maxRunLength=_4.hmax;
return _4;
},buildSegments:function(i,_5){
var _6=this.series[i],_7=_5?Math.min(_6.data.length-1,Math.ceil(this._hScaler.bounds.to-this._hScaler.bounds.from)):_6.data.length-1,_8=null,_9=[];
for(var j=0;j<=_7;j++){
var _a=_5?_3.getIndexValue(this.series,i,j):_3.getValue(this.series,i,_6.data[j]?_6.data[j].x:null);
if(_a!=null&&(_5||_a.y!=null)){
if(!_8){
_8=[];
_9.push({index:j,rseg:_8});
}
_8.push(_a);
}else{
if(!this.opt.interpolate||_5){
_8=null;
}
}
}
return _9;
}});
});

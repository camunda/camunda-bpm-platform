//>>built
define("dojox/charting/plot2d/StackedColumns",["dojo/_base/declare","./Columns","./commonStacked"],function(_1,_2,_3){
return _1("dojox.charting.plot2d.StackedColumns",_2,{getSeriesStats:function(){
var _4=_3.collectStats(this.series);
this._maxRunLength=_4.hmax;
_4.hmin-=0.5;
_4.hmax+=0.5;
return _4;
},getDataLength:function(_5){
return this._maxRunLength;
},getValue:function(_6,_7,_8,_9){
var y,x;
if(_9){
x=_7;
y=_3.getIndexValue(this.series,_8,x);
}else{
x=_6.x-1;
y=_3.getValue(this.series,_8,_6.x);
y=y?y.y:null;
}
return {y:y,x:x};
}});
});

//>>built
define("dojox/gfx/shape",["./_base","dojo/_base/lang","dojo/_base/declare","dojo/_base/kernel","dojo/_base/sniff","dojo/_base/connect","dojo/_base/array","dojo/dom-construct","dojo/_base/Color","./matrix"],function(g,_1,_2,_3,_4,_5,_6,_7,_8,_9){
var _a=g.shape={};
var _b={};
var _c={};
_a.register=function(s){
var t=s.declaredClass.split(".").pop();
var i=t in _b?++_b[t]:((_b[t]=0));
var _d=t+i;
_c[_d]=s;
return _d;
};
_a.byId=function(id){
return _c[id];
};
_a.dispose=function(s){
delete _c[s.getUID()];
};
_a.Shape=_2("dojox.gfx.shape.Shape",null,{constructor:function(){
this.rawNode=null;
this.shape=null;
this.matrix=null;
this.fillStyle=null;
this.strokeStyle=null;
this.bbox=null;
this.parent=null;
this.parentMatrix=null;
var _e=_a.register(this);
this.getUID=function(){
return _e;
};
},destroy:function(){
_a.dispose(this);
},getNode:function(){
return this.rawNode;
},getShape:function(){
return this.shape;
},getTransform:function(){
return this.matrix;
},getFill:function(){
return this.fillStyle;
},getStroke:function(){
return this.strokeStyle;
},getParent:function(){
return this.parent;
},getBoundingBox:function(){
return this.bbox;
},getTransformedBoundingBox:function(){
var b=this.getBoundingBox();
if(!b){
return null;
}
var m=this._getRealMatrix(),gm=_9;
return [gm.multiplyPoint(m,b.x,b.y),gm.multiplyPoint(m,b.x+b.width,b.y),gm.multiplyPoint(m,b.x+b.width,b.y+b.height),gm.multiplyPoint(m,b.x,b.y+b.height)];
},getEventSource:function(){
return this.rawNode;
},setClip:function(_f){
this.clip=_f;
},getClip:function(){
return this.clip;
},setShape:function(_10){
this.shape=g.makeParameters(this.shape,_10);
this.bbox=null;
return this;
},setFill:function(_11){
if(!_11){
this.fillStyle=null;
return this;
}
var f=null;
if(typeof (_11)=="object"&&"type" in _11){
switch(_11.type){
case "linear":
f=g.makeParameters(g.defaultLinearGradient,_11);
break;
case "radial":
f=g.makeParameters(g.defaultRadialGradient,_11);
break;
case "pattern":
f=g.makeParameters(g.defaultPattern,_11);
break;
}
}else{
f=g.normalizeColor(_11);
}
this.fillStyle=f;
return this;
},setStroke:function(_12){
if(!_12){
this.strokeStyle=null;
return this;
}
if(typeof _12=="string"||_1.isArray(_12)||_12 instanceof _8){
_12={color:_12};
}
var s=this.strokeStyle=g.makeParameters(g.defaultStroke,_12);
s.color=g.normalizeColor(s.color);
return this;
},setTransform:function(_13){
this.matrix=_9.clone(_13?_9.normalize(_13):_9.identity);
return this._applyTransform();
},_applyTransform:function(){
return this;
},moveToFront:function(){
var p=this.getParent();
if(p){
p._moveChildToFront(this);
this._moveToFront();
}
return this;
},moveToBack:function(){
var p=this.getParent();
if(p){
p._moveChildToBack(this);
this._moveToBack();
}
return this;
},_moveToFront:function(){
},_moveToBack:function(){
},applyRightTransform:function(_14){
return _14?this.setTransform([this.matrix,_14]):this;
},applyLeftTransform:function(_15){
return _15?this.setTransform([_15,this.matrix]):this;
},applyTransform:function(_16){
return _16?this.setTransform([this.matrix,_16]):this;
},removeShape:function(_17){
if(this.parent){
this.parent.remove(this,_17);
}
return this;
},_setParent:function(_18,_19){
this.parent=_18;
return this._updateParentMatrix(_19);
},_updateParentMatrix:function(_1a){
this.parentMatrix=_1a?_9.clone(_1a):null;
return this._applyTransform();
},_getRealMatrix:function(){
var m=this.matrix;
var p=this.parent;
while(p){
if(p.matrix){
m=_9.multiply(p.matrix,m);
}
p=p.parent;
}
return m;
}});
_a._eventsProcessing={connect:function(_1b,_1c,_1d){
return _5.connect(this.getEventSource(),_1b,_a.fixCallback(this,g.fixTarget,_1c,_1d));
},disconnect:function(_1e){
_5.disconnect(_1e);
}};
_a.fixCallback=function(_1f,_20,_21,_22){
if(!_22){
_22=_21;
_21=null;
}
if(_1.isString(_22)){
_21=_21||_3.global;
if(!_21[_22]){
throw (["dojox.gfx.shape.fixCallback: scope[\"",_22,"\"] is null (scope=\"",_21,"\")"].join(""));
}
return function(e){
return _20(e,_1f)?_21[_22].apply(_21,arguments||[]):undefined;
};
}
return !_21?function(e){
return _20(e,_1f)?_22.apply(_21,arguments):undefined;
}:function(e){
return _20(e,_1f)?_22.apply(_21,arguments||[]):undefined;
};
};
_1.extend(_a.Shape,_a._eventsProcessing);
_a.Container={_init:function(){
this.children=[];
},openBatch:function(){
},closeBatch:function(){
},add:function(_23){
var _24=_23.getParent();
if(_24){
_24.remove(_23,true);
}
this.children.push(_23);
return _23._setParent(this,this._getRealMatrix());
},remove:function(_25,_26){
for(var i=0;i<this.children.length;++i){
if(this.children[i]==_25){
if(_26){
}else{
_25.parent=null;
_25.parentMatrix=null;
}
this.children.splice(i,1);
break;
}
}
return this;
},clear:function(_27){
var _28;
for(var i=0;i<this.children.length;++i){
_28=this.children[i];
_28.parent=null;
_28.parentMatrix=null;
if(_27){
_28.destroy();
}
}
this.children=[];
return this;
},getBoundingBox:function(){
if(this.children){
var _29=null;
_6.forEach(this.children,function(_2a){
var bb=_2a.getBoundingBox();
if(bb){
var ct=_2a.getTransform();
if(ct){
bb=_9.multiplyRectangle(ct,bb);
}
if(_29){
_29.x=Math.min(_29.x,bb.x);
_29.y=Math.min(_29.y,bb.y);
_29.endX=Math.max(_29.endX,bb.x+bb.width);
_29.endY=Math.max(_29.endY,bb.y+bb.height);
}else{
_29={x:bb.x,y:bb.y,endX:bb.x+bb.width,endY:bb.y+bb.height};
}
}
});
if(_29){
_29.width=_29.endX-_29.x;
_29.height=_29.endY-_29.y;
}
return _29;
}
return null;
},_moveChildToFront:function(_2b){
for(var i=0;i<this.children.length;++i){
if(this.children[i]==_2b){
this.children.splice(i,1);
this.children.push(_2b);
break;
}
}
return this;
},_moveChildToBack:function(_2c){
for(var i=0;i<this.children.length;++i){
if(this.children[i]==_2c){
this.children.splice(i,1);
this.children.unshift(_2c);
break;
}
}
return this;
}};
_a.Surface=_2("dojox.gfx.shape.Surface",null,{constructor:function(){
this.rawNode=null;
this._parent=null;
this._nodes=[];
this._events=[];
},destroy:function(){
_6.forEach(this._nodes,_7.destroy);
this._nodes=[];
_6.forEach(this._events,_5.disconnect);
this._events=[];
this.rawNode=null;
if(_4("ie")){
while(this._parent.lastChild){
_7.destroy(this._parent.lastChild);
}
}else{
this._parent.innerHTML="";
}
this._parent=null;
},getEventSource:function(){
return this.rawNode;
},_getRealMatrix:function(){
return null;
},isLoaded:true,onLoad:function(_2d){
},whenLoaded:function(_2e,_2f){
var f=_1.hitch(_2e,_2f);
if(this.isLoaded){
f(this);
}else{
var h=_5.connect(this,"onLoad",function(_30){
_5.disconnect(h);
f(_30);
});
}
}});
_1.extend(_a.Surface,_a._eventsProcessing);
_a.Rect=_2("dojox.gfx.shape.Rect",_a.Shape,{constructor:function(_31){
this.shape=g.getDefault("Rect");
this.rawNode=_31;
},getBoundingBox:function(){
return this.shape;
}});
_a.Ellipse=_2("dojox.gfx.shape.Ellipse",_a.Shape,{constructor:function(_32){
this.shape=g.getDefault("Ellipse");
this.rawNode=_32;
},getBoundingBox:function(){
if(!this.bbox){
var _33=this.shape;
this.bbox={x:_33.cx-_33.rx,y:_33.cy-_33.ry,width:2*_33.rx,height:2*_33.ry};
}
return this.bbox;
}});
_a.Circle=_2("dojox.gfx.shape.Circle",_a.Shape,{constructor:function(_34){
this.shape=g.getDefault("Circle");
this.rawNode=_34;
},getBoundingBox:function(){
if(!this.bbox){
var _35=this.shape;
this.bbox={x:_35.cx-_35.r,y:_35.cy-_35.r,width:2*_35.r,height:2*_35.r};
}
return this.bbox;
}});
_a.Line=_2("dojox.gfx.shape.Line",_a.Shape,{constructor:function(_36){
this.shape=g.getDefault("Line");
this.rawNode=_36;
},getBoundingBox:function(){
if(!this.bbox){
var _37=this.shape;
this.bbox={x:Math.min(_37.x1,_37.x2),y:Math.min(_37.y1,_37.y2),width:Math.abs(_37.x2-_37.x1),height:Math.abs(_37.y2-_37.y1)};
}
return this.bbox;
}});
_a.Polyline=_2("dojox.gfx.shape.Polyline",_a.Shape,{constructor:function(_38){
this.shape=g.getDefault("Polyline");
this.rawNode=_38;
},setShape:function(_39,_3a){
if(_39&&_39 instanceof Array){
this.inherited(arguments,[{points:_39}]);
if(_3a&&this.shape.points.length){
this.shape.points.push(this.shape.points[0]);
}
}else{
this.inherited(arguments,[_39]);
}
return this;
},_normalizePoints:function(){
var p=this.shape.points,l=p&&p.length;
if(l&&typeof p[0]=="number"){
var _3b=[];
for(var i=0;i<l;i+=2){
_3b.push({x:p[i],y:p[i+1]});
}
this.shape.points=_3b;
}
},getBoundingBox:function(){
if(!this.bbox&&this.shape.points.length){
var p=this.shape.points;
var l=p.length;
var t=p[0];
var _3c={l:t.x,t:t.y,r:t.x,b:t.y};
for(var i=1;i<l;++i){
t=p[i];
if(_3c.l>t.x){
_3c.l=t.x;
}
if(_3c.r<t.x){
_3c.r=t.x;
}
if(_3c.t>t.y){
_3c.t=t.y;
}
if(_3c.b<t.y){
_3c.b=t.y;
}
}
this.bbox={x:_3c.l,y:_3c.t,width:_3c.r-_3c.l,height:_3c.b-_3c.t};
}
return this.bbox;
}});
_a.Image=_2("dojox.gfx.shape.Image",_a.Shape,{constructor:function(_3d){
this.shape=g.getDefault("Image");
this.rawNode=_3d;
},getBoundingBox:function(){
return this.shape;
},setStroke:function(){
return this;
},setFill:function(){
return this;
}});
_a.Text=_2(_a.Shape,{constructor:function(_3e){
this.fontStyle=null;
this.shape=g.getDefault("Text");
this.rawNode=_3e;
},getFont:function(){
return this.fontStyle;
},setFont:function(_3f){
this.fontStyle=typeof _3f=="string"?g.splitFontString(_3f):g.makeParameters(g.defaultFont,_3f);
this._setFont();
return this;
}});
_a.Creator={createShape:function(_40){
switch(_40.type){
case g.defaultPath.type:
return this.createPath(_40);
case g.defaultRect.type:
return this.createRect(_40);
case g.defaultCircle.type:
return this.createCircle(_40);
case g.defaultEllipse.type:
return this.createEllipse(_40);
case g.defaultLine.type:
return this.createLine(_40);
case g.defaultPolyline.type:
return this.createPolyline(_40);
case g.defaultImage.type:
return this.createImage(_40);
case g.defaultText.type:
return this.createText(_40);
case g.defaultTextPath.type:
return this.createTextPath(_40);
}
return null;
},createGroup:function(){
return this.createObject(g.Group);
},createRect:function(_41){
return this.createObject(g.Rect,_41);
},createEllipse:function(_42){
return this.createObject(g.Ellipse,_42);
},createCircle:function(_43){
return this.createObject(g.Circle,_43);
},createLine:function(_44){
return this.createObject(g.Line,_44);
},createPolyline:function(_45){
return this.createObject(g.Polyline,_45);
},createImage:function(_46){
return this.createObject(g.Image,_46);
},createText:function(_47){
return this.createObject(g.Text,_47);
},createPath:function(_48){
return this.createObject(g.Path,_48);
},createTextPath:function(_49){
return this.createObject(g.TextPath,{}).setText(_49);
},createObject:function(_4a,_4b){
return null;
}};
return _a;
});

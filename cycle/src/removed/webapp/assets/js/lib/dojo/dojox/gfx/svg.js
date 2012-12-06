//>>built
define("dojox/gfx/svg",["dojo/_base/lang","dojo/_base/window","dojo/dom","dojo/_base/declare","dojo/_base/array","dojo/dom-geometry","dojo/dom-attr","dojo/_base/Color","./_base","./shape","./path"],function(_1,_2,_3,_4,_5,_6,_7,_8,g,gs,_9){
var _a=g.svg={};
_a.useSvgWeb=(typeof window.svgweb!="undefined");
var _b=navigator.userAgent.toLowerCase(),_c=_b.search("iphone")>-1||_b.search("ipad")>-1||_b.search("ipod")>-1;
function _d(ns,_e){
if(_2.doc.createElementNS){
return _2.doc.createElementNS(ns,_e);
}else{
return _2.doc.createElement(_e);
}
};
function _f(_10,ns,_11,_12){
if(_10.setAttributeNS){
return _10.setAttributeNS(ns,_11,_12);
}else{
return _10.setAttribute(_11,_12);
}
};
function _13(_14){
if(_a.useSvgWeb){
return _2.doc.createTextNode(_14,true);
}else{
return _2.doc.createTextNode(_14);
}
};
function _15(){
if(_a.useSvgWeb){
return _2.doc.createDocumentFragment(true);
}else{
return _2.doc.createDocumentFragment();
}
};
_a.xmlns={xlink:"http://www.w3.org/1999/xlink",svg:"http://www.w3.org/2000/svg"};
_a.getRef=function(_16){
if(!_16||_16=="none"){
return null;
}
if(_16.match(/^url\(#.+\)$/)){
return _3.byId(_16.slice(5,-1));
}
if(_16.match(/^#dojoUnique\d+$/)){
return _3.byId(_16.slice(1));
}
return null;
};
_a.dasharray={solid:"none",shortdash:[4,1],shortdot:[1,1],shortdashdot:[4,1,1,1],shortdashdotdot:[4,1,1,1,1,1],dot:[1,3],dash:[4,3],longdash:[8,3],dashdot:[4,3,1,3],longdashdot:[8,3,1,3],longdashdotdot:[8,3,1,3,1,3]};
var _17=0;
_a.Shape=_4("dojox.gfx.svg.Shape",gs.Shape,{destroy:function(){
if(this.fillStyle&&"type" in this.fillStyle){
var _18=this.rawNode.getAttribute("fill"),ref=_a.getRef(_18);
if(ref){
ref.parentNode.removeChild(ref);
}
}
if(this.clip){
var _19=this.rawNode.getAttribute("clip-path");
if(_19){
var _1a=_3.byId(_19.match(/gfx_clip[\d]+/)[0]);
_1a&&_1a.parentNode.removeChild(_1a);
}
}
this.rawNode=null;
gs.Shape.prototype.destroy.apply(this,arguments);
},setFill:function(_1b){
if(!_1b){
this.fillStyle=null;
this.rawNode.setAttribute("fill","none");
this.rawNode.setAttribute("fill-opacity",0);
return this;
}
var f;
var _1c=function(x){
this.setAttribute(x,f[x].toFixed(8));
};
if(typeof (_1b)=="object"&&"type" in _1b){
switch(_1b.type){
case "linear":
f=g.makeParameters(g.defaultLinearGradient,_1b);
var _1d=this._setFillObject(f,"linearGradient");
_5.forEach(["x1","y1","x2","y2"],_1c,_1d);
break;
case "radial":
f=g.makeParameters(g.defaultRadialGradient,_1b);
var _1e=this._setFillObject(f,"radialGradient");
_5.forEach(["cx","cy","r"],_1c,_1e);
break;
case "pattern":
f=g.makeParameters(g.defaultPattern,_1b);
var _1f=this._setFillObject(f,"pattern");
_5.forEach(["x","y","width","height"],_1c,_1f);
break;
}
this.fillStyle=f;
return this;
}
f=g.normalizeColor(_1b);
this.fillStyle=f;
this.rawNode.setAttribute("fill",f.toCss());
this.rawNode.setAttribute("fill-opacity",f.a);
this.rawNode.setAttribute("fill-rule","evenodd");
return this;
},setStroke:function(_20){
var rn=this.rawNode;
if(!_20){
this.strokeStyle=null;
rn.setAttribute("stroke","none");
rn.setAttribute("stroke-opacity",0);
return this;
}
if(typeof _20=="string"||_1.isArray(_20)||_20 instanceof _8){
_20={color:_20};
}
var s=this.strokeStyle=g.makeParameters(g.defaultStroke,_20);
s.color=g.normalizeColor(s.color);
if(s){
rn.setAttribute("stroke",s.color.toCss());
rn.setAttribute("stroke-opacity",s.color.a);
rn.setAttribute("stroke-width",s.width);
rn.setAttribute("stroke-linecap",s.cap);
if(typeof s.join=="number"){
rn.setAttribute("stroke-linejoin","miter");
rn.setAttribute("stroke-miterlimit",s.join);
}else{
rn.setAttribute("stroke-linejoin",s.join);
}
var da=s.style.toLowerCase();
if(da in _a.dasharray){
da=_a.dasharray[da];
}
if(da instanceof Array){
da=_1._toArray(da);
for(var i=0;i<da.length;++i){
da[i]*=s.width;
}
if(s.cap!="butt"){
for(var i=0;i<da.length;i+=2){
da[i]-=s.width;
if(da[i]<1){
da[i]=1;
}
}
for(var i=1;i<da.length;i+=2){
da[i]+=s.width;
}
}
da=da.join(",");
}
rn.setAttribute("stroke-dasharray",da);
rn.setAttribute("dojoGfxStrokeStyle",s.style);
}
return this;
},_getParentSurface:function(){
var _21=this.parent;
for(;_21&&!(_21 instanceof g.Surface);_21=_21.parent){
}
return _21;
},_setFillObject:function(f,_22){
var _23=_a.xmlns.svg;
this.fillStyle=f;
var _24=this._getParentSurface(),_25=_24.defNode,_26=this.rawNode.getAttribute("fill"),ref=_a.getRef(_26);
if(ref){
_26=ref;
if(_26.tagName.toLowerCase()!=_22.toLowerCase()){
var id=_26.id;
_26.parentNode.removeChild(_26);
_26=_d(_23,_22);
_26.setAttribute("id",id);
_25.appendChild(_26);
}else{
while(_26.childNodes.length){
_26.removeChild(_26.lastChild);
}
}
}else{
_26=_d(_23,_22);
_26.setAttribute("id",g._base._getUniqueId());
_25.appendChild(_26);
}
if(_22=="pattern"){
_26.setAttribute("patternUnits","userSpaceOnUse");
var img=_d(_23,"image");
img.setAttribute("x",0);
img.setAttribute("y",0);
img.setAttribute("width",f.width.toFixed(8));
img.setAttribute("height",f.height.toFixed(8));
_f(img,_a.xmlns.xlink,"xlink:href",f.src);
_26.appendChild(img);
}else{
_26.setAttribute("gradientUnits","userSpaceOnUse");
for(var i=0;i<f.colors.length;++i){
var c=f.colors[i],t=_d(_23,"stop"),cc=c.color=g.normalizeColor(c.color);
t.setAttribute("offset",c.offset.toFixed(8));
t.setAttribute("stop-color",cc.toCss());
t.setAttribute("stop-opacity",cc.a);
_26.appendChild(t);
}
}
this.rawNode.setAttribute("fill","url(#"+_26.getAttribute("id")+")");
this.rawNode.removeAttribute("fill-opacity");
this.rawNode.setAttribute("fill-rule","evenodd");
return _26;
},_applyTransform:function(){
var _27=this.matrix;
if(_27){
var tm=this.matrix;
this.rawNode.setAttribute("transform","matrix("+tm.xx.toFixed(8)+","+tm.yx.toFixed(8)+","+tm.xy.toFixed(8)+","+tm.yy.toFixed(8)+","+tm.dx.toFixed(8)+","+tm.dy.toFixed(8)+")");
}else{
this.rawNode.removeAttribute("transform");
}
return this;
},setRawNode:function(_28){
var r=this.rawNode=_28;
if(this.shape.type!="image"){
r.setAttribute("fill","none");
}
r.setAttribute("fill-opacity",0);
r.setAttribute("stroke","none");
r.setAttribute("stroke-opacity",0);
r.setAttribute("stroke-width",1);
r.setAttribute("stroke-linecap","butt");
r.setAttribute("stroke-linejoin","miter");
r.setAttribute("stroke-miterlimit",4);
r.__gfxObject__=this.getUID();
},setShape:function(_29){
this.shape=g.makeParameters(this.shape,_29);
for(var i in this.shape){
if(i!="type"){
this.rawNode.setAttribute(i,this.shape[i]);
}
}
this.bbox=null;
return this;
},_moveToFront:function(){
this.rawNode.parentNode.appendChild(this.rawNode);
return this;
},_moveToBack:function(){
this.rawNode.parentNode.insertBefore(this.rawNode,this.rawNode.parentNode.firstChild);
return this;
},setClip:function(_2a){
this.inherited(arguments);
var _2b=_2a?"width" in _2a?"rect":"cx" in _2a?"ellipse":"points" in _2a?"polyline":"d" in _2a?"path":null:null;
if(_2a&&!_2b){
return this;
}
if(_2b==="polyline"){
_2a=_1.clone(_2a);
_2a.points=_2a.points.join(",");
}
var _2c,_2d,_2e=_7.get(this.rawNode,"clip-path");
if(_2e){
_2c=_3.byId(_2e.match(/gfx_clip[\d]+/)[0]);
if(_2c){
_2c.removeChild(_2c.childNodes[0]);
}
}
if(_2a){
if(_2c){
_2d=_d(_a.xmlns.svg,_2b);
_2c.appendChild(_2d);
}else{
var _2f=++_17;
var _30="gfx_clip"+_2f;
var _31="url(#"+_30+")";
this.rawNode.setAttribute("clip-path",_31);
_2c=_d(_a.xmlns.svg,"clipPath");
_2d=_d(_a.xmlns.svg,_2b);
_2c.appendChild(_2d);
this.rawNode.parentNode.appendChild(_2c);
_7.set(_2c,"id",_30);
}
_7.set(_2d,_2a);
}else{
this.rawNode.removeAttribute("clip-path");
if(_2c){
_2c.parentNode.removeChild(_2c);
}
}
return this;
},_removeClipNode:function(){
var _32,_33=_7.get(this.rawNode,"clip-path");
if(_33){
_32=_3.byId(_33.match(/gfx_clip[\d]+/)[0]);
if(_32){
_32.parentNode.removeChild(_32);
}
}
return _32;
}});
_a.Group=_4("dojox.gfx.svg.Group",_a.Shape,{constructor:function(){
gs.Container._init.call(this);
},setRawNode:function(_34){
this.rawNode=_34;
this.rawNode.__gfxObject__=this.getUID();
},destroy:function(){
this.clear(true);
_a.Shape.prototype.destroy.apply(this,arguments);
}});
_a.Group.nodeType="g";
_a.Rect=_4("dojox.gfx.svg.Rect",[_a.Shape,gs.Rect],{setShape:function(_35){
this.shape=g.makeParameters(this.shape,_35);
this.bbox=null;
for(var i in this.shape){
if(i!="type"&&i!="r"){
this.rawNode.setAttribute(i,this.shape[i]);
}
}
if(this.shape.r!=null){
this.rawNode.setAttribute("ry",this.shape.r);
this.rawNode.setAttribute("rx",this.shape.r);
}
return this;
}});
_a.Rect.nodeType="rect";
_a.Ellipse=_4("dojox.gfx.svg.Ellipse",[_a.Shape,gs.Ellipse],{});
_a.Ellipse.nodeType="ellipse";
_a.Circle=_4("dojox.gfx.svg.Circle",[_a.Shape,gs.Circle],{});
_a.Circle.nodeType="circle";
_a.Line=_4("dojox.gfx.svg.Line",[_a.Shape,gs.Line],{});
_a.Line.nodeType="line";
_a.Polyline=_4("dojox.gfx.svg.Polyline",[_a.Shape,gs.Polyline],{setShape:function(_36,_37){
if(_36&&_36 instanceof Array){
this.shape=g.makeParameters(this.shape,{points:_36});
if(_37&&this.shape.points.length){
this.shape.points.push(this.shape.points[0]);
}
}else{
this.shape=g.makeParameters(this.shape,_36);
}
this.bbox=null;
this._normalizePoints();
var _38=[],p=this.shape.points;
for(var i=0;i<p.length;++i){
_38.push(p[i].x.toFixed(8),p[i].y.toFixed(8));
}
this.rawNode.setAttribute("points",_38.join(" "));
return this;
}});
_a.Polyline.nodeType="polyline";
_a.Image=_4("dojox.gfx.svg.Image",[_a.Shape,gs.Image],{setShape:function(_39){
this.shape=g.makeParameters(this.shape,_39);
this.bbox=null;
var _3a=this.rawNode;
for(var i in this.shape){
if(i!="type"&&i!="src"){
_3a.setAttribute(i,this.shape[i]);
}
}
_3a.setAttribute("preserveAspectRatio","none");
_f(_3a,_a.xmlns.xlink,"xlink:href",this.shape.src);
_3a.__gfxObject__=this.getUID();
return this;
}});
_a.Image.nodeType="image";
_a.Text=_4("dojox.gfx.svg.Text",[_a.Shape,gs.Text],{setShape:function(_3b){
this.shape=g.makeParameters(this.shape,_3b);
this.bbox=null;
var r=this.rawNode,s=this.shape;
r.setAttribute("x",s.x);
r.setAttribute("y",s.y);
r.setAttribute("text-anchor",s.align);
r.setAttribute("text-decoration",s.decoration);
r.setAttribute("rotate",s.rotated?90:0);
r.setAttribute("kerning",s.kerning?"auto":0);
r.setAttribute("text-rendering","optimizeLegibility");
if(r.firstChild){
r.firstChild.nodeValue=s.text;
}else{
r.appendChild(_13(s.text));
}
return this;
},getTextWidth:function(){
var _3c=this.rawNode,_3d=_3c.parentNode,_3e=_3c.cloneNode(true);
_3e.style.visibility="hidden";
var _3f=0,_40=_3e.firstChild.nodeValue;
_3d.appendChild(_3e);
if(_40!=""){
while(!_3f){
if(_3e.getBBox){
_3f=parseInt(_3e.getBBox().width);
}else{
_3f=68;
}
}
}
_3d.removeChild(_3e);
return _3f;
}});
_a.Text.nodeType="text";
_a.Path=_4("dojox.gfx.svg.Path",[_a.Shape,_9.Path],{_updateWithSegment:function(_41){
this.inherited(arguments);
if(typeof (this.shape.path)=="string"){
this.rawNode.setAttribute("d",this.shape.path);
}
},setShape:function(_42){
this.inherited(arguments);
if(this.shape.path){
this.rawNode.setAttribute("d",this.shape.path);
}else{
this.rawNode.removeAttribute("d");
}
return this;
}});
_a.Path.nodeType="path";
_a.TextPath=_4("dojox.gfx.svg.TextPath",[_a.Shape,_9.TextPath],{_updateWithSegment:function(_43){
this.inherited(arguments);
this._setTextPath();
},setShape:function(_44){
this.inherited(arguments);
this._setTextPath();
return this;
},_setTextPath:function(){
if(typeof this.shape.path!="string"){
return;
}
var r=this.rawNode;
if(!r.firstChild){
var tp=_d(_a.xmlns.svg,"textPath"),tx=_13("");
tp.appendChild(tx);
r.appendChild(tp);
}
var ref=r.firstChild.getAttributeNS(_a.xmlns.xlink,"href"),_45=ref&&_a.getRef(ref);
if(!_45){
var _46=this._getParentSurface();
if(_46){
var _47=_46.defNode;
_45=_d(_a.xmlns.svg,"path");
var id=g._base._getUniqueId();
_45.setAttribute("id",id);
_47.appendChild(_45);
_f(r.firstChild,_a.xmlns.xlink,"xlink:href","#"+id);
}
}
if(_45){
_45.setAttribute("d",this.shape.path);
}
},_setText:function(){
var r=this.rawNode;
if(!r.firstChild){
var tp=_d(_a.xmlns.svg,"textPath"),tx=_13("");
tp.appendChild(tx);
r.appendChild(tp);
}
r=r.firstChild;
var t=this.text;
r.setAttribute("alignment-baseline","middle");
switch(t.align){
case "middle":
r.setAttribute("text-anchor","middle");
r.setAttribute("startOffset","50%");
break;
case "end":
r.setAttribute("text-anchor","end");
r.setAttribute("startOffset","100%");
break;
default:
r.setAttribute("text-anchor","start");
r.setAttribute("startOffset","0%");
break;
}
r.setAttribute("baseline-shift","0.5ex");
r.setAttribute("text-decoration",t.decoration);
r.setAttribute("rotate",t.rotated?90:0);
r.setAttribute("kerning",t.kerning?"auto":0);
r.firstChild.data=t.text;
}});
_a.TextPath.nodeType="text";
_a.Surface=_4("dojox.gfx.svg.Surface",gs.Surface,{constructor:function(){
gs.Container._init.call(this);
},destroy:function(){
this.defNode=null;
this.inherited(arguments);
},setDimensions:function(_48,_49){
if(!this.rawNode){
return this;
}
this.rawNode.setAttribute("width",_48);
this.rawNode.setAttribute("height",_49);
return this;
},getDimensions:function(){
var t=this.rawNode?{width:g.normalizedLength(this.rawNode.getAttribute("width")),height:g.normalizedLength(this.rawNode.getAttribute("height"))}:null;
return t;
}});
_a.createSurface=function(_4a,_4b,_4c){
var s=new _a.Surface();
s.rawNode=_d(_a.xmlns.svg,"svg");
s.rawNode.setAttribute("overflow","hidden");
if(_4b){
s.rawNode.setAttribute("width",_4b);
}
if(_4c){
s.rawNode.setAttribute("height",_4c);
}
var _4d=_d(_a.xmlns.svg,"defs");
s.rawNode.appendChild(_4d);
s.defNode=_4d;
s._parent=_3.byId(_4a);
s._parent.appendChild(s.rawNode);
return s;
};
var _4e={_setFont:function(){
var f=this.fontStyle;
this.rawNode.setAttribute("font-style",f.style);
this.rawNode.setAttribute("font-variant",f.variant);
this.rawNode.setAttribute("font-weight",f.weight);
this.rawNode.setAttribute("font-size",f.size);
this.rawNode.setAttribute("font-family",f.family);
}};
var C=gs.Container,_4f={openBatch:function(){
this.fragment=_15();
},closeBatch:function(){
if(this.fragment){
this.rawNode.appendChild(this.fragment);
delete this.fragment;
}
},add:function(_50){
if(this!=_50.getParent()){
if(this.fragment){
this.fragment.appendChild(_50.rawNode);
}else{
this.rawNode.appendChild(_50.rawNode);
}
C.add.apply(this,arguments);
_50.setClip(_50.clip);
}
return this;
},remove:function(_51,_52){
if(this==_51.getParent()){
if(this.rawNode==_51.rawNode.parentNode){
this.rawNode.removeChild(_51.rawNode);
}
if(this.fragment&&this.fragment==_51.rawNode.parentNode){
this.fragment.removeChild(_51.rawNode);
}
_51._removeClipNode();
C.remove.apply(this,arguments);
}
return this;
},clear:function(){
var r=this.rawNode;
while(r.lastChild){
r.removeChild(r.lastChild);
}
var _53=this.defNode;
if(_53){
while(_53.lastChild){
_53.removeChild(_53.lastChild);
}
r.appendChild(_53);
}
return C.clear.apply(this,arguments);
},getBoundingBox:C.getBoundingBox,_moveChildToFront:C._moveChildToFront,_moveChildToBack:C._moveChildToBack};
var _54={createObject:function(_55,_56){
if(!this.rawNode){
return null;
}
var _57=new _55(),_58=_d(_a.xmlns.svg,_55.nodeType);
_57.setRawNode(_58);
_57.setShape(_56);
this.add(_57);
return _57;
}};
_1.extend(_a.Text,_4e);
_1.extend(_a.TextPath,_4e);
_1.extend(_a.Group,_4f);
_1.extend(_a.Group,gs.Creator);
_1.extend(_a.Group,_54);
_1.extend(_a.Surface,_4f);
_1.extend(_a.Surface,gs.Creator);
_1.extend(_a.Surface,_54);
_a.fixTarget=function(_59,_5a){
if(!_59.gfxTarget){
if(_c&&_59.target.wholeText){
_59.gfxTarget=gs.byId(_59.target.parentElement.__gfxObject__);
}else{
_59.gfxTarget=gs.byId(_59.target.__gfxObject__);
}
}
return true;
};
if(_a.useSvgWeb){
_a.createSurface=function(_5b,_5c,_5d){
var s=new _a.Surface();
if(!_5c||!_5d){
var pos=_6.position(_5b);
_5c=_5c||pos.w;
_5d=_5d||pos.h;
}
_5b=_3.byId(_5b);
var id=_5b.id?_5b.id+"_svgweb":g._base._getUniqueId();
var _5e=_d(_a.xmlns.svg,"svg");
_5e.id=id;
_5e.setAttribute("width",_5c);
_5e.setAttribute("height",_5d);
svgweb.appendChild(_5e,_5b);
_5e.addEventListener("SVGLoad",function(){
s.rawNode=this;
s.isLoaded=true;
var _5f=_d(_a.xmlns.svg,"defs");
s.rawNode.appendChild(_5f);
s.defNode=_5f;
if(s.onLoad){
s.onLoad(s);
}
},false);
s.isLoaded=false;
return s;
};
_a.Surface.extend({destroy:function(){
var _60=this.rawNode;
svgweb.removeChild(_60,_60.parentNode);
}});
var _61={connect:function(_62,_63,_64){
if(_62.substring(0,2)==="on"){
_62=_62.substring(2);
}
if(arguments.length==2){
_64=_63;
}else{
_64=_1.hitch(_63,_64);
}
this.getEventSource().addEventListener(_62,_64,false);
return [this,_62,_64];
},disconnect:function(_65){
this.getEventSource().removeEventListener(_65[1],_65[2],false);
delete _65[0];
}};
_1.extend(_a.Shape,_61);
_1.extend(_a.Surface,_61);
}
return _a;
});

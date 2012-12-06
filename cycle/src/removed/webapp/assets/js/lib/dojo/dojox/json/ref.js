//>>built
define("dojox/json/ref",["dojo/_base/array","dojo/_base/json","dojo/_base/kernel","dojo/_base/lang","dojo/date/stamp","dojox"],function(_1,_2,_3,_4,_5,_6){
_4.getObject("json",true,_6);
return _6.json.ref={resolveJson:function(_7,_8){
_8=_8||{};
var _9=_8.idAttribute||"id";
var _a=this.refAttribute;
var _b=_8.idAsRef;
var _c=_8.idPrefix||"";
var _d=_8.assignAbsoluteIds;
var _e=_8.index||{};
var _f=_8.timeStamps;
var ref,_10=[];
var _11=/^(.*\/)?(\w+:\/\/)|[^\/\.]+\/\.\.\/|^.*\/(\/)/;
var _12=this._addProp;
var F=function(){
};
function _13(it,_14,_15,_16,_17,_18){
var i,_19,val,id=_9 in it?it[_9]:_15;
if(_9 in it||((id!==undefined)&&_16)){
id=(_c+id).replace(_11,"$2$3");
}
var _1a=_18||it;
if(id!==undefined){
if(_d){
it.__id=id;
}
if(_8.schemas&&(!(it instanceof Array))&&(val=id.match(/^(.+\/)[^\.\[]*$/))){
_17=_8.schemas[val[1]];
}
if(_e[id]&&((it instanceof Array)==(_e[id] instanceof Array))){
_1a=_e[id];
delete _1a.$ref;
delete _1a._loadObject;
_19=true;
}else{
var _1b=_17&&_17.prototype;
if(_1b){
F.prototype=_1b;
_1a=new F();
}
}
_e[id]=_1a;
if(_f){
_f[id]=_8.time;
}
}
while(_17){
var _1c=_17.properties;
if(_1c){
for(i in it){
var _1d=_1c[i];
if(_1d&&_1d.format=="date-time"&&typeof it[i]=="string"){
it[i]=_5.fromISOString(it[i]);
}
}
}
_17=_17["extends"];
}
var _1e=it.length;
for(i in it){
if(i==_1e){
break;
}
if(it.hasOwnProperty(i)){
val=it[i];
if((typeof val=="object")&&val&&!(val instanceof Date)&&i!="__parent"){
ref=val[_a]||(_b&&val[_9]);
if(!ref||!val.__parent){
if(it!=_10){
val.__parent=_1a;
}
}
if(ref){
delete it[i];
var _1f=ref.toString().replace(/(#)([^\.\[])/,"$1.$2").match(/(^([^\[]*\/)?[^#\.\[]*)#?([\.\[].*)?/);
if(_e[(_c+ref).replace(_11,"$2$3")]){
ref=_e[(_c+ref).replace(_11,"$2$3")];
}else{
if((ref=(_1f[1]=="$"||_1f[1]=="this"||_1f[1]=="")?_7:_e[(_c+_1f[1]).replace(_11,"$2$3")])){
if(_1f[3]){
_1f[3].replace(/(\[([^\]]+)\])|(\.?([^\.\[]+))/g,function(t,a,b,c,d){
ref=ref&&ref[b?b.replace(/[\"\'\\]/,""):d];
});
}
}
}
if(ref){
val=ref;
}else{
if(!_14){
var _20;
if(!_20){
_10.push(_1a);
}
_20=true;
val=_13(val,false,val[_a],true,_1d);
val._loadObject=_8.loader;
}
}
}else{
if(!_14){
val=_13(val,_10==it,id===undefined?undefined:_12(id,i),false,_1d,_1a!=it&&typeof _1a[i]=="object"&&_1a[i]);
}
}
}
it[i]=val;
if(_1a!=it&&!_1a.__isDirty){
var old=_1a[i];
_1a[i]=val;
if(_19&&val!==old&&!_1a._loadObject&&!(i.charAt(0)=="_"&&i.charAt(1)=="_")&&i!="$ref"&&!(val instanceof Date&&old instanceof Date&&val.getTime()==old.getTime())&&!(typeof val=="function"&&typeof old=="function"&&val.toString()==old.toString())&&_e.onUpdate){
_e.onUpdate(_1a,i,old,val);
}
}
}
}
if(_19&&(_9 in it||_1a instanceof Array)){
for(i in _1a){
if(!_1a.__isDirty&&_1a.hasOwnProperty(i)&&!it.hasOwnProperty(i)&&!(i.charAt(0)=="_"&&i.charAt(1)=="_")&&!(_1a instanceof Array&&isNaN(i))){
if(_e.onUpdate&&i!="_loadObject"&&i!="_idAttr"){
_e.onUpdate(_1a,i,_1a[i],undefined);
}
delete _1a[i];
while(_1a instanceof Array&&_1a.length&&_1a[_1a.length-1]===undefined){
_1a.length--;
}
}
}
}else{
if(_e.onLoad){
_e.onLoad(_1a);
}
}
return _1a;
};
if(_7&&typeof _7=="object"){
_7=_13(_7,false,_8.defaultId,true);
_13(_10,false);
}
return _7;
},fromJson:function(str,_21){
function ref(_22){
var _23={};
_23[this.refAttribute]=_22;
return _23;
};
try{
var _24=eval("("+str+")");
}
catch(e){
throw new SyntaxError("Invalid JSON string: "+e.message+" parsing: "+str);
}
if(_24){
return this.resolveJson(_24,_21);
}
return _24;
},toJson:function(it,_25,_26,_27){
var _28=this._useRefs;
var _29=this._addProp;
var _2a=this.refAttribute;
_26=_26||"";
var _2b={};
var _2c={};
function _2d(it,_2e,_2f){
if(typeof it=="object"&&it){
var _30;
if(it instanceof Date){
return "\""+_5.toISOString(it,{zulu:true})+"\"";
}
var id=it.__id;
if(id){
if(_2e!="#"&&((_28&&!id.match(/#/))||_2b[id])){
var ref=id;
if(id.charAt(0)!="#"){
if(it.__clientId==id){
ref="cid:"+id;
}else{
if(id.substring(0,_26.length)==_26){
ref=id.substring(_26.length);
}else{
ref=id;
}
}
}
var _31={};
_31[_2a]=ref;
return _2d(_31,"#");
}
_2e=id;
}else{
it.__id=_2e;
_2c[_2e]=it;
}
_2b[_2e]=it;
_2f=_2f||"";
var _32=_25?_2f+_2.toJsonIndentStr:"";
var _33=_25?"\n":"";
var sep=_25?" ":"";
if(it instanceof Array){
var res=_1.map(it,function(obj,i){
var val=_2d(obj,_29(_2e,i),_32);
if(typeof val!="string"){
val="undefined";
}
return _33+_32+val;
});
return "["+res.join(","+sep)+_33+_2f+"]";
}
var _34=[];
for(var i in it){
if(it.hasOwnProperty(i)){
var _35;
if(typeof i=="number"){
_35="\""+i+"\"";
}else{
if(typeof i=="string"&&(i.charAt(0)!="_"||i.charAt(1)!="_")){
_35=_2._escapeString(i);
}else{
continue;
}
}
var val=_2d(it[i],_29(_2e,i),_32);
if(typeof val!="string"){
continue;
}
_34.push(_33+_32+_35+":"+sep+val);
}
}
return "{"+_34.join(","+sep)+_33+_2f+"}";
}else{
if(typeof it=="function"&&_6.json.ref.serializeFunctions){
return it.toString();
}
}
return _2.toJson(it);
};
var _36=_2d(it,"#","");
if(!_27){
for(var i in _2c){
delete _2c[i].__id;
}
}
return _36;
},_addProp:function(id,_37){
return id+(id.match(/#/)?id.length==1?"":".":"#")+_37;
},refAttribute:"$ref",_useRefs:false,serializeFunctions:false};
});

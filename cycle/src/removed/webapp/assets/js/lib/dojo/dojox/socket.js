//>>built
define("dojox/socket",["dojo","dojo/on","dojo/Evented","dojo/cookie","dojo/_base/url"],function(_1,on,_2){
var _3=window.WebSocket;
function _4(_5){
if(typeof _5=="string"){
_5={url:_5};
}
return _3?dojox.socket.WebSocket(_5,true):dojox.socket.LongPoll(_5);
};
dojox.socket=_4;
_4.WebSocket=function(_6,_7){
var ws=new _3(new _1._Url(document.baseURI.replace(/^http/i,"ws"),_6.url));
ws.on=function(_8,_9){
ws.addEventListener(_8,_9,true);
};
var _a;
_1.connect(ws,"onopen",function(_b){
_a=true;
});
_1.connect(ws,"onclose",function(_c){
if(_a){
return;
}
if(_7){
_4.replace(ws,dojox.socket.LongPoll(_6),true);
}
});
return ws;
};
_4.replace=function(_d,_e,_f){
_d.send=_1.hitch(_e,"send");
_d.close=_1.hitch(_e,"close");
if(_f){
_10("open");
}
_1.forEach(["message","close","error"],_10);
function _10(_11){
(_e.addEventListener||_e.on).call(_e,_11,function(_12){
on.emit(_d,_12.type,_12);
},true);
};
};
_4.LongPoll=function(_13){
var _14=false,_15=true,_16,_17=[];
var _18={send:function(_19){
var _1a=_1.delegate(_13);
_1a.rawBody=_19;
clearTimeout(_16);
var _1b=_15?(_15=false)||_18.firstRequest(_1a):_18.transport(_1a);
_17.push(_1b);
_1b.then(function(_1c){
_18.readyState=1;
_17.splice(_1.indexOf(_17,_1b),1);
if(!_17.length){
_16=setTimeout(_21,_13.interval);
}
if(_1c){
_1e("message",{data:_1c},_1b);
}
},function(_1d){
_17.splice(_1.indexOf(_17,_1b),1);
if(!_14){
_1e("error",{error:_1d},_1b);
if(!_17.length){
_18.readyState=3;
_1e("close",{wasClean:false},_1b);
}
}
});
return _1b;
},close:function(){
_18.readyState=2;
_14=true;
for(var i=0;i<_17.length;i++){
_17[i].cancel();
}
_18.readyState=3;
_1e("close",{wasClean:true});
},transport:_13.transport||_1.xhrPost,args:_13,url:_13.url,readyState:0,CONNECTING:0,OPEN:1,CLOSING:2,CLOSED:3,on:_2.prototype.on,firstRequest:function(_1f){
var _20=(_1f.headers||(_1f.headers={}));
_20.Pragma="start-long-poll";
try{
return this.transport(_1f);
}
finally{
delete _20.Pragma;
}
}};
function _21(){
if(_18.readyState==0){
_1e("open",{});
}
if(!_17.length){
_18.send();
}
};
function _1e(_22,_23,_24){
if(_18["on"+_22]){
_23.ioArgs=_24&&_24.ioArgs;
_23.type=_22;
on.emit(_18,_22,_23);
}
};
_18.connect=_18.on;
setTimeout(_21);
return _18;
};
return _4;
});

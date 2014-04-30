// javaのgetter/setterを自動で作るマクロ
// なんか動きがダサい。要改良
(function(){
	if(Editor.IsTextSelected == 1){
		var buf = GetSelectedString;
		var nextLine = GetSelectLineTo() + 1;
		CancelMode();
		MoveCursor(nextLine,1,0);
		var reg = /(\w+|\w+<.*?>) (\w+);/g;
		var m = [];
		while(m = reg.exec(buf)){
			var type = m[1];
			var name = m[2];
			var upperName = name.charAt(0).toUpperCase() + name.slice(1);
			InsText("\r\n\tpublic " + type + " get" + upperName + "() {\r\n\t\treturn this." + name + ";\r\n\t}");
			InsText("\r\n\tpublic void set" + upperName + "(" + type + " " + name + ") {\r\n\t\tthis." + name + " = " + name + ";\r\n\t}");
		}
	}
})();
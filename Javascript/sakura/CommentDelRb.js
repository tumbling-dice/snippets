(function(){
	
	function hasComment(str) {
		str = str.replace(/^\s+/, "");
		return str.lastIndexOf("#", 0) == 0;
	};
	
	if(Editor.IsTextSelected() == 1 || Editor.IsTextSelected() == 2) {
		var lineTo = GetSelectLineTo();
		for(i = GetSelectLineFrom(); i <= lineTo; i++) {
			Jump(i, 1);
			GoLineTop(0);
			
			if(hasComment(GetLineStr(0))) {
				var s = GetLineStr(0).replace("#", "");
				SelectLine(0);
				Delete();
				InsText(s);
			}
		}
	} else {
		GoLineTop(0)
		if(hasComment(GetLineStr(0))) {
			var s = GetLineStr(0).replace("#", "");
			SelectLine(0);
			Delete();
			InsText(s);
		}
	}
	
	GoLineEnd();
})();
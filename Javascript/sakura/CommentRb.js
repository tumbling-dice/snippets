(function(){
	
	function hasStr(str) {
		str = str.replace(/\s/g, "");
		return str.length > 0;
	};
	
	if(Editor.IsTextSelected() == 1 || Editor.IsTextSelected() == 2) {
		var lineTo = GetSelectLineTo();
		for(i = GetSelectLineFrom(); i <= lineTo; i++) {
			Jump(i, 1);
			GoLineTop(0);
			
			if(hasStr(GetLineStr(0))) {
				InsText("#");
			}
		}
	} else {
		GoLineTop(0)
		if(hasStr(GetLineStr(0))) {
			InsText("#");
		}
	}
	
	GoLineEnd();
})();
// HTMLのエスケープ
(function()
{
    if(Editor.IsTextSelected == 1){
        var buf = GetSelectedString;
        buf = buf.replace(/&/g, "&amp;")
                 .replace(/</g, "&lt;")
                 .replace(/>/g, "&gt;")
                 .replace(/\"/g, "&quot;")
                 .replace(/'/g, "&apos;");
        Editor.InsText(buf);
    }
})();
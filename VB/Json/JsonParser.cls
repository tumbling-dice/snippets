Option Explicit
'********************
'JsonParser
'
'Jsonパース処理
'パースした結果はJsonObjectでラップされる
'
'注意：JsonObject→自作したJsonObjectと言うDictionaryのラッパークラス
'　　　Json(JScriptTypeInfo)→Javascriptでevalした結果のObject
'********************
 
 
Private m_js As Object  'javascript実行用オブジェクト
 
'********************
'コンストラクタ
'処理中で使用するJavascriptの定義を行う
'********************
Public Sub Class_Initialize()
    Set m_js = CreateObject("ScriptControl")
    m_js.Language = "JScript"
     
    'JsonをevalするJavactipt
    m_js.AddCode "function jsonParse(str) { return eval('(' + str + ')'); };"
     
    'Jsonからキーの配列を取得するJavascript
    m_js.AddCode "function getKeys(h) { var keys=[]; for(var k in h){keys.push(k);} return keys; };"
     
    'Jsonが配列かどうかを確認するJavascript
    m_js.AddCode "function isArray(o) { return o instanceof Array; };"
End Sub
 
'********************
'デストラクタ
'********************
Private Sub Class_Terminate()
    Set m_js = Nothing
End Sub
 
'********************
'パース処理呼び出し
'
'Argument：Jsonの形式になっている文字列(String)
'Return　：Collection(JsonObject)もしくはJsonObject
'　　　　　引数がJson形式でなかった場合はNothing
'********************
Public Function Parse(ByVal strJson As String) As Object
     
    Dim json As Object
     
    On Error GoTo ParseError
    Set json = m_js.codeobject.jsonParse(strJson)
    On Error GoTo 0
     
    'valueを解析した結果がJScriptTypeInfoかどうかを判定する
    'JScriptTypeInfo以外の場合はNothingを返す
    If IsJson(json) Then
        If IsJsonArray(json) Then
            Set Parse = JsonArrayToCollection(json)
        Else
            Set Parse = JsonToDictionary(json)
        End If
    Else
        Set Parse = Nothing
    End If
 
    Exit Function
     
ParseError:
     
    Debug.Print Err.Description
    Set Parse = Nothing
     
End Function
 
'********************
'Jsonの配列をCollectionに変換する
'
'Argument：Jsonの配列(JScriptTypeInfo)
'Return　：Key:Jsonで使用されているキー Value:JsonObjectのCollection
'********************
Private Function JsonArrayToCollection(ByVal json As Object) As Collection
 
    Dim col As New Collection
    Dim key As Variant
    Dim objJson As Object
    Dim varJson As Variant
    Dim jsonObj As JsonObject
    Dim jsonData As Dictionary
     
    For Each key In GetKeys(json)
         
        On Error GoTo VariantPattern
        Set objJson = GetObject(json, key)
         
         
        If IsJsonArray(objJson) Then
            '配列だった場合は再帰させる
            Call col.Add(JsonArrayToCollection(objJson), key)
        Else
            '一要素だった場合はJsonObjectをCollectionに追加する
            Call col.Add(JsonToDictionary(objJson), key)
        End If
         
        GoTo Continue
             
VariantPattern:
        On Error GoTo 0
        varJson = GetValue(json, key)
        Set jsonObj = New JsonObject
        Set jsonData = New Dictionary
         
        Call jsonData.Add(key, varJson)
        Call jsonObj.Init(jsonData)
        Call col.Add(jsonObj, key)
         
        Resume Continue
 
Continue:
    Next
     
    On Error GoTo 0
     
    Set JsonArrayToCollection = col
 
End Function
 
'********************
'Jsonの配列をDictionaryに変換する
'
'Argument：Json(JScriptTypeInfo)
'Return　：Key:Jsonで使用されているキー Value:Json.keyのJsonObject
'********************
Private Function JsonToDictionary(ByVal json As Object) As JsonObject
     
    Dim jsonDictionary As New Dictionary
    Dim col As New Collection
    Dim collectionValue As Variant
    Dim jsonObj As New JsonObject
    Dim key As Variant
    Dim objJson As Object
    Dim varJson As Variant
    Dim obj As Object
    Dim var As Variant
     
    For Each key In GetKeys(json)
         
        On Error GoTo VariantPattern
        Set objJson = GetObject(json, key)
        On Error GoTo 0
         
        If IsJsonArray(objJson) Then
            '配列だった場合はKey：Jsonのキー Value：Collection(JsonObject）となるDictionaryを作成
            'Collection(JsonObject)の作成
             
            For Each collectionValue In objJson
                'Collection作成中にJsonの配列が現れた場合は再帰させる
                If IsJson(collectionValue) Then
                    Call col.Add(JsonToDictionary(collectionValue))
                Else
                    Call col.Add(collectionValue)
                End If
            Next
             
            Call jsonDictionary.Add(key, col)
        Else
            On Error GoTo ObjectPattern
            var = GetValue(objJson, key)
            On Error GoTo 0
            Call jsonDictionary.Add(key, var)
            GoTo Continue
        End If
         
        GoTo Continue
 
ObjectPattern:
        Call jsonDictionary.Add(key, JsonToDictionary(objJson))
        Resume Continue
 
VariantPattern:
        On Error GoTo 0
        Call jsonDictionary.Add(key, GetValue(json, key))
        Resume Continue
Continue:
    Next
     
    '作成し終わったDictionaryでJsonObjectを作る
    Call jsonObj.Init(jsonDictionary)
     
    Set JsonToDictionary = jsonObj
     
End Function
 
'********************
'配列チェック
'
'Argument：Json(JScriptTypeInfo)
'Return　：引数が配列ならばTrue、配列でなければFalse
'********************
Private Function IsJsonArray(ByVal json As Object) As Boolean
    IsJsonArray = CallByName(m_js.codeobject, "isArray", VbMethod, json)
End Function
 
'********************
'キー取得
'
'Argument：Json(JScriptTypeInfo)
'Return　：引数のキーの配列
'********************
Private Function GetKeys(ByVal json As Object) As Object
    Set GetKeys = CallByName(m_js.codeobject, "getKeys", VbMethod, json)
End Function
 
Private Function IsJson(ByVal obj) As Boolean
    IsJson = TypeName(obj) = "JScriptTypeInfo"
End Function
 
Private Function GetObject(ByVal json As Object, ByVal key As Variant) As Object
    Set GetObject = CallByName(json, key, VbGet)
End Function
 
Private Function GetValue(ByVal json As Object, ByVal key As Variant) As Variant
    GetValue = CallByName(json, key, VbGet)
End Function
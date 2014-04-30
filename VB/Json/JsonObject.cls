VERSION 1.0 CLASS
BEGIN
  MultiUse = -1  'True
END
Attribute VB_Name = "JsonObject"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = False
Attribute VB_Exposed = False
Option Explicit
'********************
'JsonObject
'
'JsonParserによって作成されたDictionaryを使いやすくしたラッパ
'********************
  
Private m_keys As New Collection    '所有しているキー
Private m_data As Dictionary        'ラップしているデータ
Private m_inited As Boolean         '初期化済み判定
 
Public Property Get JsonKeys() As Collection
    Set JsonKeys = m_keys
End Property
 
Public Property Get Item(ByVal key As String)
Attribute Item.VB_UserMemId = 0
 
    ' "."が含まれているかどうかを調べる
    If InStr(key, ".") = 0 Then
        ' "."が含まれていない場合はそのままキーとして使用し、データを返す
        If IsObject(m_data(key)) Then
            Set Item = m_data(key)
        Else
            Item = m_data(key)
        End If
    Else
        Dim keys() As String
        Dim i As Long
        Dim tmp
         
        ' キーを分解する
        keys = Split(key, ".")
         
        Set tmp = m_data(keys(0))
         
        ' キーを一つ一つ調査していき、最後に取得した何かしらを返す
        For i = 1 To UBound(keys)
            If i = UBound(keys) Then
                If IsObject(tmp(keys(i))) Then
                    Set Item = tmp(keys(i))
                Else
                    Item = tmp(keys(i))
                End If
            Else
                Set tmp = tmp(keys(i))
            End If
        Next
    End If
End Property
 
'********************
'デストラクタ
'********************
Private Sub Class_Terminate()
  Set m_keys = Nothing
  Set m_data = Nothing
End Sub
  
'********************
'初期化
'Argument：JsonParserでパースし終わったDictionary
'********************
Public Sub Init(ByVal jsonData As Dictionary)
    Dim key As Variant
      
    '一度でもInitが呼ばれていたら何もしない
    If m_inited Then Exit Sub
      
    For Each key In jsonData.keys
        m_keys.Add (key)
    Next
     
    Set m_data = jsonData
    m_inited = True
      
End Sub
  
'********************
'キー存在チェック
'Argument：チェックしたいキー
'Return　：引数のキーが存在したらTrue、しなければFalse
'********************
Public Function HasKey(ByVal key As String) As Boolean
  
    Dim keyCache As Variant
      
    For Each keyCache In m_keys
        If key = keyCache Then
            HasKey = True
            Exit Function
        End If
    Next
      
    HasKey = False
End Function
  
'********************
'配列チェック
'Argument：チェックしたいキー
'Return　：引数のキーから取得できるItemがCollectionならTrue、それ以外ならFalse
'　　　　　また、キーそのものがない場合もFalseを返す
'********************
Public Function IsArray(ByVal key As String) As Boolean
    Dim obj As Object
      
    IsArray = False
      
    If Not HasKey(key) Then Exit Function
      
    If TypeName(m_data.Item(key)) = "Collection" Then
        IsArray = True
    End If
      
End Function
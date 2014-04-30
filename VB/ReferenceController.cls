Option Explicit
 
Private Const HKEY_CLASSES_ROOT = &H80000000
Private mReg As Object 'StdRegProv
 
'**************
'コンストラクタ
'**************
Private Sub Class_Initialize()
     
    Dim service As Object
     
    With CreateObject("WbemScripting.SWbemLocator")
        Set service = .ConnectServer(, "root\default")
    End With
     
    Set mReg = service.Get("StdRegProv")
     
    Set service = Nothing
     
End Sub
 
'**************
'デストラクタ
'**************
Private Sub Class_Terminate()
    Set mReg = Nothing
End Sub
 
'**************
'参照設定追加
'**************
Public Sub Add(ByVal target As String)
 
    Dim filePath As String
    Dim ref As Object
     
    filePath = GetFilePath(target)
     
    If filePath = vbNullString Then
        Exit Sub
    End If
     
    Set ref = ThisWorkbook.VBProject.References
    Call ref.AddFromFile(filePath)
    Set ref = Nothing
     
    Call MsgBox("成功")
     
End Sub
 
'**************
'参照設定解除
'**************
Public Sub Remove(ByVal target As String)
     
    Dim filePath As String
    Dim ref As Object
    Dim r As Object
     
    filePath = GetFilePath(target)
     
    If filePath = vbNullString Then
        Exit Sub
    End If
     
    Set ref = ThisWorkbook.VBProject.References
     
    For Each r In ref
        '既に参照設定がされているパスと取得したパスで比較する。
        If r.FullPath = filePath Then
            Call ref.Remove(r)
            Set ref = Nothing
            Call MsgBox("成功")
            Exit Sub
        End If
    Next
     
    Call MsgBox("参照設定に" & target & "は登録されていません。")
     
End Sub
 
'**************
'ファイル名取得
'**************
Private Function GetFilePath(ByVal target As String) As String
 
    Dim allSubKey() As Variant
    Dim allValueName() As Variant
    Dim allVersion() As Variant
     
    Dim subKey As Variant
    Dim version As Variant
    Dim valueName As Variant
    Dim clsid As String
    Dim filePath As String
     
    GetFilePath = vbNullString
     
    'ファイルパスが渡された場合はそのファイルを追加する
    With CreateObject("Scripting.FileSystemObject")
        If .FileExists(target) Then
            GetFilePath = target
            Exit Function
        End If
    End With
     
    'ファイルパス以外が渡された場合は一旦レジストリに同名のキー（ProgID）がないかを調べる
    allSubKey = GetRegKeys(target)
     
    If HasValue(allSubKey) Then
         
        clsid = GetRegValue(target & "\CLSID")
         
        'CLSIDが取得できなかった場合は追加できないのでメッセージを出力して終了
        If clsid = vbNullString Then
            Call MsgBox(target & "のCLSIDを取得できませんでした。", Title:="エラー")
            Exit Function
        End If
         
        'HKEY_CLASSES_ROOT\CLSID\(取得したCLSID)\InProcServer32の規定値を取得する
        filePath = GetRegValue("CLSID\" & clsid & "\InProcServer32")
         
        If filePath = vbNullString Then
            Call MsgBox(target & "のInProcServer32の値が見つかりませんでした。", Title:="エラー")
            Exit Function
        End If
         
        'そこの規定値がファイルパスになっているので、参照設定に追加する
        GetFilePath = filePath
         
        Exit Function
    End If
     
    'ファイルパスでもProgIDでもなかった場合はTypeLibの中身をチェックする
    allSubKey = GetRegKeys("TypeLib")
     
    For Each subKey In allSubKey
         
        'TypeLib\(GUID)のKey(version)を取得する
        allVersion = GetRegKeys("TypeLib\" & subKey)
         
        '引数がGUIDだった場合はとりあえず一番高いバージョンを追加する
        If target = subKey Or "{" & target & "}" = subKey Then
 
            version = allVersion(UBound(allVersion))
             
            filePath = GetRegValue("TypeLib\" & subKey & "\" & version & "\0\win64")
                 
            If filePath = vbNullString Then filePath = GetRegValue("TypeLib\" & subKey & "\" & version & "\0\win32")
             
            GetFilePath = filePath
             
            Exit Function
        End If
         
        For Each version In allVersion
             
            'versionの規定値が参照設定で表示される文字列
            'そこの値と引数で来た値が一致していた場合は参照設定に追加する
            If GetRegValue("TypeLib\" & subKey & "\" & version) = target Then
                 
                '「TypeLib\(GUID)\(version)\0\win64」
                'もしくは
                '「TypeLib\(GUID)\(version)\0\win32」
                'のどちらかにファイルパスが入っている
                 
                'win64を優先する
                filePath = GetRegValue("TypeLib\" & subKey & "\" & version & "\0\win64")
                 
                If filePath = vbNullString Then filePath = GetRegValue("TypeLib\" & subKey & "\" & version & "\0\win32")
                 
                GetFilePath = filePath
                 
                Exit Function
            End If
        Next
    Next
     
    Call MsgBox(target & "は見つかりませんでした。", Title:="エラー")
 
End Function
 
Private Function GetRegValue(ByVal path As String, Optional ByVal valueName As String = vbNullString) As String
     
    Dim v As String
     
    On Error GoTo GetError
    Call mReg.GetStringValue(HKEY_CLASSES_ROOT, path, valueName, v)
    On Error GoTo 0
     
    GetRegValue = v
 
    Exit Function
 
GetError:
 
    Debug.Print Err.Description
 
    GetRegValue = vbNullString
 
End Function
 
Private Function GetRegKeys(ByVal path As String) As Variant()
    Dim ret() As Variant
     
    Call mReg.EnumKey(HKEY_CLASSES_ROOT, path, ret)
     
    GetRegKeys = ret
     
End Function
 
Private Function HasValue(arr() As Variant) As Boolean
    HasValue = Sgn(arr) <> 0
End Function
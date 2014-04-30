using System;
using System.Collections.Generic;
using System.Text;
using System.Linq;
using Microsoft.SqlServer.Dts.Pipeline;
using Microsoft.SqlServer.Dts.Pipeline.Wrapper;
using Microsoft.SqlServer.Dts.Runtime.Wrapper;

namespace JapaneseBigger
{
    public class InputColumn
    {
        public string Name { get; set; }
        public int BufferIndex { get; set; }
        public DTSUsageType UsageType { get; set; }
        public int LineageID { get; set; }
    }

    public class OutputColumn
    {
        public string Name { get; set; }
        public int BufferIndex { get; set; }
        public int InputLineageID { get; set; }
    }

    [DtsPipelineComponentAttribute
        (
            ComponentType = ComponentType.Transform,
            DisplayName = "小書き文字変換",
            Description = "「ぁ」や「ゃ」といった文字を「あ」や「や」に変換します。"
        )
    ]
    public class JapaneseBiggerComponent : PipelineComponent
    {
        // ******************** デザイン時メソッド ******************** //

        /// <summary>
        /// コンポーネント初期化
        /// </summary>
        public override void ProvideComponentProperties()
        {
            // コンポーネントの初期化
            base.RemoveAllInputsOutputsAndCustomProperties();

            // 入力の追加
            var input = ComponentMetaData.InputCollection.New();
            input.Name = "入力0";

            // 出力の追加
            var output = ComponentMetaData.OutputCollection.New();
            output.Name = "出力0";
            output.SynchronousInputID = input.ID;

        }

        /// <summary>
        /// データ検証
        /// </summary>
        /// <returns>データ検証結果</returns>
        public override DTSValidationStatus Validate()
        {
            var input = ComponentMetaData.InputCollection[0];
            var inputColumns = input.InputColumnCollection.Cast<IDTSInputColumn100>();
            var output = ComponentMetaData.OutputCollection[0];
            var outputColumns = output.OutputColumnCollection.Cast<IDTSOutputColumn100>();
            var virtualInput = input.GetVirtualInput();

            foreach (var inputColumn in inputColumns)
            {
                // メタデータのチェック
                try
                {
                    virtualInput.VirtualInputColumnCollection
                                .GetVirtualInputColumnByLineageID(inputColumn.LineageID);
                }
                catch
                {
                    FireError("メタデータが破損しています。");
                    return DTSValidationStatus.VS_NEEDSNEWMETADATA;
                }

                // データ型チェック
                if (!ValidateDataType(inputColumn, DataType.DT_STR, DataType.DT_WSTR))
                {
                    FireError(string.Format("{0}は文字列ではありません。文字列以外のデータを入力に用いることは出来ません。", inputColumn.Name));
                    return DTSValidationStatus.VS_ISBROKEN;
                }

                // Validationに成功した入力列がREADONLYの場合は出力に新規列を追加する
                if (inputColumn.UsageType == DTSUsageType.UT_READONLY)
                {
                    var outputColumnName = string.Format("大文字_{0}", inputColumn.Name);
                    if (!outputColumns.Any(x => GetInputLineageID(x) == inputColumn.LineageID))
                    {
                        var newColumn = output.OutputColumnCollection.New();
                        newColumn.Name = outputColumnName;
                        newColumn.Description = outputColumnName;
                        newColumn.SetDataTypeProperties(inputColumn.DataType
                            , inputColumn.Length, inputColumn.Precision, inputColumn.Scale
                            , inputColumn.CodePage);

                        // 実行時にInputColumnのLineageIDから特定出来るようCustomPropertyを追加する
                        var outputProperty = newColumn.CustomPropertyCollection.New();
                        outputProperty.Name = "InputLineageID";
                        outputProperty.Description = "入力列のLineageIDと紐付けます。";
                        outputProperty.Value = inputColumn.LineageID;
                    }
                    else
                    {
                        // 既に列がある場合はメタデータを更新する
                        var target = output.OutputColumnCollection
                                           .Cast<IDTSOutputColumn100>()
                                           .Where(x => x.Name == outputColumnName)
                                           .First();
                        target.SetDataTypeProperties(inputColumn.DataType, inputColumn.Length
                            , inputColumn.Precision, inputColumn.Scale, inputColumn.CodePage);

                        target.CustomPropertyCollection.Cast<IDTSCustomProperty100>()
                                                       .Where(x => x.Name == "InputLineageID")
                                                       .First()
                                                       .Value = inputColumn.LineageID;
                    }
                }
            }

            // 出力列から入力列にないものを削除し、入力列と出力列の同期を取る
            // 削除対象となるIDを取得
            // 削除すれば当然OutputColumnCollectionの要素数が変わるため、先にToListしておかないとエラーが出る
            var targetIDList = outputColumns.Where(x => !inputColumns
                                                        .Where(y => y.UsageType == DTSUsageType.UT_READONLY)
                                                        .Any(y => GetInputLineageID(x) == y.LineageID))
                                            .Select(x => x.ID)
                                            .ToList();

            // OutputColumnCollectionのRemoveObjectByIDメソッドで出力から削除する
            targetIDList.ForEach(x => output.OutputColumnCollection.RemoveObjectByID(x));

            return DTSValidationStatus.VS_ISVALID;
        }

        /// <summary>
        /// 入力列のデータ型をチェックする
        /// </summary>
        /// <param name="inputColumn">入力列</param>
        /// <param name="types">チェックするデータ型</param>
        /// <returns>typesの中に一つでもinputColumn.DataTypeと一致するものがあったらtrue</returns>
        private bool ValidateDataType(IDTSInputColumn100 inputColumn, params DataType[] types)
        {
            return types.Any(x => x == inputColumn.DataType);
        }

        /// <summary>
        /// エラー出力
        /// </summary>
        /// <param name="errorMessage">エラーメッセージ</param>
        private void FireError(string errorMessage)
        {
            var cancel = false;
            ComponentMetaData.FireError(0, ComponentMetaData.Name, errorMessage, string.Empty, 0, out cancel);
        }

        /// <summary>
        /// カスタムプロパティであるInputLineageIDを取得する
        /// </summary>
        /// <param name="outputColumn"></param>
        /// <returns></returns>
        private int GetInputLineageID(IDTSOutputColumn100 outputColumn)
        {
            return outputColumn.CustomPropertyCollection
                               .Cast<IDTSCustomProperty100>()
                               .Where(x => x.Name == "InputLineageID")
                               .First()
                               .Value;
        }

        // ******************** 実行時メソッド ******************** //

        private Dictionary<char, char> _nameDic;
        private List<InputColumn> _inputColumns;
        private List<OutputColumn> _outputColumns;

        /// <summary>
        /// 実行前処理
        /// </summary>
        public override void PreExecute()
        {
            // 変換用辞書を初期化
            _nameDic = new Dictionary<char, char>()
            {
                {'ぁ', 'あ'},
                {'ぃ', 'い'},
                {'ぅ', 'う'},
                {'ぇ', 'え'},
                {'ぉ', 'お'},
                {'っ', 'つ'},
                {'ゃ', 'や'},
                {'ゅ', 'ゆ'},
                {'ょ', 'よ'},
                {'ゎ', 'わ'},
                {'ァ', 'ア'},
                {'ィ', 'イ'},
                {'ゥ', 'ウ'},
                {'ェ', 'エ'},
                {'ォ', 'オ'},
                {'ヵ', 'カ'},
                {'ヶ', 'ケ'},
                {'ッ', 'ツ'},
                {'ャ', 'ヤ'},
                {'ュ', 'ユ'},
                {'ョ', 'ヨ'},
                {'ヮ', 'ワ'},
            };

            // buffer上の入力列位置情報を取得
            var input = ComponentMetaData.InputCollection[0];
            _inputColumns = input.InputColumnCollection
                                 .Cast<IDTSInputColumn100>()
                                 .Select(x => new InputColumn
                                 {
                                     Name = x.Name,
                                     BufferIndex = BufferManager.FindColumnByLineageID(input.Buffer, x.LineageID),
                                     UsageType = x.UsageType,
                                     LineageID = x.LineageID,
                                 })
                                 .ToList();

            // buffer上の出力列位置情報を取得
            var output = ComponentMetaData.OutputCollection[0];
            _outputColumns = output.OutputColumnCollection
                                   .Cast<IDTSOutputColumn100>()
                                   .Select(x => new OutputColumn
                                   {
                                       Name = x.Name,
                                       BufferIndex = BufferManager.FindColumnByLineageID(input.Buffer, x.LineageID),
                                       InputLineageID = GetInputLineageID(x)
                                   })
                                   .ToList();
        }

        /// <summary>
        /// 入力に対する処理
        /// </summary>
        /// <param name="inputID"></param>
        /// <param name="buffer"></param>
        public override void ProcessInput(int inputID, PipelineBuffer buffer)
        {
            if (!buffer.EndOfRowset)
            {
                while (buffer.NextRow())
                {
                    foreach (var inputColumn in _inputColumns)
                    {
                        // NULLの場合は何もしない（NULLをセットする）
                        if (buffer.IsNull(inputColumn.BufferIndex)) continue;

                        // 入力列の文字列を取得する
                        var inputString = buffer.GetString(inputColumn.BufferIndex);

                        // 捨て仮名変換
                        var sb = new StringBuilder();
                        foreach (var c in inputString)
                        {
                            sb.Append(_nameDic.ContainsKey(c) ? _nameDic[c] : c);
                        }

                        // 変換した文字列を設定する
                        switch (inputColumn.UsageType)
                        {
                            case DTSUsageType.UT_IGNORED:
                                throw new NotImplementedException();
                            case DTSUsageType.UT_READONLY:
                                // 入力列と対応する出力列のBufferIndexを調べる
                                var outputBufferIndex = _outputColumns.Where(x => x.InputLineageID == inputColumn.LineageID)
                                                                      .Select(x => x.BufferIndex)
                                                                      .First();

                                // 出力列に値をセットする
                                buffer.SetString(outputBufferIndex, sb.ToString());
                                break;
                            case DTSUsageType.UT_READWRITE:
                                // 入力列に直接新たな値をセットする
                                buffer.SetString(inputColumn.BufferIndex, sb.ToString());
                                break;
                            default:
                                throw new NotImplementedException();
                        }
                    }
                }
            }
        }
        
    }
}
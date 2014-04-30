using System.Collections.Generic;
using System.Linq;
using Microsoft.SqlServer.Dts.Pipeline;
using Microsoft.SqlServer.Dts.Pipeline.Wrapper;
using Microsoft.SqlServer.Dts.Runtime.Wrapper;

namespace RowCounter
{
    [DtsPipelineComponentAttribute(ComponentType = ComponentType.Transform
        , DisplayName = "行番号"
        , Description = "行番号を付与します。"
        , NoEditor = true)]
    public class RowCounterComponent : PipelineComponent
    {

        public override void ProvideComponentProperties()
        {
            // コンポーネントの初期化
            base.RemoveAllInputsOutputsAndCustomProperties();

            // 入力の追加
            IDTSInput100 input = ComponentMetaData.InputCollection.New();
            input.Name = "入力0";

            // 出力の追加
            IDTSOutput100 output = ComponentMetaData.OutputCollection.New();
            output.Name = "出力0";
            output.SynchronousInputID = input.ID;

            // 出力列の追加
            IDTSOutputColumn100 outputColumn = output.OutputColumnCollection.New();
            outputColumn.Name = "行番号";
            outputColumn.Description = "行番号";
            outputColumn.SetDataTypeProperties(DataType.DT_I4, 0, 0, 0, 0);
        }

        private int _rowCountColumnIndex;

        public override void PreExecute()
        {
            var input = ComponentMetaData.InputCollection[0];
            // ProvideComponentPropertiesで設定した出力列（[行番号]カラム）の取得
            var rowCountColumn = ComponentMetaData.OutputCollection[0].OutputColumnCollection[0];

            // BufferManagerから[行番号]カラムがbufferのどこにあるかを特定する
            _rowCountColumnIndex = BufferManager.FindColumnByLineageID(input.Buffer, rowCountColumn.LineageID);
        }

        public override void ProcessInput(int inputID, PipelineBuffer buffer)
        {
            var count = 0;
            
            if (!buffer.EndOfRowset)
            {
                while (buffer.NextRow())
                {
                    // [行番号]カラムに行番号を付与する
                    buffer.SetInt32(_rowCountColumnIndex, ++count);
                }
            }
        }
    }
}

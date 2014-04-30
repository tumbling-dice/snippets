using Microsoft.SqlServer.Dts.Runtime;
using Microsoft.SqlServer.Dts.Tasks.ExecuteSQLTask;

namespace CreatePackage
{
    class Program
    {
        static void Main(string[] args)
        {
            using (var package = new Package { Name = "ExecuteSQLTest" })
            {
                //変数の追加
                package.Variables.Add("PiyoValue", false, "User", string.Empty);
                package.Variables.Add("Hoge", false, "User", string.Empty);

                //接続マネージャーの追加
                using (var manager = package.Connections.Add("OLEDB"))
                {
                    manager.Name = "TestDB";
                    manager.ConnectionString = "Provider=SQLOLEDB.1;Data Source=(local);Initial Catalog=master;Integrated Security=SSPI;";

                    //SQL実行タスクを作成
                    using (var th = package.Executables.Add("STOCK:SQLTask") as TaskHost)
                    {
                        
                        th.Name = "SQL Test";
                        th.Properties["Connection"].SetValue(th, manager.ID);
                        th.Properties["SqlStatementSource"].SetValue(th, "SELECT PIYO FROM FUGA WHERE HOGE = ?");
                        th.Properties["ResultSetType"].SetValue(th, ResultSetType.ResultSetType_SingleRow);

                        //パラメータ設定
                        var param = (th.Properties["ParameterBindings"].GetValue(th) as IDTSParameterBindings).Add();
                        param.ParameterName = "0";
                        param.DtsVariableName = "User::Hoge";
                        param.ParameterDirection = ParameterDirections.Input;
                        param.DataType = (int)System.Data.OleDb.OleDbType.WChar;
                        param.ParameterSize = -1;

                        //結果セット設定
                        var result = (th.Properties["ResultSetBindings"].GetValue(th) as IDTSResultBindings).Add();
                        result.ResultName = "0";
                        result.DtsVariableName = "User::PiyoValue";
                    }
                }

                //DTSXとして出力
                new Application().SaveToXml(string.Format(@"C:\temp\{0}.dtsx", package.Name), package, null);

            }
        }
    }
}

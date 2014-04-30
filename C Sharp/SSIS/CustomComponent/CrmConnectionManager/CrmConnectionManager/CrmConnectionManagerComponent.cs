using System;
using System.Linq;
using System.Text;
using Microsoft.SqlServer.Dts.Runtime;
using System.Linq.Expressions;
using Microsoft.Xrm.Client;
using Microsoft.Xrm.Client.Services;
using Microsoft.Crm.Sdk.Messages;

namespace CrmConnectionManager
{
    [DtsConnectionAttribute(ConnectionType = "DCRM"
        , DisplayName = "Dynamics CRM 2011 接続マネージャー"
        , Description = "Dynamics CRM 2011に接続する接続マネージャー"
        , UITypeName = "CrmConnectionManager.Resource.CrmConnectionManagerUI,CrmConnectionManager.Resource,Version=1.0.0.0,Culture=neutral,PublicKeyToken=03af297da674c476")]
    public class CrmConnectionManagerComponent : ConnectionManagerBase
    {
        public string URL { get; set; }
        public string Domain { get; set; }
        public string UserName { get; set; }
        public string Password { get; set; }

        public override string ConnectionString
        {
            get
            {
                // プロパティからConnectionStringを作成する
                return CreateConnectionString
                    (
                        Url => URL,
                        Domain => this.Domain,
                        Username => this.UserName,
                        Password => this.Password
                    );
            }
            set
            {
                base.ConnectionString = value;
            }
        }

        public override DTSExecResult Validate(IDTSInfoEvents infoEvents)
        {
            // プロパティが一つでも空だったらエラーとする
            if (!PropertiesValidate(infoEvents, URL, Domain, UserName, Password))
            {
                return DTSExecResult.Failure;
            }

            // 接続テスト
            try
            {
                var con = CrmConnection.Parse(ConnectionString);
                con.Timeout = TimeSpan.FromSeconds(30);
                using (var service = new OrganizationService(con))
                {
                    service.Execute<WhoAmIResponse>(new WhoAmIRequest());
                }
            }
            catch (Exception e)
            {
                infoEvents.FireError(0, "Dynamics CRM 2011 接続マネージャー", e.Message, string.Empty, 0);
                return DTSExecResult.Failure;
            }


            return DTSExecResult.Success;
        }

        public override object AcquireConnection(object txn)
        {
            // ConnectionStringを基にOrganizationServiceを作成する
            return new OrganizationService(CrmConnection.Parse(ConnectionString));
        }

        public override void ReleaseConnection(object connection)
        {
            ((OrganizationService)connection).Dispose();
        }

        /// <summary>
        /// プロパティ値検証
        /// </summary>
        /// <param name="infoEvents"></param>
        /// <param name="properties"></param>
        /// <returns></returns>
        private static bool PropertiesValidate(IDTSInfoEvents infoEvents, params string[] properties)
        {
            if (properties.Any(x => string.IsNullOrEmpty(x)))
            {
                infoEvents.FireError(0, "Dynamics CRM 2011 接続マネージャー", "全てのプロパティに値を入力してください。", string.Empty, 0);
                return false;
            }

            return true;
        }

        /// <summary>
        /// ConnectionString作成
        /// </summary>
        /// <param name="exprs"></param>
        /// <returns></returns>
        private static string CreateConnectionString(params Expression<Func<object, object>>[] exprs)
        {
            var sb = new StringBuilder();

            foreach (var expr in exprs)
            {
                var obj = expr.Compile().Invoke(null);

                if (obj == null) continue;

                var name = expr.Parameters[0].Name;
                var val = obj.ToString();

                if (val == string.Empty) continue;

                sb.Append(name).Append('=').Append(val).Append(';');
            }

            return sb.ToString();
        }
    }
}

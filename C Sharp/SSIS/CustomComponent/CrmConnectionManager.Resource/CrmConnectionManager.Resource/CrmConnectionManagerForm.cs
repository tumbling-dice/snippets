using System;
using System.Linq;
using System.Linq.Expressions;
using System.Text;
using System.Windows.Forms;
using Microsoft.Crm.Sdk.Messages;
using Microsoft.Xrm.Client;
using Microsoft.Xrm.Client.Services;
using Microsoft.SqlServer.Dts.Runtime;

namespace CrmConnectionManager.Resource
{
    public partial class CrmConnectionManagerForm : Form
    {
        private readonly ConnectionManager _connectionManager;

        public CrmConnectionManagerForm(ConnectionManager connectionManager)
        {
            _connectionManager = connectionManager;
            InitializeComponent();
        }

        /// <summary>
        /// 接続テスト
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="ev"></param>
        private void btnTest_Click(object sender, EventArgs ev)
        {
            var connectionString = CreateConnectionString
                    (
                        Url => txbURL.Text,
                        Domain => txbDomain.Text,
                        Username => txbUserName.Text,
                        Password => txbPassword.Text
                    );

            // connection test
            try
            {
                var con = CrmConnection.Parse(connectionString);
                con.Timeout = TimeSpan.FromSeconds(30);
                using (var service = new OrganizationService(con))
                {
                    service.Execute<WhoAmIResponse>(new WhoAmIRequest());
                }
            }
            catch (Exception e)
            {
                MessageBox.Show(e.Message, "エラー", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return;
            }

            MessageBox.Show("OK", "確認", MessageBoxButtons.OK, MessageBoxIcon.Information);
        }

        private void btnOK_Click(object sender, EventArgs e)
        {
            if (!ValidateProperties(txbDomain, txbPassword, txbURL, txbUserName)) return;

            SetProperty("URL", txbURL.Text);
            SetProperty("Domain", txbDomain.Text);
            SetProperty("UserName", txbUserName.Text);
            SetProperty("Password", txbPassword.Text);

            this.DialogResult = DialogResult.OK;
            this.Close();
        }

        /// <summary>
        /// プロパティ値検証
        /// </summary>
        /// <param name="properties"></param>
        /// <returns></returns>
        private bool ValidateProperties(params TextBox[] properties)
        {
            if (properties.Any(x => string.IsNullOrEmpty(x.Text)))
            {
                MessageBox.Show("全ての項目に値を入力してください。", "エラー", MessageBoxButtons.OK, MessageBoxIcon.Error);
                return false;
            }

            return true;
        }

        /// <summary>
        /// ConnectionManagerにプロパティを設定する
        /// </summary>
        /// <param name="name"></param>
        /// <param name="value"></param>
        private void SetProperty(string name, string value)
        {
            _connectionManager.Properties[name].SetValue(_connectionManager, value);
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

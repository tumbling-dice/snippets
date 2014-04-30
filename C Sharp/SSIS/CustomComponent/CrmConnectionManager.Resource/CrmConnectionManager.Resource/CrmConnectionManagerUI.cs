using System;
using System.Windows.Forms;
using Microsoft.SqlServer.Dts.Design;
using Microsoft.SqlServer.Dts.Runtime;
using Microsoft.SqlServer.Dts.Runtime.Design;

namespace CrmConnectionManager.Resource
{
    public class CrmConnectionManagerUI : IDtsConnectionManagerUI
    {
        public ConnectionManager ConnectionManager { get; set; }
        public IServiceProvider ServiceProvider { get; set; }

        public void Initialize(ConnectionManager connectionManager, IServiceProvider serviceProvider)
        {
            ConnectionManager = connectionManager;
            ServiceProvider = serviceProvider;
        }

        public bool New(IWin32Window parentWindow, Connections connections, ConnectionManagerUIArgs connectionUIArg)
        {
            // コピー＆ペーストされた場合でもNewメソッドが呼ばれるため、Fromを表示しないよう制御する必要がある。
            var clipboardService = (IDtsClipboardService)ServiceProvider.GetService(typeof(IDtsClipboardService));
            if (clipboardService != null && clipboardService.IsPasteActive) return true;

            return OpenEditor(parentWindow);
        }

        public bool Edit(IWin32Window parentWindow, Connections connections, ConnectionManagerUIArgs connectionUIArg)
        {
            return OpenEditor(parentWindow);
        }

        public void Delete(IWin32Window parentWindow)
        {
            // NotImplemented
        }

        private bool OpenEditor(IWin32Window parentWindow)
        {
            var form = new CrmConnectionManagerForm(ConnectionManager);

            return form.ShowDialog(parentWindow) == DialogResult.OK;

        }
    }
}

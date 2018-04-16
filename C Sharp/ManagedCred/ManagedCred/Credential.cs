using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.InteropServices;

namespace ManagedCred
{
    public class Credential
    {
        [DllImport("ole32.dll")]
        private static extern void CoTaskMemFree(IntPtr ptr);

        [DllImport("advapi32", SetLastError = true, CharSet = CharSet.Unicode)]
        private static extern bool CredEnumerate(string filter, CRED_FLAGS flag, out int count, out IntPtr pCredentials);

        [DllImport("advapi32", SetLastError = true, CharSet = CharSet.Unicode)]
        private static extern bool CredDelete(string targetName, CRED_TYPE type, CRED_FLAGS flags);

        [DllImport("advapi32", SetLastError = true, CharSet = CharSet.Unicode)]
        private static extern bool CredWrite(ref UnmanagedCredential credential, CRED_FLAGS flags);

        [DllImport("advapi32", SetLastError = true, CharSet = CharSet.Unicode)]
        private static extern bool CredRead(string targetName, CRED_TYPE type, CRED_FLAGS flags, out IntPtr pCredential);

        [DllImport("credui.dll", SetLastError = true, CharSet = CharSet.Unicode)]
        private static extern uint CredUIPromptForWindowsCredentials(ref PCREDUI_INFO uiInfo, int authError, ref uint authPackage
            , IntPtr InAuthBuffer, uint InAuthBufferSize, out IntPtr refOutAuthBuffer, out uint refOutAuthBufferSize
            , ref bool fSave, CREDUIWIN_FLAG flags);

        [DllImport("credui.dll", SetLastError = true, CharSet = CharSet.Unicode)]
        private static extern bool CredUnPackAuthenticationBuffer(CREDUIWIN_FLAG flags, IntPtr authBuffer, uint cbAuthBuffer, StringBuilder pszUserName
            , ref int pcchMaxUserName, StringBuilder pszDomainName, ref int pcchMaxDomainame, StringBuilder pszPassword, ref int pcchMaxPassword);

        [DllImport("kernel32.dll")]
        private static extern uint FormatMessage(uint dwFlags, IntPtr lpSource, uint dwMessageId, uint dwLanguageId, StringBuilder lpBuffer, int nSize
            , IntPtr Arguments);

        /// <summary>
        /// 資格情報入力ダイアログの呼び出し
        /// </summary>
        /// <param name="caption">ダイアログのキャプション</param>
        /// <param name="message">ダイアログに表示するメッセージ</param>
        public static void CallCredentialDialog(string caption, string message)
        {
            var uiInfo = new PCREDUI_INFO();
            uiInfo.cbSize = Marshal.SizeOf(uiInfo);
            uiInfo.pszCaptionText = caption;
            uiInfo.pszMessageText = message;

            uint authPackage = 0;
            IntPtr outCredBuffer;
            uint outCredSize;
            bool fSave = false;

            if (CredUIPromptForWindowsCredentials(ref uiInfo, 0, ref authPackage, IntPtr.Zero, 0
                    , out outCredBuffer, out outCredSize, ref fSave, CREDUIWIN_FLAG.AUTHPACKAGE_ONLY) != 0)
            {
                Console.WriteLine(GetErrorMessage());
                throw new ApplicationException("資格入力ダイアログの呼び出しに失敗しました。");
            }

            var userName = new StringBuilder(256);
            var userNameSize = 256;
            var domainName = new StringBuilder(256);
            var domainNameSize = 256;
            var password = new StringBuilder(256);
            var passwordSize = 256;

            // CredUnPackAuthenticationBufferのflagsにCREDUIWIN_GENERIC(0x1)を渡すとPasswordが平文で返ってくる
            // 暗号化しておきたい場合は0を渡す
            if (!CredUnPackAuthenticationBuffer(0, outCredBuffer, outCredSize, userName, ref userNameSize
                    , domainName, ref domainNameSize, password, ref passwordSize))
            {
                Console.WriteLine(GetErrorMessage());
                throw new ApplicationException("資格入力ダイアログから情報を取得することに失敗しました。");
            }

            CoTaskMemFree(outCredBuffer);
        }

        /// <summary>
        /// 資格情報の読み込み
        /// </summary>
        /// <param name="targetName"></param>
        /// <param name="type"></param>
        /// <returns></returns>
        public static ManagedCredential Read(string targetName, CRED_TYPE type)
        {
            var credential = IntPtr.Zero;

            if (!CredRead(targetName, type, 0, out credential))
            {
                Console.WriteLine(GetErrorMessage());
                throw new ApplicationException("資格情報の取得に失敗しました。");
            }

            return ConvertToManagedCredential(credential);
        }

        /// <summary>
        /// 資格情報の登録
        /// </summary>
        /// <param name="managedCred"></param>
        /// <param name="flags"></param>
        public static void Write(ManagedCredential managedCred, CRED_FLAGS flags)
        {
            Write(ConvertToUnmanagedCredential(managedCred), flags);
        }

        /// <summary>
        /// 資格情報の登録
        /// </summary>
        /// <param name="unmanagedCred"></param>
        /// <param name="flags"></param>
        private static void Write(UnmanagedCredential unmanagedCred, CRED_FLAGS flags)
        {
            if (!CredWrite(ref unmanagedCred, flags))
            {
                Console.WriteLine(GetErrorMessage());
                throw new ApplicationException("資格情報の書き込みに失敗しました。");
            }

            Console.WriteLine("ok");
        }

        /// <summary>
        /// 資格情報の削除
        /// </summary>
        /// <param name="cred">マネージドな資格情報</param>
        public static void Delete(ManagedCredential cred)
        {
            Delete(cred.TargetName, cred.Type, cred.Flags);
        }

        /// <summary>
        /// 資格情報の削除
        /// </summary>
        /// <param name="targetName"></param>
        /// <param name="type"></param>
        /// <param name="flags"></param>
        private static void Delete(string targetName, CRED_TYPE type, CRED_FLAGS flags)
        {
            if (!CredDelete(targetName, type, flags))
            {
                Console.WriteLine(GetErrorMessage());
                throw new ApplicationException("資格情報の削除に失敗しました。");
            }
        }

        /// <summary>
        /// 資格情報の列挙
        /// </summary>
        /// <returns></returns>
        public static List<ManagedCredential> Enumerate()
        {
            var count = 0;
            var pCredentials = IntPtr.Zero;

            //CredEnumerate呼び出し
            if (!CredEnumerate(null, 0, out count, out pCredentials))
            {
                Console.WriteLine(GetErrorMessage());
                throw new ApplicationException("資格情報の列挙に失敗しました。");
            }

            return Enumerable.Range(0, count)
                             .Select(n => Marshal.ReadIntPtr(pCredentials, n * Marshal.SizeOf(typeof(IntPtr))))
                             .Select(ptr => ConvertToManagedCredential(ptr))
                             .ToList();
        }

        /// <summary>
        /// アンマネージドな資格情報（ポインタ）をマネージドなEnumに変換する
        /// </summary>
        /// <param name="ptr"></param>
        /// <returns></returns>
        private static ManagedCredential ConvertToManagedCredential(IntPtr ptr)
        {
            var unmanagedCred = (UnmanagedCredential)Marshal.PtrToStructure(ptr, typeof(UnmanagedCredential));


            // 特殊な操作が必要ないものを詰め込んだManagedCredentialを作成する
            // CredentialBlobは別途変換する必要があるため、一旦サイズだけを持った配列を作成する
            var managedCred = new ManagedCredential
            {
                Flags = (CRED_FLAGS)unmanagedCred.Flags,
                Type = (CRED_TYPE)unmanagedCred.Type,
                TargetName = unmanagedCred.TargetName,
                Comment = unmanagedCred.Comment,
                LastWritten = unmanagedCred.LastWritten,
                CredentialBlob = new byte[unmanagedCred.CredentialBlobSize],
                Persist = (CRED_PERSIST)unmanagedCred.Persist,
                Attributes = Enumerable.Range(0, (int)unmanagedCred.AttributeCount)
                                                .Select(i => new IntPtr(unmanagedCred.Attributes.ToInt64() + i * Marshal.SizeOf(typeof(CREDENTIAL_ATTRIBUTE))))
                                                .Select(attr => (CREDENTIAL_ATTRIBUTE)Marshal.PtrToStructure(attr, typeof(CREDENTIAL_ATTRIBUTE)))
                                                .ToArray(),
                TargetAlias = unmanagedCred.TargetAlias,
                UserName = unmanagedCred.UserName,
            };

            if (unmanagedCred.CredentialBlobSize != 0)
            {
                Marshal.Copy(unmanagedCred.CredentialBlob, managedCred.CredentialBlob, 0, (int)unmanagedCred.CredentialBlobSize);
            }

            return managedCred;
        }

        /// <summary>
        /// マネージドな資格情報をアンマネージドなポインタに変換する
        /// </summary>
        /// <param name="managedCred"></param>
        /// <returns></returns>
        private static UnmanagedCredential ConvertToUnmanagedCredential(ManagedCredential managedCred)
        {
            var unmanagedCred = new UnmanagedCredential()
            {
                Flags = (uint)managedCred.Flags,
                Type = (uint)managedCred.Type,
                TargetName = managedCred.TargetName,
                Comment = managedCred.Comment,
                LastWritten = managedCred.LastWritten,
                CredentialBlobSize = managedCred.CredentialBlob != null ? (uint)managedCred.CredentialBlob.Length : 0,
                Persist = (uint)managedCred.Persist,
                TargetAlias = managedCred.TargetAlias,
                UserName = managedCred.UserName,
                AttributeCount = managedCred.Attributes != null ? (uint)managedCred.Attributes.Length : 0,
                Attributes = IntPtr.Zero,
                CredentialBlob = IntPtr.Zero,
            };


            if (unmanagedCred.AttributeCount != 0)
            {
                Marshal.Copy(managedCred.Attributes.Cast<int>().ToArray(), 0, unmanagedCred.Attributes, managedCred.Attributes.Length);
            }

            if (unmanagedCred.CredentialBlobSize != 0)
            {
                var p = Marshal.AllocHGlobal(managedCred.CredentialBlob.Length);
                Marshal.Copy(managedCred.CredentialBlob, 0, p, managedCred.CredentialBlob.Length);
                unmanagedCred.CredentialBlob = p;
                Marshal.FreeHGlobal(p);
            }

            return unmanagedCred;
        }

        private static string GetErrorMessage()
        {
            const uint FORMAT_MESSAGE_FROM_SYSTEM = 0x00001000;
            var win32ErrorCode = Marshal.GetLastWin32Error();
            var message = new StringBuilder(255);
            FormatMessage(FORMAT_MESSAGE_FROM_SYSTEM, IntPtr.Zero, (uint)win32ErrorCode, 0, message, message.Capacity, IntPtr.Zero);

            return message.ToString();
        }
    }
}

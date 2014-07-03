using System;
using System.Runtime.InteropServices;
using System.Text;

namespace ManagedCred
{
    public enum CRED_TYPE : uint
    {
        GENERIC = 1,
        DOMAIN_PASSWORD = 2,
        DOMAIN_CERTIFICATE = 3,
        DOMAIN_VISIBLE_PASSWORD = 4,
        GENERIC_CERTIFICATE = 5,
        DOMAIN_EXTENDED = 6,
        MAXIMUM = 7,
        MAXIMUM_EX = (MAXIMUM + 1000),
    }

    public enum CRED_PERSIST : uint
    {
        SESSION = 1,
        LOCAL_MACHINE = 2,
        ENTERPRISE = 3,
    }

    public enum CRED_FLAGS : uint
    {
        PROMPT_NOW = 0x2,
        USERNAME_TARGET = 0x4
    }

    [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Auto)]
    public struct CREDENTIAL_ATTRIBUTE
    {
        string Keyword;
        uint Flags;
        uint ValueSize;
        IntPtr Value;
    }
    
    [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
    internal struct PCREDUI_INFO
    {
        public int cbSize;
        public IntPtr hwndParent;
        public string pszMessageText;
        public string pszCaptionText;
        public IntPtr hbmBanner;
    }

    /// <summary>
    /// 資格情報入力ダイアログに渡すフラグ
    /// @see also:http://msdn.microsoft.com/en-us/library/windows/desktop/aa375178(v=vs.85).aspx
    /// </summary>
    internal enum CREDUIWIN_FLAG : int
    {
        GENERIC = 0x1,
        CHECKBOX = 0x2,
        AUTHPACKAGE_ONLY = 0x10,
        IN_CRED_ONLY = 0x20,
        ENUMERATE_ADMINS = 0x100,
        ENUMERATE_CURRENT_USER = 0x200,
        SECURE_PROMPT = 0x1000,
        PREPROMPTING = 0x2000,
        PACK_32_WOW = 0x10000000,
    }

    /// <summary>
    /// アンマネージドなCredential
    /// </summary>
    [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Unicode)]
    internal struct UnmanagedCredential
    {
        public uint Flags;
        public uint Type;
        public string TargetName;
        public string Comment;
        public System.Runtime.InteropServices.ComTypes.FILETIME LastWritten;
        public uint CredentialBlobSize;
        public IntPtr CredentialBlob;
        public uint Persist;
        public uint AttributeCount;
        public IntPtr Attributes;
        public string TargetAlias;
        public string UserName;
    }

    /// <summary>
    /// マネージドコードに置き換えたCredential
    /// </summary>
    public class ManagedCredential
    {
        public CRED_FLAGS Flags { get; set; }
        public CRED_TYPE Type { get; set; }
        public string TargetName { get; set; }
        public string Comment { get; set; }
        public System.Runtime.InteropServices.ComTypes.FILETIME LastWritten { get; set; }
        public byte[] CredentialBlob { get; set; }
        public CRED_PERSIST Persist { get; set; }
        public CREDENTIAL_ATTRIBUTE[] Attributes { get; set; }
        public string TargetAlias { get; set; }
        public string UserName { get; set; }

        public string GetPassword()
        {
            if (CredentialBlob.Length > 0)
            {
                return Encoding.UTF8.GetString(CredentialBlob);
            }
            else
            {
                return string.Empty;
            }
        }

        public void SetPassword(string password)
        {
            if (!string.IsNullOrEmpty(password))
            {
                CredentialBlob = Encoding.UTF8.GetBytes(password);
            }
        }
    }

}

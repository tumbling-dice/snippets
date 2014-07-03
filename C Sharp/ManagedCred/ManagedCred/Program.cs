using System;
using System.Runtime.InteropServices;

namespace ManagedCred
{
    class Program
    {
        public static void Main(string[] args)
        {
            // enumerate
            var credentials = Credential.Enumerate();
            credentials.ForEach(cred =>
            {
                Console.WriteLine("Flags:{0}", cred.Flags.ToString());
                Console.WriteLine("Type:{0}", cred.Type);
                Console.WriteLine("TargetName:{0}", cred.TargetName);
                Console.WriteLine("Comment:{0}", cred.Comment);
                Console.WriteLine("Persist:{0}", cred.Persist);
                Console.WriteLine("TargetAlias:{0}", cred.TargetAlias);
                Console.WriteLine("UserName:{0}", cred.UserName);
                Console.WriteLine("* * * * * * * * * *");
            });

            // delete
            // Credential.Delete(credentials[0]);

            // Console.WriteLine("{0}を削除しました。", credentials[0].TargetName);

            // 入力ダイアログ
            Credential.CallCredentialDialog("caption", "message");

            // read
            var c = Credential.Read(credentials[0].TargetName, credentials[0].Type);

            Console.WriteLine("Flags:{0}", c.Flags.ToString());
            Console.WriteLine("Type:{0}", c.Type);
            Console.WriteLine("TargetName:{0}", c.TargetName);
            Console.WriteLine("Comment:{0}", c.Comment);
            Console.WriteLine("Persist:{0}", c.Persist);
            Console.WriteLine("TargetAlias:{0}", c.TargetAlias);
            Console.WriteLine("UserName:{0}", c.UserName);
            Console.WriteLine("* * * * * * * * * *");

            // Write
            //credentials[0].Comment = "hoge";

            //Credential.Write(credentials[0], 0);

            Console.ReadKey();
        }

    }
}

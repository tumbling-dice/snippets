namespace CrmConnectionManager.Resource
{
    partial class CrmConnectionManagerForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.label1 = new System.Windows.Forms.Label();
            this.txbURL = new System.Windows.Forms.TextBox();
            this.txbDomain = new System.Windows.Forms.TextBox();
            this.label2 = new System.Windows.Forms.Label();
            this.txbUserName = new System.Windows.Forms.TextBox();
            this.label3 = new System.Windows.Forms.Label();
            this.txbPassword = new System.Windows.Forms.TextBox();
            this.label4 = new System.Windows.Forms.Label();
            this.btnOK = new System.Windows.Forms.Button();
            this.btnTest = new System.Windows.Forms.Button();
            this.SuspendLayout();
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(27, 29);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(27, 12);
            this.label1.TabIndex = 0;
            this.label1.Text = "URL";
            // 
            // txbURL
            // 
            this.txbURL.Location = new System.Drawing.Point(94, 26);
            this.txbURL.Name = "txbURL";
            this.txbURL.Size = new System.Drawing.Size(268, 19);
            this.txbURL.TabIndex = 1;
            // 
            // txbDomain
            // 
            this.txbDomain.Location = new System.Drawing.Point(94, 51);
            this.txbDomain.Name = "txbDomain";
            this.txbDomain.Size = new System.Drawing.Size(268, 19);
            this.txbDomain.TabIndex = 3;
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(27, 54);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(43, 12);
            this.label2.TabIndex = 2;
            this.label2.Text = "Domain";
            // 
            // txbUserName
            // 
            this.txbUserName.Location = new System.Drawing.Point(94, 76);
            this.txbUserName.Name = "txbUserName";
            this.txbUserName.Size = new System.Drawing.Size(268, 19);
            this.txbUserName.TabIndex = 5;
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(27, 79);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(58, 12);
            this.label3.TabIndex = 4;
            this.label3.Text = "UserName";
            // 
            // txbPassword
            // 
            this.txbPassword.Location = new System.Drawing.Point(94, 101);
            this.txbPassword.Name = "txbPassword";
            this.txbPassword.Size = new System.Drawing.Size(268, 19);
            this.txbPassword.TabIndex = 7;
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(27, 104);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(54, 12);
            this.label4.TabIndex = 6;
            this.label4.Text = "Password";
            // 
            // btnOK
            // 
            this.btnOK.Location = new System.Drawing.Point(94, 140);
            this.btnOK.Name = "btnOK";
            this.btnOK.Size = new System.Drawing.Size(75, 23);
            this.btnOK.TabIndex = 8;
            this.btnOK.Text = "OK";
            this.btnOK.UseVisualStyleBackColor = true;
            this.btnOK.Click += new System.EventHandler(this.btnOK_Click);
            // 
            // btnTest
            // 
            this.btnTest.Location = new System.Drawing.Point(250, 140);
            this.btnTest.Name = "btnTest";
            this.btnTest.Size = new System.Drawing.Size(112, 23);
            this.btnTest.TabIndex = 9;
            this.btnTest.Text = "Connection Test";
            this.btnTest.UseVisualStyleBackColor = true;
            this.btnTest.Click += new System.EventHandler(this.btnTest_Click);
            // 
            // CrmConnectionManagerForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 12F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(456, 184);
            this.Controls.Add(this.btnTest);
            this.Controls.Add(this.btnOK);
            this.Controls.Add(this.txbPassword);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.txbUserName);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.txbDomain);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.txbURL);
            this.Controls.Add(this.label1);
            this.Name = "CrmConnectionManagerForm";
            this.Text = "CrmConnectionManagerForm";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.TextBox txbURL;
        private System.Windows.Forms.TextBox txbDomain;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.TextBox txbUserName;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.TextBox txbPassword;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Button btnOK;
        private System.Windows.Forms.Button btnTest;
    }
}
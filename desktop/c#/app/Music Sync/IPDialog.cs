using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace MusicSync
{
    public partial class IPDialog : Form
    {
        private string dialogMessage;
        private string dialogTitle = "Server IP";

        public IPDialog(string message, string title)
        {
            dialogMessage = message;
            dialogTitle = title;
            InitializeComponent();
        }

        private void IPDialog_Load(object sender, EventArgs e)
        {
            mainTextBox.Text = dialogMessage;
            this.Text = dialogTitle;
        }

        private void closeButton_Click(object sender, EventArgs e)
        {
            this.Close();
        }
    }
}

using MusicSync.JSONObjects;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.ServiceProcess;
using System.Windows.Forms;

namespace MusicSync
{
    public partial class MainForm : Form
    {
        private static Settings settings;
        private string serverLocation = Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData) + @"\Wassup789\Music Sync\";
        private bool waitingForServerStop = false;

        public MainForm()
        {
            InitializeComponent();
        }

        protected override void SetVisibleCore(bool value)
        {
            if (Program.hasFormLoaded)
            {
                value = false;
                if (!this.IsHandleCreated) CreateHandle();
            }
            base.SetVisibleCore(value);
        }

        private void MainForm_Load(object sender, EventArgs e)
        {
            refreshSettings();
            //firewallRequest();
        }

        private void refreshSettings()
        {
            if (!Directory.Exists(serverLocation))
            {
                Directory.CreateDirectory(serverLocation);
            }
            if (!File.Exists(serverLocation + "settings.json"))
            {
                string settingsString = "{\r\n   \"port\": 13163,\r\n   \"playlists\": []\r\n}";
                System.IO.StreamWriter file = new System.IO.StreamWriter(serverLocation + "settings.json");
                file.WriteLine(settingsString);
                file.Close();
            }
            string jsonData = File.ReadAllText(serverLocation + "settings.json");
            settings = JsonConvert.DeserializeObject<Settings>(jsonData);

            if (settings.port > 65536 || settings.port < 1025)
            {
                MessageBox.Show("Music Sync Server is using an invalid port, defaulting to 13163", "Error");
                savePort(13163);
            }
            else
                portInput.Value = settings.port;

            playlistListView.Items.Clear();
            if (settings.playlists.Length > 0)
            {
                foreach (SettingsPlaylist playlist in settings.playlists)
                {
                    ListViewItem item = new ListViewItem();
                    item.Text = playlist.name;

                    ListViewItem.ListViewSubItem subitem = new ListViewItem.ListViewSubItem();
                    subitem.Text = playlist.address;
                    item.SubItems.Add(subitem);

                    playlistListView.Items.Add(item);
                }
                resetPlaylistListView();
            }
            Program.settings = settings;
        }
        
        private void savePortButton_Click(object sender, EventArgs e)
        {
            savePort();
        }

        private void savePort(int port = -1)
        {
            if (port == -1)
            {
                settings.port = (int)portInput.Value;
            }
            else
                settings.port = port;

            saveSettings();
        }

        private void resetPlaylistListView()
        {
            foreach (ColumnHeader column in playlistListView.Columns)
            {
                column.Width = -1;
            }
        }


        private void addPlaylistButton_Click(object sender, EventArgs e)
        {
            ListViewItem item = new ListViewItem();
            item.Text = "Unnamed";

            ListViewItem.ListViewSubItem subitem = new ListViewItem.ListViewSubItem();
            subitem.Text = "Select a folder";
            item.SubItems.Add(subitem);

            playlistListView.Items.Add(item);
            resetPlaylistListView();
        }
        private void editPlaylistButton_Click(object sender, EventArgs e)
        {
            if (playlistListView.SelectedItems.Count > 0)
            {
                string name = editPlaylist_getName(playlistListView.SelectedItems[0].Text);
                if (name != "___EXIT___")
                {
                    string path = editPlaylist_getPath();
                    if (path != "___EXIT___")
                    {
                        int index = playlistListView.SelectedIndices[0];
                        playlistListView.Items[index].SubItems[0].Text = name;
                        playlistListView.Items[index].SubItems[1].Text = path;

                        resetPlaylistListView();
                    }
                }
            }
        }

        private string editPlaylist_getName(string name = "")
        {
            string output = name;
            DialogResult result = ShowInputDialog(ref output);
            if (output == "" || result == DialogResult.Cancel || result == DialogResult.Abort)
                return "___EXIT___";
            
            foreach (ListViewItem item in playlistListView.Items)
            {
                if (item.SubItems[0].Text == output && item != playlistListView.SelectedItems[0])
                    return editPlaylist_getName();
            }
            return output;
        }

        private string editPlaylist_getPath()
        {
            FolderBrowserDialog fbd = new FolderBrowserDialog();
            DialogResult result = fbd.ShowDialog();

            if (result == DialogResult.Abort || result == DialogResult.Cancel)
                return "___EXIT___";
            if (fbd.SelectedPath == "")
                return editPlaylist_getPath();
            return fbd.SelectedPath;
        }

        private void deletePlaylistButton_Click(object sender, EventArgs e)
        {
            if (playlistListView.SelectedItems.Count > 0)
            {
                int index = playlistListView.SelectedIndices[0];
                playlistListView.Items[index].Remove();
            }
        }

        private void savePlaylistButton_Click(object sender, EventArgs e)
        {
            List<SettingsPlaylist> playlistList = new List<SettingsPlaylist>();
            foreach (ListViewItem item in playlistListView.Items)
            {
                if (item.SubItems[1].Text == "Select a folder")
                    continue;
                playlistList.Add(new SettingsPlaylist(item.SubItems[0].Text, item.SubItems[1].Text));
            }
            settings.playlists = playlistList.ToArray();

            saveSettings();
        }

        private void saveSettings()
        {
            string json = JsonConvert.SerializeObject(settings, Formatting.Indented);
            System.IO.StreamWriter file = new System.IO.StreamWriter(serverLocation + "settings.json");
            file.WriteLine(json);
            file.Close();
            restartServer(null, null);
            refreshSettings();
            MessageBox.Show("Saved!", "Success");
        }

        private static DialogResult ShowInputDialog(ref string input)
        {
            System.Drawing.Size size = new System.Drawing.Size(300, 70);
            Form inputBox = new Form();
            inputBox.StartPosition = FormStartPosition.CenterParent;

            inputBox.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedDialog;
            inputBox.ClientSize = size;
            inputBox.Text = "Enter a playlist name:";

            System.Windows.Forms.TextBox textBox = new TextBox();
            textBox.Size = new System.Drawing.Size(size.Width - 10, 23);
            textBox.Location = new System.Drawing.Point(5, 5);
            textBox.Text = input;
            inputBox.Controls.Add(textBox);

            Button okButton = new Button();
            okButton.DialogResult = System.Windows.Forms.DialogResult.OK;
            okButton.Name = "okButton";
            okButton.Size = new System.Drawing.Size(75, 23);
            okButton.Text = "&OK";
            okButton.Location = new System.Drawing.Point(size.Width - 80 - 80, 39);
            inputBox.Controls.Add(okButton);

            Button cancelButton = new Button();
            cancelButton.DialogResult = System.Windows.Forms.DialogResult.Cancel;
            cancelButton.Name = "cancelButton";
            cancelButton.Size = new System.Drawing.Size(75, 23);
            cancelButton.Text = "&Cancel";
            cancelButton.Location = new System.Drawing.Point(size.Width - 80, 39);
            inputBox.Controls.Add(cancelButton);

            inputBox.AcceptButton = okButton;
            inputBox.CancelButton = cancelButton;

            DialogResult result = inputBox.ShowDialog();
            input = textBox.Text;
            return result;
        }

        private void startServer(object sender, EventArgs e)
        {
            if(!Program.ws.IsRunning())
                Program.ws.Start();
        }

        private void stopServer(object sender, EventArgs e)
        {
            if(Program.ws.IsRunning())
                Program.ws.Stop();
        }

        private void restartServer(object sender, EventArgs e)
        {
            SetStatusText("Restarting");

            waitingForServerStop = true;
            if (Program.ws.IsRunning())
            {
                waitingForServerStop = true;
                Program.ws.Stop();
            }
            else
            {
                waitingForServerStop = false;
                Program.ws.Start();
            }
        }

        public void onWebServerStatusChange(int num)
        {
            switch (num)
            {
                case 0:
                    SetStatusText("Stopped");
                    if (waitingForServerStop)
                    {
                        waitingForServerStop = false;
                        Program.ws.Start();
                    }
                    break;
                case 1:
                    SetStatusText("Running");
                    break;
            }
        }

        delegate void SetStatusTextCallback(string text);
        private void SetStatusText(string text)
        {
            if (this.serverStatusText.InvokeRequired)
            {
                SetStatusTextCallback d = new SetStatusTextCallback(SetStatusText);
                this.Invoke(d, new object[] { text });
            }
            else
            {
                this.serverStatusText.Text = text;
                switch (text)
                {
                    case "Stopped":
                        startServerButton.Enabled = true;
                        stopServerButton.Enabled = false;
                        restartServerButton.Enabled = false;
                        getServerIPButton.Enabled = false;
                        break;
                    case "Running":
                        startServerButton.Enabled = false;
                        stopServerButton.Enabled = true;
                        restartServerButton.Enabled = true;
                        getServerIPButton.Enabled = true;
                        break;
                    case "Restarting":
                        startServerButton.Enabled = false;
                        stopServerButton.Enabled = false;
                        restartServerButton.Enabled = false;
                        getServerIPButton.Enabled = false;
                        break;
                }
            }
        }
        
        //Below deals with system tray and related

        private void MainForm_Resize(object sender, EventArgs e)
        {
            if (FormWindowState.Minimized == this.WindowState)
            {
                this.WindowState = FormWindowState.Normal;
                sendToTray();
            }

            else if (FormWindowState.Normal == this.WindowState)
                receiveFromTray();
        }

        private void MainForm_FormClosing(object sender, FormClosingEventArgs e)
        {
            e.Cancel = true;
            sendToTray();
        }

        private void systemTrayMenuQuit_Click(object sender, EventArgs e)
        {
            systemTrayIcon.Dispose();
            System.Environment.Exit(1);
        }

        private void systemTrayMenuOptions_Click(object sender, EventArgs e)
        {
            receiveFromTray();
        }

        private void sendToTray()
        {
            this.Hide();
        }
        private void receiveFromTray()
        {
            Program.hasFormLoaded = false;
            this.Show();
            this.Focus();
        }
        //END OF SYSTEM TRAY

        private void getServerIPButton_Click(object sender, EventArgs e)
        {
            var host = Dns.GetHostEntry(Dns.GetHostName());
            foreach (var ip in host.AddressList)
            {
                if (ip.AddressFamily == AddressFamily.InterNetwork)
                {
                    showIPDialog("http://" + ip + ":" + settings.port);
                    return;
                }
            }
            showIPDialog("No local IP found, are you connected to a router?", "Error");
        }

        public void showIPDialog(string message, string title = "Server IP")
        {
            IPDialog ipDialog = new IPDialog(message, title);
            ipDialog.ShowDialog();
        }

        private void systemTrayIcon_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            if(e.Button == MouseButtons.Left)
                receiveFromTray();
        }
    }
}

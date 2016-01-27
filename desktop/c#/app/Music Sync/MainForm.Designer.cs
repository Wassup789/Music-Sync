namespace MusicSync
{
    partial class MainForm
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
            this.components = new System.ComponentModel.Container();
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(MainForm));
            this.serverGroupBox = new System.Windows.Forms.GroupBox();
            this.getServerIPButton = new System.Windows.Forms.Button();
            this.serverStatusText = new System.Windows.Forms.Label();
            this.serverStatusLabel = new System.Windows.Forms.Label();
            this.restartServerButton = new System.Windows.Forms.Button();
            this.stopServerButton = new System.Windows.Forms.Button();
            this.startServerButton = new System.Windows.Forms.Button();
            this.playlistsGroupBox = new System.Windows.Forms.GroupBox();
            this.editPlaylistButton = new System.Windows.Forms.Button();
            this.savePlaylistButton = new System.Windows.Forms.Button();
            this.deletePlaylistButton = new System.Windows.Forms.Button();
            this.addPlaylistButton = new System.Windows.Forms.Button();
            this.playlistListView = new System.Windows.Forms.ListView();
            this.columnHeader1 = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.columnHeader2 = ((System.Windows.Forms.ColumnHeader)(new System.Windows.Forms.ColumnHeader()));
            this.systemTrayIcon = new System.Windows.Forms.NotifyIcon(this.components);
            this.systemTrayMenuStrip = new System.Windows.Forms.ContextMenuStrip(this.components);
            this.systemTrayMenuOptions = new System.Windows.Forms.ToolStripMenuItem();
            this.toolStripSeparator1 = new System.Windows.Forms.ToolStripSeparator();
            this.systemTrayMenuQuit = new System.Windows.Forms.ToolStripMenuItem();
            this.label1 = new System.Windows.Forms.Label();
            this.portInput = new System.Windows.Forms.NumericUpDown();
            this.savePortButton = new System.Windows.Forms.Button();
            this.settingsGroupBox = new System.Windows.Forms.GroupBox();
            this.serverGroupBox.SuspendLayout();
            this.playlistsGroupBox.SuspendLayout();
            this.systemTrayMenuStrip.SuspendLayout();
            ((System.ComponentModel.ISupportInitialize)(this.portInput)).BeginInit();
            this.settingsGroupBox.SuspendLayout();
            this.SuspendLayout();
            // 
            // serverGroupBox
            // 
            this.serverGroupBox.Controls.Add(this.getServerIPButton);
            this.serverGroupBox.Controls.Add(this.serverStatusText);
            this.serverGroupBox.Controls.Add(this.serverStatusLabel);
            this.serverGroupBox.Controls.Add(this.restartServerButton);
            this.serverGroupBox.Controls.Add(this.stopServerButton);
            this.serverGroupBox.Controls.Add(this.startServerButton);
            this.serverGroupBox.Location = new System.Drawing.Point(12, 12);
            this.serverGroupBox.Name = "serverGroupBox";
            this.serverGroupBox.Size = new System.Drawing.Size(198, 108);
            this.serverGroupBox.TabIndex = 1;
            this.serverGroupBox.TabStop = false;
            this.serverGroupBox.Text = "Server";
            // 
            // getServerIPButton
            // 
            this.getServerIPButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.getServerIPButton.Location = new System.Drawing.Point(95, 77);
            this.getServerIPButton.Name = "getServerIPButton";
            this.getServerIPButton.Size = new System.Drawing.Size(75, 23);
            this.getServerIPButton.TabIndex = 6;
            this.getServerIPButton.Text = "Get IP";
            this.getServerIPButton.UseVisualStyleBackColor = true;
            this.getServerIPButton.Click += new System.EventHandler(this.getServerIPButton_Click);
            // 
            // serverStatusText
            // 
            this.serverStatusText.AutoSize = true;
            this.serverStatusText.Location = new System.Drawing.Point(138, 24);
            this.serverStatusText.Name = "serverStatusText";
            this.serverStatusText.Size = new System.Drawing.Size(53, 13);
            this.serverStatusText.TabIndex = 4;
            this.serverStatusText.Text = "Unknown";
            // 
            // serverStatusLabel
            // 
            this.serverStatusLabel.AutoSize = true;
            this.serverStatusLabel.Location = new System.Drawing.Point(95, 24);
            this.serverStatusLabel.Name = "serverStatusLabel";
            this.serverStatusLabel.Size = new System.Drawing.Size(40, 13);
            this.serverStatusLabel.TabIndex = 3;
            this.serverStatusLabel.Text = "Status:";
            // 
            // restartServerButton
            // 
            this.restartServerButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.restartServerButton.Location = new System.Drawing.Point(14, 77);
            this.restartServerButton.Name = "restartServerButton";
            this.restartServerButton.Size = new System.Drawing.Size(75, 23);
            this.restartServerButton.TabIndex = 2;
            this.restartServerButton.Text = "Restart";
            this.restartServerButton.UseVisualStyleBackColor = true;
            this.restartServerButton.Click += new System.EventHandler(this.restartServer);
            // 
            // stopServerButton
            // 
            this.stopServerButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.stopServerButton.Location = new System.Drawing.Point(14, 48);
            this.stopServerButton.Name = "stopServerButton";
            this.stopServerButton.Size = new System.Drawing.Size(75, 23);
            this.stopServerButton.TabIndex = 1;
            this.stopServerButton.Text = "Stop";
            this.stopServerButton.UseVisualStyleBackColor = true;
            this.stopServerButton.Click += new System.EventHandler(this.stopServer);
            // 
            // startServerButton
            // 
            this.startServerButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.startServerButton.Location = new System.Drawing.Point(14, 19);
            this.startServerButton.Name = "startServerButton";
            this.startServerButton.Size = new System.Drawing.Size(75, 23);
            this.startServerButton.TabIndex = 0;
            this.startServerButton.Text = "Start";
            this.startServerButton.UseVisualStyleBackColor = true;
            this.startServerButton.Click += new System.EventHandler(this.startServer);
            // 
            // playlistsGroupBox
            // 
            this.playlistsGroupBox.Controls.Add(this.editPlaylistButton);
            this.playlistsGroupBox.Controls.Add(this.savePlaylistButton);
            this.playlistsGroupBox.Controls.Add(this.deletePlaylistButton);
            this.playlistsGroupBox.Controls.Add(this.addPlaylistButton);
            this.playlistsGroupBox.Controls.Add(this.playlistListView);
            this.playlistsGroupBox.Location = new System.Drawing.Point(216, 12);
            this.playlistsGroupBox.Name = "playlistsGroupBox";
            this.playlistsGroupBox.Size = new System.Drawing.Size(364, 192);
            this.playlistsGroupBox.TabIndex = 2;
            this.playlistsGroupBox.TabStop = false;
            this.playlistsGroupBox.Text = "Playlists";
            // 
            // editPlaylistButton
            // 
            this.editPlaylistButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.editPlaylistButton.Location = new System.Drawing.Point(87, 163);
            this.editPlaylistButton.Name = "editPlaylistButton";
            this.editPlaylistButton.Size = new System.Drawing.Size(75, 23);
            this.editPlaylistButton.TabIndex = 4;
            this.editPlaylistButton.Text = "Edit";
            this.editPlaylistButton.UseVisualStyleBackColor = true;
            this.editPlaylistButton.Click += new System.EventHandler(this.editPlaylistButton_Click);
            // 
            // savePlaylistButton
            // 
            this.savePlaylistButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.savePlaylistButton.Location = new System.Drawing.Point(283, 163);
            this.savePlaylistButton.Name = "savePlaylistButton";
            this.savePlaylistButton.Size = new System.Drawing.Size(75, 23);
            this.savePlaylistButton.TabIndex = 3;
            this.savePlaylistButton.Text = "Save";
            this.savePlaylistButton.UseVisualStyleBackColor = true;
            this.savePlaylistButton.Click += new System.EventHandler(this.savePlaylistButton_Click);
            // 
            // deletePlaylistButton
            // 
            this.deletePlaylistButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.deletePlaylistButton.Location = new System.Drawing.Point(168, 163);
            this.deletePlaylistButton.Name = "deletePlaylistButton";
            this.deletePlaylistButton.Size = new System.Drawing.Size(75, 23);
            this.deletePlaylistButton.TabIndex = 2;
            this.deletePlaylistButton.Text = "Delete";
            this.deletePlaylistButton.UseVisualStyleBackColor = true;
            this.deletePlaylistButton.Click += new System.EventHandler(this.deletePlaylistButton_Click);
            // 
            // addPlaylistButton
            // 
            this.addPlaylistButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Left)));
            this.addPlaylistButton.Location = new System.Drawing.Point(6, 163);
            this.addPlaylistButton.Name = "addPlaylistButton";
            this.addPlaylistButton.Size = new System.Drawing.Size(75, 23);
            this.addPlaylistButton.TabIndex = 1;
            this.addPlaylistButton.Text = "Add";
            this.addPlaylistButton.UseVisualStyleBackColor = true;
            this.addPlaylistButton.Click += new System.EventHandler(this.addPlaylistButton_Click);
            // 
            // playlistListView
            // 
            this.playlistListView.Activation = System.Windows.Forms.ItemActivation.OneClick;
            this.playlistListView.Anchor = ((System.Windows.Forms.AnchorStyles)(((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left)));
            this.playlistListView.Columns.AddRange(new System.Windows.Forms.ColumnHeader[] {
            this.columnHeader1,
            this.columnHeader2});
            this.playlistListView.FullRowSelect = true;
            this.playlistListView.GridLines = true;
            this.playlistListView.Location = new System.Drawing.Point(6, 19);
            this.playlistListView.Name = "playlistListView";
            this.playlistListView.Size = new System.Drawing.Size(352, 138);
            this.playlistListView.TabIndex = 0;
            this.playlistListView.UseCompatibleStateImageBehavior = false;
            this.playlistListView.View = System.Windows.Forms.View.Details;
            // 
            // columnHeader1
            // 
            this.columnHeader1.Tag = "";
            this.columnHeader1.Text = "Name";
            this.columnHeader1.Width = 45;
            // 
            // columnHeader2
            // 
            this.columnHeader2.Tag = "";
            this.columnHeader2.Text = "Directory";
            // 
            // systemTrayIcon
            // 
            this.systemTrayIcon.ContextMenuStrip = this.systemTrayMenuStrip;
            this.systemTrayIcon.Icon = ((System.Drawing.Icon)(resources.GetObject("systemTrayIcon.Icon")));
            this.systemTrayIcon.Text = "Music Sync";
            this.systemTrayIcon.Visible = true;
            this.systemTrayIcon.MouseDoubleClick += new System.Windows.Forms.MouseEventHandler(this.systemTrayIcon_MouseDoubleClick);
            // 
            // systemTrayMenuStrip
            // 
            this.systemTrayMenuStrip.Items.AddRange(new System.Windows.Forms.ToolStripItem[] {
            this.systemTrayMenuOptions,
            this.toolStripSeparator1,
            this.systemTrayMenuQuit});
            this.systemTrayMenuStrip.Name = "systemTrayMenuStrip";
            this.systemTrayMenuStrip.RenderMode = System.Windows.Forms.ToolStripRenderMode.System;
            this.systemTrayMenuStrip.Size = new System.Drawing.Size(126, 54);
            // 
            // systemTrayMenuOptions
            // 
            this.systemTrayMenuOptions.Name = "systemTrayMenuOptions";
            this.systemTrayMenuOptions.Size = new System.Drawing.Size(125, 22);
            this.systemTrayMenuOptions.Text = "Options...";
            this.systemTrayMenuOptions.Click += new System.EventHandler(this.systemTrayMenuOptions_Click);
            // 
            // toolStripSeparator1
            // 
            this.toolStripSeparator1.Name = "toolStripSeparator1";
            this.toolStripSeparator1.Size = new System.Drawing.Size(122, 6);
            // 
            // systemTrayMenuQuit
            // 
            this.systemTrayMenuQuit.Name = "systemTrayMenuQuit";
            this.systemTrayMenuQuit.Size = new System.Drawing.Size(125, 22);
            this.systemTrayMenuQuit.Text = "Quit";
            this.systemTrayMenuQuit.Click += new System.EventHandler(this.systemTrayMenuQuit_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(6, 25);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(29, 13);
            this.label1.TabIndex = 0;
            this.label1.Text = "Port:";
            // 
            // portInput
            // 
            this.portInput.Location = new System.Drawing.Point(41, 23);
            this.portInput.Maximum = new decimal(new int[] {
            65536,
            0,
            0,
            0});
            this.portInput.Minimum = new decimal(new int[] {
            1025,
            0,
            0,
            0});
            this.portInput.Name = "portInput";
            this.portInput.Size = new System.Drawing.Size(147, 20);
            this.portInput.TabIndex = 1;
            this.portInput.Value = new decimal(new int[] {
            13163,
            0,
            0,
            0});
            // 
            // savePortButton
            // 
            this.savePortButton.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Bottom | System.Windows.Forms.AnchorStyles.Right)));
            this.savePortButton.Location = new System.Drawing.Point(113, 49);
            this.savePortButton.Name = "savePortButton";
            this.savePortButton.Size = new System.Drawing.Size(75, 23);
            this.savePortButton.TabIndex = 2;
            this.savePortButton.Text = "Save";
            this.savePortButton.UseVisualStyleBackColor = true;
            this.savePortButton.Click += new System.EventHandler(this.savePortButton_Click);
            // 
            // settingsGroupBox
            // 
            this.settingsGroupBox.Controls.Add(this.savePortButton);
            this.settingsGroupBox.Controls.Add(this.portInput);
            this.settingsGroupBox.Controls.Add(this.label1);
            this.settingsGroupBox.Location = new System.Drawing.Point(12, 126);
            this.settingsGroupBox.Name = "settingsGroupBox";
            this.settingsGroupBox.Size = new System.Drawing.Size(198, 78);
            this.settingsGroupBox.TabIndex = 0;
            this.settingsGroupBox.TabStop = false;
            this.settingsGroupBox.Text = "Settings";
            // 
            // MainForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(584, 211);
            this.Controls.Add(this.playlistsGroupBox);
            this.Controls.Add(this.serverGroupBox);
            this.Controls.Add(this.settingsGroupBox);
            this.FormBorderStyle = System.Windows.Forms.FormBorderStyle.FixedSingle;
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.MaximizeBox = false;
            this.MinimizeBox = false;
            this.Name = "MainForm";
            this.Text = "Music Sync Configurer";
            this.FormClosing += new System.Windows.Forms.FormClosingEventHandler(this.MainForm_FormClosing);
            this.Load += new System.EventHandler(this.MainForm_Load);
            this.Resize += new System.EventHandler(this.MainForm_Resize);
            this.serverGroupBox.ResumeLayout(false);
            this.serverGroupBox.PerformLayout();
            this.playlistsGroupBox.ResumeLayout(false);
            this.systemTrayMenuStrip.ResumeLayout(false);
            ((System.ComponentModel.ISupportInitialize)(this.portInput)).EndInit();
            this.settingsGroupBox.ResumeLayout(false);
            this.settingsGroupBox.PerformLayout();
            this.ResumeLayout(false);

        }

        #endregion
        private System.Windows.Forms.GroupBox serverGroupBox;
        private System.Windows.Forms.Label serverStatusText;
        private System.Windows.Forms.Label serverStatusLabel;
        private System.Windows.Forms.Button restartServerButton;
        private System.Windows.Forms.Button stopServerButton;
        private System.Windows.Forms.Button startServerButton;
        private System.Windows.Forms.GroupBox playlistsGroupBox;
        private System.Windows.Forms.ListView playlistListView;
        private System.Windows.Forms.Button savePlaylistButton;
        private System.Windows.Forms.Button deletePlaylistButton;
        private System.Windows.Forms.Button addPlaylistButton;
        private System.Windows.Forms.ColumnHeader columnHeader1;
        private System.Windows.Forms.ColumnHeader columnHeader2;
        private System.Windows.Forms.Button editPlaylistButton;
        private System.Windows.Forms.Button getServerIPButton;
        private System.Windows.Forms.NotifyIcon systemTrayIcon;
        private System.Windows.Forms.ContextMenuStrip systemTrayMenuStrip;
        private System.Windows.Forms.ToolStripMenuItem systemTrayMenuQuit;
        private System.Windows.Forms.ToolStripMenuItem systemTrayMenuOptions;
        private System.Windows.Forms.ToolStripSeparator toolStripSeparator1;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.NumericUpDown portInput;
        private System.Windows.Forms.Button savePortButton;
        private System.Windows.Forms.GroupBox settingsGroupBox;
    }
}


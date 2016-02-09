using MusicSync.JSONObjects;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Runtime.CompilerServices;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace MusicSync
{
    static class Program
    {
        private static string serverLocation = Environment.GetEnvironmentVariable("AppData") + "\\Wassup789\\Music Sync\\";

        static ManualResetEvent quitEvent = new ManualResetEvent(false);

        public static Settings settings;
        public static bool hasFormLoaded = false;
        public static WebServer ws;

        
        [STAThread]
        static void Main(string[] args)
        {
            if(System.Diagnostics.Process.GetProcessesByName(System.IO.Path.GetFileNameWithoutExtension(System.Reflection.Assembly.GetEntryAssembly().Location)).Count() > 1)
                System.Environment.Exit(1);
            if (args.Length > 0 && (args[0].ToLower() == "-s" || args[0].ToLower() == "--startup"))
                hasFormLoaded = true;
            
            if (!Directory.Exists(serverLocation))
            {
                Directory.CreateDirectory(serverLocation);
            }
            if (!File.Exists(serverLocation + "settings.json"))
            {
                Debug.WriteLine("Could not find settings.json, generating a new file");

                string settingsString = "{\r\n   \"port\": 13163,\r\n   \"playlists\": []\r\n}";
                System.IO.StreamWriter file = new System.IO.StreamWriter(serverLocation + "settings.json");
                file.WriteLine(settingsString);
                file.Close();
            }
            string jsonData = File.ReadAllText(serverLocation + "settings.json");
            settings = JsonConvert.DeserializeObject<Settings>(jsonData);


            Application.EnableVisualStyles();
            Application.SetCompatibleTextRenderingDefault(false);
            MainForm form = new MainForm();

            ws = new WebServer(SendResponse, form, "http://" + GetServerIP() + ":" + settings.port + "/");
            ws.Run();

            Application.Run(form);
        }
        
        public static string SendResponse(HttpListenerRequest request)
        {
            switch (request.Url.LocalPath)
            {
                case "/playlists.php":
                    return GetPlaylists();
                case "/verify.php":
                    return GetVerifyPlaylist(request.QueryString);
                case "/getfiles.php":
                    return GetFiles(request.QueryString);
                case "/download.php":
                    return GetDownload(request.QueryString);
                case "/version.php":
                    Version v = System.Reflection.Assembly.GetExecutingAssembly().GetName().Version;
                    return "[" + v.Major + "," + v.Minor + "," + v.Build + "," + v.Revision + "]";
            }
            return "<a href=\"https://github.com/Wassup789/Music-Sync\">Music Sync by Wassup789</a>";
        }

        public static string GetPlaylists()
        {
            List<WebPlaylist> output = new List<WebPlaylist>();
            foreach (SettingsPlaylist playlist in settings.playlists)
            {
                if (Directory.Exists(playlist.address))
                {
                    int fileCount = Directory.GetFiles(playlist.address, "*", SearchOption.TopDirectoryOnly).Length;

                    string name_b64 = System.Convert.ToBase64String(System.Text.Encoding.UTF8.GetBytes(playlist.name));
                    output.Add(new WebPlaylist(playlist.name, name_b64, fileCount));
                }
                else
                    continue;
            }
            return JsonConvert.SerializeObject(output);
        }

        public static string GetVerifyPlaylist(System.Collections.Specialized.NameValueCollection query)
        {
            string q = query["q"];
            if (q != null)
            {
                q = System.Text.Encoding.UTF8.GetString(System.Convert.FromBase64String(q));
                foreach (SettingsPlaylist playlist in settings.playlists)
                {
                    if (playlist.name == q)
                        return "[true]";
                }
                return "[false]";
            }
            else
                return "[false]";
        }

        public static string GetFiles(System.Collections.Specialized.NameValueCollection query)
        {
            string q = query["q"];
            if (q != null)
            {
                q = System.Text.Encoding.UTF8.GetString(System.Convert.FromBase64String(q));
                foreach (SettingsPlaylist playlist in settings.playlists)
                {
                    if (playlist.name == q && Directory.Exists(playlist.address))
                    {
                        string[] files = Directory.GetFiles(playlist.address, "*", SearchOption.TopDirectoryOnly);
                        List<WebFile> output = new List<WebFile>();
                        foreach (string file in files)
                        {
                            if (file.EndsWith(".gitignore")) continue;

                            string name = file.Split('\\').Last();
                            string name_b64 = System.Convert.ToBase64String(System.Text.Encoding.UTF8.GetBytes(playlist.name + "/" + name));
                            int size = (int)new FileInfo(file).Length;

                            output.Add(new WebFile(name, name_b64, size));
                        }
                        return JsonConvert.SerializeObject(output);
                    }
                }
                return "";
            }
            else
                return "";
        }
        public static string GetDownload(System.Collections.Specialized.NameValueCollection query)
        {
            string q = query["q"];
            if (q != null)
            {
                q = System.Text.Encoding.UTF8.GetString(System.Convert.FromBase64String(q));
                string playlistName = q.Split('/')[0];
                string fileName = q.Split('/')[1];
                foreach (SettingsPlaylist playlist in settings.playlists)
                {
                    if (playlist.name == playlistName)
                    {
                        string separator = playlist.address.EndsWith("\\") ? "" : "\\";
                        if (File.Exists(playlist.address + separator + fileName))
                        {
                            return "___SERVEFILE___" + (playlist.address + separator + fileName);
                        }
                    }
                }
                return "[false]";
            }
            else
                return "[false]";
        }

        public static string GetServerIP()
        {
            var host = Dns.GetHostEntry(Dns.GetHostName());
            foreach (var ip in host.AddressList)
            {
                if (ip.AddressFamily == AddressFamily.InterNetwork)
                {
                    return ip.ToString();
                }
            }
            return "localhost";
        }
    }
}

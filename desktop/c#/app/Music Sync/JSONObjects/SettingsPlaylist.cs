using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MusicSync.JSONObjects
{
    class SettingsPlaylist
    {
        public string name { get; set; }
        public string address { get; set; }

        public SettingsPlaylist (string playlistName, string playlistAddress)
        {
            name = playlistName;
            address = playlistAddress;
        }
    }
}

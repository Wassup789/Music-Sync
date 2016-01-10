using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MusicSync.JSONObjects
{
    class Settings
    {
        public int port { get; set; }
        public SettingsPlaylist[] playlists { get; set; }
    }
}

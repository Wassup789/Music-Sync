using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MusicSync.JSONObjects
{
    class WebPlaylist
    {
        public string name { get; set; }
        public string name_b64 { get; set; }
        public int files { get; set; }

        public WebPlaylist(string playlistName, string playlistName_b64, int fileCount)
        {
            name = playlistName;
            name_b64 = playlistName_b64;
            files = fileCount;
        }
    }
}

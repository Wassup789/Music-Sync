using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace MusicSync.JSONObjects
{
    class WebFile
    {
        public string name { get; set; }
        public string name_b64 { get; set; }
        public int size { get; set; }

        public WebFile(string fileName, string fileName_b64, int fileSize)
        {
            name = fileName;
            name_b64 = fileName_b64;
            size = fileSize;
        }
    }
}

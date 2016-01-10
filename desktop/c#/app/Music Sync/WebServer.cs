using System;
using System.Net;
using System.Threading;
using System.Linq;
using System.Text;
using System.IO;

namespace MusicSync
{
    public class WebServer
    {
        private readonly HttpListener _listener = new HttpListener();
        private readonly Func<HttpListenerRequest, string> _responderMethod;
        private int port = -1;

        public WebServer(string[] prefixes, Func<HttpListenerRequest, string> method)
        {
            if (!HttpListener.IsSupported)
                throw new NotSupportedException(
                    "Needs Windows XP SP2, Server 2003 or later.");

            // URI prefixes are required, for example 
            // "http://localhost:8080/index/".
            if (prefixes == null || prefixes.Length == 0)
                throw new ArgumentException("prefixes");

            // A responder method is required
            if (method == null)
                throw new ArgumentException("method");

            foreach (string s in prefixes)
                _listener.Prefixes.Add(s);

            port = int.Parse(prefixes[0].Split(':')[2].Split('/')[0]);

            _responderMethod = method;
            try
            {
                _listener.Start();
            }
            catch (HttpListenerException e)
            {
                Console.WriteLine(string.Format("Error Type: HttpListenerException; Error Code: {0}\nCould not run server on port {1}, is another server running on that port?", e.ErrorCode, port));
                Console.ReadKey();
                System.Environment.Exit(1);
            }
        }

        public WebServer(Func<HttpListenerRequest, string> method, params string[] prefixes)
            : this(prefixes, method)
        { }

        public void Run()
        {
            ThreadPool.QueueUserWorkItem((o) =>
            {
                Console.WriteLine("Server running on port " + port);
                try
                {
                    while (_listener.IsListening)
                    {
                        ThreadPool.QueueUserWorkItem((c) =>
                        {
                            var ctx = c as HttpListenerContext;
                            try
                            {
                                string rstr = _responderMethod(ctx.Request);

                                ctx.Response.Headers.Add("Server", "\r\n\r\n");

                                if (rstr.StartsWith("___SERVEFILE___"))
                                {
                                    rstr = rstr.Remove(0, "___SERVEFILE___".Length);
                                    ctx.Response.Headers.Set("Content-Type", "audio/mp3");

                                    FileStream fs = File.OpenRead(rstr);

                                    int startByte = 0;
                                    int endByte = (int)fs.Length;
                                    byte[] buffer;
                                    if (ctx.Request.Headers["range"] != null)
                                    {
                                        string rangeHeader = ctx.Request.Headers["range"].Replace("bytes=", "");
                                        string[] range = rangeHeader.Split('-');
                                        startByte = int.Parse(range[0]);
                                        if (range[1].Trim().Length > 0) int.TryParse(range[1], out endByte);
                                        if (endByte == -1) endByte = (int)fs.Length;

                                        buffer = new byte[endByte - startByte];
                                        int totalCount = startByte + buffer.Length;

                                        ctx.Response.StatusCode = 206;
                                        ctx.Response.Headers.Set("Accept-Ranges", "bytes");
                                        ctx.Response.Headers.Set("Content-Range", string.Format("bytes {0}-{1}/{2}", startByte, totalCount - 1, totalCount));
                                        ctx.Response.Headers.Set("Connection", "keep-alive");
                                        //ctx.Response.Headers.Set("Content-Length", (totalCount - startByte).ToString());
                                    }
                                    else
                                        buffer = new byte[64 * 1024];

                                    int read;
                                    using (BinaryWriter bw = new BinaryWriter(ctx.Response.OutputStream))
                                    {
                                        while ((read = fs.Read(buffer, startByte, endByte)) > 0)
                                        {
                                            bw.Write(buffer, 0, read);
                                            bw.Flush();
                                        }

                                        bw.Close();
                                    }
                                }
                                else
                                {
                                    byte[] buf = Encoding.UTF8.GetBytes(rstr);
                                    ctx.Response.ContentLength64 = buf.Length;
                                    ctx.Response.OutputStream.Write(buf, 0, buf.Length);
                                }
                            }
                            catch { } // suppress any exceptions
                            finally
                            {
                                // always close the stream
                                ctx.Response.OutputStream.Close();
                            }
                        }, _listener.GetContext());
                    }
                }
                catch { } // suppress any exceptions
            });
        }

        public void Stop()
        {
            _listener.Stop();
            _listener.Close();
        }
    }
}
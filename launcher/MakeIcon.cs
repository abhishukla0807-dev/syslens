using System;
using System.Drawing;
using System.Drawing.Drawing2D;
using System.Drawing.Imaging;
using System.IO;
using System.Collections.Generic;

class MakeIcon
{
    static void Main()
    {
        int[] sizes = new int[] { 16, 32, 48, 64, 128, 256 };
        List<byte[]> pngBytesList = new List<byte[]>();
        List<int> dimensions = new List<int>();

        foreach (int size in sizes)
        {
            using (Bitmap bmp = new Bitmap(size, size, PixelFormat.Format32bppArgb))
            using (Graphics g = Graphics.FromImage(bmp))
            {
                g.SmoothingMode = SmoothingMode.AntiAlias;
                g.Clear(Color.Transparent);

                float margin = Math.Max(1f, size * 0.06f);
                RectangleF rect = new RectangleF(margin, margin, size - 2 * margin, size - 2 * margin);

                // Background gradient
                using (LinearGradientBrush bgBrush = new LinearGradientBrush(
                    rect,
                    Color.FromArgb(255, 15, 23, 42),
                    Color.FromArgb(255, 30, 41, 59),
                    45f))
                {
                    g.FillEllipse(bgBrush, rect);
                }

                // Cyan border ring
                float penWidth = Math.Max(1.5f, size * 0.07f);
                using (Pen cyanPen = new Pen(Color.FromArgb(255, 6, 182, 212), penWidth))
                {
                    g.DrawEllipse(cyanPen, rect);
                }

                // Inner radar arcs
                float innerMargin = size * 0.22f;
                RectangleF innerRect = new RectangleF(innerMargin, innerMargin, size - 2 * innerMargin, size - 2 * innerMargin);
                using (Pen emeraldPen = new Pen(Color.FromArgb(220, 16, 185, 129), Math.Max(1f, size * 0.05f)))
                {
                    g.DrawArc(emeraldPen, innerRect, 30f, 120f);
                    g.DrawArc(emeraldPen, innerRect, 210f, 120f);
                }

                // Core indicator
                float center = size / 2f;
                float coreRadius = Math.Max(2f, size * 0.12f);
                using (SolidBrush coreBrush = new SolidBrush(Color.FromArgb(255, 56, 189, 248)))
                {
                    g.FillEllipse(coreBrush, center - coreRadius, center - coreRadius, coreRadius * 2, coreRadius * 2);
                }

                using (MemoryStream ms = new MemoryStream())
                {
                    bmp.Save(ms, ImageFormat.Png);
                    pngBytesList.Add(ms.ToArray());
                    dimensions.Add(size);
                }
            }
        }

        string icoPath = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "syslens.ico");
        using (FileStream fs = File.Create(icoPath))
        using (BinaryWriter bw = new BinaryWriter(fs))
        {
            // ICONDIR
            bw.Write((ushort)0); // Reserved
            bw.Write((ushort)1); // Type 1 = ICO
            bw.Write((ushort)pngBytesList.Count);

            int offset = 6 + (16 * pngBytesList.Count);
            for (int i = 0; i < pngBytesList.Count; i++)
            {
                int sz = dimensions[i];
                byte bW = (sz >= 256) ? (byte)0 : (byte)sz;
                byte bH = (sz >= 256) ? (byte)0 : (byte)sz;

                bw.Write(bW);
                bw.Write(bH);
                bw.Write((byte)0); // Colors
                bw.Write((byte)0); // Reserved
                bw.Write((ushort)1); // Planes
                bw.Write((ushort)32); // Bit count
                bw.Write((uint)pngBytesList[i].Length);
                bw.Write((uint)offset);
                offset += pngBytesList[i].Length;
            }

            foreach (byte[] data in pngBytesList)
            {
                bw.Write(data);
            }
        }

        Console.WriteLine("Icon created successfully at: " + icoPath);
    }
}

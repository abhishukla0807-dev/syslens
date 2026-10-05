using System;
using System.Diagnostics;
using System.IO;
using System.Runtime.InteropServices;
using System.Text;

class SysLensLauncher
{
    [DllImport("kernel32.dll")]
    private static extern bool SetConsoleOutputCP(uint wCodePageID);

    [DllImport("kernel32.dll")]
    private static extern bool SetConsoleCP(uint wCodePageID);

    static int Main(string[] args)
    {
        // 1. Configure UTF-8 for console output and input
        try
        {
            SetConsoleOutputCP(65001);
            SetConsoleCP(65001);
            Console.OutputEncoding = Encoding.UTF8;
            Console.InputEncoding = Encoding.UTF8;
        }
        catch { }

        string baseDir = AppDomain.CurrentDomain.BaseDirectory.TrimEnd(Path.DirectorySeparatorChar, Path.AltDirectorySeparatorChar);

        // 2. Locate Java runtime
        string javaExe = FindJava(baseDir);
        if (string.IsNullOrEmpty(javaExe))
        {
            Console.ForegroundColor = ConsoleColor.Red;
            Console.WriteLine("[SysLens Launcher Error] Java Runtime Environment not found.");
            Console.ResetColor();
            Console.WriteLine("\nSysLens requires Java 21 or later, or a bundled JRE in the 'jre/' folder.");
            Console.WriteLine("Please install Java from https://adoptium.net/ or place a JRE in:");
            Console.WriteLine("  " + Path.Combine(baseDir, "jre"));
            Console.WriteLine("\nPress any key to exit...");
            Console.ReadKey();
            return 1;
        }

        // 3. Locate SysLens JAR
        string jarFile = FindJar(baseDir);
        if (string.IsNullOrEmpty(jarFile))
        {
            Console.ForegroundColor = ConsoleColor.Red;
            Console.WriteLine("[SysLens Launcher Error] syslens.jar could not be found.");
            Console.ResetColor();
            Console.WriteLine("\nExpected location: " + Path.Combine(baseDir, "target", "syslens.jar") + " or " + Path.Combine(baseDir, "syslens.jar"));
            Console.WriteLine("Please ensure the project has been built via 'mvn package'.");
            Console.WriteLine("\nPress any key to exit...");
            Console.ReadKey();
            return 1;
        }

        // 4. Construct arguments
        StringBuilder sb = new StringBuilder();
        sb.Append("--enable-native-access=ALL-UNNAMED ");
        sb.Append("-Dfile.encoding=UTF-8 ");
        sb.Append("-jar \"").Append(jarFile).Append("\"");

        if (args != null && args.Length > 0)
        {
            foreach (string arg in args)
            {
                sb.Append(" \"").Append(arg.Replace("\"", "\\\"")).Append("\"");
            }
        }

        // 5. Start SysLens Java Process
        ProcessStartInfo psi = new ProcessStartInfo
        {
            FileName = javaExe,
            Arguments = sb.ToString(),
            UseShellExecute = false,
            WorkingDirectory = baseDir
        };

        try
        {
            using (Process proc = Process.Start(psi))
            {
                proc.WaitForExit();
                return proc.ExitCode;
            }
        }
        catch (Exception ex)
        {
            Console.ForegroundColor = ConsoleColor.Red;
            Console.WriteLine("[SysLens Launcher Error] Failed to start application: " + ex.Message);
            Console.ResetColor();
            Console.WriteLine("\nPress any key to exit...");
            Console.ReadKey();
            return 1;
        }
    }

    private static string FindJava(string baseDir)
    {
        string[] candidates = new string[]
        {
            Path.Combine(baseDir, "jre", "bin", "java.exe"),
            Path.Combine(baseDir, "release", "syslens", "jre", "bin", "java.exe"),
            Path.Combine(baseDir, "dist", "SysLens", "runtime", "bin", "java.exe"),
            Path.Combine(baseDir, "runtime", "bin", "java.exe"),
            Path.Combine(baseDir, "..", "jre", "bin", "java.exe"),
            Path.Combine(baseDir, "..", "release", "syslens", "jre", "bin", "java.exe")
        };

        foreach (string candidate in candidates)
        {
            if (File.Exists(candidate))
            {
                return candidate;
            }
        }

        string javaHome = Environment.GetEnvironmentVariable("JAVA_HOME");
        if (!string.IsNullOrEmpty(javaHome))
        {
            string homeJava = Path.Combine(javaHome, "bin", "java.exe");
            if (File.Exists(homeJava))
            {
                return homeJava;
            }
        }

        // Check if java is available on system PATH
        try
        {
            ProcessStartInfo psi = new ProcessStartInfo("java.exe", "-version")
            {
                CreateNoWindow = true,
                UseShellExecute = false,
                RedirectStandardError = true
            };
            using (Process p = Process.Start(psi))
            {
                p.WaitForExit();
                if (p.ExitCode == 0)
                {
                    return "java.exe";
                }
            }
        }
        catch { }

        return null;
    }

    private static string FindJar(string baseDir)
    {
        string[] candidates = new string[]
        {
            Path.Combine(baseDir, "target", "syslens.jar"),
            Path.Combine(baseDir, "syslens.jar"),
            Path.Combine(baseDir, "release", "syslens", "syslens.jar"),
            Path.Combine(baseDir, "app", "syslens.jar"),
            Path.Combine(baseDir, "..", "target", "syslens.jar")
        };

        foreach (string candidate in candidates)
        {
            if (File.Exists(candidate))
            {
                return candidate;
            }
        }

        return null;
    }
}

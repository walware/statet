//------------------------------------------------------------------------------
// Copyright (c) 2005 StatET-Project (www.walware.de/goto/statet).
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Contributors:
//    Stephan Wahlbrink - initial API and implementation
//------------------------------------------------------------------------------

using System;
using System.Text;
using System.Diagnostics;
using System.Threading;
using System.Runtime.InteropServices;
using System.Windows.Forms;


namespace RGWConnector
{
	/// <summary>
	/// Zusammenfassende Beschreibung für Class1.
	/// </summary>
	public class RHandler {


		[DllImport("USER32.DLL", SetLastError=true)]
		private static extern bool IsIconic(IntPtr hWnd);

		[DllImport("USER32.DLL", SetLastError=true)]
		public static extern bool OpenIcon(IntPtr hWnd);

		// Get a handle to an application window.
		[DllImport("USER32.DLL", SetLastError=true)]
		public static extern IntPtr FindWindow(string lpClassName,
			string lpWindowName);

		// Activate an application window.
		[DllImport("USER32.DLL", SetLastError=true)]
		public static extern bool SetForegroundWindow(IntPtr hWnd);

//		[DllImport("USER32.DLL", SetLastError=true)]
//		public static extern bool SetActiveWindow(IntPtr hWnd);

//		[DllImport("USER32.DLL", SetLastError=true)]
//		public static extern int GetClassName(IntPtr hWnd, StringBuilder lpClassName, int nMaxCount);


		private char[] fEscapeChars;
		private string[] fEscapeCharsReplacements;

		private IntPtr fWindow;


		public RHandler() {

			// init replacements
			fEscapeChars = new char[] {
				'+', '^', '%', '~', '(', ')', '[', ']', '{', '}',
			};
			fEscapeCharsReplacements = new string[fEscapeChars.Length];
			for (int i = 0; i < fEscapeChars.Length; i++)
				fEscapeCharsReplacements[i] = "{"+fEscapeChars[i]+"}";

		}

		public void connect() {

			IntPtr window = FindWindow("Rgui Workspace", null);
			if (IntPtr.Zero.Equals(window)) {
				Process process = getProcess("RGui");
				if (process != null)
					window = process.MainWindowHandle;
			}
			//IntPtr window = FindWindow(null, "RGui");
			focusWindow(window);

			fWindow = window;
			//StringBuilder className = new StringBuilder(255);
			//GetClassName(window, className, 255);
			//Console.WriteLine(className);

		}

		private void focusWindow(IntPtr window) {

			if (IntPtr.Zero.Equals(window)) {
				throw new Exception("Unable to Find R-Process/Window (Windows-Error: "+Marshal.GetLastWin32Error()+").");
			}

			if (IsIconic(window)) {
				if (!OpenIcon(window)) {
					throw new Exception("Unable to Restore minimized R-Window (Windows-Error: "+Marshal.GetLastWin32Error()+").");
				}
			}

			if (!SetForegroundWindow(window))
				throw new Exception("Unable to Activate R-Window (Windows-Error: "+Marshal.GetLastWin32Error()+").");
		}

		public void submit(string[] text) {

			sendKeys(getPrefix());

			for (int i = 0; i < text.Length; i++) {
				sendKeys(prepareText(text[i]) + "{ENTER}");
			}
		}

		public void sendPasteClipboard() {

			string keys = getPrefix() + "^v";
            sendKeys(keys);
		}

		private string getPrefix() {

			// Activate Console-Window (Alt-w, 1)
			return "%w1";
		}

		private string prepareText(string text) {

			StringBuilder s = new StringBuilder(text);
			for (int i = 0; i < s.Length; i++) {
				for (int j = 0; j < fEscapeChars.Length; j++) {
					if (s[i] == fEscapeChars[j]) {
						s.Remove(i, 1);
						s.Insert(i, fEscapeCharsReplacements[j]);
						i += 2;
						break;
					}
				}
			}
			return s.ToString();
		}

		private void sendKeys(String keys) {

			Thread.Sleep(5);
			focusWindow(fWindow);
			System.Windows.Forms.SendKeys.Flush();
			focusWindow(fWindow);
			System.Windows.Forms.SendKeys.SendWait(keys);
		}

		private Process getProcess(string name) {

			name = name.ToLower();
			Process[] processes = Process.GetProcesses();

			for (int i = 0; i < processes.Length; i++) {
				string curName = processes[i].ProcessName;
				if (curName != null && curName.ToLower().StartsWith(name)
						&& processes[i].MainWindowHandle != IntPtr.Zero)
					return processes[i];
			}
			return null;
		}

	}
}

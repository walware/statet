//------------------------------------------------------------------------------
// Copyright (c) 2005-2006 WalWare/StatET-Project (www.walware.de/goto/statet).
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


namespace RGWConnector {

	/// <summary>
	/// Searches GUI Windows of R and send keys.
	/// </summary>
	public class RHandler {

		[DllImport("USER32.DLL", SetLastError=true)]
		private static extern bool IsIconic(IntPtr hWnd);

		[DllImport("USER32.DLL", SetLastError=true)]
		private static extern bool OpenIcon(IntPtr hWnd);

		[DllImport("USER32.DLL", SetLastError=true)]
		private static extern IntPtr FindWindow(string lpClassName, string lpWindowName);

//		[DllImport("USER32.DLL", SetLastError = true)]
//		private static extern IntPtr FindWindowEx(IntPtr hWndParent, IntPtr hWndChildtAfter, string lpClassName, string lpWindowName);

		[DllImport("USER32.DLL", SetLastError=true)]
		private static extern bool SetForegroundWindow(IntPtr hWnd);

//		[DllImport("USER32.DLL", SetLastError=true)]
//		public static extern bool SetActiveWindow(IntPtr hWnd);

//		[DllImport("USER32.DLL", SetLastError=true)]
//		private static extern int GetClassName(IntPtr hWnd, StringBuilder lpClassName, int nMaxCount);

		[DllImport("USER32.DLL", SetLastError = true)]
		private static extern uint RealGetWindowClass(IntPtr hWnd, StringBuilder pszType, uint cchType);

		[DllImport("USER32.DLL", SetLastError = true)]
		private static extern int GetWindowText(IntPtr hWnd, StringBuilder lpString, int nMaxCount);

		[DllImport("USER32.DLL", SetLastError = true)]
		private static extern int InternalGetWindowText(IntPtr hWnd, StringBuilder lpString, int nMaxCount);

		public delegate bool WndEnumProc(IntPtr hWdn, int lParam);

		[DllImport("USER32.DLL", SetLastError = true)]
		private static extern bool EnumWindows(WndEnumProc callback, int lParam);

		[DllImport("USER32.DLL", SetLastError = true)]
		private static extern bool EnumChildWindows(IntPtr hWndParent, WndEnumProc callback, int lParam);


		/*
		 * MDI:
		 * Main Window Class = Rgui Workspace
		 * Child Window Class = Rgui Document
		 * 
		 * SDI:
		 * Window Class = Rgui
		 * 
		 * */

		private const int M_MDI_CLIENT = 10;
		private const int M_MDI_FALLBACK = 11;
		private const int M_SDI = 20;
		private const int M_PROCESS = 50;

        private const int WAIT_MS = 10;


		private char[] fEscapeChars;
		private string[] fEscapeCharsReplacements;

		private IntPtr fWindow = IntPtr.Zero;
		private IntPtr fChildWindow = IntPtr.Zero;
		private int fMode = -1;


		public RHandler() {

			// init replacements
			fEscapeChars = new char[] {
				'+', '^', '%', '~', '(', ')', '[', ']', '{', '}',
			};
			fEscapeCharsReplacements = new string[fEscapeChars.Length];
			for (int i = 0; i < fEscapeChars.Length; i++) {
				fEscapeCharsReplacements[i] = "{"+fEscapeChars[i]+"}";
			}

		}

		public void connect() {

			// MDI
			if (fMode < 0) {
				fWindow = FindWindow("Rgui Workspace", null);
				if (!fWindow.Equals(IntPtr.Zero)) {
					EnumChildWindows(fWindow, new WndEnumProc(checkMDIChildWindow), 0);
					if (!fChildWindow.Equals(IntPtr.Zero)) {
						fMode = M_MDI_CLIENT;
					}
					else {
						fMode = M_MDI_FALLBACK;
					}
				}
			}

			// SDI
			if (fMode < 0) {
				EnumWindows(new WndEnumProc(checkSDIWindow), 0);
			}

			// Other UI? Search for processes
			if (fMode < 0) {
				Process process = getProcess("RGui");
				if (process != null) {
					fWindow = process.MainWindowHandle;
				}
				if (!fWindow.Equals(IntPtr.Zero)) {
					fMode = M_PROCESS;
				}
			}

			if (fMode > 0) {
				focusWindow(fWindow);
				if (fMode == M_MDI_CLIENT) {
                    Thread.Sleep(WAIT_MS);
					focusWindow(fChildWindow);
				}
			}
			else {
				fMode = 0;
			}
		}


		public bool checkMDIChildWindow(IntPtr hWdn, int lParam) {

			StringBuilder text = new StringBuilder(255);
			RealGetWindowClass(hWdn, text, 255);
			if (!text.ToString().Equals("Rgui Document")) {
				return true;
			}

			text.Remove(0, text.Length);
			GetWindowText(hWdn, text, 255);
			if (!text.ToString().StartsWith("R Console")) {
				return true;
			}

			fChildWindow = hWdn;
			return false;
		}

		public bool checkSDIWindow(IntPtr hWdn, int lParam) {

			StringBuilder text = new StringBuilder(255);
			RealGetWindowClass(hWdn, text, 255);
			if (!text.ToString().Equals("Rgui")) {
				return true;
			}

			text.Remove(0, text.Length);
			GetWindowText(hWdn, text, 255);
			if (!text.ToString().StartsWith("R Console")) {
				return true;
			}

			fWindow = hWdn;
			fMode = M_SDI;
			return false;
		}


		private void debugWindowClass(IntPtr window) {

			if (!IntPtr.Zero.Equals(window)) {
				StringBuilder type = new StringBuilder(255);
				RealGetWindowClass(window, type, 255);
				
				// Filter
				string testR = type.ToString();
				if (!testR.StartsWith("R")) return;

				StringBuilder text = new StringBuilder(255);
				GetWindowText(window, text, 255);

				StringBuilder iText = new StringBuilder(255);
				InternalGetWindowText(window, iText, 255);

				Console.WriteLine();
				Console.WriteLine("WindowClass=" + type.ToString());
				Console.WriteLine("InternalWindowText=" + iText.ToString());
				Console.WriteLine("WindowText=" + text.ToString());
			}
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

			switch (fMode) {
				case M_MDI_FALLBACK:
					// Activate Console-Window (Alt-w, 1)
					return "%w1";
				default:
					return null;
			}
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

			if (keys == null) {
				return;
			}

            Thread.Sleep(WAIT_MS);
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

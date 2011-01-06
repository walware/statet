//------------------------------------------------------------------------------
// Copyright (c) 2005-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// which accompanies this distribution, and is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// Contributors:
//    Stephan Wahlbrink - initial API and implementation
//------------------------------------------------------------------------------

using System;
using System.Collections;


namespace RGWConnector {

	/// <summary>
	/// Controller for Controller methods.
	/// </summary>
	class Connector {
	
		/// <summary>
		/// Der Haupteintrittspunkt für die Anwendung.
		/// </summary>
		[STAThread]
		static void Main(string[] args) {
		
			if (args.Length == 0) {
				return;
			}
			
			try {
				string cmd = args[0];
				
				RHandler handler = new RHandler();
				
				if (cmd == "donothing") {
					handler.connect();
				}
				else if (cmd == "submitinput") {
					string[] text = readInput();
					handler.connect();
					handler.submit(text);
				}
				else if (cmd == "pasteclipboard") {
					handler.connect();
					handler.sendPasteClipboard();
				}
			}
			catch (Exception e) {
				Console.Error.WriteLine(e.Message);
				Environment.ExitCode = 100;
			}
			finally { }
		}

		private static string[] readInput() {
			ArrayList text = new ArrayList();
			string line;
			while ((line = Console.ReadLine()) != null) {
				text.Add(line);
			}
			string[] array = (string[]) text.ToArray("".GetType());
			if (array == null) {
				return new string[0];
			}
			return array;
		}
	}
}

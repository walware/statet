/*=============================================================================#
 # Copyright (c) 2012-2014 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.statet.r.core.pkgmanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;

import de.walware.ecommons.collections.ConstList;

import de.walware.rj.renv.IRPkg;
import de.walware.rj.renv.RNumVersion;

import de.walware.statet.r.core.RCore;
import de.walware.statet.r.core.pkgmanager.RPkgAction.Install;


public class RPkgResolver {
	
	// R supports only >= for dependencies
	// to support more operators, see e.g.
	// org.eclipse.equinox.internal.p2.director.Projector
	// org.sat4j.pb.SolverFactory#newEclipseP2()
	
	
	private static class RPkgActionVersionIterator implements Iterator<RNumVersion> {
		
		
		private final List<? extends RPkgAction> fList;
		
		private int fIdx = 0;
		
		
		public RPkgActionVersionIterator(final List<? extends RPkgAction> list) {
			fList = list;
		}
		
		
		@Override
		public boolean hasNext() {
			return (fIdx < fList.size());
		}
		
		@Override
		public RNumVersion next() {
			return fList.get(++fIdx).getPkg().getVersion();
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	private static class RPkgVersionIterator implements Iterator<RNumVersion> {
		
		
		private final List<? extends IRPkg> fList;
		
		private int fIdx = 0;
		
		
		public RPkgVersionIterator(final List<? extends IRPkg> list) {
			fList = list;
		}
		
		
		@Override
		public boolean hasNext() {
			return (fIdx < fList.size());
		}
		
		@Override
		public RNumVersion next() {
			return fList.get(fIdx++).getVersion();
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	private static void removeInvalid(final IRPkg reqPkg, final List<? extends IRPkg> availablePkgs) {
		final RNumVersion reqVersion = reqPkg.getVersion();
		if (reqVersion.toString().startsWith(">=")) {
			for (final Iterator<? extends IRPkg> iter = availablePkgs.iterator(); iter.hasNext();) {
				if (!reqVersion.isSatisfiedBy(iter.next().getVersion())) {
					iter.remove();
				}
			}
		}
	}
	
	
	private class Context {
		
		
		Context() {
		}
		
		
		List<? extends IRPkgData> getRequired(final String name) {
			return fRequired.get(name);
		}
		
		void setRequired(final String name, final List<? extends IRPkgData> list) {
			fRequired.put(name, list);
		}
		
		void setRequiredMissing(final String name) {
			setRequired(name, Collections.<IRPkgData>emptyList());
		}
		
		void handleProblem(final int severity, final String message, final String... args) {
			fStatusList.add(new Status(severity, RCore.PLUGIN_ID,
					(args != null && args.length > 0) ? NLS.bind(message, args) : message ));
		}
		
	}
	
	private class TmpContext extends Context {
		
		private final Map<String, List<? extends IRPkgData>> fTmpRequired = new HashMap<String, List<? extends IRPkgData>>();
		
		
		TmpContext() {
		}
		
		
		@Override
		List<? extends IRPkgData> getRequired(final String name) {
			final List<? extends IRPkgData> list = fTmpRequired.get(name);
			return (list != null) ? list : super.getRequired(name);
		}
		
		@Override
		void setRequired(final String name, final List<? extends IRPkgData> list) {
			fTmpRequired.put(name, list);
		}
		
		@Override
		void handleProblem(final int severity, final String message, final String... args) {
			throw new OperationCanceledException();
		}
		
		Set<String> getTmpNames() {
			return fTmpRequired.keySet();
		}
		
		void merge() {
			fRequired.putAll(fTmpRequired);
		}
		
		void reset() {
			fTmpRequired.clear();
		}
		
	}
	
	private final IRPkgSet.Ext fAll;
	
	private final Map<String, List<RPkgAction.Install>> fSelected;
	private final List<String> fSelectedNames;
	private final Map<String, List<? extends IRPkgData>> fRequired;
	private boolean fAddRequired;
	private final LinkedHashSet<String> fRequiredToCheck;
	private Set<String> fSuggested;
	
	private final List<IStatus> fStatusList;
	private IStatus fStatus;
	
	
	public RPkgResolver(final IRPkgSet.Ext all, final Map<String, List<RPkgAction.Install>> pkgs) {
		fAll = all;
		fSelected = pkgs;
		final String[] names = pkgs.keySet().toArray(new String[pkgs.size()]);
		Arrays.sort(names);
		fSelectedNames = new ConstList<String>(names);
		
		fRequired = new HashMap<String, List<? extends IRPkgData>>();
		fAddRequired = true;
		fRequiredToCheck = new LinkedHashSet<String>(16);
		fStatusList = new ArrayList<IStatus>();
	}
	
	
	public void setAddSuggested(final boolean enabled) {
		if (fStatus != null) {
			throw new IllegalStateException();
		}
		fSuggested = (enabled) ? new HashSet<String>(8) : null;
	}
	
	public void setAddRequired(final boolean enabled) {
		if (fStatus != null) {
			throw new IllegalStateException();
		}
		fAddRequired = enabled;
	}
	
	
	public IStatus run() {
		resolve();
		if (fStatusList.isEmpty()) {
			fStatus = Status.OK_STATUS;
		}
		else {
			fStatus = new MultiStatus(RCore.PLUGIN_ID, 0,
					fStatusList.toArray(new IStatus[fStatusList.size()]),
					"Cannot install the selected packages.", null );
		}
		return fStatus;
	}
	
	public IStatus getStatus() {
		if (fStatus == null) {
			throw new IllegalStateException();
		}
		return fStatus;
	}
	
	
//	public boolean isSelected(final String name) {
//		return fSelected.containsKey(name);
//	}
//	
//	public boolean isRequired(final String name) {
//		return fRequired.containsKey(name)
//				&& (fSuggested == null || !fSuggested.contains(name));
//	}
//	
//	public boolean isSuggested(final String name) {
//		return (fSuggested != null && fSuggested.contains(name));
//	}
	
	public String getReason(final IRPkg pkg) {
		final String name = pkg.getName();
		if (fSelected.containsKey(name)) {
			return "selected";
		}
		if (fSuggested != null && fSuggested.contains(name)) {
			return "suggested";
		}
		if (fRequired.containsKey(name)) {
			return "required";
		}
		return null;
	}
	
	
	private void resolve() {
		final Context main = new Context();
		if (fAddRequired) {
			for (final String name : fSelectedNames) {
				final List<? extends Install> list = fSelected.get(name);
				for (final Install action : list) {
					final IRPkgData pkg = action.getPkg();
					check(pkg, "selected", "depends on", pkg.getDepends(), main);
					check(pkg, "selected", "imports", pkg.getImports(), main);
					check(pkg, "selected", "is linking to", pkg.getLinkingTo(), main);
				}
			}
			if (!fStatusList.isEmpty()) {
				return;
			}
			checkRequired(main);
			if (!fStatusList.isEmpty()) {
				return;
			}
			final TmpContext tmp = new TmpContext();
			if (fSuggested != null) {
				for (final String name : fSelectedNames) {
					final List<? extends Install> list = fSelected.get(name);
					for (final Install action : list) {
						final IRPkgData pkg = action.getPkg();
						try {
							check(pkg, "selected", "suggests", pkg.getSuggests(), tmp);
							checkRequired(tmp);
							fSuggested.addAll(tmp.getTmpNames());
							tmp.merge();
						}
						catch (final OperationCanceledException e) {
		//					fStatus.add();
						}
						finally {
							fRequiredToCheck.clear();
						}
						tmp.reset();
					}
				}
			}
		}
	}
	
	private void checkRequired(final Context context) {
		Iterator<String> iter = fRequiredToCheck.iterator();
		while (iter.hasNext()) {
			final String pkgName = iter.next();
			iter.remove();
			final List<? extends IRPkgData> list = fRequired.get(pkgName);
			if (list != null && !list.isEmpty()) {
				final IRPkgData pkg = list.get(0);
				check(pkg, "required", "depends on", pkg.getDepends(), context);
				check(pkg, "required", "imports", pkg.getImports(), context);
				check(pkg, "required", "is linking to", pkg.getLinkingTo(), context);
				iter = fRequiredToCheck.iterator();
			}
		}
	}
	
	private void check(final IRPkg pkg, final String pkgLabel, final String reqLabel,
			final List<? extends IRPkg> reqList, final Context context) {
		if (pkg.getName().equals("R")) {
			return;
		}
		for (final IRPkg reqPkg : reqList) {
			final String reqName = reqPkg.getName();
			final RNumVersion reqVersion = reqPkg.getVersion();
			if (reqName.equals("R")) {
				continue;
			}
			{	final List<? extends RPkgAction.Install> selected = fSelected.get(reqName);
				if (selected != null) {
					if (!(reqVersion == RNumVersion.NONE || reqVersion.isSatisfiedByAny(
							new RPkgActionVersionIterator(selected) ))) {
						context.handleProblem(IStatus.ERROR, NLS.bind((pkgLabel == "selected") ?
								"The selected packages ''{1}'' ({2}) and ''{3}'' ({4}) are not compatible, ''{1}'' {5} version {6} of ''{3}''." :
								"The {0} package ''{1}'' ({2}) and the selected package ''{3}'' ({4}) are not compatible, ''{1}'' {5} version {6} of ''{3}''.",
								new Object[] { pkgLabel, pkg.getName(), pkg.getVersion(),
										reqName, selected.get(0).getPkg().getVersion(), reqLabel,
										reqVersion }));
						continue;
					}
					continue;
				}
			}
			if (isReqInstalled(reqName, reqVersion)) {
				continue;
			}
			{	List<? extends IRPkgData> list = context.getRequired(reqName);
				if (list != null && list.isEmpty()) {
					continue; // already reported
				}
				IRPkgData old;
				if (list == null) {
					list = fAll.getAvailable().getByName(reqName);
					old = null;
				}
				else {
					old = list.get(0);
				}
				if (list == null || list.isEmpty()) {
					context.setRequiredMissing(reqName);
					context.handleProblem(IStatus.ERROR, NLS.bind(
							"The {0} package ''{1}'' ({2}) {4} package ''{3}'', but no version of ''{3}'' can be found.",
							new Object[] { pkgLabel, pkg.getName(), pkg.getVersion(),
									reqName, reqLabel }));
					continue;
				}
				removeInvalid(reqPkg, list);
				if (list.isEmpty()) {
					context.setRequiredMissing(reqName);
					if (old != null) {
						fRequiredToCheck.remove(reqName);
					}
					context.handleProblem(IStatus.ERROR, NLS.bind(
							"The {0} package ''{1}'' ({2}) {4} version {5} of package ''{3}'', but no compatible version of ''{3}'' can be found.",
							new Object[] { pkgLabel, pkg.getName(), pkg.getVersion(),
									reqName, reqLabel, reqVersion }));
					continue;
				}
				else {
					fRequired.put(reqName, list);
					if (old != list.get(0)) {
						fRequiredToCheck.add(reqName);
					}
					continue;
				}
			}
		}
	}
	
	private boolean isReqInstalled(final String reqName, final RNumVersion reqVersion) {
		final List<? extends IRPkgData> list = fAll.getInstalled().getByName(reqName);
		return (!list.isEmpty()
				&& (reqVersion == RNumVersion.NONE || reqVersion.isSatisfiedByAny(
						new RPkgVersionIterator(list) )));
	}
	
	
	public List<RPkgAction.Install> createActions() {
		final List<String> names = new ArrayList<String>();
		names.addAll(fSelectedNames);
		for (final String name : fRequired.keySet()) {
			final int idx = - Collections.binarySearch(names, name) - 1;
			names.add(idx, name);
		}
		
		final List<RPkgAction.Install> ordered = new ArrayList<RPkgAction.Install>(names.size());
		final Set<String> visited = new HashSet<String>(names.size());
		for (int i = names.size() - 1; i >= 0; i--) {
			toListAddPkg(names.get(i), ordered, names, visited);
		}
		
		return ordered;
	}
	
	private List<RPkgAction.Install> toListAddPkg(final String pkgName, final List<RPkgAction.Install> ordered,
			final List<String> names, final Set<String> visited) {
		if (visited.add(pkgName)) {
			final int nameIdx = Collections.binarySearch(names, pkgName);
			if (nameIdx < 0) {
				return null;
			}
			final List<RPkgAction.Install> actions = getFinal(pkgName);
			for (final RPkgAction.Install action : actions) {
				final IRPkgData pkg = action.getPkg();
				int idx = 0;
				final int n = pkg.getDepends().size() + pkg.getImports().size() + pkg.getLinkingTo().size();
				if (n > 0) {
					names.remove(nameIdx);
					final List<RPkgAction.Install> reqList = new ArrayList<RPkgAction.Install>(n);
					toListGetRequired(pkg.getDepends(), reqList, ordered, names, visited);
					toListGetRequired(pkg.getImports(), reqList, ordered, names, visited);
					toListGetRequired(pkg.getLinkingTo(), reqList, ordered, names, visited);
					names.add(nameIdx, pkgName);
					
					for (final RPkgAction.Install reqAction : reqList) {
						final int reqIdx = ordered.indexOf(reqAction);
						if (reqIdx >= 0 && reqIdx >= idx) {
							idx = reqIdx + 1;
						}
					}
				}
				ordered.add(idx, action);
			}
			return actions;
		}
		return null;
	}
	
	private List<RPkgAction.Install> getFinal(final String name) {
		{	final List<RPkgAction.Install> selected = fSelected.get(name);
			if (selected != null) {
				return selected;
			}
		}
		{	final List<? extends IRPkgData> list = fRequired.get(name);
			return (list == null || list.isEmpty()) ? Collections.<RPkgAction.Install>emptyList() :
					Collections.singletonList(new RPkgAction.Install(list.get(0), null, null));
		}
	}
	
	private void toListGetRequired(final List<? extends IRPkg> add, final List<RPkgAction.Install> reqList,
			final List<RPkgAction.Install> ordered, final List<String> names, final Set<String> visited) {
		for (int i = 0; i < add.size(); i++) {
			final IRPkg reqPkg = add.get(i);
			if (isReqInstalled(reqPkg.getName(), reqPkg.getVersion())) {
				continue; // later
			}
			final List<Install> list = toListAddPkg(reqPkg.getName(), ordered, names, visited);
			if (list != null) {
				for (int j = 0; j < list.size(); j++) {
					reqList.add(list.get(j));
				}
			}
		}
	}
	
}

/*=============================================================================#
 # Copyright (c) 2012-2015 Stephan Wahlbrink (WalWare.de) and others.
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
import java.util.IdentityHashMap;
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

import de.walware.ecommons.collections.ConstArrayList;

import de.walware.rj.renv.IRPkg;
import de.walware.rj.renv.RNumVersion;

import de.walware.statet.r.core.RCore;


public class RPkgResolver {
	
	// R supports only >= for dependencies
	// to support more operators, see e.g.
	// org.eclipse.equinox.internal.p2.director.Projector
	// org.sat4j.pb.SolverFactory#newEclipseP2()
	
	
	private static class RPkgActionVersionIterator implements Iterator<RNumVersion> {
		
		
		private final List<? extends RPkgAction> list;
		
		private int idx= 0;
		
		
		public RPkgActionVersionIterator(final List<? extends RPkgAction> list) {
			this.list= list;
		}
		
		
		@Override
		public boolean hasNext() {
			return (this.idx < this.list.size());
		}
		
		@Override
		public RNumVersion next() {
			return this.list.get(this.idx++).getPkg().getVersion();
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	private static class RPkgVersionIterator implements Iterator<RNumVersion> {
		
		
		private final List<? extends IRPkg> list;
		
		private int idx= 0;
		
		
		public RPkgVersionIterator(final List<? extends IRPkg> list) {
			this.list= list;
		}
		
		
		@Override
		public boolean hasNext() {
			return (this.idx < this.list.size());
		}
		
		@Override
		public RNumVersion next() {
			return this.list.get(this.idx++).getVersion();
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
	}
	
	private static void removeInvalid(final IRPkg reqPkg, final List<? extends IRPkg> availablePkgs) {
		final RNumVersion reqVersion= reqPkg.getVersion();
		if (reqVersion.toString().startsWith(">=")) {
			for (final Iterator<? extends IRPkg> iter= availablePkgs.iterator(); iter.hasNext();) {
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
			return RPkgResolver.this.required.get(name);
		}
		
		void setRequired(final String name, final List<? extends IRPkgData> list) {
			RPkgResolver.this.required.put(name, list);
		}
		
		void setRequiredMissing(final String name) {
			setRequired(name, Collections.<IRPkgData>emptyList());
		}
		
		void handleProblem(final int severity, final String message, final String... args) {
			RPkgResolver.this.statusList.add(new Status(severity, RCore.PLUGIN_ID,
					(args != null && args.length > 0) ? NLS.bind(message, args) : message ));
		}
		
	}
	
	private class TmpContext extends Context {
		
		private final Map<String, List<? extends IRPkgData>> tmpRequired= new HashMap<>();
		
		
		TmpContext() {
		}
		
		
		@Override
		List<? extends IRPkgData> getRequired(final String name) {
			final List<? extends IRPkgData> list= this.tmpRequired.get(name);
			return (list != null) ? list : super.getRequired(name);
		}
		
		@Override
		void setRequired(final String name, final List<? extends IRPkgData> list) {
			this.tmpRequired.put(name, list);
		}
		
		@Override
		void handleProblem(final int severity, final String message, final String... args) {
			throw new OperationCanceledException();
		}
		
		Set<String> getTmpNames() {
			return this.tmpRequired.keySet();
		}
		
		void merge() {
			RPkgResolver.this.required.putAll(this.tmpRequired);
		}
		
		void reset() {
			this.tmpRequired.clear();
		}
		
	}
	
	/**
	 * Package set from package manager.
	 */
	private final IRPkgSet.Ext pkgSet;
	
	/**
	 * Packages requested for installation.
	 **/
	private final Map<String, List<RPkgAction.Install>> selected;
	/**
	 * Sorted list of names of packages requested for installation.
	 **/
	private final List<String> selectedNames;
	
	/**
	 * Packages required for installation of the request (but not selected).
	 * The list for each required package contains all package versions valid for the installation.
	 * The first is finally installed. If empty, no valid package version is available.
	 **/
	private final Map<String, List<? extends IRPkgData>> required;
	/**
	 * If required packages should be added.
	 */
	private boolean addRequired;
	/**
	 * Set of names of {@link #required required packages} which need to be checked.
	 */
	private final LinkedHashSet<String> requiredToCheck;
	/**
	 * If the package is only suggested.
	 */
	private Set<String> suggested;
	
	private final List<IStatus> statusList;
	private IStatus status;
	
	
	public RPkgResolver(final IRPkgSet.Ext pkgSet, final Map<String, List<RPkgAction.Install>> pkgs) {
		this.pkgSet= pkgSet;
		this.selected= pkgs;
		final String[] names= pkgs.keySet().toArray(new String[pkgs.size()]);
		Arrays.sort(names);
		this.selectedNames= new ConstArrayList<>(names);
		
		this.required= new IdentityHashMap<>();
		this.addRequired= true;
		this.requiredToCheck= new LinkedHashSet<>(16);
		this.statusList= new ArrayList<>();
	}
	
	
	public void setAddSuggested(final boolean enabled) {
		if (this.status != null) {
			throw new IllegalStateException();
		}
		this.suggested= (enabled) ? new HashSet<String>(8) : null;
	}
	
	public void setAddRequired(final boolean enabled) {
		if (this.status != null) {
			throw new IllegalStateException();
		}
		this.addRequired= enabled;
	}
	
	
	public IStatus run() {
		resolve();
		if (this.statusList.isEmpty()) {
			this.status= Status.OK_STATUS;
		}
		else {
			this.status= new MultiStatus(RCore.PLUGIN_ID, 0,
					this.statusList.toArray(new IStatus[this.statusList.size()]),
					"Cannot install the selected packages.", null );
		}
		return this.status;
	}
	
	public IStatus getStatus() {
		if (this.status == null) {
			throw new IllegalStateException();
		}
		return this.status;
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
		final String name= pkg.getName();
		if (this.selected.containsKey(name)) {
			return "selected";
		}
		if (this.suggested != null && this.suggested.contains(name)) {
			return "suggested";
		}
		if (this.required.containsKey(name)) {
			return "required";
		}
		return null;
	}
	
	
	private void resolve() {
		final Context main= new Context();
		if (this.addRequired) {
			for (final String name : this.selectedNames) {
				final List<? extends RPkgAction.Install> list= this.selected.get(name);
				for (final RPkgAction.Install action : list) {
					final IRPkgData pkg= action.getPkg();
					check(pkg, "selected", "depends on", pkg.getDepends(), main);
					check(pkg, "selected", "imports", pkg.getImports(), main);
					check(pkg, "selected", "is linking to", pkg.getLinkingTo(), main);
				}
			}
			if (!this.statusList.isEmpty()) {
				return;
			}
			checkRequired(main);
			if (!this.statusList.isEmpty()) {
				return;
			}
			final TmpContext tmp= new TmpContext();
			if (this.suggested != null) {
				for (final String name : this.selectedNames) {
					final List<? extends RPkgAction.Install> list= this.selected.get(name);
					for (final RPkgAction.Install action : list) {
						final IRPkgData pkg= action.getPkg();
						try {
							check(pkg, "selected", "suggests", pkg.getSuggests(), tmp);
							checkRequired(tmp);
							this.suggested.addAll(tmp.getTmpNames());
							tmp.merge();
						}
						catch (final OperationCanceledException e) {
//							e.printStackTrace();
						}
						finally {
							this.requiredToCheck.clear();
						}
						tmp.reset();
					}
				}
			}
		}
	}
	
	private void checkRequired(final Context context) {
		Iterator<String> iter= this.requiredToCheck.iterator();
		while (iter.hasNext()) {
			final String pkgName= iter.next();
			iter.remove();
			final List<? extends IRPkgData> list= context.getRequired(pkgName);
			if (list != null && !list.isEmpty()) {
				final IRPkgData pkg= list.get(0);
				check(pkg, "required", "depends on", pkg.getDepends(), context);
				check(pkg, "required", "imports", pkg.getImports(), context);
				check(pkg, "required", "is linking to", pkg.getLinkingTo(), context);
				iter= this.requiredToCheck.iterator();
			}
		}
	}
	
	private void check(final IRPkg pkg, final String pkgLabel, final String reqLabel,
			final List<? extends IRPkg> reqList, final Context context) {
		if (pkg.getName().equals("R")) {
			return;
		}
		for (final IRPkg reqPkg : reqList) {
			final String reqName= reqPkg.getName();
			final RNumVersion reqVersion= reqPkg.getVersion();
			if (reqName.equals("R")) {
				continue;
			}
			{	final List<? extends RPkgAction.Install> selected= this.selected.get(reqName);
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
			{	List<? extends IRPkgData> list= context.getRequired(reqName);
				if (list != null && list.isEmpty()) {
					continue; // already reported
				}
				IRPkgData old;
				if (list == null) {
					list= this.pkgSet.getAvailable().getByName(reqName);
					old= null;
				}
				else {
					old= list.get(0);
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
						this.requiredToCheck.remove(reqName);
					}
					context.handleProblem(IStatus.ERROR, NLS.bind(
							"The {0} package ''{1}'' ({2}) {4} version {5} of package ''{3}'', but no compatible version of ''{3}'' can be found.",
							new Object[] { pkgLabel, pkg.getName(), pkg.getVersion(),
									reqName, reqLabel, reqVersion }));
					continue;
				}
				else {
					context.setRequired(reqName, list);
					if (old != list.get(0)) {
						this.requiredToCheck.add(reqName);
					}
					continue;
				}
			}
		}
	}
	
	private boolean isReqInstalled(final String reqName, final RNumVersion reqVersion) {
		final List<? extends IRPkgData> list= this.pkgSet.getInstalled().getByName(reqName);
		return (!list.isEmpty()
				&& (reqVersion == RNumVersion.NONE || reqVersion.isSatisfiedByAny(
						new RPkgVersionIterator(list) )));
	}
	
	
	private class ActionCollector {
		
		
		private final Set<String> visited;
		
		private final List<RPkgAction.Install> ordered;
		
		
		public ActionCollector() {
			final int count= RPkgResolver.this.selected.size() + RPkgResolver.this.required.size();
			
			this.visited= new HashSet<>(count);
			
			this.ordered= new ArrayList<>(count);
		}
		
		
		public void run() {
			for (final String name : RPkgResolver.this.selectedNames) {
				addPkg(name);
			}
			
			// only required if (suggested != null)
			final Set<String> keySet= RPkgResolver.this.required.keySet();
			final String[] names= keySet.toArray(new String[keySet.size()]);
			Arrays.sort(names);
			for (final String name : names) {
				addPkg(name);
			}
		}
		
		private List<RPkgAction.Install> getFinal(final String name) {
			{	final List<RPkgAction.Install> selected= RPkgResolver.this.selected.get(name);
				if (selected != null) {
					return selected;
				}
			}
			{	final List<? extends IRPkgData> list= RPkgResolver.this.required.get(name);
				if (list != null && !list.isEmpty()) {
					return Collections.singletonList(
							new RPkgAction.Install(list.get(0), null, null) );
				}
			}
			return Collections.emptyList();
		}
		
		private void addPkg(final String pkgName) {
			if (this.visited.add(pkgName)) {
				final List<RPkgAction.Install> actions= getFinal(pkgName);
				for (final RPkgAction.Install action : actions) {
					final IRPkgData pkg= action.getPkg();
					
					addReqPkgs(pkg.getDepends());
					addReqPkgs(pkg.getImports());
					addReqPkgs(pkg.getLinkingTo());
					
					this.ordered.add(action);
				}
			}
		}
		
		private void addReqPkgs(final List<? extends IRPkg> pkgs) {
			for (final IRPkg pkg : pkgs) {
				if (isReqInstalled(pkg.getName(), pkg.getVersion())) {
					continue; // later
				}
				addPkg(pkg.getName());
			}
		}
		
		public List<RPkgAction.Install> getActions() {
			return this.ordered;
		}
		
	}
	
	public List<RPkgAction.Install> createActions() {
		final ActionCollector collector= new ActionCollector();
		collector.run();
		return collector.getActions();
	}
	
}

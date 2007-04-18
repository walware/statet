package de.walware.statet.nico.internal.core;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;

import de.walware.statet.nico.core.ITool;


public class ToolPropertyTester extends PropertyTester {

	
	public static final String IS_PROVIDING_FEATURE = "isProvidingFeatureSet";
	
	
	public ToolPropertyTester() {
	}

	
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		
		ITool tool = null;
		if (receiver instanceof ITool) {
			tool = (ITool) receiver;
		}
		if (receiver instanceof IAdaptable) {
			tool = (ITool) ((IAdaptable) receiver).getAdapter(ITool.class);
		}
		if (tool == null) {
			return false;
		}
		
		if (property.equals(IS_PROVIDING_FEATURE)) {
			for (Object obj : args) {
				if (!tool.isProvidingFeatureSet((String) obj)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

}

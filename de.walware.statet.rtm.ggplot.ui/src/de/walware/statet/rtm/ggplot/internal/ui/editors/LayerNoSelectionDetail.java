package de.walware.statet.rtm.ggplot.internal.ui.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.walware.ecommons.emf.ui.forms.DetailStack;
import de.walware.ecommons.emf.ui.forms.EFFormSection;
import de.walware.ecommons.emf.ui.forms.IEFFormPage;


class LayerNoSelectionDetail extends LayerDetail {
	
	
	private static class MessageSection extends EFFormSection {
		
		
		public MessageSection(final IEFFormPage page, final Composite parent) {
			super(page, parent,
					"No Layer Selected",
					"Select a Layer to edit its details.");
			
			createClient();
		}
		
		
		@Override
		protected void createContent(final Composite composite) {
			final Label label = new Label(composite, SWT.NONE);
			final GridData gd = new GridData();
			gd.horizontalSpan = 3;
			gd.grabExcessHorizontalSpace = true;
			gd.widthHint = 10;
			label.setLayoutData(gd);
		}
		
	}
	
	
	public LayerNoSelectionDetail(final DetailStack parent) {
		super(parent, null);
	}
	
	@Override
	protected void createContent(final Composite composite) {
		final MessageSection section = new MessageSection(getPage(), composite);
		section.getSection().setLayoutData(createSectionLayoutData());
	}
	
}

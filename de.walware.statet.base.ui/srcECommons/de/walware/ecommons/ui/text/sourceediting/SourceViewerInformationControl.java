/*******************************************************************************
 * Copyright (c) 2000-2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package de.walware.ecommons.ui.text.sourceediting;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.editors.text.EditorsUI;

import de.walware.ecommons.ui.util.LayoutUtil;


/**
 * Source viewer based implementation of <code>IInformationControl</code>.
 * Displays information in a source viewer.
 */
public class SourceViewerInformationControl extends AbstractInformationControl {
	
	
	private static final int MODE_SIMPLE = 1;
	private static final int MODE_FOCUS = 2;
	
	
	private int fMode;
	
	private Composite fContentComposite;
	
	/** The control's text widget */
	private StyledText fText;
	/** The control's source viewer */
	private SourceViewer fViewer;
	
	private SourceEditorViewerConfigurator fConfigurator;
	
	private IInformationControlCreator fInformationCreator;
	
	/**
	 * The orientation of the shell
	 */
	private final int fOrientation;
	
	
	/**
	 * Creates a source viewer information control with the given shell as parent. The given
	 * styles are applied to the created styled text widget. The status field will
	 * contain the given text or be hidden.
	 * 
	 * @param parent the parent shell
	 * @param configurator used to configure the source viewer of the hover
	 * @param informationConfigurator used to configure the source viewer of the information presenter
	 * @param orientation the orientation
	 */
	public SourceViewerInformationControl(final Shell parent, final SourceEditorViewerConfigurator configurator, final int orientation, final IInformationControlCreator informationCreator) {
		super(parent, EditorsUI.getTooltipAffordanceString());
		
		assert (orientation == SWT.RIGHT_TO_LEFT || orientation == SWT.LEFT_TO_RIGHT || orientation == SWT.NONE);
		
		fMode = MODE_SIMPLE;
		fConfigurator = configurator;
		fOrientation = orientation;
		fInformationCreator = informationCreator;
		create();
	}
	
	public SourceViewerInformationControl(final Shell parent, final SourceEditorViewerConfigurator configurator, final int orientation) {
		super(parent, true);
		
		assert (orientation == SWT.RIGHT_TO_LEFT || orientation == SWT.LEFT_TO_RIGHT || orientation == SWT.NONE);
		
		fMode = MODE_FOCUS;
		fConfigurator = configurator;
		fOrientation = orientation;
		create();
	}
		
	@Override
	protected void createContent(final Composite parent) {
		fContentComposite = new Composite(parent, SWT.NONE) {
			@Override
			public Point computeSize(final int width, final int height, final boolean changed) {
				return super.computeSize(width, height, changed || width != getSize().x);
			}
		};
		fContentComposite.setBackground(parent.getBackground());
		
		final GridLayout gridLayout = LayoutUtil.applyCompositeDefaults(new GridLayout(), 1);
		fContentComposite.setLayout(gridLayout);
		
		final int vIndent = Math.max(1, LayoutUtil.defaultVSpacing() / 4);
		final int hIndent = Math.max(2, LayoutUtil.defaultHSpacing() / 3);
		
		// Source viewer
		fViewer = new SourceViewer(fContentComposite, null, null, false, (fMode == MODE_FOCUS ?
				(SWT.V_SCROLL | SWT.H_SCROLL) : SWT.NONE) | SWT.MULTI | SWT.READ_ONLY | fOrientation);
		fViewer.setEditable(false);
		final ViewerSourceEditorAdapter editor = new ViewerSourceEditorAdapter(fViewer, fConfigurator);
		fConfigurator.setTarget(editor);
		
		fText = fViewer.getTextWidget();
		final GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		fText.setLayoutData(gd);
		fText.setFont(JFaceResources.getTextFont());
		fText.setIndent(hIndent);
		
//		fText.addKeyListener(new KeyListener() {
//			public void keyPressed(final KeyEvent e)  {
//				if (e.character == 0x1B) // ESC
//					fShell.dispose();
//			}
//			public void keyReleased(final KeyEvent e) {}
//		});
		setBackgroundColor(getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
		setForegroundColor(getShell().getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
	}
	
	@Override
	public void setBackgroundColor(final Color background) {
		super.setBackgroundColor(background);
		fText.setBackground(background);
	}
	
	@Override
	public void setForegroundColor(final Color foreground) {
		super.setForegroundColor(foreground);
		fText.setForeground(foreground);
	}
	
	@Override
	public void setInformation(String content) {
		if (content == null) {
			fViewer.setInput(null);
			return;
		}
		
		if (content.length() > 2 
				&& content.charAt(content.length()-1) == '\n') {
			if (content.charAt(content.length()-2) == '\r') {
				content = content.substring(0, content.length()-2);
			}
			else {
				content = content.substring(0, content.length()-1);
			}
		}
		
		final IDocument document = new Document(content);
		fConfigurator.getDocumentSetupParticipant().setup(document);
		fViewer.setInput(document);
	}
	
	@Override
	public final void dispose() {
		if (fConfigurator != null) {
			fConfigurator.unconfigureTarget();
		}
		super.dispose();
		fText = null;
	}
	
	@Override
	public void setFocus() {
		fText.setFocus();
		super.setFocus();
	}
	
	public boolean hasContents() {
		return fText.getCharCount() > 0;
	}
	
	protected ISourceViewer getViewer()  {
		return fViewer;
	}
	
	@Override
	public Point computeSizeHint() {
		final Point sizeConstraints = getSizeConstraints();
		final Rectangle trim = computeTrim();
		
		int widthHint = fText.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x + LayoutUtil.defaultHSpacing();
		final int widthMax = ((sizeConstraints != null && sizeConstraints.x != SWT.DEFAULT) ?
				sizeConstraints.x : LayoutUtil.hintWidth(fText, null, 80)) - trim.width;
		if (widthHint > widthMax) {
			widthHint = widthMax;
		}
		
		final int heightMax = ((sizeConstraints != null && sizeConstraints.y != SWT.DEFAULT) ?
				sizeConstraints.y :
				fText.getLineHeight()*12) - trim.height;
		
		final Point size = fContentComposite.computeSize(widthHint, SWT.DEFAULT, false);
		size.x = Math.max(Math.min(size.x, widthMax), 200) + trim.width;
		size.y = Math.max(Math.min(size.y, heightMax), 80) + trim.height;
		return size;
	}
	
	@Override
	public Rectangle computeTrim() {
		final Rectangle trim = super.computeTrim();
		
		final Rectangle textTrim = fText.computeTrim(0, 0, 0, 0);
		trim.x += textTrim.x;
		trim.y += textTrim.y;
		trim.width += textTrim.width;
		trim.height += textTrim.height;
		
		return trim;
	}
	
	@Override
	public Point computeSizeConstraints(final int widthInChars, final int heightInChars) {
		final GC gc = new GC(fText);
		gc.setFont(JFaceResources.getDialogFont());
		final int width = gc.getFontMetrics().getAverageCharWidth() * widthInChars;
		final int height = gc.getFontMetrics().getHeight() * heightInChars;
		gc.dispose();
		
		return new Point(width, height);
	}
	
	@Override
	public IInformationControlCreator getInformationPresenterControlCreator() {
		return fInformationCreator;
	}
	
}

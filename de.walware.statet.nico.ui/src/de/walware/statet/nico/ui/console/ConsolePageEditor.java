/*******************************************************************************
 * Copyright (c) 2005-2011 WalWare/StatET-Project (www.walware.de/goto/statet).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Stephan Wahlbrink - initial API and implementation
 *******************************************************************************/

package de.walware.statet.nico.ui.console;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.filebuffers.IDocumentSetupParticipant;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.commands.ActionHandler;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import de.walware.ecommons.io.FileUtil;
import de.walware.ecommons.ltk.ISourceUnit;
import de.walware.ecommons.ltk.ui.sourceediting.DeleteLineHandler;
import de.walware.ecommons.ltk.ui.sourceediting.GotoMatchingBracketHandler;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditor;
import de.walware.ecommons.ltk.ui.sourceediting.ISourceEditorCommandIds;
import de.walware.ecommons.ltk.ui.sourceediting.ITextEditToolSynchronizer;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfiguration;
import de.walware.ecommons.ltk.ui.sourceediting.SourceEditorViewerConfigurator;
import de.walware.ecommons.ltk.ui.sourceediting.SourceViewerJFaceUpdater;
import de.walware.ecommons.ltk.ui.sourceediting.StructureSelectHandler;
import de.walware.ecommons.ltk.ui.sourceediting.StructureSelectionHistory;
import de.walware.ecommons.ltk.ui.sourceediting.StructureSelectionHistoryBackHandler;
import de.walware.ecommons.preferences.PreferencesUtil;
import de.walware.ecommons.text.PairMatcher;
import de.walware.ecommons.text.PartitioningConfiguration;
import de.walware.ecommons.text.ui.InformationDispatchHandler;
import de.walware.ecommons.text.ui.TextViewerAction;
import de.walware.ecommons.ui.ISettingsChangedHandler;
import de.walware.ecommons.ui.util.UIAccess;

import de.walware.statet.nico.core.runtime.History;
import de.walware.statet.nico.core.runtime.History.Entry;
import de.walware.statet.nico.core.runtime.IHistoryListener;
import de.walware.statet.nico.core.runtime.Prompt;
import de.walware.statet.nico.core.runtime.SubmitType;
import de.walware.statet.nico.core.runtime.ToolController;
import de.walware.statet.nico.core.runtime.ToolProcess;
import de.walware.statet.nico.core.runtime.ToolWorkspace;
import de.walware.statet.nico.internal.ui.Messages;
import de.walware.statet.nico.internal.ui.NicoUIPlugin;
import de.walware.statet.nico.internal.ui.preferences.ConsolePreferences;


/**
 * The input line (prompt, input, submit button) of the console page.
 */
public class ConsolePageEditor implements ISettingsChangedHandler, ISourceEditor {
	
	
	final static int KEY_HIST_UP = SWT.ARROW_UP;
	final static int KEY_HIST_DOWN = SWT.ARROW_DOWN;
	final static int KEY_HIST_SPREFIX_UP = SWT.ALT | SWT.ARROW_UP;
	final static int KEY_HIST_SPREFIX_DOWN = SWT.ALT | SWT.ARROW_DOWN;
	final static int KEY_SUBMIT_DEFAULT = SWT.CR;
	final static int KEY_SUBMIT_KEYPAD = SWT.KEYPAD_CR;
	
	final static int KEY_OUTPUT_LINEUP = SWT.SHIFT | SWT.ARROW_UP;
	final static int KEY_OUTPUT_LINEDOWN = SWT.SHIFT | SWT.ARROW_DOWN;
	final static int KEY_OUTPUT_PAGEUP = SWT.SHIFT | SWT.PAGE_UP;
	final static int KEY_OUTPUT_PAGEDOWN = SWT.SHIFT | SWT.PAGE_DOWN;
	final static int KEY_OUTPUT_START = SWT.MOD1 | SWT.SHIFT | SWT.HOME;
	final static int KEY_OUTPUT_END = SWT.MOD1 | SWT.SHIFT | SWT.END;
	
	
	/**
	 * Creates and returns a new SWT image with the given size on
	 * the given display which is used as this range indicator's image.
	 * 
	 * @see org.eclipse.ui.texteditor.DefaultRangeIndicator
	 * 
	 * @param display the display on which to create the image
	 * @param size the image size
	 * @return a new image
	 */
	private static Image createImage(final Display display) {
		final Point size = new Point(8, 8);
		final int width = size.x;
		final int height = size.y;
		
		if (fgPaletteData == null)
			fgPaletteData = createPalette(display);
		
		final ImageData imageData = new ImageData(width, height, 1, fgPaletteData);
		
		for (int y= 0; y < height; y++)
			for (int x= 0; x < width; x++)
				imageData.setPixel(x, y, (x + y) % 2);
		
		return new Image(display, imageData);
	}
	
	/**
	 * Creates and returns a new color palette data.
	 * 
	 * @param display
	 * @return the new color palette data
	 */
	private static PaletteData createPalette(final Display display) {
		Color c1;
		Color c2;
		
		if (true) {
			// range lighter
			c1= display.getSystemColor(SWT.COLOR_LIST_SELECTION);
			c2= display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);
		} else {
			// range darker
			c1= display.getSystemColor(SWT.COLOR_LIST_SELECTION);
			c2= display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		}
		
		final RGB rgbs[] = new RGB[] {
			new RGB(c1.getRed(), c1.getGreen(), c1.getBlue()),
			new RGB(c2.getRed(), c2.getGreen(), c2.getBlue())};
		
		return new PaletteData(rgbs);
	}
	
	
	private final class ScrollControl implements Listener {
		
		private static final int MAX = 150;
		private static final int SPECIAL = 80;
		private final Slider fSlider;
		private int fLastPos = 0;
		
		private ScrollControl(final Slider slider) {
			fSlider = slider;
			fSlider.setMaximum(MAX);
			fSlider.addListener(SWT.Selection, this);
		}
		
		public void handleEvent(final Event event) {
			final int selection = fSlider.getSelection();
			int newIndex;
			final StyledText output = fConsolePage.getOutputViewer().getTextWidget();
			final StyledText input = fSourceViewer.getTextWidget();
			int current = Math.max(output.getHorizontalIndex(), input.getHorizontalIndex());
			if (event.detail == SWT.DRAG) {
				if (fLastPos < 0) {
					fLastPos = current;
				}
				if (current > SPECIAL || fLastPos > SPECIAL) {
					current = fLastPos;
					final double diff = selection-SPECIAL;
					newIndex = current + (int) (
							Math.signum(diff) * Math.max(Math.exp(Math.abs(diff)/7.5)-1.0, 0.0) * 2.0 );
					if (newIndex < selection) {
						newIndex = selection;
					}
				}
				else {
					newIndex = selection;
				}
			}
			else {
				if (current > SPECIAL) {
					newIndex = current + selection-SPECIAL;
				}
				else {
					newIndex = selection;
				}
			}
			output.setHorizontalIndex(newIndex);
			input.setHorizontalIndex(newIndex);
			current = Math.max(output.getHorizontalIndex(), input.getHorizontalIndex());
			if (event.detail != SWT.DRAG) {
				fSlider.setSelection(current > 80 ? 80 : current);
				fLastPos = -1;
			}
		}
		
		public void reset() {
			fSlider.setSelection(0);
			final StyledText output = fConsolePage.getOutputViewer().getTextWidget();
			output.setHorizontalIndex(0);
			fLastPos = -1;
		}
	}
	
	private class StatusLine implements IEditorStatusLine {
		
		private boolean fMessageSetted;
		private String fMessage;
		
		public void setMessage(final boolean error, final String message, final Image image) {
			final IStatusLineManager manager = fConsolePage.getSite().getActionBars().getStatusLineManager();
			if (manager != null) {
				fMessageSetted = true;
				if (error) {
					manager.setErrorMessage(image, message);
				}
				else {
					manager.setMessage(image, message);
				}
			}
		}
		
		void cleanStatusLine() {
			if (fMessageSetted) {
				final IStatusLineManager manager = fConsolePage.getSite().getActionBars().getStatusLineManager();
				if (manager != null) {
					fMessageSetted = false;
					manager.setErrorMessage(null);
					updateWD();
				}
				
			}
		}
		
		void updateWD() {
			if (!fMessageSetted) {
				final IStatusLineManager manager = fConsolePage.getSite().getActionBars().getStatusLineManager();
				if (manager != null) {
					final String path = FileUtil.toString(fConsolePage.getTool().getWorkspaceData().getWorkspaceDir());
					fMessage = path;
					manager.setMessage(path);
				}
			}
		}
		
		void refresh() {
			final IStatusLineManager manager = fConsolePage.getSite().getActionBars().getStatusLineManager();
			if (manager != null) {
				manager.setMessage(fMessage);
			}
		}
		
	}
	
	private class ThisKeyListener implements VerifyKeyListener {
		
		public void verifyKey(final VerifyEvent e) {
			final int key = (e.stateMask | e.keyCode);
			switch (key) {
			case KEY_HIST_UP:
				doHistoryOlder(null);
				break;
			case KEY_HIST_DOWN:
				doHistoryNewer(null);
				break;
			case KEY_SUBMIT_DEFAULT:
			case KEY_SUBMIT_KEYPAD:
				doSubmit();
				return; // e.doit = true, to exit linked mode
			
			case KEY_OUTPUT_LINEUP:
				doOutputLineUp();
				break;
			case KEY_OUTPUT_LINEDOWN:
				doOutputLineDown();
				break;
			case KEY_OUTPUT_PAGEUP:
				doOutputPageUp();
				break;
			case KEY_OUTPUT_PAGEDOWN:
				doOutputPageDown();
				break;
			default:
				// non-constant values
				if (key == KEY_OUTPUT_START) {
					doOutputFirstLine();
					break;
				}
				if (key == KEY_OUTPUT_END) {
					doOutputLastLine();
					break;
				}
				// no special key
				return;
			}
			e.doit = false;
		}
		
		public void keyReleased(final KeyEvent e) {
		}
		
		private void doOutputLineUp() {
			final StyledText output = (StyledText) fConsolePage.getOutputViewer().getControl();
			final int next = output.getTopIndex() - 1;
			if (next < 0) {
				return;
			}
			output.setTopIndex(next);
		}
		
		private void doOutputLineDown() {
			final StyledText output = (StyledText) fConsolePage.getOutputViewer().getControl();
			final int next = output.getTopIndex() + 1;
			if (next >= output.getLineCount()) {
				return;
			}
			output.setTopIndex(next);
		}
		
		private void doOutputPageUp() {
			final StyledText output = (StyledText) fConsolePage.getOutputViewer().getControl();
			final int current = output.getTopIndex();
			final int move = Math.max(1, (output.getClientArea().height / output.getLineHeight()) - 1);
			final int next = Math.max(0, current - move);
			if (next == current) {
				return;
			}
			output.setTopIndex(next);
		}
		
		private void doOutputPageDown() {
			final StyledText output = (StyledText) fConsolePage.getOutputViewer().getControl();
			final int current = output.getTopIndex();
			final int move = Math.max(1, (output.getClientArea().height / output.getLineHeight()) - 1);
			final int next = Math.min(current + move, output.getLineCount() - 1);
			if (next == current) {
				return;
			}
			output.setTopIndex(next);
		}
		
		private void doOutputFirstLine() {
			final StyledText output = (StyledText) fConsolePage.getOutputViewer().getControl();
			final int next = 0;
			output.setTopIndex(next);
		}
		
		private void doOutputLastLine() {
			final StyledText output = (StyledText) fConsolePage.getOutputViewer().getControl();
			final int next = output.getLineCount()-1;
			if (next < 0) {
				return;
			}
			output.setTopIndex(next);
		}
		
	}
	
	
	private static PaletteData fgPaletteData;
	
	private NIConsolePage fConsolePage;
	private ToolProcess fProcess;
	
	private History.Entry fCurrentHistoryEntry;
	private IHistoryListener fHistoryListener;
	private EnumSet<SubmitType> fHistoryTypesFilter;
	
	private ISourceUnit fSourceUnit;
	
	private Composite fComposite;
	private Label fPrefix;
	private boolean fIsPrefixHighlighted;
	private Image fPrefixBackground;
	private InputSourceViewer fSourceViewer;
	protected final InputDocument fDocument;
	private Button fSubmitButton;
	private ScrollControl fScroller;
	
	private final StatusLine fStatusLine = new StatusLine();
	private SourceViewerDecorationSupport fSourceViewerDecorationSupport;
	private SourceEditorViewerConfigurator fConfigurator;
	
	/**
	 * Saves the selection before starting a history navigation session.
	 * non-null value indicates in history session */
	private Point fHistoryCompoundChange;
	/** There are no caret listener */
	private Point fHistoryCaretWorkaround;
	/** Indicates that the document is change by a history action */
	private boolean fInHistoryChange = false;
	
	private StructureSelectionHistory fSelectionHistory;
	
	private ToolWorkspace.Listener fWorkspaceListener;
	
	
	public ConsolePageEditor(final NIConsolePage page) {
		fConsolePage = page;
		fProcess = page.getConsole().getProcess();
		
		fDocument = new InputDocument();
		fSourceUnit = createSourceUnit();
		
		updateSettings();
	}
	
	
	private void updateSettings() {
		fHistoryTypesFilter = PreferencesUtil.getInstancePrefs().getPreferenceValue(ConsolePreferences.PREF_HISTORYNAVIGATION_SUBMIT_TYPES);
	}
	
	
	protected ISourceUnit createSourceUnit() {
		return null;
	}
	
	public Composite createControl(final Composite parent, final SourceEditorViewerConfigurator editorConfig) {
		fComposite = new Composite(parent, SWT.NONE);
		final GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 3;
		fComposite.setLayout(layout);
		
		fPrefix = new Label(fComposite, SWT.LEFT);
		GridData gd = new GridData(SWT.LEFT, SWT.FILL, false, false);
		gd.verticalIndent = 1;
		fPrefix.setLayoutData(gd);
		fPrefixBackground = createImage(Display.getCurrent());
		fIsPrefixHighlighted = false;
		fPrefix.setText("> "); //$NON-NLS-1$
		fPrefix.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(final MouseEvent e) {
				if (UIAccess.isOkToUse(fSourceViewer)) {
					fSourceViewer.doOperation(SourceViewer.SELECT_ALL);
				}
			}
		});
		
		createSourceViewer(editorConfig);
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.verticalIndent = 1;
		fSourceViewer.getControl().setLayoutData(gd);
		fSourceViewer.appendVerifyKeyListener(new ThisKeyListener());
		
		fSubmitButton = new Button(fComposite, SWT.NONE);
		gd = new GridData(SWT.FILL, SWT.FILL, false, true);
		gd.horizontalIndent = layout.verticalSpacing;
		gd.heightHint = new PixelConverter(fSubmitButton).convertHeightInCharsToPixels(1)+4;
		gd.verticalSpan = 2;
		fSubmitButton.setLayoutData(gd);
		fSubmitButton.setText(Messages.Console_SubmitButton_label);
		fSubmitButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(final SelectionEvent e) {
				doSubmit();
				fSourceViewer.getControl().setFocus();
			}
			public void widgetDefaultSelected(final SelectionEvent e) {
			}
		});
		
		final Slider slider = new Slider(fComposite, SWT.HORIZONTAL);
		slider.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		fScroller = new ScrollControl(slider);
		
		setFont(fConsolePage.getConsole().getFont());
		
		fHistoryListener = new IHistoryListener() {
			public void entryAdded(final History source, final Entry e) {
			}
			public void entryRemoved(final History source, final Entry e) {
			}
			public void completeChange(final History source, final Entry[] es) {
				fCurrentHistoryEntry = null;
			}
		};
		fProcess.getHistory().addListener(fHistoryListener);
		
		fDocument.addPrenotifiedDocumentListener(new IDocumentListener() {
			public void documentAboutToBeChanged(final DocumentEvent event) {
				if (fHistoryCompoundChange != null && !fInHistoryChange) {
					fHistoryCompoundChange = null;
					fSourceViewer.getUndoManager().endCompoundChange();
				}
			}
			public void documentChanged(final DocumentEvent event) {
			}
		});
		
		fWorkspaceListener = new ToolWorkspace.Listener() {
			public void propertyChanged(final ToolWorkspace workspace, final Map<String, Object> properties) {
				if (properties.containsKey("wd")) {
					UIAccess.getDisplay(null).asyncExec(new Runnable() {
						public void run() {
							fStatusLine.updateWD();
						}
					});
				}
			}
		};
		fConsolePage.getControl().addListener(SWT.Activate, new Listener() {
			public void handleEvent(final Event event) {
				fStatusLine.refresh();
			}
		});
		fConsolePage.getTool().getWorkspaceData().addPropertyListener(fWorkspaceListener);
		fStatusLine.updateWD();
		
		return fComposite;
	}
	
	protected void createSourceViewer(final SourceEditorViewerConfigurator editorConfigurator) {
		fConfigurator = editorConfigurator;
		fSourceViewer = new InputSourceViewer(fComposite);
		fConfigurator.setTarget(this);
		final SourceEditorViewerConfiguration configuration = fConfigurator.getSourceViewerConfiguration();
		
		fSourceViewerDecorationSupport = new de.walware.epatches.ui.SourceViewerDecorationSupport(
				fSourceViewer, null, null, EditorsUI.getSharedTextColors());
		fConfigurator.configureSourceViewerDecorationSupport(fSourceViewerDecorationSupport);
//		fSourceViewerDecorationSupport.setCursorLinePainterPreferenceKeys(
//				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE,
//				AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR);
		final MarkerAnnotationPreferences markerAnnotationPreferences = EditorsPlugin.getDefault().getMarkerAnnotationPreferences();
		for (final Object pref : markerAnnotationPreferences.getAnnotationPreferences()) {
			fSourceViewerDecorationSupport.setAnnotationPreference((AnnotationPreference) pref);
		}
		fSourceViewerDecorationSupport.install(configuration.getPreferences());
		
		final IDocumentSetupParticipant docuSetup = fConfigurator.getDocumentSetupParticipant();
		if (docuSetup != null) {
			docuSetup.setup(fDocument.getMasterDocument());
		}
		
		new SourceViewerJFaceUpdater(fSourceViewer, configuration);
		
		final AnnotationModel annotationModel = new AnnotationModel();
		// annotationModel.setLockObject(fDocument.getLockObject());
		fSourceViewer.setDocument(fDocument, annotationModel);
		fSourceViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				fStatusLine.cleanStatusLine();
			}
		});
		fSourceViewer.getTextWidget().addListener(SWT.FocusOut, new Listener() {
			public void handleEvent(final Event event) {
				fStatusLine.cleanStatusLine();
			}
		});
		fSourceViewer.removeSpecialBinding(SWT.DEL);
		fSourceViewer.removeSpecialBinding(KEY_HIST_UP);
		fSourceViewer.removeSpecialBinding(KEY_HIST_DOWN);
		fSourceViewer.removeSpecialBinding(KEY_SUBMIT_DEFAULT);
		fSourceViewer.removeSpecialBinding(KEY_SUBMIT_KEYPAD);
		fSourceViewer.removeSpecialBinding(KEY_OUTPUT_LINEUP);
		fSourceViewer.removeSpecialBinding(KEY_OUTPUT_LINEDOWN);
		fSourceViewer.removeSpecialBinding(KEY_OUTPUT_PAGEUP);
		fSourceViewer.removeSpecialBinding(KEY_OUTPUT_PAGEDOWN);
		fSourceViewer.removeSpecialBinding(KEY_OUTPUT_START);
		fSourceViewer.removeSpecialBinding(KEY_OUTPUT_END);
	}
	
	public void handleSettingsChanged(final Set<String> groupIds, final Map<String, Object> options) {
		fConfigurator.handleSettingsChanged(groupIds, options);
		if (groupIds.contains(ConsolePreferences.GROUP_ID)) {
			updateSettings();
		}
	}
	
	public void configureServices(final IHandlerService commands, final IContextService keys) {
		keys.activateContext("de.walware.statet.base.contexts.ConsoleEditor"); //$NON-NLS-1$
		
		IAction action;
		final PairMatcher matcher = fConfigurator.getSourceViewerConfiguration().getPairMatcher();
		if (matcher != null) {
			commands.activateHandler(ISourceEditorCommandIds.GOTO_MATCHING_BRACKET,
					new GotoMatchingBracketHandler(matcher, this));
		}
		
		commands.activateHandler(ITextEditorActionDefinitionIds.CUT_LINE,
				new DeleteLineHandler(this, ITextEditorActionDefinitionIds.CUT_LINE));
		commands.activateHandler(ITextEditorActionDefinitionIds.CUT_LINE_TO_BEGINNING,
				new DeleteLineHandler(this, ITextEditorActionDefinitionIds.CUT_LINE_TO_BEGINNING));
		commands.activateHandler(ITextEditorActionDefinitionIds.CUT_LINE_TO_END,
				new DeleteLineHandler(this, ITextEditorActionDefinitionIds.CUT_LINE_TO_END));
		commands.activateHandler(ITextEditorActionDefinitionIds.DELETE_LINE,
				new DeleteLineHandler(this, ITextEditorActionDefinitionIds.DELETE_LINE));
		commands.activateHandler(ITextEditorActionDefinitionIds.DELETE_LINE_TO_BEGINNING,
				new DeleteLineHandler(this, ITextEditorActionDefinitionIds.DELETE_LINE_TO_BEGINNING));
		commands.activateHandler(ITextEditorActionDefinitionIds.DELETE_LINE_TO_END,
				new DeleteLineHandler(this, ITextEditorActionDefinitionIds.DELETE_LINE_TO_END));
		
		action = new TextViewerAction(getViewer(), ISourceViewer.CONTENTASSIST_PROPOSALS);
		commands.activateHandler(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, new ActionHandler(action));
		
		final IHandler2 informationHandler = new InformationDispatchHandler(getViewer());
		commands.activateHandler(ITextEditorActionDefinitionIds.SHOW_INFORMATION, informationHandler);
		
		fSelectionHistory = new StructureSelectionHistory(this);
		commands.activateHandler(ISourceEditorCommandIds.SELECT_ENCLOSING,
				new StructureSelectHandler.Enclosing(this, fSelectionHistory));
		commands.activateHandler(ISourceEditorCommandIds.SELECT_PREVIOUS,
				new StructureSelectHandler.Previous(this, fSelectionHistory));
		commands.activateHandler(ISourceEditorCommandIds.SELECT_NEXT,
				new StructureSelectHandler.Next(this, fSelectionHistory));
		final StructureSelectionHistoryBackHandler backHandler = new StructureSelectionHistoryBackHandler(this, fSelectionHistory);
		commands.activateHandler(ISourceEditorCommandIds.SELECT_LAST, backHandler);
		fSelectionHistory.addUpdateListener(backHandler);
		
		commands.activateHandler("de.walware.statet.nico.commands.SearchHistoryOlder", new AbstractHandler() { //$NON-NLS-1$
			public Object execute(final ExecutionEvent arg0)
					throws ExecutionException {
				doHistoryOlder(getLineStart());
				return null;
			}
		});
		commands.activateHandler("de.walware.statet.nico.commands.SearchHistoryNewer", new AbstractHandler() { //$NON-NLS-1$
			public Object execute(final ExecutionEvent arg0)
					throws ExecutionException {
				doHistoryNewer(getLineStart());
				return null;
			}
		});
		commands.activateHandler("de.walware.statet.nico.commands.GotoHistoryNewest", new AbstractHandler() { //$NON-NLS-1$
			public Object execute(final ExecutionEvent arg0)
					throws ExecutionException {
				doHistoryNewest();
				return null;
			}
		});
	}
	
	
	void setFont(final Font font) {
		fPrefix.setFont(font);
		fSourceViewer.getControl().setFont(font);
	}
	
	void updateBusy(final boolean isBusy) {
		final boolean highlight = !isBusy;
		if (highlight == fIsPrefixHighlighted) {
			return;
		}
		if (UIAccess.isOkToUse(fPrefix)) {
			fIsPrefixHighlighted = highlight;
			fPrefix.setBackgroundImage(highlight ? fPrefixBackground : null);
		}
	}
	
	/**
	 * @param prompt new prompt or null.
	 */
	void updatePrompt(final Prompt prompt) {
		final Prompt p = (prompt != null) ? prompt : fProcess.getWorkspaceData().getPrompt();
		if (UIAccess.isOkToUse(fPrefix)) {
			final String oldText = fPrefix.getText();
			if (!oldText.equals(p.text)) {
				fPrefix.setText(p.text);
				if (oldText.length() != p.text.length()) { // assuming monospace font
					getComposite().layout(new Control[] { fPrefix });
				}
			}
			onPromptUpdate(p);
		}
	}
	
	protected void onPromptUpdate(final Prompt prompt) {
	}
	
	private String getLineStart() {
		try {
			return fDocument.get(0, fSourceViewer.getSelectedRange().x);
		} catch (final BadLocationException e) {
			NicoUIPlugin.logError(-1, "Error while extracting prefix for history search", e); //$NON-NLS-1$
			return ""; //$NON-NLS-1$
		}
	}
	
	
	public void doHistoryNewer(final String prefix) {
		if (fCurrentHistoryEntry == null) {
			return;
		}
		
		History.Entry next = fCurrentHistoryEntry.getNewer();
		final EnumSet<SubmitType> filter = fHistoryTypesFilter;
		SubmitType type;
		while (next != null
				&& (   ((type = next.getSubmitType()) != null && !filter.contains(type))
					|| (next.getCommandMarker() < 0)
					|| (prefix != null && !next.getCommand().startsWith(prefix)) )) {
			next = next.getNewer();
		}
		
		if (next == null && prefix != null) {
			Display.getCurrent().beep();
			return;
		}
		
		fCurrentHistoryEntry = next;
		setHistoryContent((fCurrentHistoryEntry != null) ?
				fCurrentHistoryEntry.getCommand() : ""); //$NON-NLS-1$
	}
	
	public void doHistoryOlder(final String prefix) {
		History.Entry next;
		if (fCurrentHistoryEntry != null) {
			next = fCurrentHistoryEntry.getOlder();
		}
		else {
			next = fProcess.getHistory().getNewest();
		}
		final EnumSet<SubmitType> filter = fHistoryTypesFilter;
		SubmitType type;
		while (next != null
				&& (   ((type = next.getSubmitType()) != null && !filter.contains(type))
					|| (next.getCommandMarker() < 0)
					|| (prefix != null && !next.getCommand().startsWith(prefix)) )) {
			next = next.getOlder();
		}
		
		if (next == null) {
			Display.getCurrent().beep();
			return;
		}
		
		fCurrentHistoryEntry = next;
		setHistoryContent(fCurrentHistoryEntry.getCommand());
	}
	
	public void doHistoryNewest() {
		final History.Entry next = fProcess.getHistory().getNewest();
		if (next == null) {
			Display.getCurrent().beep();
			return;
		}
		fCurrentHistoryEntry = next;
		setHistoryContent(fCurrentHistoryEntry.getCommand());
	}
	
	protected void setHistoryContent(final String text) {
		if (fHistoryCompoundChange == null) {
			fHistoryCompoundChange = fSourceViewer.getSelectedRange();
			fSourceViewer.getUndoManager().beginCompoundChange();
		}
		else {
			final Point current = fSourceViewer.getSelectedRange();
			if (!current.equals(fHistoryCaretWorkaround)) {
				fHistoryCompoundChange = current;
			}
		}
		fInHistoryChange = true;
		
		final StyledText widget = fSourceViewer.getTextWidget();
		if (UIAccess.isOkToUse(widget)) {
			widget.setRedraw(false);
			fDocument.set(text);
			fSourceViewer.setSelectedRange(fHistoryCompoundChange.x, fHistoryCompoundChange.y);
			widget.setRedraw(true);
		}
		else {
			fDocument.set(text);
		}
		
		fHistoryCaretWorkaround = fSourceViewer.getSelectedRange();
		fInHistoryChange = false;
	}
	
	public void doSubmit() {
		final String content = fDocument.get();
		final ToolController controller = fProcess.getController();
		
		if (controller != null) {
			final IStatus status = controller.submit(content, SubmitType.CONSOLE);
			if (status.getSeverity() >= IStatus.ERROR) {
				fStatusLine.setMessage(true, status.getMessage(), null);
				Display.getCurrent().beep();
				return;
			}
			clear();
		}
	}
	
	public void clear() {
		fDocument.set(""); //$NON-NLS-1$
		fCurrentHistoryEntry = null;
		fHistoryCompoundChange = null;
		fSourceViewer.getUndoManager().reset();
		fScroller.reset();
		fSelectionHistory.flush();
	}
	
	public Composite getComposite() {
		return fComposite;
	}
	
	public Button getSubmitButton() {
		return fSubmitButton;
	}
	
	protected NIConsolePage getConsolePage() {
		return fConsolePage;
	}
	
	public void dispose() {
		fProcess.getHistory().removeListener(fHistoryListener);
		fHistoryListener = null;
		fCurrentHistoryEntry = null;
		
		if (fWorkspaceListener != null) {
			fConsolePage.getTool().getWorkspaceData().removePropertyListener(fWorkspaceListener);
			fWorkspaceListener = null;
		}
		
		if (fSourceViewerDecorationSupport != null) {
			fSourceViewerDecorationSupport.dispose();
			fSourceViewerDecorationSupport = null;
		}
		fProcess = null;
		fConsolePage = null;
		
		if (fPrefixBackground != null) {
			fPrefixBackground.dispose();
			fPrefixBackground = null;
		}
	}
	
/*- Complete ISourceEditor --------------------------------------------------*/
	
	public ISourceUnit getSourceUnit() {
		return fSourceUnit;
	}
	
	public IWorkbenchPart getWorkbenchPart() {
		return fConsolePage.getView();
	}
	
	public IServiceLocator getServiceLocator() {
		return fConsolePage.fInputServices;
	}
	
	public InputSourceViewer getViewer() {
		return fSourceViewer;
	}
	
	public PartitioningConfiguration getPartitioning() {
		return fConfigurator.getPartitioning();
	}
	
	public boolean isEditable(final boolean validate) {
		return true;
	}
	
	public void selectAndReveal(final int offset, final int length) {
		if (UIAccess.isOkToUse(fSourceViewer)) {
			fSourceViewer.setSelectedRange(offset, length);
			fSourceViewer.revealRange(offset, length);
		}
	}
	
	public Object getAdapter(final Class required) {
		if (ISourceEditor.class.equals(required)) {
			return this;
		}
		if (IEditorStatusLine.class.equals(required)) {
			return fStatusLine;
		}
		if (ITextOperationTarget.class.equals(required)) {
			return fSourceViewer;
		}
		return fConsolePage.getAdapter(required);
	}
	
	public ITextEditToolSynchronizer getTextEditToolSynchronizer() {
		return null;
	}
	
}

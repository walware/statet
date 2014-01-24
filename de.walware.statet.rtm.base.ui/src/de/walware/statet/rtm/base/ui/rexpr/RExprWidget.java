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

package de.walware.statet.rtm.base.ui.rexpr;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import de.walware.ecommons.FastList;
import de.walware.ecommons.emf.core.util.IContext;
import de.walware.ecommons.ui.components.IObjValueListener;
import de.walware.ecommons.ui.components.IObjValueWidget;
import de.walware.ecommons.ui.components.ObjValueEvent;
import de.walware.ecommons.ui.util.LayoutUtil;
import de.walware.ecommons.ui.util.MenuUtil;

import de.walware.statet.rtm.base.ui.RtModelUIPlugin;
import de.walware.statet.rtm.base.util.RExprType;
import de.walware.statet.rtm.base.util.RExprTypes;
import de.walware.statet.rtm.rtdata.types.RTypedExpr;


public class RExprWidget extends Composite implements IObjValueWidget<RTypedExpr> {
	
	
	public static class TypeDef implements IObjValueListener<RTypedExpr> {
		
		
		private final RExprTypeUIAdapter fAdapter;
		
		private Control fControl;
		
		private RExprWidget fWidget;
		private String fLastValue;
		
		
		public TypeDef(final RExprTypeUIAdapter type) {
			fAdapter = type;
		}
		
		
		public String getTypeKey() {
			return fAdapter.getType().getTypeKey();
		}
		
		public RExprTypeUIAdapter getUIAdapter() {
			return fAdapter;
		}
		
		private Control getDetailControl(final RExprWidget widget) {
			if (fWidget != null && fWidget != widget) {
				throw new IllegalStateException();
			}
			if (fControl == null) {
				fWidget = widget;
				fControl = createDetailControl(widget);
			}
			return fControl;
		}
		
		public boolean hasDetail() {
			return false;
		}
		
		protected Control createDetailControl(final Composite parent) {
			return null;
		}
		
		protected void activate(final Text text) {
		}
		
		protected void deactivate(final Text text) {
		}
		
		@Override
		public void valueAboutToChange(final ObjValueEvent<RTypedExpr> event) {
		}
		
		@Override
		public void valueChanged(final ObjValueEvent<RTypedExpr> event) {
		}
		
		protected IContext getContext() {
			return fWidget.fContext;
		}
		
		protected void setExpr(final String expr) {
			fWidget.doSetValue(fWidget.fCurrentTypeDef, expr, 0);
		}
		
	}
	
	private static class UndefinedAdapter extends RExprTypeUIAdapter {
		
		
		public UndefinedAdapter(final String type) {
			super(new RExprType(type, -1, NLS.bind("Unknown data type (''{0}'')", type)),
					RtModelUIPlugin.getDefault().getImageRegistry().get(
							RtModelUIPlugin.OBJ_UNKOWN_TYPE_IMAGE_ID) );
		}
		
	}
	
	
	public static final int MIN_SIZE = 1 << 8;
	
	
	private static int DELAY_MS = 333;
	
	private class SWTListener implements Listener {
		
		private boolean fTypeHover;
		
		private int fIgnoreMouse;
		
		@Override
		public void handleEvent(final Event event) {
			switch (event.type) {
			case SWT.Resize:
				updateLayout();
				return;
			case SWT.MouseEnter:
				fTypeHover = true;
				fTypeControl.redraw();
				return;
			case SWT.MouseExit:
				fTypeHover = false;
				fTypeControl.redraw();
				return;
			case SWT.FocusIn:
				fTypeControl.redraw();
				return;
			case SWT.FocusOut:
				fTypeControl.redraw();
				fireValueChanged();
				return;
			case SWT.MouseDown:
				doFocus();
				if (event.button == 1 && event.time != fIgnoreMouse) {
					showTypeMenu();
					return;
				}
				return;
			case SWT.KeyDown:
				if (event.character == SWT.CR
						|| (event.stateMask & (SWT.MOD1 | SWT.MOD3 | SWT.MOD4)) != 0) {
					fireValueChanged();
					return;
				}
				if (event.character == '+' && event.stateMask == SWT.MOD3) {
					showTypeMenu();
					return;
				}
				return;
			case SWT.Paint:
				decorateType(event, fTypeHover);
				return;
			case SWT.Modify:
				checkValueChanged(event.time, true);
				return;
			case SWT.Dispose:
				onDispose();
				return;
			}
		}
		
	}
	
	
	private final int fOptions;
	
	private final RExprTypes fTypes;
	private final List<TypeDef> fTypeDefs;
	
	private IContext fContext;
	
	private boolean fWithDetail;
	
	private TypeDef fCurrentTypeDef;
	
	private SWTListener fMainListener;
	private Label fTypeControl;
	private Menu fTypeMenu;
	
	private Text fTextControl;
	
	private Control fDetailControl;
	
	private Color fTypeBorder2Color;
	private Color fTypeBorderColor;
	private Color fTypeHoverColor;
	
	private int fTextWidthHint;
	
	private RTypedExpr fCurrentValue;
	
	private ObjValueEvent<RTypedExpr> fNextEvent;
	private long fNextEventTime;
	private final Runnable fNextEventRunnable = new Runnable() {
		@Override
		public void run() {
			if (fNextEventTime == 0) {
				return;
			}
			final long diff = (System.nanoTime() - DELAY_MS * 1000000L - fNextEventTime) / 1000000L;
			if (diff < -(DELAY_MS / 10)) {
				getDisplay().timerExec(-(int) diff, fNextEventRunnable);
				return;
			}
			fireValueChanged();
		}
	};
	private final FastList<IObjValueListener<RTypedExpr>> fListeners = (FastList) new FastList<IObjValueListener>(IObjValueListener.class);
	
	private int fIgnoreChanges;
	
	
	public RExprWidget(final Composite parent, final int options,
			final RExprTypes types, final List<RExprTypeUIAdapter> uiAdapters) {
		super(parent, SWT.NONE);
		if (uiAdapters == null) {
			throw new NullPointerException("uiAdapters"); //$NON-NLS-1$
		}
		
		fOptions = options;
		fTypes = types;
		
		final List<RExprWidget.TypeDef> typeDefs = new ArrayList<RExprWidget.TypeDef>(uiAdapters.size());
		for (final RExprTypeUIAdapter adapter : uiAdapters) {
			final TypeDef typeDef = adapter.createWidgetDef();
			typeDefs.add(typeDef);
			if (typeDef.hasDetail()) {
				fWithDetail = true;
			}
		}
		fTypeDefs = typeDefs;
		
		createContent(this, uiAdapters);
	}
	
	
	@Override
	public Control getControl() {
		return this;
	}
	
	public Text getText() {
		return fTextControl;
	}
	
	@Override
	public Class<RTypedExpr> getValueType() {
		return RTypedExpr.class;
	}
	
	protected void createContent(final Composite composite, final List<RExprTypeUIAdapter> uiAdapters) {
		final SWTListener listener = new SWTListener();
		fMainListener = listener;
		addListener(SWT.Resize, listener);
		addListener(SWT.Dispose, listener);
		
		fTypeControl = new Label(composite, SWT.CENTER);
		fTypeControl.addListener(SWT.MouseEnter, listener);
		fTypeControl.addListener(SWT.MouseExit, listener);
		fTypeControl.addListener(SWT.MouseDown, listener);
		fTypeControl.addListener(SWT.KeyDown, listener);
		fTypeControl.addListener(SWT.Paint, listener);
		fTypeControl.setSize(fTypeControl.computeSize(16 + 4, 16));
		
		fTextControl = new Text(composite, SWT.BORDER | SWT.LEFT_TO_RIGHT);
		fTextControl.addListener(SWT.KeyDown, listener);
		fTextControl.addListener(SWT.FocusIn, listener);
		fTextControl.addListener(SWT.FocusOut, listener);
		fTextControl.addListener(SWT.Modify, listener);
		fTextWidthHint = LayoutUtil.hintWidth(fTextControl, null, 25);
		
		final DropTarget dropTarget = new DropTarget(fTextControl, DND.DROP_COPY);
		dropTarget.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer(), TextTransfer.getInstance() } );
		dropTarget.addDropListener(new RExprDropAdapter(uiAdapters) {
			@Override
			protected IContext getContext() {
				return fContext;
			}
			@Override
			protected String getCurrentTypeKey() {
				return fCurrentTypeDef.getTypeKey();
			}
			@Override
			protected boolean insertText(final String text) {
				fTextControl.insert(text);
				return true;
			}
			@Override
			protected boolean setExpr(final String typeKey, final String expr, final int time) {
				final TypeDef typeDef = getTypeDef(typeKey);
				if (typeDef != null) {
					doSetValue(typeDef, expr, time);
					return true;
				}
				return false;
			}
		});
	}
	
	public void setContext(final IContext context) {
		fContext = context;
	}
	
	@Override
	public void setFont(final Font font) {
		super.setFont(font);
		fTextControl.setFont(font);
		fTextWidthHint = LayoutUtil.hintWidth(fTextControl, null, 25);
	}
	
	public void setTypeBackgroundColor(final Color color) {
		fTypeControl.setBackground(color);
	}
	
	public void setTypeBorderColor(final Color color) {
		fTypeBorderColor = color;
	}
	
	public void setTypeBorder2Color(final Color color) {
		fTypeBorder2Color = color;
	}
	
	public void setTypeHoverColor(final Color color) {
		fTypeHoverColor = color;
	}
	
	public boolean getShowDetail() {
		return fWithDetail;
	}
	
	public void setShowDetail(final boolean enabled) {
		fWithDetail = enabled;
		updateLayout();
	}
	
	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		fTextControl.setEnabled(enabled);
		if (fDetailControl != null) {
			fDetailControl.setEnabled(enabled);
		}
	}
	
	@Override
	public Point computeSize(final int wHint, final int hHint, final boolean changed) {
		final int detailWidth = fTextWidthHint / 2;
		final int minTextWidth = fTextWidthHint / 3;
		final int spacing = LayoutUtil.defaultHSpacing();
		final Point typeSize = fTypeControl.getSize();
		final int width = (wHint == SWT.DEFAULT) ?
				(((fOptions & MIN_SIZE) != 0) ? minTextWidth : fTextWidthHint) :
				Math.max((fTextWidthHint / 3), wHint - typeSize.x - detailWidth);
		final Rectangle trim = computeTrim(0, 0,
				width + typeSize.x + spacing + detailWidth,
				Math.max(fTextControl.computeSize(width, hHint).y, typeSize.y) );
		return new Point(trim.width, trim.height);
	}
	
	protected void updateLayout() {
		final int detailWidth = fTextWidthHint / 2;
		final int minTextSize = fTextWidthHint / 3;
		final int spacing = LayoutUtil.defaultHSpacing();
		final Rectangle clientArea = getClientArea();
		final Point typeSize = fTypeControl.getSize();
		int x = clientArea.x;
		final int indent = 0;
//		int indent = fText.getBorderWidth();
		fTypeControl.setBounds(x + indent, clientArea.y, typeSize.x, clientArea.height);
		x += (++typeSize.x);
//		indent += spacing;
		if (!fWithDetail) {
			fTextControl.setBounds(x, clientArea.y,
					Math.max(minTextSize, clientArea.width - typeSize.x), clientArea.height );
			return;
		}
		final int textWidth = Math.max(minTextSize, clientArea.width - typeSize.x - detailWidth - spacing);
		fTextControl.setBounds(x, clientArea.y, textWidth, clientArea.height );
		x += textWidth + spacing;
		if (fDetailControl != null) {
			fDetailControl.setBounds(x, clientArea.y,
					Math.max(clientArea.width - x, 0), clientArea.height );
		}
	}
	
	public void decorateType(final Event event, boolean hover) {
		if (fTypeBorderColor == null || !isEnabled()) {
			return;
		}
		if (!hover && fTypeMenu != null && fTypeMenu.isVisible()) {
			hover = true;
		}
		final GC gc = event.gc;
		final Point size = fTypeControl.getSize();
		
		Color border;
		Color blend;
		if (hover) {
			border = fTypeHoverColor;
			blend = fTypeBorder2Color;
			if (border == null) {
				border = fTypeBorderColor;
			}
		}
		else if (fTextControl.isFocusControl()) {
			border = fTypeBorderColor;
			blend = fTypeBorder2Color;
		}
		else {
			border = getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
			blend = null;
		}
		
		if (fTextControl.getBorderWidth() > 2 && blend != null) {
			gc.setForeground(blend);
			gc.drawRectangle(1, 1, size.x - 3, size.y - 3);
		}
		
		gc.setForeground(border);
		gc.drawRectangle(0, 0, size.x - 1, size.y - 1);
		gc.setAlpha(127);
		gc.drawPoint(1, 1);
		gc.drawPoint(1, size.y - 2);
		gc.setAlpha(63);
		gc.drawPoint(size.x - 2, 1);
		gc.drawPoint(size.x - 2, size.y - 2);
		
		gc.setForeground(getBackground());
		gc.setAlpha(63);
		gc.drawPoint(size.x - 1, 0);
		gc.drawPoint(size.x - 1, size.y - 1);
		gc.drawPoint(1, 0);
		gc.drawPoint(0, 1);
		gc.drawPoint(0, size.y - 2);
		gc.drawPoint(1, size.y - 1);
		
		gc.setAlpha(127);
		gc.drawPoint(0, 0);
		gc.drawPoint(0, size.y - 1);
	}
	
	protected void showTypeMenu() {
		if (!isEnabled()) {
			return;
		}
		
		if (fTypeMenu == null) {
			fTypeMenu = new Menu(fTypeControl);
			
			final Listener listener = new Listener() {
				@Override
				public void handleEvent(final Event event) {
					switch (event.type) {
					case SWT.Show:
						if (!fMainListener.fTypeHover) {
							fTypeControl.redraw();
						}
						break;
					case SWT.Hide:
						fMainListener.fIgnoreMouse = event.time;
						fTypeControl.redraw();
						break;
					default:
						break;
					}
				}
			};
			fTypeMenu.addListener(SWT.Show, listener);
			fTypeMenu.addListener(SWT.Hide, listener);
			fillTypeMenu(fTypeMenu);
		}
		
		MenuUtil.setPullDownPosition(fTypeMenu, fTypeControl);
		fTypeMenu.setVisible(true);
	}
	
	protected void fillTypeMenu(final Menu menu) {
		final SelectionListener listener = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {
				doSetValue((TypeDef) event.widget.getData(), null, event.time);
				doFocus();
			}
		};
		for (final TypeDef typeDef : fTypeDefs) {
			final MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
			final RExprTypeUIAdapter uiAdapter = typeDef.getUIAdapter();
			menuItem.setImage(uiAdapter.getImage());
			menuItem.setText(uiAdapter.getLabel());
			menuItem.setData(typeDef);
			menuItem.addSelectionListener(listener);
			
			menuItem.setSelection(typeDef == fCurrentTypeDef);
		}
	}
	
	protected void doSetValue(TypeDef typeDef, String expr, final int time) {
		if (typeDef == null) {
			typeDef = getTypeDef(fTypes.getDefaultTypeKey());
		}
		fIgnoreChanges++;
		try {
			if (fCurrentTypeDef == typeDef) {
				if (expr != null) {
					fTextControl.setText(expr);
				}
			}
			else {
				if (fCurrentTypeDef != null) {
					fireValueChanged();
					fCurrentTypeDef.fLastValue = (fCurrentValue != null) ? fCurrentValue.getExpr() : null;
				}
				fCurrentTypeDef = typeDef;
				if (expr == null) {
					if (typeDef != null) {
						if (typeDef.fLastValue != null) {
							expr = typeDef.fLastValue;
						}
						else if (fCurrentValue != null) {
							expr = typeDef.getUIAdapter().adopt(fCurrentValue.getTypeKey(), fCurrentValue.getExpr());
						}
					}
					if (expr == null) {
						expr = ""; //$NON-NLS-1$
					}
				}
				if (fDetailControl != null) {
					fDetailControl.setVisible(false);
				}
				if (typeDef != null) {
					final RExprTypeUIAdapter uiAdapter = typeDef.getUIAdapter();
					fTypeControl.setImage(uiAdapter.getImage());
					fTypeControl.setToolTipText(uiAdapter.getLabel());
					fTextControl.setText(expr);
					if (fWithDetail) {
						fDetailControl = typeDef.getDetailControl(this);
						updateLayout();
						if (fDetailControl != null) {
							fDetailControl.setEnabled(getEnabled());
							fDetailControl.setVisible(true);
						}
					}
				}
				else {
					fTypeControl.setImage(null);
					fTypeControl.setToolTipText(null);
					fDetailControl = null;
				}
			}
		}
		finally {
			fIgnoreChanges--;
			checkValueChanged(time, false);
		}
	}
	
	protected void doFocus() {
		fTextControl.selectAll();
		fTextControl.setFocus();
	}
	
	protected void onDispose() {
		if (fTypeMenu != null) {
			fTypeMenu.dispose();
		}
	}
	
	
	protected TypeDef getTypeDef(final String key) {
		for (final TypeDef def : fTypeDefs) {
			if (def.getTypeKey() == key) {
				return def;
			}
		}
		return null;
	}
	
	@Override
	public void addValueListener(final IObjValueListener<RTypedExpr> listener) {
		fListeners.add(listener);
	}
	
	@Override
	public void removeValueListener(final IObjValueListener<RTypedExpr> listener) {
		fListeners.remove(listener);
	}
	
	protected void checkValueChanged(final int time, final boolean delay) {
		if (fIgnoreChanges > 0) {
			return;
		}
		
		fNextEvent = new ObjValueEvent<RTypedExpr>(this, time,
				0, fCurrentValue, doGetValue(), SWT.NONE );
		if (fCurrentTypeDef != null) {
			fCurrentTypeDef.valueAboutToChange(fNextEvent);
		}
		
		if ((fCurrentValue != null) ? fCurrentValue.equals(fNextEvent.newValue) :
				null == fNextEvent.newValue) {
			return;
		}
		
		if (delay) {
			final boolean schedule = (fNextEventTime == 0);
			fNextEventTime = System.nanoTime();
			if (schedule) {
				getDisplay().timerExec(DELAY_MS, fNextEventRunnable);
			}
		}
		else {
			fireValueChanged();
		}
	}
	
	protected void fireValueChanged() {
		fNextEventTime = 0;
		final ObjValueEvent<RTypedExpr> event = fNextEvent;
		if (event == null) {
			return;
		}
		fNextEvent = null;
		fCurrentValue = event.newValue;
		final IObjValueListener<RTypedExpr>[] listeners = fListeners.toArray();
		for (final IObjValueListener<RTypedExpr> listener : listeners) {
			listener.valueChanged(event);
		}
	}
	
	protected RTypedExpr doGetValue() {
		final String text = fTextControl.getText();
		if (fCurrentTypeDef == null || text.isEmpty()) {
			return null;
		}
		return new RTypedExpr(fCurrentTypeDef.getTypeKey(), text);
	}
	
	@Override
	public RTypedExpr getValue(final int idx) {
		if (idx != 0) {
			throw new IllegalArgumentException("idx: " + idx); //$NON-NLS-1$
		}
		fireValueChanged();
		return fCurrentValue;
	}
	
	@Override
	public void setValue(final int idx, final RTypedExpr value) {
		if (idx != 0) {
			throw new IllegalArgumentException("idx: " + idx); //$NON-NLS-1$
		}
		if (value == null) {
			doSetValue(null, "", 0); //$NON-NLS-1$
			return;
		}
		TypeDef def = getTypeDef(value.getTypeKey());
		if (def == null) {
			def = new TypeDef(new UndefinedAdapter(value.getTypeKey()));
		}
		doSetValue(def, value.getExpr(), 0);
	}
	
}

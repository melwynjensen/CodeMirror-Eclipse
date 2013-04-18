/*******************************************************************************
 * Copyright (c) 2002, 2011 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/
package codemirror.eclipse.swt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import codemirror.eclipse.swt.internal.org.apache.commons.lang3.StringEscapeUtils;

public class CMControl extends AbstractCMControl {

	private boolean dirty = false;
	private List<DirtyListener> listeners = new ArrayList<DirtyListener>();

	private IValidator validator;

	public CMControl(File file, Composite parent, int style) {
		super(file, parent, style, SWT.NONE);
	}

	public CMControl(String url, Composite parent, int style) {
		super(url, parent, style, SWT.NONE);
	}

	public CMControl(File file, Composite parent, int style, int browserStyle) {
		super(file, parent, style, browserStyle);
	}

	public CMControl(String url, Composite parent, int style, int browserStyle) {
		super(url, parent, style, browserStyle);
	}

	protected void doSetText(String text) {
		String js = new StringBuilder(
				" cmIsDirtyFired=true; try { editor.setValue( \"")
				.append(StringEscapeUtils.escapeEcmaScript(text))
				.append("\" ); } catch(e){alert(e)}; cmIsDirtyFired=false;return null;")
				.toString();
		browser.evaluate(js);
		dirty = false;
	}

	public boolean isDirty() {
		return dirty;
	}

	@Override
	protected String doGetText() {
		return (String) browser.evaluate("return editor.getValue();");
	}

	@Override
	protected void createBrowserFunctions() {
		super.createBrowserFunctions();
		new BrowserFunction(browser, "cm_dirty") {
			public Object function(Object[] arguments) {

				dirty = true;
				notifyListeners();
				return null;
			}
		};

		new BrowserFunction(browser, "cm_validate") {
			public Object function(Object[] arguments) {
				try {
					String json = validate(getText());
					String js = "f(" + json + ")";
					browser.evaluate(js);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				return null;
			}
		};

	}

	public String validate(String code) {
		IValidator validator = getValidator();
		if (validator != null) {
			return validator.validate(code);
		}
		return null;
	}

	public void addDirtyListener(DirtyListener l) {
		listeners.add(l);
	}

	private void notifyListeners() {
		Display.getCurrent().asyncExec(new Runnable() {
			public void run() {
				for (DirtyListener l : listeners) {
					l.dirtyChanged(isDirty());
				}
			}
		});

	}

	// public static String getHostUrl() {
	// return hostUrl;
	// }
	//
	// public static void setHostUrl(String hostUrl) {
	// CMControl.hostUrl = hostUrl;
	// }

	public void setDirty(boolean b) {
		dirty = b;
		browser.evaluate(" cmIsDirtyFired=false");
	}

	public void setValidator(IValidator validator) {
		this.validator = validator;
	}

	public IValidator getValidator() {
		return validator;
	}
}
/*******************************************************************************
 * Copyright (c) 2013 Angelo ZERR.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:      
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package codemirror.eclipse.swt.xquery.builder;

import java.util.List;

import codemirror.eclipse.swt.builder.CMBuilder;
import codemirror.eclipse.swt.builder.ExtraKeysOption;
import codemirror.eclipse.swt.builder.Function;
import codemirror.eclipse.swt.builder.GuttersOptionUpdater;
import codemirror.eclipse.swt.builder.Options;
import codemirror.eclipse.swt.builder.Theme;
import codemirror.eclipse.swt.builder.addon.fold.FoldGutterOption;
import codemirror.eclipse.swt.builder.addon.fold.FoldType;
import codemirror.eclipse.swt.builder.commands.PassAndHintCommand;
import codemirror.eclipse.swt.xquery.builder.addon.hover.XQueryHover;
import codemirror.eclipse.swt.xquery.builder.commands.XQueryAutocompleteCommand;

/**
 * XQuery CodeMirror builder.
 * 
 */
public class CMXQueryBuilder extends CMBuilder {

	private static final FoldType[] SUPPORTED_FOLDTYPE = new FoldType[] {
			FoldType.COMMENT_FOLD, FoldType.BRACE_FOLD, FoldType.XML_FOLD };

	public CMXQueryBuilder(String baseURL) {
		super(XQueryMode.INSTANCE, baseURL);
		addScript("scripts/codemirror-xquery/addon/xquery-commons.js");

		Options options = super.getOptions();

		// brackets
		options.setAutoCloseBrackets(true);
		options.setMatchBrackets(true);

		// Line numbers
		options.setLineNumbers(true);
		List<String> gutters = options.getGutters();
		gutters.add(GuttersOptionUpdater.LINENUMBERS);

		// Completion
		ExtraKeysOption extraKeys = options.getExtraKeys();
		extraKeys.addOption("':'", PassAndHintCommand.INSTANCE);
		extraKeys.addOption("'$'", PassAndHintCommand.INSTANCE);
		extraKeys.addOption("Ctrl-Space", XQueryAutocompleteCommand.INSTANCE);
		installHint(true, true);
		
		installTrackVars(options);

		// Fold
		super.setSupportedFoldTypes(SUPPORTED_FOLDTYPE);
		gutters.add(GuttersOptionUpdater.FOLDGUTTER);
		FoldGutterOption fold = options.getFoldGutter();
		fold.setRangeFinder(getSupportedFoldTypes());

		// MatchHighlighterOption matchHighlighter =
		// options.getMatchHighlighter();
		// matchHighlighter.setShowToken("/[\\w|-]/");

		options.getTextHover(XQueryHover.INSTANCE).setTextHover(true);

		setTheme(Theme.XQ_LIGHT);
	}

	private void installTrackVars(Options options) {
		// <!-- CodeMirror-XQuery -->
		addScript("scripts/codemirror-xquery/addon/execute/xquery/track-vars.js");
		getOptions().addOption(
				"trackVars",
				new Function("function(globalVars, changed) {\n"
						+ "if (changed) {\n"
						+ "if (typeof cm_refreshVars == 'function') cm_refreshVars(globalVars);\n}\n" + "}"));
	}

	@Override
	protected void installHint(boolean withContextInfo, boolean withTemplates) {
		super.installHint(withContextInfo, withTemplates);
		installXQueryHint();
	}

	protected void installXQueryHint() {
		// <!-- CodeMirror-XQuery -->
		addScript("scripts/codemirror-xquery/addon/hint/xquery-hint.js");
		addStyle("scripts/codemirror-xquery/addon/hint/xquery-hint.css");
		addScript("scripts/codemirror-xquery/addon/hint/system-functions.xml.js");
		// XQuery Templates
		addScript("scripts/codemirror-xquery/addon/hint/xquery-templates.js");
	}

}

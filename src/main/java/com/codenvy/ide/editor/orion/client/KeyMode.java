/*******************************************************************************
 * Copyright (c) 2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.editor.orion.client;

import com.codenvy.ide.jseditor.client.editortype.EditorType;
import com.codenvy.ide.jseditor.client.keymap.Keymap;
import com.google.gwt.core.shared.GWT;

/**
 * Keymaps supported by Orion.
 * 
 * @author "Mickaël Leduque"
 */
public class KeyMode {


    public static Keymap DEFAULT;
    public static Keymap EMACS;
    public static Keymap VI;


    public final static void init() {
        KeymodeDisplayConstants constants = GWT.create(KeymodeDisplayConstants.class);
        EditorType orionEditor = EditorType.fromKey(OrionEditorExtension.ORION_EDITOR_KEY);
        DEFAULT = Keymap.newKeymap("orion_default", constants.defaultKeymap(), orionEditor);
        EMACS = Keymap.newKeymap("Orion_emacs", constants.emacs(), orionEditor);
        VI = Keymap.newKeymap("Orion_vim", constants.vi(), orionEditor);
    }
}

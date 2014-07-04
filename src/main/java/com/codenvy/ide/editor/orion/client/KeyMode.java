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

import com.codenvy.ide.editor.common.client.keymap.Keymap;
import com.google.gwt.core.shared.GWT;

/**
 * Enum for keymaps supported by Orion.
 * 
 * @author "Mickaël Leduque"
 */
public enum KeyMode implements Keymap {

    DEFAULT(0, "default"),
    EMACS(1, "emacs"),
    VI(2, "vi");

    private String orionKey;
    private int    index;

    private KeyMode(final int index, final String key) {
        this.index = index;
        this.orionKey = key;
    }

    public String getOrionKey() {
        return this.orionKey;
    }

    public int getIndex() {
        return this.index;
    }

    public static KeyMode fromOrionKey(final String key) {
        for (KeyMode value : values()) {
            if (value.orionKey.equals(key)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown keymode code");
    }

    public static KeyMode fromIndex(final int index) {
        for (KeyMode value : values()) {
            if (value.index == index) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown keymode index");
    }

    @Override
    public String getKey() {
        return this.orionKey;
    }

    @Override
    public String getDisplay() {
        KeymodeDisplayConstants constants = GWT.create(KeymodeDisplayConstants.class);
        switch (this) {
            case EMACS:
                return constants.emacs();
            case VI:
                return constants.vi();
            case DEFAULT:
                return constants.defaultKeymap();
            default:
                throw new IllegalArgumentException("Unknown keymode enum");
        }
    }
}

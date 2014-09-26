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

import com.codenvy.ide.api.texteditor.HandlesUndoRedo;
import com.codenvy.ide.editor.orion.client.jso.OrionUndoStackOverlay;

/**
 * Undo/redo handler for orion editors.
 */
class OrionUndoRedo implements HandlesUndoRedo {

    /** The document. */
    private final OrionUndoStackOverlay undoStack;

    public OrionUndoRedo(final OrionUndoStackOverlay undoStack) {
        this.undoStack = undoStack;
    }

    @Override
    public boolean redoable() {
        return this.undoStack.canRedo();
    }

    @Override
    public boolean undoable() {
        return this.undoStack.canUndo();
    }

    @Override
    public void redo() {
        this.undoStack.redo();
    }

    @Override
    public void undo() {
        this.undoStack.undo();
    }
}
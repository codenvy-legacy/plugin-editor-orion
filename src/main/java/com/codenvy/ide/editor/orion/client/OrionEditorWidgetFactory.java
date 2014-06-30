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

import com.codenvy.ide.text.Document;

/**
 * Interface for factories of {@link OrionEditorWidget}.
 * 
 * @author "Mickaël Leduque"
 */
public interface OrionEditorWidgetFactory {
    /**
     * Create an instance of {@link OrionEditorWidget}.
     * 
     * @param editorMode the language mode of the editor
     * @param document the displayed document
     * @return an instance of {@link OrionEditorWidget}
     */
    OrionEditorWidget createEditorWidget(final String editorMode, final Document document);
}
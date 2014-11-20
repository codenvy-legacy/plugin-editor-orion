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

import com.codenvy.ide.Resources;
import com.codenvy.ide.api.parts.WorkspaceAgent;
import com.codenvy.ide.debug.BreakpointManager;
import com.codenvy.ide.jseditor.client.JsEditorConstants;
import com.codenvy.ide.jseditor.client.codeassist.CodeAssistantFactory;
import com.codenvy.ide.jseditor.client.debug.BreakpointRendererFactory;
import com.codenvy.ide.jseditor.client.document.DocumentStorage;
import com.codenvy.ide.jseditor.client.filetype.FileTypeIdentifier;
import com.codenvy.ide.jseditor.client.quickfix.QuickAssistantFactory;
import com.codenvy.ide.jseditor.client.texteditor.EditorModule;
import com.codenvy.ide.jseditor.client.texteditor.EditorWidgetFactory;
import com.codenvy.ide.jseditor.client.texteditor.EmbeddedTextEditorPartView;
import com.codenvy.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import com.codenvy.ide.ui.dialogs.DialogFactory;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

/**
 * {@link EmbeddedTextEditorPresenter} using orion.
 * This class is only defined to allow the Gin binding to be performed.
 */
public class OrionEditorPresenter extends EmbeddedTextEditorPresenter<OrionEditorWidget> {

    @AssistedInject
    public OrionEditorPresenter(final CodeAssistantFactory codeAssistantFactory,
                                     final BreakpointManager breakpointManager,
                                     final BreakpointRendererFactory breakpointRendererFactory,
                                     final DialogFactory dialogFactory,
                                     final DocumentStorage documentStorage,
                                     final JsEditorConstants constant,
                                     @Assisted final EditorWidgetFactory<OrionEditorWidget> editorWigetFactory,
                                     final EditorModule<OrionEditorWidget> editorModule,
                                     final EmbeddedTextEditorPartView editorView,
                                     final EventBus eventBus,
                                     final FileTypeIdentifier fileTypeIdentifier,
                                     final QuickAssistantFactory quickAssistantFactory,
                                     final Resources resources,
                                     final WorkspaceAgent workspaceAgent) {
        super(codeAssistantFactory, breakpointManager, breakpointRendererFactory, dialogFactory, documentStorage, constant, editorWigetFactory,
              editorModule, editorView, eventBus, fileTypeIdentifier, quickAssistantFactory, resources, workspaceAgent);
    }
}
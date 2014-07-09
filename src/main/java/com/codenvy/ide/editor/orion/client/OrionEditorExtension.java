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

import javax.inject.Inject;

import com.codenvy.ide.api.editor.CodenvyTextEditor;
import com.codenvy.ide.api.editor.TextEditorProvider;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.Notification.Type;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.core.editor.EditorType;
import com.codenvy.ide.core.editor.EditorTypeRegistry;
import com.codenvy.ide.jseditor.client.requirejs.ModuleHolder;
import com.codenvy.ide.jseditor.client.requirejs.RequireJsLoader;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.Node;

@Extension(title = "Orion Editor.", version = "1.0.0")
public class OrionEditorExtension {

    /** The editor type key. */
    public static final String           ORION_EDITOR_KEY = "orion";

    private final NotificationManager    notificationManager;
    private final ModuleHolder           moduleHolder;
    private final EditorTypeRegistry     editorTypeRegistry;
    private final RequireJsLoader        requireJsLoader;

    private final OrionTextEditorFactory orionTextEditorFactory;

    @Inject
    public OrionEditorExtension(final EditorTypeRegistry editorTypeRegistry,
                                final ModuleHolder moduleHolder,
                                final NotificationManager notificationManager,
                                final RequireJsLoader requireJsLoader,
                                final OrionTextEditorFactory orionTextEditorFactory) {
        this.notificationManager = notificationManager;
        this.moduleHolder = moduleHolder;
        this.editorTypeRegistry = editorTypeRegistry;
        this.requireJsLoader = requireJsLoader;
        this.orionTextEditorFactory = orionTextEditorFactory;

        injectOrion();
        // no need to delay
        KeyMode.init();
    }

    private void injectOrion() {
        final String[] scripts = new String[]{
                "orion-6.0/built-editor-amd",
                "orion/editor/stylers/text_x-java-source/syntax",
                "orion/editor/stylers/application_javascript/syntax",
                "orion/editor/stylers/application_json/syntax",
                "orion/editor/stylers/application_schema_json/syntax",
                "orion/editor/stylers/application_x-ejs/syntax",
                "orion/editor/stylers/application_xml/syntax",
                "orion/editor/stylers/lib/syntax",
                "orion/editor/stylers/text_css/syntax",
                "orion/editor/stylers/text_html/syntax",
                "orion/editor/stylers/text_x-arduino/syntax",
                "orion/editor/stylers/text_x-c__src/syntax",
                "orion/editor/stylers/text_x-csrc/syntax",
                "orion/editor/stylers/text_x-lua/syntax",
                "orion/editor/stylers/text_x-php/syntax",
                "orion/editor/stylers/text_x-python/syntax",
                "orion/editor/stylers/text_x-ruby/syntax",
                "orion/editor/stylers/text_x-yaml/syntax",
                "orion/emacs",
                "orion/vi",
        };

        this.requireJsLoader.require(new Callback<Void, Throwable>() {
            @Override
            public void onSuccess(final Void result) {
                requireOrion();
            }

            @Override
            public void onFailure(final Throwable e) {
                Log.error(OrionEditorExtension.class, "Unable to inject Orion", e);
                initializationFailed("Unable to inject Orion");
            }
        }, scripts, new String[0]);

        injectCssLink(GWT.getModuleBaseForStaticFiles() + "orion-6.0/built-editor.css");
    }

    private static void injectCssLink(final String url) {
        LinkElement link = Document.get().createLinkElement();
        link.setRel("stylesheet");
        link.setHref(url);
        nativeAttachToHead(link);
    }

    /**
     * Attach an element to document head.
     * 
     * @param scriptElement the element to attach
     */
    private static native void nativeAttachToHead(Node element) /*-{
        $doc.getElementsByTagName("head")[0].appendChild(element);
    }-*/;

    private void requireOrion() {
        this.requireJsLoader.require(new Callback<Void, Throwable>() {

            @Override
            public void onFailure(final Throwable reason) {
                Log.error(OrionEditorExtension.class, "Unable to initialize Orion ", reason);
                initializationFailed("Unable to initialize Orion.");
            }

            @Override
            public void onSuccess(final Void result) {
                registerEditor();
            }
        },
                                     new String[]{"orion/editor/edit", "orion/editor/emacs", "orion/editor/vi", "orion/keyBinding"},
                                     new String[]{"OrionEditor", "OrionEmacs", "OrionVi", "OrionKeyBinding"});
    }

    private void registerEditor() {
        this.editorTypeRegistry.registerEditorType(EditorType.fromKey(ORION_EDITOR_KEY), "Orion", new TextEditorProvider() {

            @Override
            public CodenvyTextEditor getEditor() {
                return orionTextEditorFactory.createTextEditor();
            }
        });
    }

    private void initializationFailed(final String errorMessage) {
        this.notificationManager.showNotification(new Notification(errorMessage, Type.ERROR));
        this.notificationManager.showNotification(new Notification("Orion editor is not available", Type.WARNING));
    }
}

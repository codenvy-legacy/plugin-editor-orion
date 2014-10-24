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

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.Notification.Type;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.editor.orion.client.jso.OrionTextThemeOverlay;
import com.codenvy.ide.editor.orion.client.style.OrionResource;
import com.codenvy.ide.jseditor.client.defaulteditor.EditorBuilder;
import com.codenvy.ide.jseditor.client.editorconfig.DefaultTextEditorConfiguration;
import com.codenvy.ide.jseditor.client.editortype.EditorType;
import com.codenvy.ide.jseditor.client.editortype.EditorTypeRegistry;
import com.codenvy.ide.jseditor.client.requirejs.ModuleHolder;
import com.codenvy.ide.jseditor.client.requirejs.RequireJsLoader;
import com.codenvy.ide.jseditor.client.texteditor.ConfigurableTextEditor;
import com.codenvy.ide.jseditor.client.texteditor.EmbeddedTextEditorPresenter;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LinkElement;
import com.google.gwt.dom.client.Node;

@Extension(title = "Orion Editor", version = "1.1.0")
public class OrionEditorExtension {

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(OrionEditorExtension.class.getSimpleName());

    /** The editor type key. */
    public static final String ORION_EDITOR_KEY = "orion";

    private final NotificationManager    notificationManager;
    private final ModuleHolder           moduleHolder;
    private final EditorTypeRegistry     editorTypeRegistry;
    private final RequireJsLoader        requireJsLoader;

    private final OrionTextEditorFactory orionTextEditorFactory;

    private final OrionResource          orionResource;

    @Inject
    public OrionEditorExtension(final EditorTypeRegistry editorTypeRegistry,
                                final ModuleHolder moduleHolder,
                                final NotificationManager notificationManager,
                                final RequireJsLoader requireJsLoader,
                                final OrionTextEditorFactory orionTextEditorFactory,
                                final OrionResource orionResource) {
        this.notificationManager = notificationManager;
        this.moduleHolder = moduleHolder;
        this.editorTypeRegistry = editorTypeRegistry;
        this.requireJsLoader = requireJsLoader;
        this.orionTextEditorFactory = orionTextEditorFactory;
        this.orionResource = orionResource;

        injectOrion();
        // no need to delay
        KeyMode.init();
    }

    private void injectOrion() {
        // styler scripts are loaded on-demand by orion
        final String[] scripts = new String[]{
                "orion-6.0/built-editor-amd",
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
                LOG.log(Level.SEVERE, "Unable to inject Orion", e);
                initializationFailed("Unable to inject Orion");
            }
        }, scripts, new String[0]);

        injectCssLink(GWT.getModuleBaseForStaticFiles() + "built-editor-compat.css");
    }

    private static void injectCssLink(final String url) {
        final LinkElement link = Document.get().createLinkElement();
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
                LOG.log(Level.SEVERE, "Unable to initialize Orion ", reason);
                initializationFailed("Unable to initialize Orion.");
            }

            @Override
            public void onSuccess(final Void result) {
                endConfiguration();
            }
        },
         new String[]{"orion/editor/edit", "orion/editor/emacs", "orion/editor/vi", "orion/keyBinding"},
         new String[]{"OrionEditor", "OrionEmacs", "OrionVi", "OrionKeyBinding"});
    }

    private void endConfiguration() {
        registerEditor();
        defineDefaultTheme();
    }

    private void registerEditor() {
        LOG.fine("Registering Orion editor type.");
        this.editorTypeRegistry.registerEditorType(EditorType.fromKey(ORION_EDITOR_KEY), "Orion", new EditorBuilder() {

            @Override
            public ConfigurableTextEditor buildEditor() {
                final EmbeddedTextEditorPresenter editor = orionTextEditorFactory.createTextEditor();
                editor.initialize(new DefaultTextEditorConfiguration(), notificationManager);
                return editor;
            }
        });
    }

    private void defineDefaultTheme() {
        // The codenvy theme uses both an orion css file and a CssResource
        this.orionResource.editorStyle().ensureInjected();
        OrionTextThemeOverlay.setDefaultTheme("orionCodenvy", "orion-codenvy.css");
    }

    private void initializationFailed(final String errorMessage) {
        this.notificationManager.showNotification(new Notification(errorMessage, Type.ERROR));
        this.notificationManager.showNotification(new Notification("Orion editor is not available", Type.WARNING));
    }
}

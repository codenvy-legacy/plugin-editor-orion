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

import java.util.Stack;

import javax.inject.Inject;

import com.codenvy.ide.api.editor.EditorPartPresenter;
import com.codenvy.ide.api.editor.EditorProvider;
import com.codenvy.ide.api.extension.Extension;
import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.Notification.Type;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.core.editor.EditorType;
import com.codenvy.ide.core.editor.EditorTypeRegistry;
import com.codenvy.ide.editor.common.client.requirejs.ModuleHolder;
import com.codenvy.ide.editor.common.client.requirejs.RequireJsLoader;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.LinkElement;

@Extension(title = "Orion Editor.", version = "1.0.0")
public class OrionEditorExtension {

    /** The editor type key. */
    private static final String          ORION_EDITOR_KEY = "orion";

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
    }

    private void injectOrion() {
        final Stack<String> scripts = new Stack<String>();
        final String ORION_BASE = "orion/";
        final String[] scriptsNames = new String[]{
                // inject orions'
                "orion/editor/stylers/text_x-java-source/syntax.js",
                "orion/editor/stylers/application_javascript/syntax.js",
                "orion/editor/stylers/application_json/syntax.js",
                "orion/editor/stylers/application_schema_json/syntax.js",
                "orion/editor/stylers/application_x-ejs/syntax.js",
                "orion/editor/stylers/application_xml/syntax.js",
                "orion/editor/stylers/lib/syntax.js",
                "orion/editor/stylers/text_css/syntax.js",
                "orion/editor/stylers/text_html/syntax.js",
                "orion/editor/stylers/text_x-arduino/syntax.js",
                "orion/editor/stylers/text_x-c__src/syntax.js",
                "orion/editor/stylers/text_x-csrc/syntax.js",
                "orion/editor/stylers/text_x-lua/syntax.js",
                "orion/editor/stylers/text_x-php/syntax.js",
                "orion/editor/stylers/text_x-python/syntax.js",
                "orion/editor/stylers/text_x-ruby/syntax.js",
                "orion/editor/stylers/text_x-yaml/syntax.js",
                "orion/emacs.js",
                "orion/vi.js",
        };
        for (final String script : scriptsNames) {
            scripts.add(script); // not push, it would need to be fed in reverse order
        }

        ScriptInjector.fromUrl(GWT.getModuleBaseForStaticFiles() + ORION_BASE + "built-editor.js")
                      .setWindow(ScriptInjector.TOP_WINDOW)
                      .setCallback(new Callback<Void, Exception>() {
                          @Override
                          public void onSuccess(final Void result) {
                              injectOrionExtensions(scripts);
                          }

                          @Override
                          public void onFailure(final Exception e) {
                              Log.error(OrionEditorExtension.class, "Unable to inject Orion", e);
                              initializationFailed("Unable to inject Orion main script");
                          }
                      }).inject();
        injectCssLink(GWT.getModuleBaseForStaticFiles() + "orion/built-editor.css");
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
    private static native void nativeAttachToHead(JavaScriptObject scriptElement) /*-{
        $doc.getElementsByTagName("head")[0].appendChild(scriptElement);
    }-*/;

    private void injectOrionExtensions(final Stack<String> scripts) {
        if (scripts.isEmpty()) {
            Log.info(OrionEditorExtension.class, "Finished loading Orion scripts.");
            requireOrion();
        } else {
            final String script = scripts.pop();
            ScriptInjector.fromUrl(GWT.getModuleBaseForStaticFiles() + script)
                          .setWindow(ScriptInjector.TOP_WINDOW)
                          .setCallback(new Callback<Void, Exception>() {
                              @Override
                              public void onSuccess(final Void aVoid) {
                                  injectOrionExtensions(scripts);
                              }

                              @Override
                              public void onFailure(final Exception e) {
                                  Log.error(OrionEditorExtension.class, "Unable to inject Orion script " + script, e);
                                  initializationFailed("Unable to inject Orion script " + script);
                              }
                          }).inject();
        }
    }

    private void requireOrion() {
        this.requireJsLoader.require(
                                     new Callback<Void, Throwable>() {

                                         @Override
                                         public void onFailure(final Throwable reason) {
                                             Log.error(OrionEditorExtension.class, "Unable to initialize Orion ", reason);
                                             initializationFailed("Unable to initialize Orion.");
                                         }

                                         @Override
                                         public void onSuccess(final Void result) {
                                             initialize();
                                         }
                                     },
                                     new String[]{"orion/editor/edit", "orion/editor/emacs", "orion/editor/vi", "orion/keyBinding"},
                                     new String[]{"OrionEditor", "OrionEmacs", "OrionVi", "OrionKeyBinding"});
    }

    private void initialize() {
        this.editorTypeRegistry.registerEditorType(EditorType.fromKey(ORION_EDITOR_KEY), "Orion", new EditorProvider() {

            @Override
            public EditorPartPresenter getEditor() {
                return orionTextEditorFactory.createTextEditor();
            }
        });
    }

    private void initializationFailed(final String errorMessage) {
        this.notificationManager.showNotification(new Notification(errorMessage, Type.ERROR));
        this.notificationManager.showNotification(new Notification("Orion editor is not available", Type.WARNING));
    }
}

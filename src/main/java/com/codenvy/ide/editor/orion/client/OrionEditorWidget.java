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


import com.codenvy.ide.api.notification.Notification;
import com.codenvy.ide.api.notification.Notification.Type;
import com.codenvy.ide.api.notification.NotificationManager;
import com.codenvy.ide.api.preferences.PreferencesManager;
import com.codenvy.ide.editor.common.client.events.CursorActivityEvent;
import com.codenvy.ide.editor.common.client.events.CursorActivityHandler;
import com.codenvy.ide.editor.common.client.events.HasCursorActivityHandlers;
import com.codenvy.ide.editor.common.client.keymap.KeymapChangeEvent;
import com.codenvy.ide.editor.common.client.keymap.KeymapChangeHandler;
import com.codenvy.ide.editor.common.client.keymap.KeymapPrefReader;
import com.codenvy.ide.editor.common.client.requirejs.ModuleHolder;
import com.codenvy.ide.editor.common.client.texteditor.EditorWidget;
import com.codenvy.ide.editor.common.client.texteditor.EmbeddedDocument;
import com.codenvy.ide.editor.orion.client.jso.OrionEditorOverlay;
import com.codenvy.ide.editor.orion.client.jso.OrionKeyBindingOverlay;
import com.codenvy.ide.editor.orion.client.jso.OrionKeyModeOverlay;
import com.codenvy.ide.editor.orion.client.jso.OrionSelectionOverlay;
import com.codenvy.ide.editor.orion.client.jso.OrionTextThemeOverlay;
import com.codenvy.ide.editor.orion.client.jso.OrionTextViewOverlay;
import com.codenvy.ide.text.Region;
import com.codenvy.ide.text.RegionImpl;
import com.codenvy.ide.util.loging.Log;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.web.bindery.event.shared.EventBus;

/**
 * Orion implementation for {@link EditorWidget}.
 * 
 * @author "Mickaël Leduque"
 */
public class OrionEditorWidget extends Composite implements EditorWidget, HasChangeHandlers, HasCursorActivityHandlers {

    static {
        OrionTextThemeOverlay.setDefaultTheme("nimbus", "orion/editor/themes/nimbus.css");
    }

    private static final String           KEY_MODE_SWITCH    = "keymode_switch";

    private final SimplePanel             panel              = new SimplePanel();
    private final OrionEditorOverlay      editorOverlay;
    private String                        modeName;
    private final KeyModeInstances        keyModeInstances;
    private KeyMode                       currentKeyMode     = KeyMode.DEFAULT;
    private final NotificationManager     notificationManager;
    private final PreferencesManager      preferencesManager;

    private com.codenvy.ide.text.Document document;
    private EmbeddedDocument              embeddedDocument;

    private boolean                       changeHandlerAdded = false;
    private boolean                       focusHandlerAdded  = false;
    private boolean                       blurHandlerAdded   = false;
    private boolean                       scrollHandlerAdded = false;
    private boolean                       cursorHandlerAdded = false;

    @AssistedInject
    public OrionEditorWidget(final ModuleHolder moduleHolder,
                             final KeyModeInstances keyModeInstances,
                             final NotificationManager notificationManager,
                             final PreferencesManager preferencesManager,
                             final EventBus eventBus,
                             @Assisted final String editorMode,
                             @Assisted final com.codenvy.ide.text.Document document) {
        this.panel.setSize("100%", "100%");
        initWidget(this.panel);

        this.notificationManager = notificationManager;
        this.preferencesManager = preferencesManager;

        JavaScriptObject orionEditorModule = moduleHolder.getModule("OrionEditor");

        setMode(editorMode);

        this.editorOverlay = OrionEditorOverlay.createEditor(panel.getElement(), getConfiguration(), orionEditorModule);

        this.keyModeInstances = keyModeInstances;
        final OrionTextViewOverlay textView = this.editorOverlay.getTextView();
        this.keyModeInstances.add(KeyMode.VI, OrionKeyModeOverlay.getViKeyMode(moduleHolder.getModule("OrionVi"), textView));
        this.keyModeInstances.add(KeyMode.EMACS, OrionKeyModeOverlay.getEmacsKeyMode(moduleHolder.getModule("OrionEmacs"), textView));

        final OrionKeyModeOverlay defaultKeyMode = OrionKeyModeOverlay.getDefaultKeyMode(textView);
        OrionKeyBindingOverlay keyBinding = OrionKeyBindingOverlay.createKeyStroke("K", true, true, true,
                                                                                   false, "keydown",
                                                                                   moduleHolder.getModule("OrionKeyBinding"));
        defaultKeyMode.setKeyBinding(keyBinding, KEY_MODE_SWITCH);
        textView.setAction(KEY_MODE_SWITCH, new Action() {

            @Override
            public void onAction() {
                changeKeyMode();
            }
        });

        setupKeymode();
        eventBus.addHandler(KeymapChangeEvent.TYPE, new KeymapChangeHandler() {

            @Override
            public void onKeymapChanged(final KeymapChangeEvent event) {
                setupKeymode();
            }
        });
    }

    @Override
    public String getValue() {
        return editorOverlay.getText();
    }

    @Override
    public void setValue(String newValue) {
        this.editorOverlay.setText(newValue);
    }

    private JavaScriptObject getConfiguration() {
        final JSONObject json = new JSONObject();

        json.put("theme", new JSONObject(OrionTextThemeOverlay.getDefautTheme()));
        json.put("contentType", new JSONString(this.modeName));

        return json.getJavaScriptObject();
    }

    protected void autoComplete(OrionEditorOverlay editor) {
        // TODO
    }

    @Override
    public void setMode(String modeName) {
        String mode = modeName;
        if (modeName.equals("clike")) {
            mode = "text/x-java-source";
        }
        if (modeName.equals("xml")) {
            mode = "application/xml";
        }
        if (modeName.equals("javascript")) {
            mode = "application/javascript";
        }
        Log.info(OrionEditorWidget.class, "Requested mode: " + modeName + " kept " + mode);

        this.modeName = mode;
        // editorOverlay.setOption("mode", modeName);
    }

    public String getMode() {
        return modeName;
    }

    @Override
    public void setReadOnly(final boolean isReadOnly) {
        this.editorOverlay.getTextView().getOptions().setReadOnly(isReadOnly);
        this.editorOverlay.getTextView().update();
    }


    @Override
    public boolean isReadOnly() {
        return this.editorOverlay.getTextView().getOptions().isReadOnly();
    }

    @Override
    public boolean isDirty() {
        return this.editorOverlay.isDirty();
    }

    @Override
    public void markClean() {
        this.editorOverlay.setDirty(false);
    }

    private void changeKeyMode() {
        KeyMode next = KeyMode.fromIndex((currentKeyMode.getIndex() + 1) % 4);
        Log.info(OrionEditorWidget.class, "Setting editor keymap: " + next.getOrionKey());
        notificationManager.showNotification(new Notification("Changed key binding: " + next.getOrionKey(), Type.INFO));

        this.currentKeyMode = next;

        resetKeyModes(); // remove all keymodes except default

        selectKeyMode(next);
    }

    private void selectKeyMode(KeyMode keymode) {
        switch (keymode) {
            case DEFAULT:
                break;
            case VI:
                this.editorOverlay.getTextView().addKeyMode(keyModeInstances.getInstance(KeyMode.VI));
                break;
            case EMACS:
                this.editorOverlay.getTextView().addKeyMode(keyModeInstances.getInstance(KeyMode.EMACS));
                break;
            default:
                throw new RuntimeException("Unknown keymode type: " + keymode);
        }
    }

    private void resetKeyModes() {
        this.editorOverlay.getTextView().removeKeyMode(keyModeInstances.getInstance(KeyMode.VI));
        this.editorOverlay.getTextView().removeKeyMode(keyModeInstances.getInstance(KeyMode.EMACS));
    }

    @Override
    public EmbeddedDocument getDocument() {
        if (this.embeddedDocument == null) {
            this.embeddedDocument = new OrionDocument(this.editorOverlay.getTextView(), this);
        }
        return this.embeddedDocument;
    }

    @Override
    public Region getSelectedRange() {
        final OrionSelectionOverlay selection = this.editorOverlay.getSelection();

        final int start = selection.getStart();
        final int end = selection.getEnd();

        if (start < 0 || end > this.editorOverlay.getModel().getCharCount() || start > end) {
            throw new RuntimeException("Invalid selection");
        }
        return new RegionImpl(start, end - start);
    }

    @Override
    public int getTabSize() {
        return this.editorOverlay.getTextView().getOptions().getTabSize();
    }

    @Override
    public void setTabSize(int tabSize) {
        this.editorOverlay.getTextView().getOptions().setTabSize(tabSize);
    }

    @Override
    public HandlerRegistration addChangeHandler(final ChangeHandler handler) {
        if (!changeHandlerAdded) {
            changeHandlerAdded = true;
            final OrionTextViewOverlay textView = this.editorOverlay.getTextView();
            textView.addEventListener(OrionEventContants.MODEL_CHANGED_EVENT, new OrionTextViewOverlay.EventHandlerNoParameter() {

                @Override
                public void onEvent() {
                    fireChangeEvent();
                }
            });
        }
        return addHandler(handler, ChangeEvent.getType());
    }

    private void fireChangeEvent() {
        DomEvent.fireNativeEvent(Document.get().createChangeEvent(), this);
    }

    @Override
    public HandlerRegistration addCursorActivityHandler(CursorActivityHandler handler) {
        if (!cursorHandlerAdded) {
            cursorHandlerAdded = true;
            final OrionTextViewOverlay textView = this.editorOverlay.getTextView();
            textView.addEventListener(OrionEventContants.SELECTION_EVENT, new OrionTextViewOverlay.EventHandlerNoParameter() {

                @Override
                public void onEvent() {
                    fireCursorActivityEvent();
                }
            });
        }
        return addHandler(handler, CursorActivityEvent.TYPE);
    }

    private void fireCursorActivityEvent() {
        fireEvent(new CursorActivityEvent());
    }

    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        if (!focusHandlerAdded) {
            focusHandlerAdded = true;
            final OrionTextViewOverlay textView = this.editorOverlay.getTextView();
            textView.addEventListener(OrionEventContants.FOCUS_EVENT, new OrionTextViewOverlay.EventHandlerNoParameter() {

                @Override
                public void onEvent() {
                    fireFocusEvent();
                }
            });
        }
        return addHandler(handler, FocusEvent.getType());
    }

    private void fireFocusEvent() {
        DomEvent.fireNativeEvent(Document.get().createFocusEvent(), this);
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
        if (!blurHandlerAdded) {
            blurHandlerAdded = true;
            final OrionTextViewOverlay textView = this.editorOverlay.getTextView();
            textView.addEventListener(OrionEventContants.BLUR_EVENT, new OrionTextViewOverlay.EventHandlerNoParameter() {

                @Override
                public void onEvent() {
                    fireBlurEvent();
                }
            });
        }
        return addHandler(handler, BlurEvent.getType());
    }

    private void fireBlurEvent() {
        DomEvent.fireNativeEvent(Document.get().createBlurEvent(), this);
    }

    private void setupKeymode() {
        final String propertyValue = KeymapPrefReader.readPref(this.preferencesManager,
                                                               OrionEditorExtension.ORION_EDITOR_KEY);
        KeyMode keymode;
        try {
            keymode = KeyMode.fromOrionKey(propertyValue);
        } catch (final IllegalArgumentException e) {
            Log.error(OrionEditorWidget.class, "Unknown value in keymap preference.", e);
            return;
        }
        selectKeyMode(keymode);
    }
}

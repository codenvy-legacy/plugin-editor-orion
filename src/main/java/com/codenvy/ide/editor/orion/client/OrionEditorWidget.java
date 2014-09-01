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

import com.codenvy.ide.api.preferences.PreferencesManager;
import com.codenvy.ide.api.text.Region;
import com.codenvy.ide.api.text.RegionImpl;
import com.codenvy.ide.editor.orion.client.jso.OrionEditorOverlay;
import com.codenvy.ide.editor.orion.client.jso.OrionKeyModeOverlay;
import com.codenvy.ide.editor.orion.client.jso.OrionSelectionOverlay;
import com.codenvy.ide.editor.orion.client.jso.OrionTextThemeOverlay;
import com.codenvy.ide.editor.orion.client.jso.OrionTextViewOverlay;
import com.codenvy.ide.jseditor.client.document.EmbeddedDocument;
import com.codenvy.ide.jseditor.client.editortype.EditorType;
import com.codenvy.ide.jseditor.client.events.CursorActivityEvent;
import com.codenvy.ide.jseditor.client.events.CursorActivityHandler;
import com.codenvy.ide.jseditor.client.events.HasCursorActivityHandlers;
import com.codenvy.ide.jseditor.client.events.HasScrollHandlers;
import com.codenvy.ide.jseditor.client.events.ScrollEvent;
import com.codenvy.ide.jseditor.client.events.ScrollHandler;
import com.codenvy.ide.jseditor.client.keymap.Keymap;
import com.codenvy.ide.jseditor.client.keymap.KeymapChangeEvent;
import com.codenvy.ide.jseditor.client.keymap.KeymapChangeHandler;
import com.codenvy.ide.jseditor.client.keymap.KeymapPrefReader;
import com.codenvy.ide.jseditor.client.position.PositionConverter;
import com.codenvy.ide.jseditor.client.requirejs.ModuleHolder;
import com.codenvy.ide.jseditor.client.texteditor.EditorWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
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
public class OrionEditorWidget extends Composite implements EditorWidget, HasChangeHandlers, HasCursorActivityHandlers, HasScrollHandlers {

    /** The UI binder instance. */
    private static final OrionEditorWidgetUiBinder UIBINDER = GWT.create(OrionEditorWidgetUiBinder.class);

    /** The logger. */
    private static final Logger LOG = Logger.getLogger(OrionEditorWidget.class.getSimpleName());

    @UiField
    SimplePanel                                    panel;

    /** The instance of the orion editor native element style. */
    @UiField
    EditorElementStyle                             editorElementStyle;

    private final OrionEditorOverlay               editorOverlay;
    private String                                 modeName;
    private final KeyModeInstances                 keyModeInstances;
    private final PreferencesManager               preferencesManager;
    private final JavaScriptObject                 orionEditorModule;

    private OrionDocument                       embeddedDocument;

    private boolean                                changeHandlerAdded = false;
    private boolean                                focusHandlerAdded  = false;
    private boolean                                blurHandlerAdded   = false;
    private boolean                                scrollHandlerAdded = false;
    private boolean                                cursorHandlerAdded = false;

    private Keymap                                 keymap;

    @AssistedInject
    public OrionEditorWidget(final ModuleHolder moduleHolder,
                             final KeyModeInstances keyModeInstances,
                             final PreferencesManager preferencesManager,
                             final EventBus eventBus,
                             @Assisted final String editorMode) {
        initWidget(UIBINDER.createAndBindUi(this));

        this.preferencesManager = preferencesManager;

        this.orionEditorModule = moduleHolder.getModule("OrionEditor");

        setMode(editorMode);

        panel.getElement().setId("orion-parent-" + Document.get().createUniqueId());
        panel.getElement().addClassName(this.editorElementStyle.editorParent());
        this.editorOverlay = OrionEditorOverlay.createEditor(panel.getElement(), getConfiguration(), orionEditorModule);

        this.keyModeInstances = keyModeInstances;
        final OrionTextViewOverlay textView = this.editorOverlay.getTextView();
        this.keyModeInstances.add(KeyMode.VI, OrionKeyModeOverlay.getViKeyMode(moduleHolder.getModule("OrionVi"), textView));
        this.keyModeInstances.add(KeyMode.EMACS, OrionKeyModeOverlay.getEmacsKeyMode(moduleHolder.getModule("OrionEmacs"), textView));

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
        this.editorOverlay.getUndoStock().reset();
    }

    private JavaScriptObject getConfiguration() {
        final JSONObject json = new JSONObject();

        json.put("theme", new JSONObject(OrionTextThemeOverlay.getDefautTheme()));
        json.put("contentType", new JSONString(this.modeName));
        json.put("noComputeSize", JSONBoolean.getInstance(true));

        return json.getJavaScriptObject();
    }

    protected void autoComplete(OrionEditorOverlay editor) {
        // TODO
    }

    @Override
    public void setMode(String modeName) {
        String mode = modeName;
        if (modeName.equals("text/x-java")) {
            mode = "text/x-java-source";
        }
        LOG.fine("Requested mode: " + modeName + " kept " + mode);

        this.modeName = mode;
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

    private void selectKeyMode(Keymap keymap) {
        resetKeyModes();
        if (keymap == null || KeyMode.DEFAULT.equals(keymap)) {
            return;
        } else if (KeyMode.EMACS.equals(keymap)) {
            this.editorOverlay.getTextView().addKeyMode(keyModeInstances.getInstance(KeyMode.EMACS));
        } else if (KeyMode.VI.equals(keymap)) {
            this.editorOverlay.getTextView().addKeyMode(keyModeInstances.getInstance(KeyMode.VI));
        } else {
            this.keymap = null;
            throw new RuntimeException("Unknown keymap type: " + keymap);
        }
        this.keymap = keymap;
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

    public void setSelectedRange(final Region selection, final boolean show) {
        this.editorOverlay.setSelection(selection.getOffset(), selection.getLength(), show);
    }

    public void setDisplayRange(final Region range) {
        // show the line at the head of the range
        final int headOffset = range.getOffset() + range.getLength();
        if (range.getLength() < 0) {
            this.editorOverlay.getTextView().setTopIndex(headOffset);
        } else {
            this.editorOverlay.getTextView().setBottomIndex(headOffset);
        }
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


    @Override
    public HandlerRegistration addScrollHandler(final ScrollHandler handler) {
        if (!scrollHandlerAdded) {
            scrollHandlerAdded = true;
            final OrionTextViewOverlay textView = this.editorOverlay.getTextView();
            textView.addEventListener(OrionEventContants.SCROLL_EVENT, new OrionTextViewOverlay.EventHandlerNoParameter() {

                @Override
                public void onEvent() {
                    fireScrollEvent();
                }
            });
        }
        return addHandler(handler, ScrollEvent.TYPE);
    }

    private void fireScrollEvent() {
        fireEvent(new ScrollEvent());
    }

    private void setupKeymode() {
        final String propertyValue = KeymapPrefReader.readPref(this.preferencesManager,
                                                               OrionEditorExtension.ORION_EDITOR_KEY);
        Keymap keymap;
        try {
            keymap = Keymap.fromKey(propertyValue);
        } catch (final IllegalArgumentException e) {
            LOG.log(Level.WARNING, "Unknown value in keymap preference.", e);
            return;
        }
        selectKeyMode(keymap);
    }

    @Override
    public EditorType getEditorType() {
        return EditorType.fromKey(OrionEditorExtension.ORION_EDITOR_KEY);
    }

    @Override
    public Keymap getKeymap() {
        return this.keymap;
    }

    public PositionConverter getPositionConverter() {
        return this.getPositionConverter();
    }

    public void setFocus() {
        this.editorOverlay.focus();
    }

    @Override
    protected void onLoad() {

        // fix for native editor height
        if (panel.getElement().getChildCount() > 0) {
            final Element child = panel.getElement().getFirstChildElement();
            child.setId("orion-editor-" + Document.get().createUniqueId());
            child.getStyle().clearHeight();

        } else {
            LOG.severe("Orion insertion failed.");
        }
    }

    /**
     * UI binder interface for this component.
     * 
     * @author "Mickaël Leduque"
     */
    interface OrionEditorWidgetUiBinder extends UiBinder<SimplePanel, OrionEditorWidget> {
    }

    /**
     * CSS style for the orion native editor element.
     * 
     * @author "Mickaël Leduque"
     */
    public interface EditorElementStyle extends CssResource {

        @ClassName("editor-parent")
        String editorParent();
    }
}

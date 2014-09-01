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

import com.codenvy.ide.api.text.Region;
import com.codenvy.ide.editor.orion.client.jso.OrionPixelPositionOverlay;
import com.codenvy.ide.editor.orion.client.jso.OrionTextViewOverlay;
import com.codenvy.ide.jseditor.client.document.DocumentEventBus;
import com.codenvy.ide.jseditor.client.document.DocumentHandle;
import com.codenvy.ide.jseditor.client.document.EmbeddedDocument;
import com.codenvy.ide.jseditor.client.events.CursorActivityHandler;
import com.codenvy.ide.jseditor.client.events.HasCursorActivityHandlers;
import com.codenvy.ide.jseditor.client.position.PositionConverter;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * The implementation of {@link EmbeddedDocument} for Orion.
 *
 * @author "Mickaël Leduque"
 */
public class OrionDocument implements EmbeddedDocument, DocumentHandle {

    private final OrionTextViewOverlay textViewOverlay;

    private final OrionPositionConverter positionConverter;

    private final HasCursorActivityHandlers  hasCursorActivityHandlers;

    private final DocumentEventBus eventBus = new DocumentEventBus();

    public OrionDocument(final OrionTextViewOverlay textViewOverlay,
                         final HasCursorActivityHandlers hasCursorActivityHandlers) {
        this.textViewOverlay = textViewOverlay;
        this.hasCursorActivityHandlers = hasCursorActivityHandlers;
        this.positionConverter = new OrionPositionConverter();
    }

    @Override
    public TextPosition getPositionFromIndex(final int index) {
        final int line = this.textViewOverlay.getModel().getLineAtOffset(index);
        if (line == -1) {
            return null;
        }
        final int lineStart = this.textViewOverlay.getModel().getLineStart(line);
        if (lineStart == -1) {
            return null;
        }
        final int character = index - lineStart;
        if (character < 0) {
            return null;
        }
        return new TextPosition(line, character);
    }

    @Override
    public int getIndexFromPosition(final TextPosition position) {
        final int lineStart = this.textViewOverlay.getModel().getLineStart(position.getLine());
        if (lineStart == -1) {
            return -1;
        }

        final int result = lineStart + position.getCharacter();
        final int lineEnd = this.textViewOverlay.getModel().getLineEnd(position.getLine());

        if (lineEnd < result) {
            return -1;
        }
        return result;
    }

    @Override
    public void setCursorPosition(final TextPosition position) {
        this.textViewOverlay.setCaretOffset(getIndexFromPosition(position));

    }

    @Override
    public TextPosition getCursorPosition() {
        final int offset = this.textViewOverlay.getCaretOffset();
        return getPositionFromIndex(offset);
    }

    @Override
    public int getLineCount() {
        return this.textViewOverlay.getModel().getLineCount();
    }

    @Override
    public HandlerRegistration addCursorHandler(final CursorActivityHandler handler) {
        return this.hasCursorActivityHandlers.addCursorActivityHandler(handler);
    }

    @Override
    public String getContents() {
        return this.textViewOverlay.getModel().getText();
    }

    public PositionConverter getPositionConverter() {
        return this.positionConverter;
    }

    private class OrionPositionConverter implements PositionConverter {

        @Override
        public PixelCoordinates textToPixel(TextPosition textPosition) {
            final int textOffset = getIndexFromPosition(textPosition);
            return offsetToPixel(textOffset);
        }

        @Override
        public PixelCoordinates offsetToPixel(int textOffset) {
            final OrionPixelPositionOverlay location = textViewOverlay.getLocationAtOffset(textOffset);
            return new PixelCoordinates(location.getX(), location.getY());
        }

        @Override
        public TextPosition pixelToText(PixelCoordinates coordinates) {
            final int offset = pixelToOffset(coordinates);
            return getPositionFromIndex(offset);
        }

        @Override
        public int pixelToOffset(PixelCoordinates coordinates) {
            return textViewOverlay.getOffsetAtLocation(coordinates.getX(),
                                                                    coordinates.getY());
        }
    }

    public void replace(final Region region, final String text) {
        this.textViewOverlay.getModel().setText(text, region.getOffset(), region.getLength());
    }


    public DocumentHandle getDocumentHandle() {
        return this;
    }

    @Override
    public boolean isSameAs(final DocumentHandle document) {
        return (this.equals(document));
    }

    public DocumentEventBus getDocEventBus() {
        return this.eventBus;
    }

    public EmbeddedDocument getDocument() {
        return this;
    }

    public int getContentsCharCount() {
        return this.textViewOverlay.getModel().getCharCount();
    }
}

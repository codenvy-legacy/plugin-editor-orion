/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.editor.orion.client;

import org.eclipse.che.ide.api.text.Region;
import org.eclipse.che.ide.editor.orion.client.jso.OrionPixelPositionOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionSelectionOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionTextViewOverlay;
import org.eclipse.che.ide.jseditor.client.document.AbstractEmbeddedDocument;
import org.eclipse.che.ide.jseditor.client.document.EmbeddedDocument;
import org.eclipse.che.ide.jseditor.client.events.CursorActivityHandler;
import org.eclipse.che.ide.jseditor.client.events.HasCursorActivityHandlers;
import org.eclipse.che.ide.jseditor.client.position.PositionConverter;
import org.eclipse.che.ide.jseditor.client.text.LinearRange;
import org.eclipse.che.ide.jseditor.client.text.TextPosition;
import org.eclipse.che.ide.jseditor.client.text.TextRange;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * The implementation of {@link EmbeddedDocument} for Orion.
 *
 * @author "Mickaël Leduque"
 */
public class OrionDocument extends AbstractEmbeddedDocument {

    private final OrionTextViewOverlay textViewOverlay;

    private final OrionPositionConverter positionConverter;

    private final HasCursorActivityHandlers  hasCursorActivityHandlers;

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

    public int getCursorOffset() {
        return this.textViewOverlay.getCaretOffset();
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

    @Override
    public String getContentRange(final int offset, final int length) {
        return this.textViewOverlay.getModel().getText(offset, offset + length);
    }

    @Override
    public String getContentRange(final TextRange range) {
        final int startOffset = getIndexFromPosition(range.getFrom());
        final int endOffset = getIndexFromPosition(range.getTo());
        return this.textViewOverlay.getModel().getText(startOffset, endOffset);
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

    public int getContentsCharCount() {
        return this.textViewOverlay.getModel().getCharCount();
    }

    @Override
    public String getLineContent(final int line) {
        return this.textViewOverlay.getModel().getLine(line);
    }

    @Override
    public TextRange getTextRangeForLine(final int line) {
        final int startOffset = this.textViewOverlay.getModel().getLineStart(line);
        final int endOffset = this.textViewOverlay.getModel().getLineEnd(line);
        final int length = endOffset - startOffset;
        return new TextRange(new TextPosition(line, 0), new TextPosition(line, length - 1));
    }

    @Override
    public LinearRange getLinearRangeForLine(final int line) {
        return LinearRange.createWithStart(this.textViewOverlay.getModel().getLineStart(line))
                          .andEnd(textViewOverlay.getModel().getLineEnd(line));
    }

    @Override
    public TextRange getSelectedTextRange() {
        final OrionSelectionOverlay selection = this.textViewOverlay.getSelection();
        final int start = selection.getStart();
        final TextPosition startPosition = getPositionFromIndex(start);
        final int end = selection.getEnd();
        final TextPosition endPosition = getPositionFromIndex(end);
        return new TextRange(startPosition, endPosition);
    }

    @Override
    public LinearRange getSelectedLinearRange() {
        final OrionSelectionOverlay selection = this.textViewOverlay.getSelection();

        final int start = selection.getStart();
        final int end = selection.getEnd();
        return LinearRange.createWithStart(start).andEnd(end);
    }
}

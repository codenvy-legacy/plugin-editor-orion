package com.codenvy.ide.jseditor.client.text;


/**
 * Oriented range of text.<br>
 */
public class TextRange {
    /** The start position of the range. */
    private final TextPosition from;

    /** The end position of the range. */
    private final TextPosition to;

    public TextRange(final TextPosition from, final TextPosition to) {
        this.from = from;
        this.to = to;
    }

    public TextPosition getFrom() {
        return this.from;
    }

    public TextPosition getTo() {
        return this.to;
    }
}

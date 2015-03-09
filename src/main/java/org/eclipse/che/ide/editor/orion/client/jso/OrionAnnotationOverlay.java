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
package org.eclipse.che.ide.editor.orion.client.jso;

import com.google.gwt.core.client.JavaScriptObject;

/** Overlay on the orion JS Annotation objects. */
public class OrionAnnotationOverlay extends JavaScriptObject {

    /** JSO mandated protected constructor. */
    protected OrionAnnotationOverlay() {
    }

    public final native String getType() /*-{
        return this.type;
    }-*/;

    public final native void setType(String type) /*-{
        this.type = type;
    }-*/;

    public final native int getStart() /*-{
        return this.start;
    }-*/;

    public final native void setStart(int offset) /*-{
        this.start = start;
    }-*/;

    public final native int getEnd() /*-{
        return this.end;
    }-*/;

    public final native void setEnd(int offset) /*-{
        this.end = end;
    }-*/;

    public final native String getHtml() /*-{
        return this.html;
    }-*/;

    public final native void setHtml(String html) /*-{
        this.html = html;
    }-*/;

    public final native String getTitle() /*-{
        return this.title;
    }-*/;

    public final native void setTitle(String title) /*-{
        this.title = title;
    }-*/;

    public final native OrionStyleOverlay getStyle() /*-{
        return this.style;
    }-*/;

    public final native void setStyle(OrionStyleOverlay style) /*-{
        this.style = style;
    }-*/;

    public final native OrionStyleOverlay getOverviewStyle() /*-{
        return this.overviewStyle;
    }-*/;

    public final native void setOverviewStyle(OrionStyleOverlay style) /*-{
        this.overviewStyle = style;
    }-*/;

    public final native OrionStyleOverlay getRangeStyle() /*-{
        return this.rangeStyle;
    }-*/;

    public final native void setRangeStyle(OrionStyleOverlay style) /*-{
        this.rangeStyle = style;
    }-*/;

    public final native OrionStyleOverlay getLineStyle() /*-{
        return this.lineStyle;
    }-*/;

    public final native void setLineStyle(OrionStyleOverlay style) /*-{
        this.lineStyle = style;
    }-*/;
}

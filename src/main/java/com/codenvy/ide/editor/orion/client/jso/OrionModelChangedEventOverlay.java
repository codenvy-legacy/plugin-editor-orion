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
package com.codenvy.ide.editor.orion.client.jso;

public class OrionModelChangedEventOverlay extends OrionEventOverlay {

    protected OrionModelChangedEventOverlay() {}

    public final native int getAddedCharCount() /*-{
        return this.addedCharCount;
    }-*/;

    public final native int getAddedLineCount() /*-{
        return this.addedLineCount;
    }-*/;

    public final native int getRemovedCharCount() /*-{
        return this.removedCharCount;
    }-*/;

    public final native int getRemovedLineCount() /*-{
        return this.removedLineCount;
    }-*/;

    public final native int getStart() /*-{
        return this.start;
    }-*/;
}

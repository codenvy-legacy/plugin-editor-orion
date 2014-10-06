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
package com.codenvy.ide.editor.orion.client.inject;

import com.codenvy.ide.api.extension.ExtensionGinModule;
import com.codenvy.ide.editor.orion.client.OrionEditorWidget;
import com.codenvy.ide.editor.orion.client.OrionTextEditorViewFactory;
import com.codenvy.ide.jseditor.client.texteditor.EditorWidgetFactory;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.TypeLiteral;

@ExtensionGinModule
public class OrionEditorGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        // Bind the Orion EditorWidget factory
        install(new GinFactoryModuleBuilder().build(new TypeLiteral<EditorWidgetFactory<OrionEditorWidget>>() {
        }));
        bind(OrionTextEditorViewFactory.class);
    }
}

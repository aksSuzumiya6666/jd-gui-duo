/*
 * Copyright (c) 2026 @nbauma109.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view.bean;

import org.jd.gui.api.model.TreeNodeData;

import javax.swing.Icon;

public class QuickOutlineListCellBean implements TreeNodeData {
    private final String label;
    private final String fragment;
    private final Icon icon;

    public QuickOutlineListCellBean(String label, String fragment, Icon icon) {
        this.label = label;
        this.fragment = fragment;
        this.icon = icon;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getTip() {
        return label;
    }

    public String getFragment() {
        return fragment;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public Icon getOpenIcon() {
        return icon;
    }
}

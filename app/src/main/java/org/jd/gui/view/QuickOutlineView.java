/*
 * Copyright (c) 2026 @nbauma109.
 * This project is distributed under the GPLv3 license.
 * This is a Copyleft license that gives the user the right to use,
 * copy and modify the code freely for non-commercial purposes.
 */

package org.jd.gui.view;

import org.jd.gui.util.swing.SwingUtil;
import org.jd.gui.view.bean.QuickOutlineListCellBean;
import org.jd.gui.view.component.Tree;
import org.jd.gui.view.renderer.TreeNodeRenderer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class QuickOutlineView {

    private static final Color OUTLINE_BACKGROUND = Color.decode("#FFFFE1");

    private JDialog quickOutlineDialog;
    private Tree quickOutlineTree;

    public QuickOutlineView(JFrame mainFrame, Consumer<String> selectedMemberCallback) {
        SwingUtil.invokeLater(() -> {
            quickOutlineDialog = new JDialog(mainFrame, "Quick Outline", false);
            quickOutlineDialog.setUndecorated(true);
            quickOutlineDialog.setResizable(false);
            quickOutlineDialog.setLayout(new BorderLayout());
            quickOutlineDialog.getContentPane().setBackground(OUTLINE_BACKGROUND);

            quickOutlineTree = new Tree();
            quickOutlineTree.setRootVisible(true);
            quickOutlineTree.setShowsRootHandles(true);
            quickOutlineTree.setBackground(OUTLINE_BACKGROUND);
            quickOutlineTree.setCellRenderer(new TreeNodeRenderer());
            quickOutlineTree.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        onMemberSelected(selectedMemberCallback);
                    }
                }
            });
            quickOutlineTree.registerKeyboardAction(
                    e -> onMemberSelected(selectedMemberCallback),
                    KeyStroke.getKeyStroke("ENTER"),
                    JComponent.WHEN_FOCUSED);

            JScrollPane scrollPane = new JScrollPane(quickOutlineTree);
            scrollPane.setPreferredSize(new Dimension(430, 260));
            scrollPane.getViewport().setBackground(OUTLINE_BACKGROUND);
            quickOutlineDialog.add(scrollPane, BorderLayout.CENTER);

            JRootPane rootPane = quickOutlineDialog.getRootPane();
            rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                    KeyStroke.getKeyStroke("ESCAPE"), "QuickOutlineView.cancel");
            rootPane.getActionMap().put("QuickOutlineView.cancel", new AbstractAction() {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    quickOutlineDialog.setVisible(false);
                }
            });

            quickOutlineDialog.pack();
            quickOutlineDialog.setLocationRelativeTo(mainFrame);
        });
    }

    public void show(DefaultMutableTreeNode rootNode, JComponent anchorComponent) {
        SwingUtil.invokeLater(() -> {
            quickOutlineTree.setModel(new DefaultTreeModel(rootNode));
            quickOutlineTree.expandRow(0);

            int selectedRow = rootNode.getChildCount() > 0 ? 1 : 0;
            quickOutlineTree.setSelectionRow(selectedRow);

            if (anchorComponent != null && anchorComponent.isShowing()) {
                Point location = anchorComponent.getLocationOnScreen();
                int x = location.x + anchorComponent.getWidth() - quickOutlineDialog.getWidth() - 20;
                int y = location.y + anchorComponent.getHeight() - quickOutlineDialog.getHeight() - 20;
                quickOutlineDialog.setLocation(x, y);
            }

            quickOutlineDialog.setVisible(true);
            quickOutlineTree.requestFocus();
        });
    }

    protected void onMemberSelected(Consumer<String> selectedMemberCallback) {
        TreePath path = quickOutlineTree.getSelectionPath();
        if (path == null) {
            return;
        }

        Object lastPathComponent = path.getLastPathComponent();
        if (lastPathComponent instanceof DefaultMutableTreeNode treeNode) {
            Object userObject = treeNode.getUserObject();
            if (userObject instanceof QuickOutlineListCellBean cellBean) {
                String fragment = cellBean.fragment();
                if (fragment != null && !fragment.isEmpty()) {
                    selectedMemberCallback.accept(fragment);
                    quickOutlineDialog.setVisible(false);
                }
            }
        }
    }
}

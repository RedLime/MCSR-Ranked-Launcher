/*
 * MCSR Ranked Launcher - https://github.com/RedLime/MCSR-Ranked-Launcher
 * Copyright (C) 2023 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.gui.tabs.instances;

import java.awt.Dimension;
import java.awt.event.ItemEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.utils.sort.InstanceSortingStrategies;
import com.atlauncher.utils.sort.InstanceSortingStrategy;
import com.atlauncher.viewmodel.base.IInstancesTabViewModel;

public final class InstancesNavigationPanel extends JPanel implements RelocalizationListener {
    private final IInstancesTabViewModel viewModel;
    private final InstancesSearchField searchField;
    private final JComboBox<InstanceSortingStrategy> sortingBox = new JComboBox<>(InstanceSortingStrategies.values());

    public InstancesNavigationPanel(final InstancesTab parent, final IInstancesTabViewModel viewModel) {
        this.viewModel = viewModel;
        this.searchField = new InstancesSearchField(parent,viewModel);
        this.sortingBox.setMaximumSize(new Dimension(190, 23));

        if (App.settings.defaultInstanceSorting != InstanceSortingStrategies.BY_NAME) {
            this.sortingBox.setSelectedItem(App.settings.defaultInstanceSorting);
        }

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.add(Box.createHorizontalGlue());
        this.add(searchField);
        this.add(Box.createHorizontalStrut(5));
        this.add(this.sortingBox);
        this.addListeners();

        RelocalizationManager.addListener(this);
    }

    private void addListeners() {
        // item listeners
        this.sortingBox.addItemListener((e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                this.viewModel.setSort((InstanceSortingStrategy) e.getItem());
            }
        });
    }

    @Override
    public void onRelocalization() {
        this.searchField.putClientProperty("JTextField.placeholderText", GetText.tr("Search"));
        this.sortingBox.repaint();
    }
}

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
package com.atlauncher.gui.tabs.news;

import java.util.function.Consumer;

import com.atlauncher.managers.NewsManager;

public class NewsViewModel implements INewsViewModel {
    private Consumer<String> _onReload;

    @Override
    public void addOnReloadListener(Consumer<String> onReload) {
        _onReload = onReload;
    }

    @Override
    public void reload() {
        _onReload.accept(NewsManager.getNewsHTML());
    }
}

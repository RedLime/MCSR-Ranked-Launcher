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
package com.atlauncher.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.atlauncher.App;

public final class Timestamper {
    private static final SimpleDateFormat format = new SimpleDateFormat(App.settings.dateFormat + " HH:mm:ss a");

    public static String now() {
        return format.format(new Date());
    }

    public static String was(Date date) {
        return format.format(date);
    }

    public static void updateDateFormat(String dateFormat) {
        format.applyPattern(dateFormat + " HH:mm:ss a");
    }
}

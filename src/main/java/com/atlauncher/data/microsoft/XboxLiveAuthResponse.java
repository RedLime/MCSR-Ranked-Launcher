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
package com.atlauncher.data.microsoft;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class XboxLiveAuthResponse {
    @SerializedName("IssueInstant")
    public Date issueInstant;

    @SerializedName("NotAfter")
    public Date notAfter;

    @SerializedName("Token")
    public String token;

    @SerializedName("DisplayClaims")
    public XboxLiveDisplayClaims displayClaims;
}

/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2020 ATLauncher
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

import java.util.List;
import java.util.Map;

import com.atlauncher.Gsons;
import com.atlauncher.data.Constants;
import com.atlauncher.data.microsoft.LoginResponse;
import com.atlauncher.data.microsoft.OauthTokenResponse;
import com.atlauncher.data.microsoft.Profile;
import com.atlauncher.data.microsoft.Store;
import com.atlauncher.data.microsoft.XboxLiveAuthResponse;
import com.atlauncher.network.Download;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;

/**
 * Various utility methods for interacting with the Microsoft Auth API.
 */
public class MicrosoftAuthAPI {
    public static OauthTokenResponse tradeCodeForAccessToken(String code) {
        RequestBody data = new FormBody.Builder().add("client_id", Constants.MICROSOFT_LOGIN_CLIENT_ID)
                .add("code", code).add("grant_type", "authorization_code")
                .add("redirect_uri", Constants.MICROSOFT_LOGIN_REDIRECT_URL)
                .add("scope", String.join(" ", Constants.MICROSOFT_LOGIN_SCOPES)).build();

        OauthTokenResponse oauthTokenResponse = Download.build().setUrl(Constants.MICROSOFT_AUTH_TOKEN_URL)
                .header("Content-Type", "application/x-www-form-urlencoded").post(data)
                .asClass(OauthTokenResponse.class);

        return oauthTokenResponse;
    }

    public static OauthTokenResponse refreshAccessToken(String refreshToken) {
        RequestBody data = new FormBody.Builder().add("client_id", Constants.MICROSOFT_LOGIN_CLIENT_ID)
                .add("refresh_token", refreshToken).add("grant_type", "refresh_token")
                .add("redirect_uri", Constants.MICROSOFT_LOGIN_REDIRECT_URL).build();

        OauthTokenResponse oauthTokenResponse = Download.build().setUrl(Constants.MICROSOFT_AUTH_TOKEN_URL)
                .header("Content-Type", "application/x-www-form-urlencoded").post(data)
                .asClass(OauthTokenResponse.class);

        return oauthTokenResponse;
    }

    public static XboxLiveAuthResponse getXBLToken(String accessToken) {
        Map<Object, Object> data = Map.of("Properties",
                Map.of("AuthMethod", "RPS", "SiteName", "user.auth.xboxlive.com", "RpsTicket", "d=" + accessToken),
                "RelyingParty", "http://auth.xboxlive.com", "TokenType", "JWT");

        XboxLiveAuthResponse xblAuthResponse = Download.build().setUrl(Constants.MICROSOFT_XBL_AUTH_TOKEN_URL)
                .header("Content-Type", "application/json").header("Accept", "application/json")
                .header("x-xbl-contract-version", "1")
                .post(RequestBody.create(Gsons.DEFAULT.toJson(data), MediaType.get("application/json; charset=utf-8")))
                .asClass(XboxLiveAuthResponse.class);

        return xblAuthResponse;
    }

    public static XboxLiveAuthResponse getXstsToken(String xblToken) {
        Map<Object, Object> data = Map.of("Properties", Map.of("SandboxId", "RETAIL", "UserTokens", List.of(xblToken)),
                "RelyingParty", "rp://api.minecraftservices.com/", "TokenType", "JWT");

        XboxLiveAuthResponse xstsAuthResponse = Download.build().setUrl(Constants.MICROSOFT_XSTS_AUTH_TOKEN_URL)
                .header("Content-Type", "application/json").header("Accept", "application/json")
                .header("x-xbl-contract-version", "1")
                .post(RequestBody.create(Gsons.DEFAULT.toJson(data), MediaType.get("application/json; charset=utf-8")))
                .asClass(XboxLiveAuthResponse.class);

        return xstsAuthResponse;
    }

    public static LoginResponse loginToMinecraft(String xstsToken) {
        LoginResponse loginResponse = Download.build().setUrl(Constants.MICROSOFT_MINECRAFT_LOGIN_URL)
                .header("Content-Type", "application/json").header("Accept", "application/json")
                .post(RequestBody.create(Gsons.DEFAULT.toJson(Map.of("identityToken", xstsToken)),
                        MediaType.get("application/json; charset=utf-8")))
                .asClass(LoginResponse.class);

        return loginResponse;
    }

    public static Store getMcEntitlements(String accessToken) {
        Store store = Download.build().setUrl(Constants.MICROSOFT_MINECRAFT_STORE_URL)
                .header("Authorization", "Bearer " + accessToken).asClass(Store.class);

        return store;
    }

    public static Profile getMcProfile(String accessToken) {
        Profile profile = Download.build().setUrl(Constants.MICROSOFT_MINECRAFT_PROFILE_URL)
                .header("Authorization", "Bearer " + accessToken).asClass(Profile.class);

        return profile;
    }
}

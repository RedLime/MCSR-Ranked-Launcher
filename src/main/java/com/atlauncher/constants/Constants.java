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
package com.atlauncher.constants;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.atlauncher.App;
import com.atlauncher.data.LauncherVersion;
import com.atlauncher.utils.OS;

public class Constants {
    static {
        String versionFromFile = new BufferedReader(
                new InputStreamReader(App.class.getResourceAsStream("/version"), StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("")).trim();
        String[] versionParts = versionFromFile.split("\\.", 4);

        String stream = "Release";

        if (versionParts[3].endsWith(".Beta")) {
            versionParts[3] = versionParts[3].replace(".Beta", "");
            stream = "Beta";
        }

        VERSION = new LauncherVersion(Integer.parseInt(versionParts[0]), Integer.parseInt(versionParts[1]),
                Integer.parseInt(versionParts[2]), Integer.parseInt(versionParts[3]), stream,
                OS.getRunningProgramHashCode());
    }

    // Launcher config
    public static final LauncherVersion VERSION;
    public static final String LAUNCHER_NAME = "MCSR Ranked Launcher";
    public static final String LAUNCHER_WEBSITE = "https://mcsrranked.com";
    public static final String DEFAULT_THEME_CLASS = "com.atlauncher.themes.Dark";
    public static final String DISCORD_CLIENT_ID = "589393213723246592";
    public static final String GA_TRACKING_ID = "UA-88820616-7";
    // Launcher domains, endpoints, etc
    public static String BASE_LAUNCHER_PROTOCOL = "https://";
    public static String BASE_LAUNCHER_DOMAIN = "mcsrranked.com";

    // CDN domains, endpoints, etc
    public static String BASE_CDN_PROTOCOL = "https://";
    public static String BASE_CDN_DOMAIN = "mcsr-ranked.github.io";
    public static String BASE_CDN_PATH = "/meta/v1";
    public static String DOWNLOAD_SERVER = BASE_CDN_PROTOCOL + BASE_CDN_DOMAIN + BASE_CDN_PATH;
    public static String DOWNLOAD_HOST = BASE_CDN_DOMAIN;

    public static String LAUNCHER_UPDATE_URL = "https://github.com/RedLime/MCSR-Ranked-Launcher/releases/latest";


    // Modrinth domains, endpoints, config, etc
    public static final String MODRINTH_API_URL = "https://api.modrinth.com/v2";
    public static final String MODRINTH_HOST = "api.modrinth.com";
    public static final String MODRINTH_FABRIC_MOD_ID = "P7dR8mSH";
    public static final String MODRINTH_LEGACY_FABRIC_MOD_ID = "9CJED7xi";
    public static final String MODRINTH_QSL_MOD_ID = "qvIfYCYJ";
    public static final int MODRINTH_PAGINATION_SIZE = 20;

    // Fabric domains, endpoints, etc
    public static final String FABRIC_MAVEN = "https://maven.fabricmc.net/";
    public static final String FABRIC_HOST = "maven.fabricmc.net";

    // Legacy Fabric domains, endpoints, etc
    public static final String LEGACY_FABRIC_MAVEN = "https://maven.legacyfabric.net/";
    public static final String LEGACY_FABRIC_HOST = "maven.legacyfabric.net";

    // Quilt domains, endpoints, etc
    public static final String QUILT_MAVEN = "https://maven.quiltmc.org/repository/release/";
    public static final String QUILT_HOST = "maven.quiltmc.org";

    // Minecraft domains, endpoints, etc
    public static final String LAUNCHER_META_MINECRAFT = "https://launchermeta.mojang.com";
    public static final String MINECRAFT_LIBRARIES = "https://libraries.minecraft.net/";
    public static final String MINECRAFT_RESOURCES = "https://resources.download.minecraft.net";
    public static final String MINECRAFT_VERSION_MANIFEST_URL = LAUNCHER_META_MINECRAFT
            + "/mc/game/version_manifest.json";
    public static final String MINECRAFT_JAVA_RUNTIME_URL = LAUNCHER_META_MINECRAFT
            + "/v1/products/java-runtime/2ec0cc96c44e5a76b9c8b7c39df7210883d12871/all.json";

    // Misc
    public static final String LEGACY_JAVA_FIXER_URL = "https://cdn.atlcdn.net/legacyjavafixer-1.0.jar";
    public static final String LEGACY_JAVA_FIXER_MD5 = "12c337cb2445b56b097e7c25a5642710";
    public static final String[] DATE_FORMATS = { "dd/MM/yyyy", "MM/dd/yyyy", "yyyy/MM/dd", "dd MMMM yyyy",
            "dd-MM-yyyy", "MM-dd-yyyy", "yyyy-MM-dd" };
    // instance name, pack name, pack version, minecraft version
    public static final String[] INSTANCE_TITLE_FORMATS = { "%1$s (%2$s %3$s)", "%1$s", "%1$s (%4$s)", "%1$s (%3$s)" };
    public static final String[] SCREEN_RESOLUTIONS = { "854x480", "1280x720", "1366x768", "1600x900", "1920x1080",
            "2560x1440", "3440x1440", "3840x2160" };
    public static final String DEFAULT_JAVA_PARAMETERS = "-XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=32M";

    // Custom for ATLauncher Microsoft login constants
    // if you fork or modify this launcher, you must not use this Client ID
    public static final String MICROSOFT_LOGIN_CLIENT_ID = "9c60df0c-d89b-4106-a100-156cd239e819";
    public static final int MICROSOFT_LOGIN_REDIRECT_PORT = 28562;
    public static final String MICROSOFT_LOGIN_REDIRECT_URL = "http://localhost:" + MICROSOFT_LOGIN_REDIRECT_PORT;
    public static final String MICROSOFT_LOGIN_REDIRECT_URL_ENCODED = "http%3A%2F%2Flocalhost%3A" + MICROSOFT_LOGIN_REDIRECT_PORT;
    public static final String[] MICROSOFT_LOGIN_SCOPES = { "XboxLive.signin", "XboxLive.offline_access" };

    // General Microsoft login constants
    public static final String MICROSOFT_LOGIN_URL = "https://login.live.com/oauth20_authorize.srf"
            + "?client_id=" + MICROSOFT_LOGIN_CLIENT_ID
            + "&prompt=select_account"
            + "&cobrandid=8058f65d-ce06-4c30-9559-473c9275a65d"
            + "&response_type=code"
            + "&scope=" + String.join("%20", MICROSOFT_LOGIN_SCOPES)
            + "&redirect_uri=" + MICROSOFT_LOGIN_REDIRECT_URL_ENCODED;
    public static final String MICROSOFT_DEVICE_CODE_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/devicecode";
    public static final String MICROSOFT_DEVICE_TOKEN_URL = "https://login.microsoftonline.com/consumers/oauth2/v2.0/token";
    public static final String MICROSOFT_AUTH_TOKEN_URL = "https://login.live.com/oauth20_token.srf";
    public static final String MICROSOFT_XBL_AUTH_TOKEN_URL = "https://user.auth.xboxlive.com/user/authenticate";
    public static final String MICROSOFT_XSTS_AUTH_TOKEN_URL = "https://xsts.auth.xboxlive.com/xsts/authorize";
    public static final String MICROSOFT_MINECRAFT_LOGIN_URL = "https://api.minecraftservices.com/launcher/login";
    public static final String MICROSOFT_MINECRAFT_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile";
    public static final String MICROSOFT_MINECRAFT_ENTITLEMENTS_URL = "https://api.minecraftservices.com/entitlements/license";

    public static void setBaseLauncherDomain(String baseLauncherDomain) {
        String host = baseLauncherDomain.replace("https://", "").replace("http://", "");

        BASE_LAUNCHER_PROTOCOL = baseLauncherDomain.startsWith("https://") ? "https://" : "http://";
        BASE_LAUNCHER_DOMAIN = host;
    }

    public static void setBaseCdnDomain(String baseCdnDomain) {
        String host = baseCdnDomain.replace("https://", "").replace("http://", "");

        BASE_CDN_PROTOCOL = baseCdnDomain.startsWith("https://") ? "https://" : "http://";
        BASE_CDN_DOMAIN = host;
        DOWNLOAD_SERVER = baseCdnDomain + BASE_CDN_PATH;
        DOWNLOAD_HOST = host;
    }

    public static void setBaseCdnPath(String baseCdnPath) {
        BASE_CDN_PATH = baseCdnPath;
        DOWNLOAD_SERVER = BASE_CDN_PROTOCOL + BASE_CDN_DOMAIN + baseCdnPath;
    }
}

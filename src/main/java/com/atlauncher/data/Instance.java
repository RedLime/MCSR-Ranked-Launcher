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
package com.atlauncher.data;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.App;
import com.atlauncher.Data;
import com.atlauncher.FileSystem;
import com.atlauncher.Gsons;
import com.atlauncher.Network;
import com.atlauncher.annot.Json;
import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.Constants;
import com.atlauncher.data.installables.Installable;
import com.atlauncher.data.installables.VanillaInstallable;
import com.atlauncher.data.minecraft.AssetIndex;
import com.atlauncher.data.minecraft.JavaRuntime;
import com.atlauncher.data.minecraft.JavaRuntimeManifest;
import com.atlauncher.data.minecraft.JavaRuntimeManifestFileType;
import com.atlauncher.data.minecraft.JavaRuntimes;
import com.atlauncher.data.minecraft.LWJGLLibrary;
import com.atlauncher.data.minecraft.Library;
import com.atlauncher.data.minecraft.LoggingFile;
import com.atlauncher.data.minecraft.MinecraftVersion;
import com.atlauncher.data.minecraft.MojangAssetIndex;
import com.atlauncher.data.minecraft.VersionManifestVersion;
import com.atlauncher.data.minecraft.VersionManifestVersionType;
import com.atlauncher.data.minecraft.loaders.LoaderType;
import com.atlauncher.data.minecraft.loaders.LoaderVersion;
import com.atlauncher.data.minecraft.loaders.fabric.FabricLoader;
import com.atlauncher.data.minecraft.loaders.legacyfabric.LegacyFabricLoader;
import com.atlauncher.data.minecraft.loaders.quilt.QuiltLoader;
import com.atlauncher.data.modcheck.ModCheckProject;
import com.atlauncher.data.modrinth.ModrinthFile;
import com.atlauncher.data.modrinth.ModrinthProject;
import com.atlauncher.data.modrinth.ModrinthVersion;
import com.atlauncher.exceptions.CommandException;
import com.atlauncher.exceptions.InvalidMinecraftVersion;
import com.atlauncher.gui.LauncherFrame;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.dialogs.RenameInstanceDialog;
import com.atlauncher.managers.AccountManager;
import com.atlauncher.managers.DialogManager;
import com.atlauncher.managers.InstanceManager;
import com.atlauncher.managers.LWJGLManager;
import com.atlauncher.managers.LogManager;
import com.atlauncher.managers.MinecraftManager;
import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.mclauncher.MCLauncher;
import com.atlauncher.network.DownloadPool;
import com.atlauncher.utils.ArchiveUtils;
import com.atlauncher.utils.ComboItem;
import com.atlauncher.utils.CommandExecutor;
import com.atlauncher.utils.FileUtils;
import com.atlauncher.utils.Hashing;
import com.atlauncher.utils.Java;
import com.atlauncher.utils.ModrinthApi;
import com.atlauncher.utils.OS;
import com.atlauncher.utils.Utils;
import com.atlauncher.utils.ZipNameMapper;
import com.google.common.collect.Sets;
import com.google.gson.JsonIOException;

import okhttp3.OkHttpClient;

@Json
public class Instance extends MinecraftVersion {

    public static final Set<String> AUTO_ENABLE_MODS = Sets.newHashSet("Sodium", "Lithium", "Fast Reset", "LazyDFU", "Starlight", "Voyager", "Force Port", "LazyStronghold", "antiresourcereload");

    public String inheritsFrom;
    public InstanceLauncher launcher;

    public transient Path ROOT;

    /**
     * @deprecated moved within launcher property
     */
    public transient Instant lastPlayed;

    /**
     * @deprecated moved within launcher property
     */
    public transient long numPlays;

    public Instance(MinecraftVersion version) {
        setValues(version);
    }

    public void setValues(MinecraftVersion version) {
        this.id = version.id;
        this.libraries = version.libraries;
        this.lwjglVersions = version.lwjglVersions;
        this.mainClass = version.mainClass;
        this.minecraftArguments = version.minecraftArguments;
        this.arguments = version.arguments;
        setUpdatedValues(version);
    }

    public void setUpdatedValues(MinecraftVersion version) {
        this.complianceLevel = version.complianceLevel;
        this.javaVersion = version.javaVersion;
        this.type = version.type;
        this.time = version.time;
        this.releaseTime = version.releaseTime;
        this.minimumLauncherVersion = version.minimumLauncherVersion;
        this.assetIndex = version.assetIndex;
        this.assets = version.assets;
        this.downloads = version.downloads;
        this.rules = version.rules;
        this.logging = version.logging;
    }

    public String getSafeName() {
        return this.launcher.name.replaceAll("[^A-Za-z0-9]", "");
    }

    public String getSafePackName() {
        return this.launcher.pack.replaceAll("[^A-Za-z0-9]", "");
    }

    public Path getRoot() {
        return this.ROOT;
    }

    public Pack getPack() {
        return null;
    }

    public boolean hasUpdate() {
        if (launcher.vanillaInstance) {
            // must be reinstalled
            return false;
        } else {
            Pack pack = this.getPack();

            if (pack != null) {
                if (pack.hasVersions() && !this.launcher.isDev) {
                    // Lastly check if the current version we installed is different than the latest
                    // version of the Pack and that the latest version of the Pack is not restricted
                    // to disallow updates.
                    if (!pack.getLatestVersion().version.equalsIgnoreCase(this.launcher.version)
                            && !pack.isLatestVersionNoUpdate()) {
                        return true;
                    }
                }

                if (this.launcher.isDev && (this.launcher.hash != null)) {
                    PackVersion devVersion = pack.getDevVersionByName(this.launcher.version);
                    if (devVersion != null && !devVersion.hashMatches(this.launcher.hash)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public PackVersion getLatestVersion() {
        Pack pack = this.getPack();

        if (pack != null) {
            if (pack.hasVersions() && !this.launcher.isDev) {
                return pack.getLatestVersion();
            }

            if (this.launcher.isDev) {
                return pack.getLatestDevVersion();
            }
        }

        return null;
    }

    public String getPackDescription() {
        Pack pack = this.getPack();

        if (pack != null) {
            return pack.description;
        } else {
            if (launcher.description != null) {
                return launcher.description;
            }

            return GetText.tr("No Description");
        }
    }

    private boolean hasCustomImage() {
        File customImage = this.getRoot().resolve("instance.png").toFile();

        return customImage.exists();
    }

    public ImageIcon getImage() {
        File customImage = this.getRoot().resolve("instance.png").toFile();

        if (customImage.exists()) {
            try {
                BufferedImage img = ImageIO.read(customImage);
                if (img != null) {
                    // if a square image, then make it 300x150 (without stretching) centered
                    if (img.getHeight(null) == img.getWidth(null)) {
                        BufferedImage dimg = new BufferedImage(300, 150, BufferedImage.TYPE_INT_ARGB);

                        Graphics2D g2d = dimg.createGraphics();
                        g2d.drawImage(img, 75, 0, 150, 150, null);
                        g2d.dispose();

                        return new ImageIcon(dimg);
                    }

                    return new ImageIcon(img.getScaledInstance(300, 150, Image.SCALE_SMOOTH));
                }
            } catch (IIOException e) {
                LogManager.warn("Error creating scaled image from the custom image of instance " + this.launcher.name
                        + ". Using default image.");
            } catch (Exception e) {
                LogManager.logStackTrace(
                        "Error creating scaled image from the custom image of instance " + this.launcher.name, e,
                        false);
            }
        }

        if (getPack() != null) {
            File instancesImage = FileSystem.IMAGES.resolve(this.getSafePackName().toLowerCase() + ".png").toFile();

            if (instancesImage.exists()) {
                return Utils.getIconImage(instancesImage);
            }
        }

        return Utils.getIconImage("/assets/image/default-image.png");
    }

    public void ignoreUpdate() {
        String version;

        if (launcher.vanillaInstance) {
            return;
        } else {
            if (this.launcher.isDev) {
                version = getLatestVersion().hash;
            } else {
                version = getLatestVersion().version;
            }
        }

        if (!hasUpdateBeenIgnored(version)) {
            this.launcher.ignoredUpdates.add(version);
            this.save();
        }
    }

    public void ignoreAllUpdates() {
        this.launcher.ignoreAllUpdates = true;
        this.save();
    }

    public boolean hasLatestUpdateBeenIgnored() {
        if (launcher.vanillaInstance) {
            return false;
        }

        if (isExternalPack()) {
            return false;
        }

        String version;

        if (this.launcher.isDev) {
            version = getLatestVersion().hash;
        } else {
            version = getLatestVersion().version;
        }

        return hasUpdateBeenIgnored(version);
    }

    private boolean hasUpdateBeenIgnored(String version) {
        if (this.launcher.ignoreAllUpdates) {
            return true;
        }

        if (version == null || this.launcher.ignoredUpdates.size() == 0) {
            return false;
        }

        return this.launcher.ignoredUpdates.stream().anyMatch(v -> v.equalsIgnoreCase(version));
    }

    public Path getMinecraftJarLibraryPath() {
        return FileSystem.LIBRARIES.resolve(String.format("net/minecraft/client/%1$s/client-%1$s.jar", this.id));
    }

    public Path getCustomMinecraftJarLibraryPath() {
        return ROOT.resolve("bin/minecraft.jar");
    }

    /**
     * This will prepare the instance for launch. It will download the assets,
     * Minecraft jar and libraries, as well as organise the libraries, ready to be
     * played.
     */
    public boolean prepareForLaunch(ProgressDialog progressDialog, Path nativesTempDir, Path lwjglNativesTempDir) {
        PerformanceManager.start();
        OkHttpClient httpClient = Network.createProgressClient(progressDialog);

        // make sure latest manifest is being used
        PerformanceManager.start("Grabbing Latest Manifest");
        try {
            progressDialog.setLabel(GetText.tr("Grabbing Latest Manifest"));
            VersionManifestVersion minecraftVersionManifest = MinecraftManager
                    .getMinecraftVersion(id);

            com.atlauncher.network.Download download = com.atlauncher.network.Download.build()
                    .setUrl(minecraftVersionManifest.url).hash(minecraftVersionManifest.sha1)
                    .size(minecraftVersionManifest.size)
                    .downloadTo(FileSystem.MINECRAFT_VERSIONS_JSON.resolve(minecraftVersionManifest.id + ".json"))
                    .withHttpClient(httpClient);

            MinecraftVersion minecraftVersion = download.asClass(MinecraftVersion.class);

            if (minecraftVersion != null) {
                setUpdatedValues(minecraftVersion);
                save();
            }
        } catch (Exception e) {
            // ignored
        }
        progressDialog.doneTask();
        PerformanceManager.end("Grabbing Latest Manifest");

        PerformanceManager.start("Downloading Minecraft");
        try {
            progressDialog.setLabel(GetText.tr("Downloading Minecraft"));
            com.atlauncher.network.Download clientDownload = com.atlauncher.network.Download.build()
                    .setUrl(this.downloads.client.url).hash(this.downloads.client.sha1).size(this.downloads.client.size)
                    .withHttpClient(httpClient).downloadTo(this.getMinecraftJarLibraryPath());

            if (clientDownload.needToDownload()) {
                progressDialog.setTotalBytes(this.downloads.client.size);
                clientDownload.downloadFile();
            }

            progressDialog.doneTask();
        } catch (IOException e) {
            LogManager.logStackTrace(e);
            PerformanceManager.end("Downloading Minecraft");
            PerformanceManager.end();
            return false;
        }
        PerformanceManager.end("Downloading Minecraft");

        if (logging != null) {
            PerformanceManager.start("Downloading Logging Config");
            try {
                progressDialog.setLabel(GetText.tr("Downloading Logging Config"));

                LoggingFile loggingFile = logging.client.file;

                com.atlauncher.network.Download loggerDownload = com.atlauncher.network.Download.build()
                        .setUrl(loggingFile.url).hash(loggingFile.sha1)
                        .size(loggingFile.size).downloadTo(FileSystem.RESOURCES_LOG_CONFIGS.resolve(loggingFile.id))
                        .withHttpClient(httpClient);

                if (loggerDownload.needToDownload()) {
                    progressDialog.setTotalBytes(loggingFile.size);
                    loggerDownload.downloadFile();
                }

                progressDialog.doneTask();
            } catch (IOException e) {
                LogManager.logStackTrace(e);
                PerformanceManager.end("Downloading Logging Config");
                PerformanceManager.end();
                return false;
            }
            PerformanceManager.end("Downloading Logging Config");
        } else {
            progressDialog.doneTask();
        }

        // download libraries
        PerformanceManager.start("Downloading Libraries");
        progressDialog.setLabel(GetText.tr("Downloading Libraries"));
        DownloadPool librariesPool = new DownloadPool();

        List<Library> totalLibraries = new ArrayList<>(this.libraries);
        totalLibraries.addAll(LWJGLManager.getLWJGLLibraries(this.launcher.lwjglVersion));

        totalLibraries.stream()
                .filter(library -> library.shouldInstall() && library.downloads.artifact != null && library.downloads.artifact.url != null)
                .distinct()
                .forEach(library -> {
                    com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                            .setUrl(library.downloads.artifact.url)
                            .downloadTo(FileSystem.LIBRARIES.resolve(library.downloads.artifact.path))
                            .hash(library.downloads.artifact.sha1).size(library.downloads.artifact.size)
                            .withHttpClient(httpClient);

                    librariesPool.add(download);
                });

        totalLibraries.stream().filter(Library::hasNativeForOS)
                .forEach(library -> {
                    com.atlauncher.data.minecraft.Download download = library.getNativeDownloadForOS();

                    librariesPool.add(new com.atlauncher.network.Download().setUrl(download.url)
                            .downloadTo(FileSystem.LIBRARIES.resolve(download.path)).hash(download.sha1)
                            .size(download.size)
                            .withHttpClient(httpClient));
                });

        DownloadPool smallLibrariesPool = librariesPool.downsize();

        progressDialog.setTotalBytes(smallLibrariesPool.totalSize());

        smallLibrariesPool.downloadAll();

        progressDialog.doneTask();
        PerformanceManager.end("Downloading Libraries");

        // download Java runtime
        PerformanceManager.start("Java Runtime");
        if (javaVersion != null && Data.JAVA_RUNTIMES != null && Optional
                .ofNullable(launcher.useJavaProvidedByMinecraft).orElse(App.settings.useJavaProvidedByMinecraft)) {
            Map<String, List<JavaRuntime>> runtimesForSystem = Data.JAVA_RUNTIMES.getForSystem();
            String runtimeSystemString = JavaRuntimes.getSystem();

            String runtimeToUse = Optional.ofNullable(launcher.javaRuntimeOverride).orElse(javaVersion.component);

            if (runtimesForSystem.containsKey(runtimeToUse)
                    && runtimesForSystem.get(runtimeToUse).size() != 0) {
                // #. {0} is the version of Java were downloading
                progressDialog.setLabel(GetText.tr("Downloading Java Runtime {0}",
                        runtimesForSystem.get(runtimeToUse).get(0).version.name));

                JavaRuntime runtimeToDownload = runtimesForSystem.get(runtimeToUse).get(0);

                try {
                    JavaRuntimeManifest javaRuntimeManifest = com.atlauncher.network.Download.build()
                            .setUrl(runtimeToDownload.manifest.url).size(runtimeToDownload.manifest.size)
                            .hash(runtimeToDownload.manifest.sha1).downloadTo(FileSystem.MINECRAFT_RUNTIMES
                                    .resolve(runtimeToUse).resolve("manifest.json"))
                            .asClassWithThrow(JavaRuntimeManifest.class);

                    DownloadPool pool = new DownloadPool();

                    // create root directory
                    Path runtimeSystemDirectory = FileSystem.MINECRAFT_RUNTIMES.resolve(runtimeToUse)
                            .resolve(runtimeSystemString);
                    Path runtimeDirectory = runtimeSystemDirectory.resolve(runtimeToUse);
                    FileUtils.createDirectory(runtimeDirectory);

                    // create all the directories
                    javaRuntimeManifest.files.forEach((key, file) -> {
                        if (file.type == JavaRuntimeManifestFileType.DIRECTORY) {
                            FileUtils.createDirectory(runtimeDirectory.resolve(key));
                        }
                    });

                    // collect the files we need to download
                    javaRuntimeManifest.files.forEach((key, file) -> {
                        if (file.type == JavaRuntimeManifestFileType.FILE) {
                            com.atlauncher.network.Download download = new com.atlauncher.network.Download()
                                    .setUrl(file.downloads.raw.url).downloadTo(runtimeDirectory.resolve(key))
                                    .hash(file.downloads.raw.sha1).size(file.downloads.raw.size)
                                    .executable(file.executable).withHttpClient(httpClient);

                            pool.add(download);
                        }
                    });

                    DownloadPool smallPool = pool.downsize();

                    progressDialog.setTotalBytes(smallPool.totalSize());

                    smallPool.downloadAll();

                    // write out the version file (theres also a .sha1 file created, but we're not
                    // doing that)
                    Files.write(runtimeSystemDirectory.resolve(".version"),
                            runtimeToDownload.version.name.getBytes(StandardCharsets.UTF_8));
                    // Files.write(runtimeSystemDirectory.resolve(runtimeToUse
                    // + ".sha1"), runtimeToDownload.version.name.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    LogManager.logStackTrace("Failed to download Java runtime", e);
                }
            }
        }
        progressDialog.doneTask();
        PerformanceManager.end("Java Runtime");

        // organise assets
        PerformanceManager.start("Organising Resources 1");
        progressDialog.setLabel(GetText.tr("Organising Resources"));
        MojangAssetIndex assetIndex = this.assetIndex;

        AssetIndex index = com.atlauncher.network.Download.build().setUrl(assetIndex.url).hash(assetIndex.sha1)
                .size(assetIndex.size).downloadTo(FileSystem.RESOURCES_INDEXES.resolve(assetIndex.id + ".json"))
                .withHttpClient(httpClient).asClass(AssetIndex.class);

        DownloadPool pool = new DownloadPool();

        index.objects.forEach((key, object) -> {
            String filename = object.hash.substring(0, 2) + "/" + object.hash;
            String url = String.format("%s/%s", Constants.MINECRAFT_RESOURCES, filename);

            com.atlauncher.network.Download download = new com.atlauncher.network.Download().setUrl(url)
                    .downloadTo(FileSystem.RESOURCES_OBJECTS.resolve(filename)).hash(object.hash).size(object.size)
                    .withHttpClient(httpClient);

            pool.add(download);
        });

        DownloadPool smallPool = pool.downsize();

        if (smallPool.size() != 0) {
            progressDialog.setLabel(GetText.tr("Downloading Resources"));

            progressDialog.setTotalBytes(smallPool.totalSize());

            smallPool.downloadAll();
        }
        PerformanceManager.end("Organising Resources 1");

        // copy resources to instance
        if (index.mapToResources || assetIndex.id.equalsIgnoreCase("legacy")) {
            PerformanceManager.start("Organising Resources 2");
            progressDialog.setLabel(GetText.tr("Organising Resources"));

            index.objects.forEach((key, object) -> {
                String filename = object.hash.substring(0, 2) + "/" + object.hash;

                Path downloadedFile = FileSystem.RESOURCES_OBJECTS.resolve(filename);
                Path assetPath = index.mapToResources ? this.ROOT.resolve("resources/" + key)
                        : FileSystem.RESOURCES_VIRTUAL_LEGACY.resolve(key);

                if (!Files.exists(assetPath)) {
                    FileUtils.copyFile(downloadedFile, assetPath, true);
                }
            });
            PerformanceManager.end("Organising Resources 2");
        }

        progressDialog.doneTask();

        progressDialog.setLabel(GetText.tr("Organising Libraries"));

        // extract natives to a temp dir
        PerformanceManager.start("Extracting Natives");
        boolean useSystemGlfw = Optional.ofNullable(launcher.useSystemGlfw).orElse(App.settings.useSystemGlfw);
        boolean useSystemOpenAl = Optional.ofNullable(launcher.useSystemOpenAl).orElse(App.settings.useSystemOpenAl);
        totalLibraries.stream().filter(Library::shouldInstall)
                .forEach(library -> {
                    if (library.hasNativeForOS()) {
                        if (library.name.contains("glfw") && useSystemGlfw) {
                            LogManager.warn("useSystemGlfw was enabled, not using glfw natives from Minecraft");
                            return;
                        }

                        if (library.name.contains("openal") && useSystemOpenAl) {
                            LogManager.warn("useSystemOpenAl was enabled, not using openal natives from Minecraft");
                            return;
                        }

                        Path nativePath = FileSystem.LIBRARIES.resolve(library.getNativeDownloadForOS().path);

                        ArchiveUtils.extract(nativePath, nativesTempDir, name -> {
                            if (library.extract != null && library.extract.shouldExclude(name)) {
                                return null;
                            }

                            // keep META-INF folder as per normal
                            if (name.startsWith("META-INF")) {
                                return name;
                            }

                            // don't extract folders
                            if (name.endsWith("/")) {
                                return null;
                            }

                            // if it has a / then extract just to root
                            if (name.contains("/")) {
                                return name.substring(name.lastIndexOf("/") + 1);
                            }

                            return name;
                        });
                    }
                });

        progressDialog.doneTask();
        PerformanceManager.end("Extracting Natives");

        if (usesCustomMinecraftJar()) {
            PerformanceManager.start("Creating custom minecraft.jar");
            progressDialog.setLabel(GetText.tr("Creating custom minecraft.jar"));

            if (Files.exists(getCustomMinecraftJarLibraryPath())) {
                FileUtils.delete(getCustomMinecraftJarLibraryPath());
            }

            if (!Utils.combineJars(getMinecraftJar(), getRoot().resolve("bin/modpack.jar").toFile(),
                    getCustomMinecraftJar())) {
                LogManager.error("Failed to combine jars into custom minecraft.jar");
                PerformanceManager.end("Creating custom minecraft.jar");
                PerformanceManager.end();
                return false;
            }
            PerformanceManager.end("Creating custom minecraft.jar");
        }

        progressDialog.doneTask();

        PerformanceManager.end();
        return true;
    }

    public boolean launch() {
        return launch(false);
    }

    public boolean launch(boolean offline) {
        final AbstractAccount account = launcher.account == null ? AccountManager.getSelectedAccount()
                : AccountManager.getAccountByName(launcher.account);

        if (account == null) {
            int ret = DialogManager.yesNoDialog(false).setTitle(GetText.tr("No Account Selected"))
                .setContent(GetText.tr("Cannot play instance as you have no account selected. do you want to login with your account?"))
                .setType(DialogManager.ERROR).show();

            if (ret == DialogManager.YES_OPTION) {
                LauncherFrame.getInstance().openTab(3);
            }

            App.launcher.setMinecraftLaunched(false);
            return false;
        }

        // if Microsoft account must login again, then make sure to do that
        if (!offline && account instanceof MicrosoftAccount && ((MicrosoftAccount) account).mustLogin) {
            if (!((MicrosoftAccount) account).ensureAccountIsLoggedIn()) {
                LogManager.info("You must login to your account before continuing.");
                return false;
            }
        }

        String playerName = account.minecraftUsername;

        if (offline) {
            playerName = DialogManager.okDialog().setTitle(GetText.tr("Offline Player Name"))
                    .setContent(GetText.tr("Choose your offline player name:")).showInput(playerName);

            if (playerName == null || playerName.isEmpty()) {
                LogManager.info("No player name provided for offline launch, so cancelling launch.");
                return false;
            }
        }

        final String username = offline ? playerName : account.minecraftUsername;

        int maximumMemory = (this.launcher.maximumMemory == null) ? App.settings.maximumMemory
                : this.launcher.maximumMemory;
        if ((maximumMemory < this.launcher.requiredMemory)
                && (this.launcher.requiredMemory <= OS.getSafeMaximumRam())) {
            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Insufficient Ram"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "This pack has set a minimum amount of ram needed to <b>{0}</b> MB.<br/><br/>Do you want to continue loading the instance anyway?",
                            this.launcher.requiredMemory)).build())
                    .setType(DialogManager.ERROR).show();

            if (ret != 0) {
                LogManager.warn("Launching of instance cancelled due to user cancelling memory warning!");
                App.launcher.setMinecraftLaunched(false);
                return false;
            }
        }
        int permGen = (this.launcher.permGen == null) ? App.settings.metaspace : this.launcher.permGen;
        if (permGen < this.launcher.requiredPermGen) {
            int ret = DialogManager.yesNoDialog().setTitle(GetText.tr("Insufficent Permgen"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "This pack has set a minimum amount of permgen to <b>{0}</b> MB.<br/><br/>Do you want to continue loading the instance anyway?",
                            this.launcher.requiredPermGen)).build())
                    .setType(DialogManager.ERROR).show();
            if (ret != 0) {
                LogManager.warn("Launching of instance cancelled due to user cancelling permgen warning!");
                App.launcher.setMinecraftLaunched(false);
                return false;
            }
        }

        Path nativesTempDir = FileSystem.TEMP.resolve("natives-" + UUID.randomUUID().toString().replace("-", ""));
        Path lwjglNativesTempDir = FileSystem.TEMP
                .resolve("lwjgl-natives-" + UUID.randomUUID().toString().replace("-", ""));

        try {
            Files.createDirectory(nativesTempDir);
        } catch (IOException e2) {
            LogManager.logStackTrace(e2, false);
        }

        ProgressDialog<Boolean> prepareDialog = new ProgressDialog<>(GetText.tr("Preparing For Launch"),
                7, GetText.tr("Preparing For Launch"));
        prepareDialog.addThread(new Thread(() -> {
            LogManager.info("Preparing for launch!");
            prepareDialog.setReturnValue(prepareForLaunch(prepareDialog, nativesTempDir, lwjglNativesTempDir));
            prepareDialog.close();
        }));
        prepareDialog.start();

        if (prepareDialog.getReturnValue() == null || !prepareDialog.getReturnValue()) {
            LogManager.error(
                    "Failed to prepare instance " + this.launcher.name + " for launch. Check the logs and try again.");
            return false;
        }

        Thread launcher = new Thread(() -> {
            try {
                long start = System.currentTimeMillis();
                if (App.launcher.getParent() != null) {
                    App.launcher.getParent().setVisible(false);
                }

                LogManager.info(String.format("Launching pack %s %s for Minecraft %s", this.launcher.pack,
                    this.launcher.version, this.id));

                Process process = null;

                boolean enableCommands = Optional.ofNullable(this.launcher.enableCommands)
                        .orElse(App.settings.enableCommands);
                String preLaunchCommand = Optional.ofNullable(this.launcher.preLaunchCommand)
                        .orElse(App.settings.preLaunchCommand);
                String postExitCommand = Optional.ofNullable(this.launcher.postExitCommand)
                        .orElse(App.settings.postExitCommand);
                String wrapperCommand = Optional.ofNullable(this.launcher.wrapperCommand)
                        .orElse(App.settings.wrapperCommand);
                if (!enableCommands) {
                    wrapperCommand = null;
                }

                if (account instanceof MicrosoftAccount) {
                    MicrosoftAccount microsoftAccount = (MicrosoftAccount) account;

                    if (!offline) {
                        LogManager.info("Logging into Minecraft!");
                        ProgressDialog<Boolean> loginDialog = new ProgressDialog<>(GetText.tr("Logging Into Minecraft"),
                                0, GetText.tr("Logging Into Minecraft"), "Aborted login to Minecraft!");
                        loginDialog.addThread(new Thread(() -> {
                            loginDialog.setReturnValue(microsoftAccount.ensureAccessTokenValid());
                            loginDialog.close();
                        }));
                        loginDialog.start();

                        if (!(Boolean) loginDialog.getReturnValue()) {
                            LogManager.error("Failed to login");
                            App.launcher.setMinecraftLaunched(false);
                            if (App.launcher.getParent() != null) {
                                App.launcher.getParent().setVisible(true);
                            }
                            DialogManager.okDialog().setTitle(GetText.tr("Error Logging In"))
                                    .setContent(GetText.tr("Couldn't login with Microsoft account"))
                                    .setType(DialogManager.ERROR).show();
                            return;
                        }
                    }

                    if (enableCommands && preLaunchCommand != null) {
                        if (!executeCommand(preLaunchCommand)) {
                            LogManager.error("Failed to execute pre-launch command");

                            App.launcher.setMinecraftLaunched(false);

                            if (App.launcher.getParent() != null) {
                                App.launcher.getParent().setVisible(true);
                            }

                            return;
                        }
                    }

                    process = MCLauncher.launch(microsoftAccount, this, nativesTempDir, null,
                            wrapperCommand, username);
                }

                if (process == null) {
                    LogManager.error("Failed to get process for Minecraft");
                    App.launcher.setMinecraftLaunched(false);
                    if (App.launcher.getParent() != null) {
                        App.launcher.getParent().setVisible(true);
                    }
                    return;
                }

                if ((App.autoLaunch != null && App.closeLauncher)
                        || (!App.settings.keepLauncherOpen && !App.settings.enableLogs)) {
                    System.exit(0);
                }

                App.launcher.showKillMinecraft(process);
                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(isr);
                String line;
                int detectedError = 0;

                String replaceUUID = account.uuid.replace("-", "");

                while ((line = br.readLine()) != null) {
                    if (line.contains("java.lang.OutOfMemoryError")
                            || line.contains("There is insufficient memory for the Java Runtime Environment")) {
                        detectedError = MinecraftError.OUT_OF_MEMORY;
                    }

                    if (line.contains("java.util.ConcurrentModificationException")
                            && Utils.matchVersion(this.id, "1.6", true, true)) {
                        detectedError = MinecraftError.CONCURRENT_MODIFICATION_ERROR_1_6;
                    }

                    if (line.contains(
                            "has been compiled by a more recent version of the Java Runtime (class file version 60.0)")) {
                        detectedError = MinecraftError.NEED_TO_USE_JAVA_16_OR_NEWER;
                    }

                    if (line.contains(
                            "has been compiled by a more recent version of the Java Runtime (class file version 61.0)")) {
                        detectedError = MinecraftError.NEED_TO_USE_JAVA_17_OR_NEWER;
                    }

                    if (line.contains(
                            "class jdk.internal.loader.ClassLoaders$AppClassLoader cannot be cast to class")) {
                        detectedError = MinecraftError.USING_NEWER_JAVA_THAN_8;
                    }

                    if (!LogManager.showDebug) {
                        line = line.replace(account.minecraftUsername, "**MINECRAFTUSERNAME**");
                        line = line.replace(account.username, "**MINECRAFTUSERNAME**");
                        line = line.replace(account.uuid, "**UUID**");
                        line = line.replace(replaceUUID, "**UUID**");
                    }

                    if (account.getAccessToken() != null) {
                        line = line.replace(account.getAccessToken(), "**ACCESSTOKEN**");
                    }

                    if (line.contains("log4j:")) {
                        try {
                            // start of a new event so clear string builder
                            if (line.contains("<log4j:Event>")) {
                                sb.setLength(0);
                            }

                            sb.append(line);

                            // end of the xml object so parse it
                            if (line.contains("</log4j:Event>")) {
                                LogManager.minecraftLog4j(sb.toString());
                                sb.setLength(0);
                            }

                            continue;
                        } catch (Exception e) {
                            // ignored
                        }
                    }

                    LogManager.minecraft(line);
                }
                App.launcher.hideKillMinecraft();
                if (App.launcher.getParent() != null && App.settings.keepLauncherOpen) {
                    App.launcher.getParent().setVisible(true);
                }
                long end = System.currentTimeMillis();

                int exitValue = 0; // Assume we exited fine
                try {
                    exitValue = process.exitValue(); // Try to get the real exit value
                } catch (IllegalThreadStateException e) {
                    process.destroy(); // Kill the process
                }
                if (!App.settings.keepLauncherOpen) {
                    App.console.setVisible(false); // Hide the console to pretend we've closed
                }

                if (exitValue != 0) {
                    LogManager.error(
                            "Oh no. Minecraft crashed. Please check the logs for any errors and provide these logs when asking for support.");

                    this.launcher.mods.forEach(mod -> {
                        if (!mod.userAdded && mod.wasSelected && mod.disabled) {
                            LogManager.warn("The mod " + mod.name + " (" + mod.file + ") has been disabled.");
                        }
                    });

                    Files.list(
                            this.ROOT.resolve("mods")).filter(
                            file -> Files.isRegularFile(file)
                                && this.launcher.mods.stream()
                                .noneMatch(m -> m.type == Type.mods && !m.userAdded
                                    && m.getFile(this).toPath().equals(file)))
                        .forEach(newMod -> {
                            LogManager.warn("The mod " + newMod.getFileName().toString() + " has been added.");
                        });
                }

                if (detectedError != 0) {
                    MinecraftError.showInformationPopup(detectedError);
                }

                if (enableCommands && postExitCommand != null) {
                    if (!executeCommand(postExitCommand)) {
                        LogManager.error("Failed to execute post-exit command");
                    }
                }

                App.launcher.setMinecraftLaunched(false);
                if (App.settings.enableAutomaticBackupAfterLaunch) {
                    backup();
                }
                if (App.settings.keepLauncherOpen) {
                    App.launcher.reloadInstancesPanel();
                    App.launcher.updateData();
                }
                if (Files.isDirectory(nativesTempDir)) {
                    FileUtils.deleteDirectoryQuietly(nativesTempDir);
                }
                if (Files.isDirectory(lwjglNativesTempDir)) {
                    FileUtils.deleteDirectoryQuietly(lwjglNativesTempDir);
                }
                if (usesCustomMinecraftJar() && Files.exists(getCustomMinecraftJarLibraryPath())) {
                    FileUtils.delete(getCustomMinecraftJarLibraryPath());
                }
                if (!App.settings.keepLauncherOpen) {
                    System.exit(0);
                }
            } catch (Exception e1) {
                LogManager.logStackTrace(e1);
                App.launcher.setMinecraftLaunched(false);
                if (App.launcher.getParent() != null) {
                    App.launcher.getParent().setVisible(true);
                }
            }
        });

        this.setLastPlayed(Instant.now());
        this.incrementNumberOfPlays();
        this.save();

        launcher.start();
        return true;
    }

    private boolean executeCommand(String command) {
        try {
            CommandExecutor.executeCommand(this, command);
            return true;
        } catch (CommandException e) {
            String content = GetText.tr("Error executing command");

            if (e.getMessage() != null) {
                content += ":" + System.lineSeparator() + e.getLocalizedMessage();
            }

            content += System.lineSeparator() + GetText.tr("Check the console for details");

            DialogManager.okDialog().setTitle(GetText.tr("Error executing command")).setContent(content)
                    .setType(DialogManager.ERROR).show();

            return false;
        }
    }

    public void addFileFromModrinth(ModrinthProject mod, ModrinthVersion version, ModrinthFile file,
            ProgressDialog dialog) {
        ModrinthFile fileToDownload = Optional.ofNullable(file).orElse(version.getPrimaryFile());

        Path downloadLocation = FileSystem.DOWNLOADS.resolve(fileToDownload.filename);
        Path finalLocation = this.getRoot().resolve("mods").resolve(fileToDownload.filename);
        com.atlauncher.network.Download download = com.atlauncher.network.Download.build().setUrl(fileToDownload.url)
                .downloadTo(downloadLocation).copyTo(finalLocation)
                .withHttpClient(Network.createProgressClient(dialog));

        if (fileToDownload.hashes != null && fileToDownload.hashes.containsKey("sha512")) {
            download = download.hash(fileToDownload.hashes.get("sha512"));
        } else if (fileToDownload.hashes != null && fileToDownload.hashes.containsKey("sha1")) {
            download = download.hash(fileToDownload.hashes.get("sha1"));
        }

        if (fileToDownload.size != null && fileToDownload.size != 0) {
            dialog.setTotalBytes(fileToDownload.size);
            download = download.size(fileToDownload.size);
        }

        if (Files.exists(finalLocation)) {
            FileUtils.delete(finalLocation);
        }

        // find mods with the same Modrinth id
        List<DisableableMod> sameMods = this.launcher.mods.stream().filter(
                installedMod -> installedMod.isFromModrinth()
                        && installedMod.modrinthProject.id.equalsIgnoreCase(mod.id))
                .collect(Collectors.toList());

        // delete mod files that are the same mod id
        sameMods.forEach(disableableMod -> Utils.delete(disableableMod.getFile(this)));

        if (download.needToDownload()) {
            try {
                download.downloadFile();
            } catch (IOException e) {
                LogManager.logStackTrace(e);
                DialogManager.okDialog().setType(DialogManager.ERROR).setTitle("Failed to download")
                        .setContent("Failed to download " + fileToDownload.filename + ". Please try again later.")
                        .show();
                return;
            }
        } else {
            download.copy();
        }

        // remove any mods that are from the same mod from the master mod list
        this.launcher.mods = this.launcher.mods.stream().filter(
                installedMod -> !installedMod.isFromModrinth()
                        || !installedMod.modrinthProject.id.equalsIgnoreCase(mod.id))
                .collect(Collectors.toList());

        // add this mod
        this.launcher.mods.add(new DisableableMod(mod.title, version.name, true, fileToDownload.filename, Type.mods,
                null, mod.description, false, true, true, false, mod, version));

        this.save();

        // #. {0} is the name of a mod that was installed
        App.TOASTER.pop(GetText.tr("{0} Installed", mod.title));
    }

    public void addFileFromModCheck(ModCheckProject modCheckProject, boolean forceEnable) {
        Path finalLocation = this.getRoot().resolve("mods").resolve(modCheckProject.getModFile().getName());

        if (Files.exists(finalLocation)) {
            FileUtils.delete(finalLocation);
        }

        // find mods with the same modcheck
        List<DisableableMod> sameMods = this.launcher.mods.stream().filter(
                installedMod -> installedMod.isFromModCheck()
                    && installedMod.modCheckProject.getName().equalsIgnoreCase(modCheckProject.getName()))
            .collect(Collectors.toList());

        // delete mod files that are the same mod id
        sameMods.forEach(disableableMod -> Utils.delete(disableableMod.getFile(this)));

        try {
            //noinspection ResultOfMethodCallIgnored
            finalLocation.getParent().toFile().mkdirs();
            URL url = new URL(modCheckProject.getModFile().getUrl());

            URLConnection con = url.openConnection();
            con.setRequestProperty("User-Agent", "ModCheck");

            ReadableByteChannel rbc = Channels.newChannel(con.getInputStream());
            try (FileOutputStream fos = new FileOutputStream(finalLocation.toFile())) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
        } catch (IOException e) {
            LogManager.logStackTrace(e);
            DialogManager.okDialog().setType(DialogManager.ERROR).setTitle("Failed to download")
                .setContent("Failed to download " + modCheckProject.getName() + ". Please try again later.")
                .show();
            return;
        }

        // remove any mods that are from the same mod from the master mod list
        this.launcher.mods = this.launcher.mods.stream().filter(
                installedMod -> !installedMod.isFromModCheck()
                    || !installedMod.modCheckProject.getName().equalsIgnoreCase(modCheckProject.getName()))
            .collect(Collectors.toList());

        Type modType = Type.mods;

        // add this mod
        DisableableMod disableableMod = new DisableableMod(modCheckProject.getName(), modCheckProject.getModFile().getVersion(),
            true, modCheckProject.getModFile().getName(), modType, null, "", false, true, modCheckProject);
        this.launcher.mods.add(disableableMod);
        if (!forceEnable && ((sameMods.size() == 0 && !AUTO_ENABLE_MODS.contains(disableableMod.getName())) || sameMods.stream().anyMatch(DisableableMod::isDisabled))) disableableMod.disable(this);

        this.save();
    }

    public boolean hasCustomMods() {
        return this.launcher.mods.stream().anyMatch(DisableableMod::isUserAdded);
    }

    public List<String> getCustomMods(Type type) {
        return this.launcher.mods.stream().filter(DisableableMod::isUserAdded).filter(m -> m.getType() == type)
                .map(DisableableMod::getFilename).collect(Collectors.toList());
    }

    public List<String> getPackMods(Type type) {
        return this.launcher.mods.stream().filter(dm -> !dm.userAdded && dm.type == type)
                .map(DisableableMod::getFilename).collect(Collectors.toList());
    }

    public List<DisableableMod> getCustomDisableableMods() {
        return this.launcher.mods.stream().filter(DisableableMod::isUserAdded).collect(Collectors.toList());
    }

    public boolean wasModInstalled(String name) {
        if (this.launcher.mods != null) {
            for (DisableableMod mod : this.launcher.mods) {
                if (mod.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean wasModSelected(String name) {
        if (this.launcher.mods != null) {
            for (DisableableMod mod : this.launcher.mods) {
                if (mod.getName().equalsIgnoreCase(name)) {
                    return mod.wasSelected();
                }
            }
        }
        return false;
    }

    public Map<String, Object> getShareCodeData() {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> mods = new HashMap<>();
        List<Map<String, Object>> optional = new ArrayList<>();

        this.launcher.mods.stream().filter(mod -> mod.optional && !mod.userAdded).forEach(mod -> {
            Map<String, Object> modInfo = new HashMap<>();
            modInfo.put("name", mod.name);
            modInfo.put("selected", true);
            optional.add(modInfo);
        });

        mods.put("optional", optional);
        data.put("mods", mods);

        return data;
    }

    public boolean canBeExported() {
        if (launcher.loaderVersion == null) {
            LogManager.debug("Instance " + launcher.name + " cannot be exported due to: No loader");
            return false;
        }

        return true;
    }

    public boolean rename(String newName) {
        String oldName = this.launcher.name;
        File oldDir = getRoot().toFile();
        this.launcher.name = newName;
        this.ROOT = FileSystem.INSTANCES.resolve(this.getSafeName());
        File newDir = getRoot().toFile();
        if (oldDir.renameTo(newDir)) {
            this.save();
            return true;
        } else {
            this.launcher.name = oldName;
            return false;
        }
    }

    public void save() {
        try (FileWriter fileWriter = new FileWriter(this.getRoot().resolve("instance.json").toFile())) {
            Gsons.MINECRAFT.toJson(this, fileWriter);
        } catch (JsonIOException | IOException e) {
            LogManager.logStackTrace(e);
        }
    }

    public File getAssetsDir() {
        if (this.launcher.assetsMapToResources) {
            return this.getRoot().resolve("resources").toFile();
        }

        return FileSystem.RESOURCES_VIRTUAL.resolve(this.assets).toFile();
    }

    public File getRootDirectory() {
        return getRoot().toFile();
    }

    public File getJarModsDirectory() {
        return getRoot().resolve("jarmods").toFile();
    }

    public File getBinDirectory() {
        return getRoot().resolve("bin").toFile();
    }

    public File getNativesDirectory() {
        return getRoot().resolve("bin/natives").toFile();
    }

    public File getMinecraftJar() {
        return getMinecraftJarLibraryPath().toFile();
    }

    public File getCustomMinecraftJar() {
        return getCustomMinecraftJarLibraryPath().toFile();
    }

    public String getName() {
        return launcher.name;
    }

    public String getPackName() {
        return launcher.pack;
    }

    public String getVersion() {
        return launcher.version;
    }

    public LoaderVersion getLoaderVersion() {
        return launcher.loaderVersion;
    }

    public long incrementNumberOfPlays() {
        if (this.launcher.numPlays == null) {
            this.launcher.numPlays = 0l;
        }

        return this.launcher.numPlays++;
    }

    public long getNumberOfPlays() {
        if (this.launcher.numPlays == null) {
            this.launcher.numPlays = 0l;
        }

        return this.launcher.numPlays;
    }

    public void setLastPlayed(final Instant ts) {
        this.launcher.lastPlayed = ts;
    }

    public Instant getLastPlayedOrEpoch() {
        return this.launcher.lastPlayed != null ? this.launcher.lastPlayed : Instant.EPOCH;
    }

    public String getMainClass() {
        return mainClass;
    }

    public String getMinecraftVersion() {
        return id;
    }

    public String getAssets() {
        return assets;
    }

    public int getMemory() {
        return launcher.requiredMemory;
    }

    public int getPermGen() {
        return launcher.requiredPermGen;
    }

    public boolean isMultiMcImport() {
        return launcher.multiMCManifest != null;
    }

    public boolean isVanillaInstance() {
        return launcher.vanillaInstance;
    }

    public boolean isExternalPack() {
        return isMultiMcImport();
    }

    public boolean isUpdatableExternalPack() {
        return isExternalPack();
    }

    public String getAnalyticsCategory() {
        if (isMultiMcImport()) {
            return "MultiMcImport";
        }

        if (isVanillaInstance()) {
            return "VanillaInstance";
        }

        return "Instance";
    }

    public void backup() {
        backup(App.settings.backupMode);
    }

    public void backup(BackupMode backupMode) {
        // #. {0} is the name of the instance
        final JDialog dialog = new JDialog(App.launcher.getParent(), GetText.tr("Backing Up {0}", launcher.name),
                ModalityType.DOCUMENT_MODAL);
        dialog.setSize(300, 100);
        dialog.setLocationRelativeTo(App.launcher.getParent());
        dialog.setResizable(false);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        // #. {0} is the name of the instance
        JLabel doing = new JLabel(GetText.tr("Backing Up {0}", launcher.name));
        doing.setHorizontalAlignment(JLabel.CENTER);
        doing.setVerticalAlignment(JLabel.TOP);
        topPanel.add(doing);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        JProgressBar progressBar = new JProgressBar();
        bottomPanel.add(progressBar, BorderLayout.NORTH);
        progressBar.setIndeterminate(true);

        dialog.add(topPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);

        final Thread backupThread = new Thread(() -> {
            Timestamp timestamp = new Timestamp(new Date().getTime());
            String time = timestamp.toString().replaceAll("[^0-9]", "_");
            String filename = getSafeName() + "-" + time.substring(0, time.lastIndexOf("_")) + ".zip";

            ArchiveUtils.createZip(getRoot(), FileSystem.BACKUPS.resolve(filename),
                    ZipNameMapper.getMapperForBackupMode(backupMode));

            dialog.dispose();
            App.TOASTER.pop(GetText.tr("Backup is complete"));
        });
        backupThread.start();
        dialog.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                backupThread.interrupt();
                dialog.dispose();
            }
        });
        dialog.setVisible(true);
    }

    public boolean canChangeDescription() {
        return isExternalPack() || launcher.vanillaInstance || (getPack() != null && getPack().system);
    }


    public void startRename() {
        new RenameInstanceDialog(this);
    }

    public void startClone() {
        String clonedName = JOptionPane.showInputDialog(App.launcher.getParent(),
                GetText.tr("Enter a new name for this cloned instance."),
                GetText.tr("Cloning Instance"), JOptionPane.INFORMATION_MESSAGE);

        if (clonedName != null && clonedName.length() >= 1
                && InstanceManager.getInstanceByName(clonedName) == null
                && InstanceManager
                        .getInstanceBySafeName(clonedName.replaceAll("[^A-Za-z0-9]", "")) == null
                && clonedName.replaceAll("[^A-Za-z0-9]", "").length() >= 1 && !Files.exists(
                        FileSystem.INSTANCES.resolve(clonedName.replaceAll("[^A-Za-z0-9]", "")))) {

            final String newName = clonedName;
            final ProgressDialog dialog = new ProgressDialog(GetText.tr("Cloning Instance"), 0,
                    GetText.tr("Cloning Instance. Please wait..."), null, App.launcher.getParent());
            dialog.addThread(new Thread(() -> {
                InstanceManager.cloneInstance(this, newName);
                dialog.close();
                App.TOASTER.pop(GetText.tr("Cloned Instance Successfully"));
            }));
            dialog.start();
        } else if (clonedName == null || clonedName.equals("")) {
            LogManager.error("Error Occurred While Cloning Instance! Dialog Closed/Cancelled!");
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "An error occurred while cloning the instance.<br/><br/>Please check the console and try again."))
                            .build())
                    .setType(DialogManager.ERROR).show();
        } else if (clonedName.replaceAll("[^A-Za-z0-9]", "").length() == 0) {
            LogManager.error("Error Occurred While Cloning Instance! Invalid Name!");
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "An error occurred while cloning the instance.<br/><br/>Please check the console and try again."))
                            .build())
                    .setType(DialogManager.ERROR).show();
        } else if (Files
                .exists(FileSystem.INSTANCES.resolve(clonedName.replaceAll("[^A-Za-z0-9]", "")))) {
            LogManager.error(
                    "Error Occurred While Cloning Instance! Folder Already Exists Rename It And Try Again!");
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "An error occurred while cloning the instance.<br/><br/>Please check the console and try again."))
                            .build())
                    .setType(DialogManager.ERROR).show();
        } else {
            LogManager.error(
                    "Error Occurred While Cloning Instance! Instance With That Name Already Exists!");
            DialogManager.okDialog().setTitle(GetText.tr("Error"))
                    .setContent(new HTMLBuilder().center().text(GetText.tr(
                            "An error occurred while cloning the instance.<br/><br/>Please check the console and try again."))
                            .build())
                    .setType(DialogManager.ERROR).show();
        }
    }

    public void startChangeDescription() {
        JTextArea textArea = new JTextArea(launcher.description);
        textArea.setColumns(30);
        textArea.setRows(10);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setSize(300, 150);

        int ret = JOptionPane.showConfirmDialog(App.launcher.getParent(), new JScrollPane(textArea),
                GetText.tr("Changing Description"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);

        if (ret == 0) {
            launcher.description = textArea.getText();
            save();
        }
    }

    public void startChangeImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("PNG Files", "png"));
        int ret = chooser.showOpenDialog(App.launcher.getParent());
        if (ret == JFileChooser.APPROVE_OPTION) {
            File img = chooser.getSelectedFile();
            if (img.getAbsolutePath().endsWith(".png")) {
                try {
                    Utils.safeCopy(img, getRoot().resolve("instance.png").toFile());
                    save();
                } catch (IOException ex) {
                    LogManager.logStackTrace("Failed to set instance image", ex);
                }
            }
        }
    }

    public void changeLoaderVersion() {
        LoaderVersion loaderVersion = showLoaderVersionSelector(launcher.loaderVersion.getLoaderType());

        if (loaderVersion == null) {
            return;
        }

        boolean success = false;

        try {
            Installable installable = new VanillaInstallable(MinecraftManager.getMinecraftVersion(id), loaderVersion,
                    launcher.description, launcher.lwjglVersion);
            installable.instance = this;
            installable.instanceName = launcher.name;
            installable.isReinstall = true;
            installable.changingLoader = true;
            installable.saveMods = true;

            success = installable.startInstall();
        } catch (InvalidMinecraftVersion e) {
            LogManager.logStackTrace(e);
        }

        if (success) {
            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("{0} Installed", launcher.loaderVersion.getLoaderType()))
                    .setContent(
                            new HTMLBuilder().center()
                                    // #. {0} is the loader (Forge/Fabric/Quilt) {1} is the version
                                    .text(GetText.tr("{0} {1} has been installed.",
                                            launcher.loaderVersion.getLoaderType(), loaderVersion.version))
                                    .build())
                    .setType(DialogManager.INFO).show();
        } else {
            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("{0} Not Installed", launcher.loaderVersion.getLoaderType()))
                    .setContent(new HTMLBuilder().center()
                            // #. {0} is the loader (Forge/Fabric/Quilt)
                            .text(GetText.tr("{0} has not been installed. Check the console for more information.",
                                    launcher.loaderVersion.getLoaderType()))
                            .build())
                    .setType(DialogManager.ERROR).show();
        }
    }

    public void addLoader(LoaderType loaderType) {
        LoaderVersion loaderVersion = showLoaderVersionSelector(loaderType);

        if (loaderVersion == null) {
            return;
        }

        boolean success = false;

        try {
            Installable installable = new VanillaInstallable(MinecraftManager.getMinecraftVersion(id), loaderVersion,
                    launcher.description, launcher.lwjglVersion);
            installable.instance = this;
            installable.instanceName = launcher.name;
            installable.isReinstall = true;
            installable.addingLoader = true;
            installable.saveMods = true;

            success = installable.startInstall();
        } catch (InvalidMinecraftVersion e) {
            LogManager.logStackTrace(e);
        }

        if (success) {
            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("{0} Installed", loaderType))
                    .setContent(new HTMLBuilder().center()
                            // #. {0} is the loader (Forge/Fabric/Quilt) {1} is the version
                            .text(GetText.tr("{0} {1} has been installed.", loaderType, loaderVersion.version)).build())
                    .setType(DialogManager.INFO).show();
        } else {
            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("{0} Not Installed", loaderType))
                    // #. {0} is the loader (Forge/Fabric/Quilt)
                    .setContent(new HTMLBuilder().center().text(GetText
                            .tr("{0} has not been installed. Check the console for more information.", loaderType))
                            .build())
                    .setType(DialogManager.ERROR).show();
        }
    }

    private LoaderVersion showLoaderVersionSelector(LoaderType loaderType) {
        ProgressDialog<List<LoaderVersion>> progressDialog = new ProgressDialog<>(
                // #. {0} is the loader (Forge/Fabric/Quilt)
                GetText.tr("Checking For {0} Versions", loaderType), 0,
                // #. {0} is the loader (Forge/Fabric/Quilt)
                GetText.tr("Checking For {0} Versions", loaderType));
        progressDialog.addThread(new Thread(() -> {
            if (loaderType == LoaderType.FABRIC) {
                progressDialog.setReturnValue(FabricLoader.getChoosableVersions(id));
            } else if (loaderType == LoaderType.LEGACY_FABRIC) {
                progressDialog.setReturnValue(LegacyFabricLoader.getChoosableVersions(id));
            } else if (loaderType == LoaderType.QUILT) {
                progressDialog.setReturnValue(QuiltLoader.getChoosableVersions(id));
            }

            progressDialog.doneTask();
            progressDialog.close();
        }));
        progressDialog.start();

        List<LoaderVersion> loaderVersions = progressDialog.getReturnValue();

        if (loaderVersions == null || loaderVersions.size() == 0) {
            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("No Versions Available For {0}", loaderType))
                    .setContent(new HTMLBuilder().center()
                            // #. {0} is the loader (Forge/Fabric/Quilt)
                            .text(GetText.tr("{0} has not been installed/updated as there are no versions available.",
                                    loaderType))
                            .build())
                    .setType(DialogManager.ERROR).show();
            return null;
        }

        JComboBox<ComboItem<LoaderVersion>> loaderVersionsDropDown = new JComboBox<>();

        int loaderVersionLength = 0;

        // ensures that font width is taken into account
        for (LoaderVersion version : loaderVersions) {
            loaderVersionLength = Math.max(loaderVersionLength, loaderVersionsDropDown
                    .getFontMetrics(App.THEME.getNormalFont()).stringWidth(version.toStringWithCurrent(this)) + 25);
        }

        loaderVersions.forEach(version -> loaderVersionsDropDown
                .addItem(new ComboItem<>(version, version.toStringWithCurrent(this))));

        if (launcher.loaderVersion != null) {
            String loaderVersionString = launcher.loaderVersion.version;

            for (int i = 0; i < loaderVersionsDropDown.getItemCount(); i++) {
                LoaderVersion loaderVersion = loaderVersionsDropDown.getItemAt(i).getValue();

                if (loaderVersion.version.equals(loaderVersionString)) {
                    loaderVersionsDropDown.setSelectedIndex(i);
                    break;
                }
            }
        }

        // ensures that the dropdown is at least 300 px wide
        loaderVersionLength = Math.max(300, loaderVersionLength);

        // ensures that there is a maximum width of 400 px to prevent overflow
        loaderVersionLength = Math.min(400, loaderVersionLength);

        loaderVersionsDropDown.setPreferredSize(new Dimension(loaderVersionLength, 23));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        Box box = Box.createHorizontalBox();
        // #. {0} is the loader (Forge/Fabric/Quilt)
        box.add(new JLabel(GetText.tr("Select {0} Version To Install", loaderType)));
        box.add(Box.createHorizontalGlue());

        panel.add(box);
        panel.add(Box.createVerticalStrut(20));
        panel.add(loaderVersionsDropDown);
        panel.add(Box.createVerticalStrut(20));

        Object[] buttons = { "Change", "Cancel" };
        int ret = JOptionPane.showOptionDialog(App.launcher.getParent(), panel,
                // #. {0} is the loader (Forge/Fabric/Quilt)
                launcher.loaderVersion == null ? GetText.tr("Installing {0}", loaderType)
                        // #. {0} is the loader (Forge/Fabric/Quilt)
                        : GetText.tr("Changing {0} Version", loaderType),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, buttons, buttons[0]);

        if (ret != 0) {
            return null;
        }

        return ((ComboItem<LoaderVersion>) loaderVersionsDropDown.getSelectedItem()).getValue();
    }

    public void removeLoader() {
        String loaderType = launcher.loaderVersion.type;

        boolean success = false;

        try {
            Installable installable = new VanillaInstallable(MinecraftManager.getMinecraftVersion(id), null,
                    launcher.description, launcher.lwjglVersion);
            installable.instance = this;
            installable.instanceName = launcher.name;
            installable.isReinstall = true;
            installable.removingLoader = true;
            installable.saveMods = true;

            success = installable.startInstall();
        } catch (InvalidMinecraftVersion e) {
            LogManager.logStackTrace(e);
        }

        if (success) {
            App.launcher.reloadInstancesPanel();

            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("{0} Removed", loaderType))
                    .setContent(new HTMLBuilder().center()
                            // #. {0} is the loader (Forge/Fabric/Quilt)
                            .text(GetText.tr("{0} has been removed from this instance.", loaderType)).build())
                    .setType(DialogManager.INFO).show();
        } else {
            // #. {0} is the loader (Forge/Fabric/Quilt)
            DialogManager.okDialog().setTitle(GetText.tr("{0} Not Removed", loaderType))
                    .setContent(new HTMLBuilder().center().text(
                            // #. {0} is the loader (Forge/Fabric/Quilt)
                            GetText.tr("{0} has not been removed. Check the console for more information.", loaderType))
                            .build())
                    .setType(DialogManager.ERROR).show();
        }
    }

    public boolean usesCustomMinecraftJar() {
        return Files.exists(getRoot().resolve("bin/modpack.jar"));
    }

    public boolean shouldUseLegacyLaunch() {
        try {
            String[] versionParts = id.split("\\.", 3);

            return Integer.parseInt(versionParts[0]) == 1 && Integer.parseInt(versionParts[1]) < 6;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean usesLegacyLaunch() {
        if (type != VersionManifestVersionType.RELEASE
                || Optional.ofNullable(launcher.disableLegacyLaunching).orElse(App.settings.disableLegacyLaunching)) {
            return false;
        }

        return shouldUseLegacyLaunch();
    }

    public boolean isUsingJavaRuntime() {
        return javaVersion != null && Optional.ofNullable(launcher.useJavaProvidedByMinecraft)
                .orElse(App.settings.useJavaProvidedByMinecraft);
    }

    public String getJavaPath() {
        String javaPath = Optional.ofNullable(launcher.javaPath).orElse(App.settings.javaPath);

        // are we using Mojangs provided runtime?
        if (isUsingJavaRuntime()) {
            Map<String, List<JavaRuntime>> runtimesForSystem = Data.JAVA_RUNTIMES.getForSystem();
            String runtimeToUse = Optional.ofNullable(launcher.javaRuntimeOverride).orElse(javaVersion.component);

            // make sure the runtime is available in the data set (so it's not disabled
            // remotely)
            if (runtimesForSystem.containsKey(runtimeToUse)
                    && runtimesForSystem.get(runtimeToUse).size() != 0) {
                Path runtimeDirectory = FileSystem.MINECRAFT_RUNTIMES.resolve(runtimeToUse)
                        .resolve(JavaRuntimes.getSystem()).resolve(runtimeToUse);

                if (OS.isMac()) {
                    runtimeDirectory = runtimeDirectory.resolve("jre.bundle/Contents/Home");
                }

                if (Files.isDirectory(runtimeDirectory)) {
                    javaPath = runtimeDirectory.toAbsolutePath().toString();
                    if (launcher.javaRuntimeOverride != null) {
                        LogManager.info(String.format("Using overriden Java runtime %s (Java %s) at path %s",
                                runtimeToUse, runtimesForSystem.get(runtimeToUse).get(0).version.name, javaPath));
                    } else {
                        LogManager.info(String.format("Using Java runtime %s (Java %s) at path %s",
                                runtimeToUse, runtimesForSystem.get(runtimeToUse).get(0).version.name, javaPath));
                    }
                }
            }
        }

        return javaPath;
    }

    public boolean shouldShowWrongJavaWarning() {
        if (launcher.java == null) {
            return false;
        }

        String javaVersion = Java.getVersionForJavaPath(new File(getJavaPath()));

        if (javaVersion.equalsIgnoreCase("Unknown")) {
            return false;
        }

        int majorJavaVersion = Java.parseJavaVersionNumber(javaVersion);

        return !launcher.java.conforms(majorJavaVersion);
    }

    public String getVersionOfPack() {
        return launcher.version;
    }

    public void scanMissingMods() {
        scanMissingMods(App.launcher.getParent());
    }

    public void scanMissingMods(Window parent) {
        PerformanceManager.start("Instance::scanMissingMods - CheckForAddedMods");

        // files to scan
        List<Path> files = new ArrayList<>();

        // find the mods that have been added by the user manually
        for (Path path : Arrays.asList(ROOT.resolve("mods"), ROOT.resolve("disabledmods"),
                ROOT.resolve("resourcepacks"), ROOT.resolve("jarmods"))) {
            if (!Files.exists(path)) {
                continue;
            }

            com.atlauncher.data.Type fileType = path.equals(ROOT.resolve("resourcepacks"))
                    ? com.atlauncher.data.Type.resourcepack
                    : (path.equals(ROOT.resolve("jarmods")) ? com.atlauncher.data.Type.jar
                            : com.atlauncher.data.Type.mods);

            try (Stream<Path> stream = Files.list(path)) {
                files.addAll(stream
                        .filter(file -> !Files.isDirectory(file) && Utils.isAcceptedModFile(file)).filter(
                                file -> launcher.mods.stream()
                                        .noneMatch(mod -> mod.type == fileType
                                                && mod.file.equals(file.getFileName().toString())))
                        .collect(Collectors.toList()));
            } catch (IOException e) {
                LogManager.logStackTrace("Error scanning missing mods", e);
            }
        }

        if (files.size() != 0) {
            final ProgressDialog progressDialog = new ProgressDialog(GetText.tr("Scanning New Mods"), 0,
                    GetText.tr("Scanning New Mods"), parent);

            progressDialog.addThread(new Thread(() -> {
                List<DisableableMod> mods = files.parallelStream()
                        .map(file -> {
                            com.atlauncher.data.Type fileType = file.getParent().equals(ROOT.resolve("resourcepacks"))
                                    ? com.atlauncher.data.Type.resourcepack
                                    : (file.getParent().equals(ROOT.resolve("jarmods")) ? com.atlauncher.data.Type.jar
                                            : com.atlauncher.data.Type.mods);

                            return DisableableMod.generateMod(file.toFile(), fileType,
                                    !file.getParent().equals(ROOT.resolve("disabledmods")));
                        })
                        .collect(Collectors.toList());


                if (!App.settings.dontCheckModsOnModrinth) {
                    Map<String, DisableableMod> sha1Hashes = new HashMap<>();

                    mods.stream()
                            .filter(dm -> dm.modrinthProject == null && dm.modrinthVersion == null)
                            .filter(dm -> dm.getFile(ROOT, id) != null).forEach(dm -> {
                                try {
                                    sha1Hashes.put(Hashing
                                            .sha1(dm.disabled ? dm.getDisabledFile(this).toPath()
                                                    : dm
                                                            .getFile(ROOT, id).toPath())
                                            .toString(), dm);
                                } catch (Throwable t) {
                                    LogManager.logStackTrace(t);
                                }
                            });

                    if (sha1Hashes.size() != 0) {
                        Set<String> keys = sha1Hashes.keySet();
                        Map<String, ModrinthVersion> modrinthVersions = ModrinthApi
                                .getVersionsFromSha1Hashes(keys.toArray(new String[keys.size()]));

                        if (modrinthVersions != null && modrinthVersions.size() != 0) {
                            String[] projectIdsFound = modrinthVersions.values().stream().map(mv -> mv.projectId)
                                    .toArray(String[]::new);

                            if (projectIdsFound.length != 0) {
                                Map<String, ModrinthProject> foundProjects = ModrinthApi
                                        .getProjectsAsMap(projectIdsFound);

                                if (foundProjects != null) {
                                    for (Map.Entry<String, ModrinthVersion> entry : modrinthVersions.entrySet()) {
                                        ModrinthVersion version = entry.getValue();
                                        ModrinthProject project = foundProjects.get(version.projectId);

                                        if (project != null) {
                                            DisableableMod dm = sha1Hashes.get(entry.getKey());

                                            // add Modrinth information
                                            dm.modrinthProject = project;
                                            dm.modrinthVersion = version;

                                            if (App.settings.defaultModPlatform == ModPlatform.MODRINTH) {
                                                dm.name = project.title;
                                                dm.description = project.description;
                                            }

                                            LogManager.debug(String.format(
                                                    "Found matching mod from Modrinth called %s with file %s",
                                                    project.title, version.name));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                mods.forEach(mod -> LogManager.info("Found extra mod with name of " + mod.file));
                launcher.mods.addAll(mods);
                save();
                progressDialog.close();
            }));

            progressDialog.start();
        }
        PerformanceManager.end("Instance::scanMissingMods - CheckForAddedMods");

        PerformanceManager.start("Instance::scanMissingMods - CheckForRemovedMods");
        // next remove any mods that the no longer exist in the filesystem
        List<DisableableMod> removedMods = launcher.mods.parallelStream().filter(mod -> {
            if (!mod.wasSelected || mod.skipped || mod.type != com.atlauncher.data.Type.mods) {
                return false;
            }

            if (mod.disabled) {
                return (mod.getFile(this) != null && !mod.getDisabledFile(this).exists());
            } else {
                return (mod.getFile(this) != null && !mod.getFile(this).exists());
            }
        }).collect(Collectors.toList());

        if (removedMods.size() != 0) {
            removedMods.forEach(mod -> LogManager.info("Mod no longer in filesystem: " + mod.file));
            launcher.mods.removeAll(removedMods);
            save();
        }
        PerformanceManager.end("Instance::scanMissingMods - CheckForRemovedMods");
    }
}

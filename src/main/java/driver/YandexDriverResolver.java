package driver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class YandexDriverResolver {

    private static final String DRIVER_PROPERTY = "yandex.driver";
    private static final String CHROMIUM_VERSION_PROPERTY = "yandex.chromium.version";
    private static final String PLIST_CHROMIUM_KEY = "CFBundleChromiumShortVersionString";

    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.\\d+\\.\\d+\\.\\d+");
    private static final Pattern CHROMIUM_VERSION_PATTERN =
            Pattern.compile("(?:Chromium|Chrome)/(\\d+)\\.");

    private YandexDriverResolver() {
    }

    public static String resolve(String yandexBinary) {
        String explicitDriver = System.getProperty(DRIVER_PROPERTY);
        if (explicitDriver != null && !explicitDriver.trim().isEmpty()) {
            File driver = new File(explicitDriver);
            return driver.isFile() ? driver.getAbsolutePath() : null;
        }

        String majorVersion = chromiumMajorVersion(yandexBinary);
        if (majorVersion.isEmpty()) {
            return null;
        }

        String platform = platform();
        if (platform.isEmpty()) {
            return null;
        }

        for (File cacheRoot : cacheRoots()) {
            File platformDirectory = new File(new File(cacheRoot, "chromedriver"), platform);
            File driver = findDriver(platformDirectory, majorVersion);
            if (driver != null) {
                return driver.getAbsolutePath();
            }
        }
        return null;
    }

    private static String chromiumMajorVersion(String yandexBinary) {
        String override = System.getProperty(CHROMIUM_VERSION_PROPERTY);
        if (override != null && !override.trim().isEmpty()) {
            return majorOf(override);
        }

        String fromPlist = chromiumVersionFromPlist(yandexBinary);
        if (!fromPlist.isEmpty()) {
            return majorOf(fromPlist);
        }

        String fromCommandLine = chromiumVersionFromCommandLine(yandexBinary);
        return majorOf(fromCommandLine);
    }

    private static String chromiumVersionFromPlist(String yandexBinary) {
        if (!isMac()) {
            return "";
        }
        File binary = new File(yandexBinary);
        File macOsDirectory = binary.getParentFile();
        if (macOsDirectory == null || !"MacOS".equals(macOsDirectory.getName())) {
            return "";
        }
        File contentsDirectory = macOsDirectory.getParentFile();
        if (contentsDirectory == null) {
            return "";
        }
        File plist = new File(contentsDirectory, "Info.plist");
        if (!plist.isFile()) {
            return "";
        }

        try {
            Process process = new ProcessBuilder(
                    "plutil", "-extract", PLIST_CHROMIUM_KEY, "raw", "-o", "-", plist.getAbsolutePath())
                    .redirectErrorStream(true)
                    .start();
            String output = readProcessOutput(process);
            return output.isEmpty() ? "" : output.split("\\R")[0].trim();
        } catch (IOException e) {
            return "";
        }
    }

    private static String chromiumVersionFromCommandLine(String yandexBinary) {
        try {
            Process process = new ProcessBuilder(yandexBinary, "--version")
                    .redirectErrorStream(true)
                    .start();
            String output = readProcessOutput(process);

            Matcher chromiumMatcher = CHROMIUM_VERSION_PATTERN.matcher(output);
            if (chromiumMatcher.find()) {
                return chromiumMatcher.group(1);
            }
            Matcher versionMatcher = VERSION_PATTERN.matcher(output);
            return versionMatcher.find() ? versionMatcher.group() : "";
        } catch (IOException e) {
            return "";
        }
    }

    private static String readProcessOutput(Process process) throws IOException {
        try {
            if (!process.waitFor(10, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return "";
            }
            return new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            process.destroyForcibly();
            return "";
        }
    }

    private static File findDriver(File platformDirectory, String majorVersion) {
        File[] versionDirectories = platformDirectory.listFiles(File::isDirectory);
        if (versionDirectories == null) {
            return null;
        }

        List<File> candidates = new ArrayList<>();
        for (File versionDirectory : versionDirectories) {
            if (!versionDirectory.getName().startsWith(majorVersion + ".")) {
                continue;
            }
            File driver = new File(versionDirectory, driverName());
            if (driver.isFile()) {
                candidates.add(driver);
            }
        }
        candidates.sort((first, second) -> compareVersions(
                first.getParentFile().getName(), second.getParentFile().getName()));
        return candidates.isEmpty() ? null : candidates.get(candidates.size() - 1);
    }

    private static int compareVersions(String first, String second) {
        String[] firstParts = first.split("\\.");
        String[] secondParts = second.split("\\.");
        int length = Math.max(firstParts.length, secondParts.length);
        for (int i = 0; i < length; i++) {
            int firstPart = i < firstParts.length ? parseVersionPart(firstParts[i]) : 0;
            int secondPart = i < secondParts.length ? parseVersionPart(secondParts[i]) : 0;
            if (firstPart != secondPart) {
                return Integer.compare(firstPart, secondPart);
            }
        }
        return 0;
    }

    private static int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static Set<File> cacheRoots() {
        Set<File> roots = new LinkedHashSet<>();

        String seCachePath = System.getenv("SE_CACHE_PATH");
        if (seCachePath != null && !seCachePath.isEmpty()) {
            roots.add(new File(seCachePath));
        }

        String xdgCacheHome = System.getenv("XDG_CACHE_HOME");
        if (xdgCacheHome != null && !xdgCacheHome.isEmpty()) {
            roots.add(new File(xdgCacheHome, "selenium"));
        }

        roots.add(new File(System.getProperty("user.home"), ".cache/selenium"));

        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData != null && !localAppData.isEmpty()) {
            roots.add(new File(localAppData, "selenium"));
        }

        return roots;
    }

    private static String platform() {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

        if (os.contains("mac")) {
            return isArm(arch) ? "mac-arm64" : "mac-x64";
        }
        if (os.contains("win")) {
            return is64Bit(arch) ? "win64" : "win32";
        }
        if (os.contains("linux")) {
            return isArm(arch) ? "linux-arm64" : "linux64";
        }
        return "";
    }

    private static String driverName() {
        return isWindows() ? "chromedriver.exe" : "chromedriver";
    }

    private static boolean isMac() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    }

    private static boolean isArm(String arch) {
        return arch.contains("aarch64") || arch.contains("arm64");
    }

    private static boolean is64Bit(String arch) {
        return arch.contains("64");
    }

    private static String majorOf(String version) {
        if (version == null) {
            return "";
        }
        String trimmed = version.trim();
        int dot = trimmed.indexOf('.');
        if (dot > 0) {
            trimmed = trimmed.substring(0, dot);
        }
        return trimmed.matches("\\d+") ? trimmed : "";
    }
}

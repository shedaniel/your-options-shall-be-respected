package me.shedaniel.yosbr;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


public class YourOptionsShallBeRespected implements PreLaunchEntrypoint {
    public static final Logger LOGGER = LogManager.getLogger("YOSBR");
    public static final File RUN_DIR = FabricLoader.getInstance().getGameDirectory();
    public static final File CONFIG_DIR = FabricLoader.getInstance().getConfigDirectory();
    
    /**
     * We are using pre-launch entrypoint here as we want to be faster than everyone.
     */
    @Override
    public void onPreLaunch() {
        try {
            File yosbr = new File(CONFIG_DIR, "yosbr");
            if (!yosbr.exists() && !yosbr.mkdirs()) {
                throw new IllegalStateException("Could not create directory: " + yosbr.getAbsolutePath());
            }
            new File(yosbr, "options.txt").createNewFile();
            File config = new File(yosbr, "config");
            if (!config.exists() && !config.mkdirs()) {
                throw new IllegalStateException("Could not create directory: " + config.getAbsolutePath());
            }
            Files.walk(yosbr.toPath()).forEach(path -> {
                File file = path.toAbsolutePath().toFile();
                if (!file.isFile()) return;
                try {
                    if (file.getAbsolutePath().startsWith(config.getAbsolutePath())) {
                        String name = file.getAbsolutePath().replace(config.getAbsolutePath() + "/", "/");
                        if (name.startsWith("yosbr/"))
                            throw new IllegalStateException("Illegal default config file: " + file);
                        applyDefaultOptions(new File(CONFIG_DIR, name), file);
                    } else {
                        applyDefaultOptions(new File(RUN_DIR, file.getAbsolutePath().replace(yosbr.getAbsolutePath() + "/", "/")), file);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Failed to apply default options.", e);
        }
    }
    
    private void applyDefaultOptions(File file, File defaultFile) throws IOException {
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IllegalStateException("Could not create directory: " + file.getParentFile().getAbsolutePath());
        }
        if (!defaultFile.getParentFile().exists() && !defaultFile.getParentFile().mkdirs()) {
            throw new IllegalStateException("Could not create directory: " + defaultFile.getParentFile().getAbsolutePath());
        }
        if (!defaultFile.exists()) {
            defaultFile.createNewFile();
            return;
        }
        if (file.exists()) return;
        LOGGER.info("Applying default options for /" + RUN_DIR.toPath().relativize(file.toPath()).toString() + " from /" +
                    RUN_DIR.toPath().relativize(defaultFile.toPath()).toString());
        Files.copy(defaultFile.toPath(), file.toPath());
    }
}

package me.shedaniel.yosbr;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.ModContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class YourOptionsShallBeRespected implements LanguageAdapter {
    public static final Logger LOGGER = LogManager.getLogger("YOSBR");
    public static final File RUN_DIR = FabricLoader.getInstance().getGameDirectory();
    public static final File CONFIG_DIR = FabricLoader.getInstance().getConfigDirectory();
    
    /**
     * We are using pre-launch entrypoint here as we want to be faster than everyone.
     */
    public YourOptionsShallBeRespected() {
        LOGGER.info("Applying default options... (YOSBR)");
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
                File file = path.normalize().toAbsolutePath().normalize().toFile();
                if (!file.isFile()) return;
                try {
                    try {
                        Path configRelative = config.toPath().toAbsolutePath().normalize().relativize(file.toPath().toAbsolutePath().normalize());
                        if (configRelative.startsWith("yosbr"))
                            throw new IllegalStateException("Illegal default config file: " + file);
                        applyDefaultOptions(new File(CONFIG_DIR, configRelative.normalize().toString()), file);
                    } catch (IllegalArgumentException e) {
                        System.out.println(yosbr.toPath().toAbsolutePath().normalize());
                        System.out.println(file.toPath().toAbsolutePath().normalize());
                        System.out.println(yosbr.toPath().toAbsolutePath().normalize().relativize(file.toPath().toAbsolutePath().normalize()));
                        applyDefaultOptions(new File(RUN_DIR, yosbr.toPath().toAbsolutePath().normalize().relativize(file.toPath().toAbsolutePath().normalize()).normalize().toString()), file);
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
        LOGGER.info("Applying default options for " + File.separator + RUN_DIR.toPath().toAbsolutePath().normalize().relativize(file.toPath().toAbsolutePath().normalize()).normalize().toString() + " from " + File.separator +
                    RUN_DIR.toPath().toAbsolutePath().normalize().relativize(defaultFile.toPath().toAbsolutePath().normalize()).normalize().toString());
        Files.copy(defaultFile.toPath(), file.toPath());
    }
    
    @Override
    public <T> T create(ModContainer mod, String value, Class<T> type) throws LanguageAdapterException {
        throw new IllegalStateException();
    }
}

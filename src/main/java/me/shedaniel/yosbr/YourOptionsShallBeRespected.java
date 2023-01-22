package me.shedaniel.yosbr;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class YourOptionsShallBeRespected implements PreLaunchEntrypoint {
    public static final Logger LOGGER = LoggerFactory.getLogger("YOSBR");
    public static final Path YOSBR_PATH = FabricLoader.getInstance().getConfigDir().resolve("yosbr");

    @Override
    public void onPreLaunch() {
        try {
            if (Files.notExists(YOSBR_PATH)) {
                Files.createDirectory(YOSBR_PATH);
            }

            try (var walk = Files.walk(YOSBR_PATH).skip(1)) {
                walk.forEach(path -> {
                    try {
                        if (Files.isRegularFile(path)) {
                            var root_path = FabricLoader.getInstance().getGameDir().resolve(YOSBR_PATH.relativize(path).toString());

                            if (Files.notExists(root_path)) {
                                var parent = root_path.getParent();
                                if (Files.notExists(parent)) {
                                    Files.createDirectories(parent);
                                }

                                LOGGER.info("Applying default files for {} from {}", FabricLoader.getInstance().getGameDir().relativize(root_path), FabricLoader.getInstance().getConfigDir().relativize(path));
                                Files.copy(path, root_path);
                            }
                        }
                    }
                    catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                });
            }
        }
        catch (Exception e) {
            LOGGER.error("Failed to apply default options.", e);
        }
    }
}

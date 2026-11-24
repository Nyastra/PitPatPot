
package nya.pitpatpot.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import net.fabricmc.loader.api.FabricLoader;

import nya.pitpatpot.PitPatPot;

public class PitPatPotConf {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    private static final Path CONFIG_FILE =
            FabricLoader.getInstance().getConfigDir().resolve("pitpatpot.json");

    public static PitPatPotConf INSTANCE = new PitPatPotConf();

    // General settings
    public boolean pettingEnabled = true;
    public boolean chatMessagesEnabled = true;
    public String chatMessage = "You give {entity} a quick pet :3";

    // Petting rules
    public EntityPreset entityPreset = EntityPreset.ALL;
    public EntityListMode entityListMode = EntityListMode.BLACKLIST;
    public List<String> entityList = new ArrayList<>();

    // Appearance and timing
    public int chatCooldownSeconds = 300;
    public int heartsPerPat = 3;

    public enum EntityPreset {
        ALL,
        MOBS_ONLY,
        NO_MONSTERS,
    }
    public enum EntityListMode {
        WHITELIST,
        BLACKLIST,
    }
    // Replaces null/out-of-range values (e.g. from a hand-edited config) with safe ones
    private void sanitize() {
        if (entityPreset == null) entityPreset = EntityPreset.ALL;
        if (entityListMode == null) entityListMode = EntityListMode.BLACKLIST;
        if (entityList == null) entityList = new ArrayList<>();
        entityList = entityList.stream()
                .filter(s -> s != null && !s.isBlank())
                .map(PitPatPotConf::normaliseId)
                .distinct()
                .collect(Collectors.toCollection(ArrayList::new));
        if (chatMessage == null) chatMessage = "You give {entity} a quick pet :3";
        heartsPerPat = Math.clamp(heartsPerPat, 0, 100);
        chatCooldownSeconds = Math.clamp(chatCooldownSeconds, 0, 86400);
    }
    // Adds 'minecraft:' to the ID if no alternative is specified
    public static String normaliseId(String raw) {
        String id = raw.trim().toLowerCase(Locale.ROOT);
        return id.contains(":") ? id : "minecraft:" + id;
    }
    public static void load() {
        if (Files.exists(CONFIG_FILE)) {
            try {
                String json = Files.readString(CONFIG_FILE, StandardCharsets.UTF_8);

                PitPatPotConf loaded = GSON.fromJson(json, PitPatPotConf.class);

                if (loaded != null) {
                    loaded.sanitize();
                    INSTANCE = loaded;
                }

            } catch (IOException | JsonParseException e) {
                PitPatPot.LOGGER.error("Could not load PitPatPot config!", e);
            }
        }

        save();
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_FILE.getParent());

            String json = GSON.toJson(INSTANCE);

            Files.writeString(
                    CONFIG_FILE,
                    json,
                    StandardCharsets.UTF_8
            );

        } catch (IOException e) {
            PitPatPot.LOGGER.error("Could not save PitPatPot config!", e);
        }
    }
}

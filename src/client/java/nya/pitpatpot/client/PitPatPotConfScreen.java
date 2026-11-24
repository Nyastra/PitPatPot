
package nya.pitpatpot.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import nya.pitpatpot.config.PitPatPotConf;

public class PitPatPotConfScreen {

    public static Screen create(Screen parent) {

        PitPatPotConf config = PitPatPotConf.INSTANCE;

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("PitPatPot Settings"))
                .setSavingRunnable(PitPatPotConf::save)
                .setAfterInitConsumer(screen -> {
                    if (screen instanceof me.shedaniel.clothconfig2.gui.AbstractConfigScreen configScreen) {
                        for (var child : configScreen.childrenL()) {
                            if (child instanceof Button button
                                    && button.getMessage().equals(Component.translatable("text.cloth-config.save_and_done"))) {
                                button.setMessage(Component.literal("Done"));
                                break;
                            }
                        }
                    }
                });

        ConfigEntryBuilder entries = builder.entryBuilder();

        // General settings
        ConfigCategory general = builder.getOrCreateCategory(
                Component.literal("General"));

        general.addEntry(entries.startBooleanToggle(
                        Component.literal("Enable petting"),
                        config.pettingEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.pettingEnabled = value)
                .build());

        general.addEntry(entries.startBooleanToggle(
                        Component.literal("Chat messages"),
                        config.chatMessagesEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(value -> config.chatMessagesEnabled = value)
                .build());

        general.addEntry(entries.startStrField(
                        Component.literal("Chat message"),
                        config.chatMessage)
                .setDefaultValue("You give {entity} a quick pet :3")
                .setSaveConsumer(value -> config.chatMessage = value)
                .build());

        general.addEntry(entries.startIntField(
                        Component.literal("Chat cooldown (seconds)"),
                        config.chatCooldownSeconds)
                .setDefaultValue(300)
                .setMin(0)
                .setMax(86400)
                .setSaveConsumer(value -> config.chatCooldownSeconds = value)
                .build());

        // Entity filtering
        ConfigCategory filtering = builder.getOrCreateCategory(
                Component.literal("Entity Filtering"));

        filtering.addEntry(entries.startEnumSelector(
                        Component.literal("Presets"),
                        PitPatPotConf.EntityPreset.class,
                        config.entityPreset)
                .setDefaultValue(PitPatPotConf.EntityPreset.ALL)
                .setSaveConsumer(value -> config.entityPreset = value)
                .build());

        filtering.addEntry(entries.startEnumSelector(
                        Component.literal("Exception mode toggle"),
                        PitPatPotConf.EntityListMode.class,
                        config.entityListMode)
                .setDefaultValue(PitPatPotConf.EntityListMode.BLACKLIST)
                .setTooltip(
                        Component.literal("Whitelist: Allowed in addition to preset."),
                        Component.literal("Blacklist: Disallowed, regardless of preset."))
                .setSaveConsumer(value -> config.entityListMode = value)
                .build());

        filtering.addEntry(entries.startStrList(
                        Component.literal("Exceptions (As ID)"),
                        config.entityList)
                .setDefaultValue(List.of())
                .setTooltip(Component.literal(
                        "One entity per line. 'wolf' means 'minecraft:wolf'. Modded mobs need their full ID, e.g. 'supercoolmod:big_fish'."))
                .setCellErrorSupplier(text -> {
                    Identifier id = Identifier.tryParse(PitPatPotConf.normaliseId(text));
                    if (id == null) {
                        return Optional.of(Component.literal("Not a valid ID: " + text));
                    }
                    if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
                        return Optional.of(Component.literal("Unknown entity: " + id));
                    }
                    return Optional.empty();
                })
                .setSaveConsumer(value -> config.entityList = value.stream()
                        .map(PitPatPotConf::normaliseId)
                        .distinct()
                        .collect(java.util.stream.Collectors.toCollection(ArrayList::new)))
                .build());

        // Particle settings
        ConfigCategory particles = builder.getOrCreateCategory(
                Component.literal("Particles"));

        particles.addEntry(entries.startIntField(
                        Component.literal("Hearts per pat"),
                        config.heartsPerPat)
                .setDefaultValue(3)
                .setMin(0)
                .setMax(100)
                .setTooltip(Component.literal("Set to 0 to disable heart particles"))
                .setSaveConsumer(value -> config.heartsPerPat = value)
                .build());

        return builder.build();
    }
}

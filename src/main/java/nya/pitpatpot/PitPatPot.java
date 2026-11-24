package nya.pitpatpot;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import nya.pitpatpot.config.PitPatPotConf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PitPatPot implements ModInitializer {
	public static final String MOD_ID = "pitpatpot";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// This DefaultParticleType gets called when you want to use your particle in code.
	public static final SimpleParticleType PET_HEART = FabricParticleTypes.simple();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		// Posts the startup message to log
		LOGGER.info("PitPatPot Enabled!");
		// Loads conf
		PitPatPotConf.load();
		// Register our custom particle type in the mod initializer.
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, id("pet_heart"), PET_HEART);
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}


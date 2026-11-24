package nya.pitpatpot.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.Mob;
import net.minecraft.util.RandomSource;
import nya.pitpatpot.PitPatPot;
import nya.pitpatpot.config.PitPatPotConf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PitPatPotClient implements ClientModInitializer {
	private static final Map<UUID, Long> LAST_PET_MESSAGE = new HashMap<>();
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		PitPatPot.LOGGER.info("Running PitPatPot client!");
		// Particle selection and stuff.
		ParticleProviderRegistry.getInstance().register(PitPatPot.PET_HEART, PetHeartParticle.Provider::new);
		// Pat detection! I hope
		UseEntityCallback.EVENT.register((player, world, hand, entity, hitresult) -> {

			if (world.isClientSide() && hand == InteractionHand.MAIN_HAND && player.isCrouching() && player.getMainHandItem().isEmpty() && PitPatPotConf.INSTANCE.pettingEnabled && isEntityAllowed(entity)) {
				//Console logger, logs every pet to console,
				//PitPatPot.LOGGER.info("You pet the " + entity.getName().getString());
				// Gives a cooldown for the chat message
				long currentTime = System.currentTimeMillis();
				UUID animalId = entity.getUUID();
				Long lastMessageTime = LAST_PET_MESSAGE.get(animalId);

				long cooldown = PitPatPotConf.INSTANCE.chatCooldownSeconds * 1000L;
				// Forget entries once their cooldown (plus a minute) has passed
				LAST_PET_MESSAGE.values().removeIf(time -> currentTime - time >= cooldown);
				PitPatPot.LOGGER.info("tracked: {}", LAST_PET_MESSAGE.size());
				if (PitPatPotConf.INSTANCE.chatMessagesEnabled
						&& (lastMessageTime == null || currentTime - lastMessageTime >= cooldown)) {
					String petName = entity.hasCustomName()
							? entity.getName().getString()
							: "the " + entity.getName().getString().toLowerCase(java.util.Locale.ROOT);

					String chatMessage = PitPatPotConf.INSTANCE.chatMessage;

					if (chatMessage == null || chatMessage.isBlank()) {
						chatMessage = "You give {entity} a quick pet :3";
					}

					String message = chatMessage.replace("{entity}", petName);
					player.sendSystemMessage(Component.literal("[PitPatPot] " + message));
					LAST_PET_MESSAGE.put(animalId, currentTime);
				}
				// Buckshot
				double baseX = entity.getX();
				double baseY = entity.getY() + entity.getBbHeight() + 0.2;
				double baseZ = entity.getZ();
				double width = entity.getBbWidth();
				RandomSource random = world.getRandom();

				for (int i = 0; i < PitPatPotConf.INSTANCE.heartsPerPat; i++) {

					// Fire muh lasers
					double offsetX = (random.nextDouble() - 0.5) * width;
					double offsetZ = (random.nextDouble() - 0.5) * width;

					// Ascend
					double velocityX = (random.nextDouble() - 0.5) * 0.04;
					double velocityY = 0.04 + random.nextDouble() * 0.08;
					double velocityZ = (random.nextDouble() - 0.5) * 0.04;

					world.addParticle(
							PitPatPot.PET_HEART,
							false,
							false,
							baseX + offsetX,
							baseY,
							baseZ + offsetZ,
							velocityX,
							velocityY,
							velocityZ
					);
				}
				return  InteractionResult.FAIL;
			}
			return InteractionResult.PASS;
		});
	}
	// Checks if mob is, indeed, pettable
	private static boolean isEntityAllowed(Entity entity) {
		String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();

		boolean allowedByPreset = switch (PitPatPotConf.INSTANCE.entityPreset) {
			case ALL -> true;
			case MOBS_ONLY -> entity instanceof Mob;
			case NO_MONSTERS -> !(entity instanceof Monster);
		};

		boolean isInList = PitPatPotConf.INSTANCE.entityList.contains(entityId);

		if (PitPatPotConf.INSTANCE.entityListMode == PitPatPotConf.EntityListMode.WHITELIST) {
			return allowedByPreset || isInList;
		}

		return allowedByPreset && !isInList;
	}
}
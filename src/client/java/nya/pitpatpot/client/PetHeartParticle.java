
package nya.pitpatpot.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class PetHeartParticle extends SingleQuadParticle {

    protected PetHeartParticle(
            ClientLevel level,
            double x, double y, double z,
            double velocityX, double velocityY, double velocityZ,
            SpriteSet sprites
    ) {
        super(level, x, y, z, 0, 0, 0, sprites.first());

        // Apply the exact velocities supplied by our particle burst
        this.setParticleSpeed(velocityX, velocityY, velocityZ);

        // Movement
        this.gravity = 0.0F;
        this.friction = 0.98F;
        this.hasPhysics = false;

        // Appearance and lifetime
        this.scale(0.65F);
        this.setLifetime(30);
    }

    @Override
    public void tick() {
        super.tick();

        // Fade out over the last 10 ticks of life
        int fadeTicks = 10;
        int remaining = this.lifetime - this.age;
        if (remaining < fadeTicks) {
            this.alpha = Math.max(0.0F, remaining / (float) fadeTicks);
        }
    }

    @Override
    public SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x, double y, double z,
                double velocityX, double velocityY, double velocityZ,
                RandomSource random
        ) {
            return new PetHeartParticle(
                    level, x, y, z,
                    velocityX, velocityY, velocityZ,
                    sprites
            );
        }
    }
}

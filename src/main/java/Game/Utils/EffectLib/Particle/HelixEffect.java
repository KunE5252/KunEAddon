package Game.Utils.EffectLib.Particle;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.SimpleParticle;
import org.bukkit.Location;

public class HelixEffect extends GameTimer {

    public SimpleParticle particle = ParticleLib.REDSTONE;
    public int strands = 8;
    public int particles = 80;
    public float radius = 10;
    public float curve = 10;
    public double rotation = Math.PI / 4;

    private final Location location;

    public HelixEffect(AbstractGame abstractGame, int iterations, Location location) {
        abstractGame.super(TaskType.NORMAL, iterations);
        setPeriod(TimeUnit.TICKS, 10);
        this.location = location;
    }

    @Override
    public void run(int count) {
        for (int i = 1; i <= strands; i++) {
            for (int j = 1; j <= particles; j++) {
                float ratio = (float) j / particles;
                double angle = curve * ratio * 2 * Math.PI / strands + (2 * Math.PI * i / strands) + rotation;
                double x = Math.cos(angle) * ratio * radius;
                double z = Math.sin(angle) * ratio * radius;
                location.add(x, 0, z);
                particle.spawnParticle(location);
                location.subtract(x, 0, z);
            }
        }
    }

}

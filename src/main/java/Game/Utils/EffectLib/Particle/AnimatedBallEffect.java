package Game.Utils.EffectLib.Particle;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.FastMath;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.SimpleParticle;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class AnimatedBallEffect extends GameTimer {

    public SimpleParticle particle = ParticleLib.REDSTONE;
    public int particles = 150;
    public int particlesPerIteration = 10;
    public float size = 1F;
    public float xFactor = 1F, yFactor = 2F, zFactor = 1F;
    public float xOffset, yOffset = 0.8F, zOffset;
    public double xRotation, yRotation, zRotation = 0;
    private final Location location;

    public AnimatedBallEffect(AbstractGame abstractGame, int iterations, Location location) {
        abstractGame.super(TaskType.NORMAL, iterations);
        setPeriod(TimeUnit.TICKS, 10);
        this.location = location;
    }

    @Override
    public void run(int count) {
        Vector vector = new Vector();
        for (int i = 0; i < particlesPerIteration; i++) {

            double t = (Math.PI / particles) * ((count - 1) * particlesPerIteration + i);
            double r = FastMath.sin(t) * size;
            double s = 2 * Math.PI * t;

            vector.setX(xFactor * r * FastMath.cos(s) + xOffset);
            vector.setZ(zFactor * r * FastMath.sin(s) + zOffset);
            vector.setY(yFactor * size * FastMath.cos(t) + yOffset);

            VectorUtil.rotateAroundAxisX(vector, xRotation);
            VectorUtil.rotateAroundAxisY(vector, yRotation);
            VectorUtil.rotateAroundAxisZ(vector, zRotation);

            particle.spawnParticle(location.clone().add(vector));
        }
    }

}
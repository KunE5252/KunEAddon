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

public class HeartEffect extends GameTimer {

    public SimpleParticle particle = ParticleLib.REDSTONE;
    public int particles = 50;
    public double xRotation, yRotation, zRotation = 0;
    public double yFactor = 1, xFactor = 1;
    public double factorInnerSpike = 0.8;
    public double compressYFactorTotal = 2;
    public float compilaction = 2F;

    private final Location location;

    public HeartEffect(AbstractGame abstractGame, int iterations, Location location) {
        abstractGame.super(TaskType.NORMAL, iterations);
        setPeriod(TimeUnit.TICKS, 10);
        this.location = location;
    }

    @Override
    public void run(int count) {
        Vector vector = new Vector();
        for (int i = 0; i < particles; i++) {
            double alpha = ((Math.PI / compilaction) / particles) * i;
            double phi = Math.pow(Math.abs(FastMath.sin(2 * compilaction * alpha)) + factorInnerSpike * Math.abs(FastMath.sin(compilaction * alpha)), 1 / compressYFactorTotal);

            vector.setY(phi * (FastMath.sin(alpha) + FastMath.cos(alpha)) * yFactor);
            vector.setZ(phi * (FastMath.cos(alpha) - FastMath.sin(alpha)) * xFactor);

            VectorUtil.rotateAroundAxisX(vector, Math.toDegrees(xRotation));
            VectorUtil.rotateAroundAxisY(vector, Math.toDegrees(yRotation));
            VectorUtil.rotateAroundAxisZ(vector, Math.toDegrees(zRotation));

            particle.spawnParticle(location.add(vector));
            location.subtract(vector);
        }
    }

}
package KunE.Addon.Game.Utils.EffectLib.Particle;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.SimpleParticle;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Random;

public class ConeEffect extends GameTimer {

    public SimpleParticle particle = ParticleLib.REDSTONE;
    public float lengthGrow = .05f;
    public double angularVelocity = Math.PI / 16;
    public int particles = 10;
    public float radiusGrow = 0.006f;
    public int particlesCone = 180;
    public double rotation = 0;
    public boolean randomize = false;
    protected int step = 0;

    private final Location location;
    private final Random random = new Random();

    public ConeEffect(AbstractGame abstractGame, int iterations, Location location) {
        abstractGame.super(TaskType.NORMAL, iterations);
        setPeriod(TimeUnit.TICKS, 10);
        this.location = location;
    }

    @Override
    public void run(int count) {
        for (int x = 0; x < particles; x++) {
            if (step > particlesCone) {
                step = 0;
            }
            if (randomize && step == 0) {
                rotation = random.nextDouble() * 2 * Math.PI;
            }
            double angle = step * angularVelocity + rotation;
            float radius = step * radiusGrow;
            float length = step * lengthGrow;
            Vector v = new Vector(Math.cos(angle) * radius, length, Math.sin(angle) * radius);
            VectorUtil.rotateAroundAxisX(v, location.getPitch() + 90);
            VectorUtil.rotateAroundAxisY(v, -location.getYaw());

            location.add(v);
            particle.spawnParticle(location);
            location.subtract(v);
            step++;
        }
    }
}
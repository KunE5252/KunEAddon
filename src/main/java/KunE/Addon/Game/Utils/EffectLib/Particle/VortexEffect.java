package KunE.Addon.Game.Utils.EffectLib.Particle;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.SimpleParticle;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class VortexEffect extends GameTimer {

    public SimpleParticle particle = ParticleLib.REDSTONE;
    public float radius = 2;
    public float grow = .05f;
    public double radials = Math.PI / 16;
    public int circles = 3;
    public int helixes = 4;
    protected int step = 0;

    private final Location location;

    public VortexEffect(AbstractGame abstractGame, int iterations, Location location) {
        abstractGame.super(TaskType.NORMAL, iterations);
        setPeriod(TimeUnit.TICKS, 10);
        this.location = location;
    }

    @Override
    public void run(int count) {
        for (int x = 0; x < circles; x++) {
            for (int i = 0; i < helixes; i++) {
                double angle = step * radials + (2 * Math.PI * i / helixes);
                Vector v = new Vector(Math.cos(angle) * radius, step * grow, Math.sin(angle) * radius);
                VectorUtil.rotateAroundAxisX(v, location.getPitch() + 90);
                VectorUtil.rotateAroundAxisY(v, -location.getYaw());

                location.add(v);
                particle.spawnParticle(location);
                location.subtract(v);
            }
            step++;
        }
    }

}
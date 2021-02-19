package KunE.Addon.Game.Utils.EffectLib.Particle;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.ColouredParticle;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Random;

public class DiscoBallEffect extends GameTimer {

    public float sphereRadius = .6f;
    public int max = 15;
    public ColouredParticle lineParticle = ParticleLib.REDSTONE;
    public ColouredParticle sphereParticle = ParticleLib.REDSTONE;
    public RGB sphereColor = null, lineColor = null;
    public int maxLines = 7;
    public int lineParticles = 100, sphereParticles = 50;
    public Direction direction = Direction.DOWN;
    private final Random random = new Random();
    private final Location location;

    public DiscoBallEffect(AbstractGame abstractGame, int iterations, Location location) {
        abstractGame.super(TaskType.NORMAL, iterations);
        setPeriod(TimeUnit.TICKS, 10);
        this.location = location;
    }

    @Override
    public void run(int count) {
        //Lines
        int mL = random.nextInt(maxLines - 2) + 2;
        for (int m = 0; m < mL * 2; m++) {
            double x = random.nextInt(max - max * (-1)) + max * (-1);
            double y = random.nextInt(max - max * (-1)) + max * (-1);
            double z = random.nextInt(max - max * (-1)) + max * (-1);
            if (direction == Direction.DOWN) {
                y = random.nextInt(max * 2 - max) + max;
            } else if (direction == Direction.UP) {
                y = random.nextInt(max * (-1) - max * (-2)) + max * (-2);
            }
            Location target = location.clone().subtract(x, y, z);
            if (target == null) {
                stop(false);
                return;
            }
            Vector link = target.toVector().subtract(location.toVector());
            float length = (float) link.length();
            link.normalize();

            float ratio = length / lineParticles;
            Vector v = link.multiply(ratio);
            Location loc = location.clone().subtract(v);
            for (int i = 0; i < lineParticles; i++) {
                loc.add(v);
                lineParticle.spawnParticle(loc, lineColor);
            }
        }

        //Sphere
        for (int i = 0; i < sphereParticles; i++) {
            Vector vector = new Vector(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1).normalize().multiply(sphereRadius);
            location.add(vector);
            sphereParticle.spawnParticle(location, sphereColor);
            location.subtract(vector);
        }
    }

    public enum Direction {

        UP, DOWN, BOTH;
    }

}
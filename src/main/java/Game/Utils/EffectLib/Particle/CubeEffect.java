package Game.Utils.EffectLib.Particle;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.SimpleParticle;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class CubeEffect extends GameTimer {

    public SimpleParticle particle = ParticleLib.REDSTONE;
    public float edgeLength = 3;
    public double angularVelocityX = Math.PI / 200;
    public double angularVelocityY = Math.PI / 170;
    public double angularVelocityZ = Math.PI / 155;
    public int particles = 8;
    public boolean enableRotation = true;
    public boolean outlineOnly = true;
    protected int step = 0;

    private final Location location;

    public CubeEffect(AbstractGame abstractGame, int iterations, Location location) {
        abstractGame.super(TaskType.NORMAL, iterations);
        setPeriod(TimeUnit.TICKS, 10);
        this.location = location;
    }

    @Override
    public void run(int count) {
        if (outlineOnly) {
            drawCubeOutline(location);
        } else {
            drawCubeWalls(location);
        }
        step++;
    }

    private void drawCubeOutline(Location location) {
        double xRotation = 0, yRotation = 0, zRotation = 0;
        if (enableRotation) {
            xRotation = step * angularVelocityX;
            yRotation = step * angularVelocityY;
            zRotation = step * angularVelocityZ;
        }
        float a = edgeLength / 2;
        double angleX, angleY;
        Vector v = new Vector();
        for (int i = 0; i < 4; i++) {
            angleY = i * Math.PI / 2;
            for (int j = 0; j < 2; j++) {
                angleX = j * Math.PI;
                for (int p = 0; p <= particles; p++) {
                    v.setX(a).setY(a);
                    v.setZ(edgeLength * p / particles - a);
                    VectorUtil.rotateAroundAxisX(v, Math.toDegrees(angleX));
                    VectorUtil.rotateAroundAxisY(v, Math.toDegrees(angleY));

                    if (enableRotation) {
                        VectorUtil.rotateAroundAxisX(v, Math.toDegrees(xRotation));
                        VectorUtil.rotateAroundAxisY(v, Math.toDegrees(yRotation));
                        VectorUtil.rotateAroundAxisZ(v, Math.toDegrees(zRotation));
                    }
                    particle.spawnParticle(location);
                    location.subtract(v);
                }
            }
            for (int p = 0; p <= particles; p++) {
                v.setX(a).setZ(a);
                v.setY(edgeLength * p / particles - a);
                VectorUtil.rotateAroundAxisY(v, angleY);

                if (enableRotation) {
                    VectorUtil.rotateAroundAxisX(v, Math.toDegrees(xRotation));
                    VectorUtil.rotateAroundAxisY(v, Math.toDegrees(yRotation));
                    VectorUtil.rotateAroundAxisZ(v, Math.toDegrees(zRotation));
                }
                particle.spawnParticle(location);
                location.subtract(v);
            }
        }
    }

    private void drawCubeWalls(Location location) {
        double xRotation = 0, yRotation = 0, zRotation = 0;
        if (enableRotation) {
            xRotation = step * angularVelocityX;
            yRotation = step * angularVelocityY;
            zRotation = step * angularVelocityZ;
        }
        float a = edgeLength / 2;
        for (int x = 0; x <= particles; x++) {
            float posX = edgeLength * ((float) x / particles) - a;
            for (int y = 0; y <= particles; y++) {
                float posY = edgeLength * ((float) y / particles) - a;
                for (int z = 0; z <= particles; z++) {
                    if (x != 0 && x != particles && y != 0 && y != particles && z != 0 && z != particles) {
                        continue;
                    }
                    float posZ = edgeLength * ((float) z / particles) - a;
                    Vector v = new Vector(posX, posY, posZ);
                    if (enableRotation) {
                        VectorUtil.rotateAroundAxisX(v, Math.toDegrees(xRotation));
                        VectorUtil.rotateAroundAxisY(v, Math.toDegrees(yRotation));
                        VectorUtil.rotateAroundAxisZ(v, Math.toDegrees(zRotation));
                    }
                    particle.spawnParticle(location);
                    location.subtract(v);
                }
            }
        }
    }
}
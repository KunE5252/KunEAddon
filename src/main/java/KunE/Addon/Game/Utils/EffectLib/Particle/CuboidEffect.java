package KunE.Addon.Game.Utils.EffectLib.Particle;

import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.SimpleParticle;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class CuboidEffect extends GameTimer {

    public SimpleParticle particle = ParticleLib.REDSTONE;
    public int particles = 8;
    public double xLength = 0;
    public double yLength = 0;
    public double zLength = 0;
    public double padding = 0;
    public boolean blockSnap = false;
    protected Location minCorner;
    protected boolean initialized;

    private final Location location;

    public CuboidEffect(AbstractGame abstractGame, int iterations, Location location) {
        abstractGame.super(TaskType.NORMAL, iterations);
        setPeriod(TimeUnit.TICKS, 10);
        this.location = location;
    }

    @Override
    public void run(int count) {
        Location target = this.location;
        if (!initialized) {
            if (blockSnap) {
                target = target.getBlock().getLocation();
                minCorner = location.getBlock().getLocation();
            } else {
                minCorner = location.clone();
            }
            if (xLength == 0 && yLength == 0 && zLength == 0) {
                if (target == null || !target.getWorld().equals(location.getWorld())) {
                    stop(false);
                    return;
                }
                if (target.getX() < minCorner.getX()) {
                    minCorner.setX(target.getX());
                }
                if (target.getY() < minCorner.getY()) {
                    minCorner.setY(target.getY());
                }
                if (target.getZ() < minCorner.getZ()) {
                    minCorner.setZ(target.getZ());
                }
                if (padding != 0) {
                    minCorner.add(-padding, -padding, -padding);
                }
                double extra = padding * 2;
                if (blockSnap) extra++;
                xLength = Math.abs(location.getX() - target.getX()) + extra;
                yLength = Math.abs(location.getY() - target.getY()) + extra;
                zLength = Math.abs(location.getZ() - target.getZ()) + extra;
            }
            initialized = true;
        }
        drawOutline(location, target);
    }

    private void drawOutline(Location location, Location target) {
        Vector v = new Vector();
        for (int i = 0; i < particles; i++) {
            drawEdge(v, i, 0, 2, 2);
            drawEdge(v, i, 0, 1, 2);
            drawEdge(v, i, 0, 1, 1);
            drawEdge(v, i, 0, 2, 1);

            drawEdge(v, i, 2, 0, 2);
            drawEdge(v, i, 1,0, 2);
            drawEdge(v, i, 1,0, 1);
            drawEdge(v, i, 2,0, 1);

            drawEdge(v, i, 2, 2, 0);
            drawEdge(v, i, 1, 2, 0);
            drawEdge(v, i, 1, 1, 0);
            drawEdge(v, i, 2, 1, 0);
        }
    }

    private void drawEdge(Vector v, int i, int dx, int dy, int dz) {
        if (dx == 0) {
            v.setX(xLength * i / particles);
        } else {
            v.setX(xLength * (dx - 1));
        }
        if (dy == 0) {
            v.setY(yLength * i / particles);
        } else {
            v.setY(yLength * (dy - 1));
        }
        if (dz == 0) {
            v.setZ(zLength * i / particles);
        } else {
            v.setZ(zLength * (dz - 1));
        }
        particle.spawnParticle(location);
        minCorner.subtract(v);
    }
}
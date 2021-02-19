package KunE.Addon.KunEAbility;

import KunE.Addon.KunAddon;
import KunE.Addon.KunEAbility.AbilityEffect.Thorn;
import com.google.common.collect.Iterables;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.module.DeathManager.Handler;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.color.Gradient;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.math.geometry.ImageVector;
import daybreak.abilitywar.utils.base.math.geometry.ImageVector.Point2D;
import daybreak.abilitywar.utils.base.minecraft.damage.Damages;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.minecraft.raytrace.RayTrace;
import daybreak.abilitywar.utils.base.minecraft.version.ServerVersion;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.abilitywar.utils.library.item.ItemLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.function.Predicate;

@AbilityManifest(name = "메이릴",rank = Rank.S, species = Species.GOD, explain = {
        "§7근접 공격 §8- §a하프 연주§f: 메이릴이 주는 모든 공격 데미지는 원거리 데미지 취급이 됩니다, 또한 $[HAF_CONFIG]% 확률로 주는 데미지의 2배의 데미지로 공격합니다.",
        "§7철괴 우클릭 §8- §b요정의 행운§f: 자신의 위치를 기준으로 반지름 4칸의 구역을 생성하며 해당 구역은 $[RIGHT_CLICK_DURATION]초 동안 지속됩니다.",
        " 또한 이때 호수에 존재하는 모든 플레이어는 속박의 가시에 $[THORN_DURATION]초 동안 휩싸입니다. §c쿨타임 §7: §f$[RIGHT_CLICK_COOLDOWN_CONFIG]초",
        "§7철괴 좌클릭 §8- §2페어리의 도움§f: 메이릴의 주변에 페어리가 생성되며,",
        " 2마리의 페어리가 메이릴이 바라보는 방향으로 투사체를 발사합니다. §c쿨타임 §7: §f$[LEFT_CLICK_COOLDOWN_CONFIG]초",
        "§7상태이상 §8- §2속박의 가시§f: 속박에 가시 효과를 보유 중인 대상은 이동 불능 상태에 돌입하며, 움직임을 시도할 경우 독에 감염됩니다."
})
public class Mayreel extends AbilityBase implements ActiveHandler {
    public static final AbilitySettings.SettingObject<Integer> RIGHT_CLICK_COOLDOWN_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Mayreel.class, "RIGHT_COOLDOWN", 120,
            "# 우클릭 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> RIGHT_CLICK_DURATION = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Mayreel.class, "RIGHTCLICK_DURATION", 5,
            "# 우클릭 지속시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> THORN_DURATION = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Mayreel.class, "THORN_DURATION", 2,
            "# 우클릭 속박의 가시 지속시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> LEFT_CLICK_COOLDOWN_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Mayreel.class, "LEFT_COOLDOWN", 60,
            "# 좌클릭 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> LEFT_CLICK_DURATION = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Mayreel.class, "LEFT_DURATION", 10,
            "# 좌클릭 지속시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> HAF_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Mayreel.class, "HAF_CONFIG", 3,
            "# 기본 공격 데미지 배율 확률") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };

    private static final ImageVector imageVector = ImageVector.parse(Mayreel.class.getResourceAsStream("/ImageParticle/clover.png")).addTransparent(0);
    private static final ImageVector imageVectorcrit = ImageVector.parse(Mayreel.class.getResourceAsStream("/ImageParticle/greenapple.png")).addTransparent(0);
    private static final ImageVector imageVectorfiary = ImageVector.parse(Mayreel.class.getResourceAsStream("/ImageParticle/fairy.png")).addTransparent(0);

    private final Cooldown right_cooldown = new Cooldown(RIGHT_CLICK_COOLDOWN_CONFIG.getValue(), "요정의 행운", 50);
    private final Cooldown left_cooldown = new Cooldown(LEFT_CLICK_COOLDOWN_CONFIG.getValue(), "페어리의 도움", 50);
    private final int hafint = HAF_CONFIG.getValue();
    private final int thornint = THORN_DURATION.getValue();
    private final int rightClickDuration = RIGHT_CLICK_DURATION.getValue();
    private final int leftClickDuration = LEFT_CLICK_DURATION.getValue();
    private final Circle circleVectors = Circle.of(4, 70);
    private MayreelZone mayreelZone = null;
    private FairyZone fairyZone = null;
    private static final List<RGB> gradation = Gradient.createGradient(25,
            new RGB(166, 255, 77),
            new RGB(130, 255, 77),
            new RGB(89, 255, 77),
            new RGB(77, 255, 101),
            new RGB(166, 255, 77));
    private final Predicate<Entity> predicate = new Predicate<Entity>() {
        @Override
        public boolean test(Entity entity) {
            if (entity.equals(getPlayer())) return false;
            if (!getGame().isParticipating(entity.getUniqueId())
                    || (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
                    || !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
                return false;
            }
            if (getGame() instanceof Teamable) {
                final Teamable teamGame = (Teamable) getGame();
                final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = getParticipant();
                return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
            }
            return true;
        }
    };

    private final Random random = new Random();
    private static final String prefix = "§2[§a메이릴§2] §f";

    public Mayreel(Participant participant) {
        super(participant);
    }

    @SubscribeEvent
    private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        double damage = e.getDamage();
        if (hafint > random.nextInt(100)) {
            if (e.getDamager().equals(getParticipant().getPlayer())) {
                e.setCancelled(true);
                Damages.damageArrow(e.getEntity(), getPlayer(), (float) damage * 2);
                getPlayer().sendMessage(prefix + "2배 데미지로 공격되었습니다.");
                SoundLib.BLOCK_NOTE_BLOCK_BELL.playSound(getPlayer());
                final Location playerLocation = getPlayer().getLocation().clone().add(0, 4, 0);
                for (Point2D point2D : imageVectorcrit) {
                    ParticleLib.REDSTONE.spawnParticle(playerLocation.clone().add(VectorUtil.rotateAroundAxisY(point2D.clone(), -playerLocation.getYaw())), point2D.getColor());
                }

            }
        } else {
            if (e.getDamager().equals(getParticipant().getPlayer())) {
                e.setCancelled(true);
                Damages.damageArrow(e.getEntity(), getPlayer(), (float) damage);
            }
        }

    }


    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            if (!right_cooldown.isCooldown()) {
                if (mayreelZone == null) {
                    new MayreelZone(rightClickDuration, getPlayer().getLocation()).start();
                    return true;
                }
            }
        } else if (clickType == ClickType.LEFT_CLICK) {
            if (!left_cooldown.isCooldown()) {
                if (fairyZone == null) {
                    new FairyZone(leftClickDuration, getPlayer().getLocation()).start();
                    return true;
                }
            }
        }
        return false;
    }

    private class MayreelZone extends AbilityTimer implements Listener {

        private final ActionbarChannel actionbar = getParticipant().actionbar().newChannel();
        private final Iterator<RGB> iterator = Iterables.cycle(gradation).iterator();

        private MayreelZone(final int duration, final Location center) {
            super(TaskType.REVERSE, duration * 10);
            setPeriod(TimeUnit.TICKS, 2);
            mayreelZone = this;
        }

        @Override
        protected void onEnd() {
            onSilentEnd();
        }

        @Override
        protected void onSilentEnd() {
            actionbar.unregister();
            right_cooldown.start();
            mayreelZone = null;
        }

        @Override
        protected void run(int count) {
            final RGB color = iterator.next();
            for (final Location location : circleVectors.toLocations(getPlayer().getLocation()).floor(getPlayer().getLocation().getY())) {
                ParticleLib.REDSTONE.spawnParticle(location, color);
            }
            if (count % 5 == 0) {
                final Location playerLocation = getPlayer().getLocation().clone().add(0, 4, 0);
                for (Point2D point2D : imageVector) {
                    ParticleLib.REDSTONE.spawnParticle(playerLocation.clone().add(VectorUtil.rotateAroundAxisY(point2D.clone(), -playerLocation.getYaw())), point2D.getColor());
                }
            }
            for (Player player : LocationUtil.getEntitiesInCircle(Player.class, getPlayer().getLocation(), 4, predicate)) {
                Participant participant = getGame().getParticipant(player);
                Thorn.apply(participant, TimeUnit.SECONDS, thornint);
            }
            actionbar.update("§a요정의 행운 지속시간 §f: " + (count / 10.0) + "초");
        }
    }

    private class FairyZone extends AbilityTimer implements Listener {

        private final ActionbarChannel Fairyactionbar = getParticipant().actionbar().newChannel();

        private FairyZone(final int duration, final Location center) {
            super(TaskType.REVERSE, duration * 10);
            setPeriod(TimeUnit.TICKS, 2);
            fairyZone = this;
        }

        @Override
        protected void onEnd() {
            onSilentEnd();
        }

        @Override
        protected void onSilentEnd() {
            Fairyactionbar.unregister();
            fairyZone = null;
            left_cooldown.start();
        }

        @Override
        protected void run(int count) {
            final Location playerLocation = getPlayer().getLocation().clone().add(0, 1, 0);
            final Vector left = VectorUtil.rotateAroundAxisY(playerLocation.getDirection().setY(0).normalize(), -90).multiply(4), right  = VectorUtil.rotateAroundAxisY(playerLocation.getDirection().setY(0).normalize(), 90).multiply(4);
            if (count % 5 == 0) {
                final Location focus = getPlayer().getTargetBlock(null, 15).getLocation();
                for (Location location : new Location[] {playerLocation.clone().add(left), playerLocation.clone().add(right)}) {
                    new Bullet(getPlayer(), location, focus.toVector().subtract(location.toVector()), focus, BULLET_COLOR).start();
                    SoundLib.ENTITY_ARROW_HIT_PLAYER.playSound(getPlayer());
                }
                for (Point2D point2D : imageVectorfiary) {
                    final Vector looking = VectorUtil.rotateAroundAxisY(point2D.clone(), -playerLocation.getYaw());
                    ParticleLib.REDSTONE.spawnParticle(playerLocation.clone().add(looking).add(left), point2D.getColor());
                    ParticleLib.REDSTONE.spawnParticle(playerLocation.clone().add(looking).add(right), point2D.getColor());
                }
            }
            Fairyactionbar.update("§b페어리의 축복 지속시간 §f: " + (count / 10.0) + "초");
        }
    }

    private static final Material GLASS_PANE = ServerVersion.getVersion() > 12 ? Material.valueOf("GLASS_PANE") : Material.valueOf("THIN_GLASS");
    private static final RGB BULLET_COLOR = new RGB(99, 255, 82);

    public class Bullet extends AbilityTimer {

        private final LivingEntity shooter;
        private final CustomEntity entity;
        private final Location startLocation;
        private final double focusDistanceSquared;
        private final Vector forward;
        private final Predicate<Entity> predicate;

        private final RGB color;

        private Bullet(LivingEntity shooter, Location startLocation, Vector arrowVelocity, Location focus, RGB color) {
            super(5);
            setPeriod(TimeUnit.TICKS, 1);
            this.shooter = shooter;
            this.entity = new ArrowEntity(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()).resizeBoundingBox(-.75, -.75, -.75, .75, .75, .75);
            this.forward = arrowVelocity.multiply(3);
            this.startLocation = startLocation;
            this.focusDistanceSquared = startLocation.distanceSquared(focus);
            this.color = color;
            this.lastLocation = startLocation;
            this.predicate = new Predicate<Entity>() {
                @Override
                public boolean test(Entity entity) {
                    if (entity.equals(shooter)) return false;
                    if (entity instanceof Player) {
                        if (!getGame().isParticipating(entity.getUniqueId())
                                || (getGame() instanceof Handler && ((Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
                                || !getGame().getParticipant(entity.getUniqueId()).attributes().TARGETABLE.getValue()) {
                            return false;
                        }
                        if (getGame() instanceof Teamable) {
                            final Teamable teamGame = (Teamable) getGame();
                            final Participant entityParticipant = teamGame.getParticipant(entity.getUniqueId()), participant = teamGame.getParticipant(shooter.getUniqueId());
                            if (participant != null) {
                                return !teamGame.hasTeam(entityParticipant) || !teamGame.hasTeam(participant) || (!teamGame.getTeam(entityParticipant).equals(teamGame.getTeam(participant)));
                            }
                        }
                    }
                    return true;
                }
            };
        }

        private Location lastLocation;

        @Override
        protected void run(int i) {
            final Location newLocation = lastLocation.clone().add(forward);
            for (Iterator<Location> iterator = new Iterator<Location>() {
                private final Vector vectorBetween = newLocation.toVector().subtract(lastLocation.toVector()), unit = vectorBetween.clone().normalize().multiply(.35);
                private final int amount = (int) (vectorBetween.length() / .35);
                private int cursor = 0;

                @Override
                public boolean hasNext() {
                    return cursor < amount;
                }

                @Override
                public Location next() {
                    if (cursor >= amount) throw new NoSuchElementException();
                    cursor++;
                    return lastLocation.clone().add(unit.clone().multiply(cursor));
                }
            }; iterator.hasNext(); ) {
                final Location location = iterator.next();
                if (startLocation.distanceSquared(location) >= focusDistanceSquared) {
                    stop(true);
                    return;
                }
                entity.setLocation(location);
                if (!isRunning()) {
                    return;
                }
                final Block block = location.getBlock();
                final Material type = block.getType();
                final double y = location.getY();
                if (y < 0 || y > 256 || !location.getChunk().isLoaded()) {
                    stop(false);
                    return;
                }
                if (type.isSolid()) {
                    if (ItemLib.STAINED_GLASS.compareType(type) || Material.GLASS == type || ItemLib.STAINED_GLASS_PANE.compareType(type) || type == GLASS_PANE) {
                        block.breakNaturally();
                        SoundLib.BLOCK_GLASS_BREAK.playSound(block.getLocation(), 3, 1);
                    } else if (RayTrace.hitsBlock(location.getWorld(), lastLocation.getX(), lastLocation.getY(), lastLocation.getZ(), location.getX(), location.getY(), location.getZ())) {
                        stop(false);
                        return;
                    }
                }
                for (Damageable damageable : LocationUtil.getConflictingEntities(Damageable.class, shooter.getWorld(), entity.getBoundingBox(), predicate)) {
                    if (!shooter.equals(damageable)) {
                        Damages.damageArrow(damageable, shooter, (float) Math.min((forward.getX() * forward.getX()) + (forward.getY() * forward.getY()) + (forward.getZ() * forward.getZ()) / 2.0, 2));
                        stop(false);
                        return;
                    }
                }
                ParticleLib.REDSTONE.spawnParticle(location, color);
            }
            lastLocation = newLocation;
        }

        @Override
        protected void onEnd() {
            entity.remove();
        }

        @Override
        protected void onSilentEnd() {
            entity.remove();
        }

        public class ArrowEntity extends CustomEntity implements Deflectable {

            public ArrowEntity(World world, double x, double y, double z) {
                getGame().super(world, x, y, z);
            }

            @Override
            public Vector getDirection() {
                return forward.clone();
            }

            @Override
            public void onDeflect(Participant deflector, Vector newDirection) {
                stop(false);
                final Player deflectedPlayer = deflector.getPlayer();
                new Bullet(deflectedPlayer, lastLocation, newDirection, lastLocation.clone().add(newDirection.clone().multiply(15)), color).start();
            }

            @Override
            public ProjectileSource getShooter() {
                return shooter;
            }

            @Override
            protected void onRemove() {
                Bullet.this.stop(false);
            }

        }

    }

}








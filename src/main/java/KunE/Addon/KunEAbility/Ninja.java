package KunE.Addon.KunEAbility;

import KunE.Addon.Game.Utils.EffectLib.Particle.AnimatedBallEffect;
import KunE.Addon.KunAddon;
import KunE.Addon.KunEAbility.AbilityEffect.Blind;
import com.google.common.collect.Iterables;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.CustomEntity;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.color.Gradient;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.geometry.ImageVector;
import daybreak.abilitywar.utils.base.math.geometry.ImageVector.Point2D;
import daybreak.abilitywar.utils.base.math.geometry.Line;
import daybreak.abilitywar.utils.base.minecraft.entity.decorator.Deflectable;
import daybreak.abilitywar.utils.base.minecraft.entity.health.event.PlayerSetHealthEvent;
import daybreak.abilitywar.utils.library.MaterialX;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.google.common.collect.ImmutableSet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Predicate;

@AbilityManifest(name = "닌자", rank = AbilityManifest.Rank.S, species = Species.HUMAN, explain = {
        "§7사망 §8- §d병합§f: 그림자 잔상의 위치에서 부활하며 이동속도가 빨라집니다.",
        "§7검 우클릭 §8- §8표창§f: 자신이 바라보는 방향으로 표창을 날립니다. §c쿨타임 §7: §f$[SWORD_RIGHT_CLICK_COOLDOWN_CONFIG]초",
        "§7철괴 우클릭 §8- §7그림자 잔상§f: 자신의 위치를 사망시 부활하는 스폰장소로 지정합니다.",
        "§7철괴 좌클릭 §8- §f그림자 연막§f: 바라보는 방향으로 빠르게 대시합니다. §c쿨타임 §7: §f$[LEFT_CLICK_COOLDOWN_CONFIG]초"
})

public class Ninja extends AbilityBase implements ActiveHandler {
    public static final AbilitySettings.SettingObject<Integer> SWORD_RIGHT_CLICK_COOLDOWN_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Ninja.class, "SWORD_RIGHTCLICK_COOLDOWN", 8,
            "# 검 우클릭 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> LEFT_CLICK_COOLDOWN_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Ninja.class, "LEFTCLICK_COOLDOWN", 25,
            "# 좌클릭 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> RESPAWN_HEALTH = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Ninja.class, "RESPAWN_HEALTH", 4,
            "# 부활 체력") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    private static final String prefix = "§7[§f닌자§7] §f";
    private static final ImageVector imageVector = ImageVector.parse(Ninja.class.getResourceAsStream("/ImageParticle/Knife.png")).addTransparent(0);
    private static final List<RGB> gradation = Gradient.createGradient(25,
            new RGB(205, 205, 209),
            new RGB(163, 163, 163),
            new RGB(205, 205, 209));
    private static final Set<Material> swords;

    static {
        if (MaterialX.NETHERITE_SWORD.isSupported()) {
            swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD, MaterialX.NETHERITE_SWORD.getMaterial());
        } else {
            swords = ImmutableSet.of(MaterialX.WOODEN_SWORD.getMaterial(), Material.STONE_SWORD, Material.IRON_SWORD, MaterialX.GOLDEN_SWORD.getMaterial(), Material.DIAMOND_SWORD);
        }
    }

    private final Cooldown sword_cooldown = new Cooldown(SWORD_RIGHT_CLICK_COOLDOWN_CONFIG.getValue(), "표창", 1);
    private final Cooldown left_cooldown = new Cooldown(LEFT_CLICK_COOLDOWN_CONFIG.getValue(), "그림자 연막", 1);
    private final Iterator<RGB> iterator = Iterables.cycle(gradation).iterator();
    private final int respawn_health = RESPAWN_HEALTH.getValue();
    private Bullet bullet = null;
    private boolean respawn = false;
    private boolean rightclick = false;
    private Location deathloc = null;
    private Location startLocation;

    public Ninja(Participant participant) {
        super(participant);
    }

    @Override
    public boolean usesMaterial(Material material) {
        return super.usesMaterial(material) || swords.contains(material);
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (clickType == ClickType.RIGHT_CLICK) {
            if (material == Material.IRON_INGOT) {
                if (!rightclick) {
                    final RGB color = iterator.next();
                    AnimatedBallEffect effect = new AnimatedBallEffect(getGame(), 100, getPlayer().getLocation());
                    effect.setPeriod(TimeUnit.TICKS, 1);
                    effect.rgb = color;
                    effect.start();
                    SoundLib.ENTITY_WITHER_SKELETON_HURT.playSound(getPlayer().getLocation(), 1, 2f);
                    respawn = true;
                    rightclick = true;
                    deathloc = getPlayer().getLocation();
                    getPlayer().sendMessage(prefix + "부활 좌표가 " + "§5X§f: " + ((int) deathloc.getX()) + " §5Y§f: " + ((int) deathloc.getY()) + " §5Z§f: " + ((int) deathloc.getZ()) + "로 설정되었습니다.");
                } else {
                    getPlayer().sendMessage(prefix + "이미 좌표가 등록되어 있습니다. §7| §f현재 등록된 좌표 : " + "§5X§f: " + ((int) deathloc.getX()) + " §5Y§f: " + ((int) deathloc.getY()) + " §5Z§f: " + ((int) deathloc.getZ()));
                }
            } else if (swords.contains(material)) {
                if (!sword_cooldown.isCooldown()) {
                    new Bullet(getPlayer(), getPlayer().getLocation().clone().add(0, 1, 0), getPlayer().getLocation().getDirection()).start();
                    SoundLib.ENTITY_PLAYER_ATTACK_SWEEP.playSound(getPlayer().getLocation(), 1, 2f);
                    sword_cooldown.start();
                    return true;
                }
            }
        } else if (clickType == ClickType.LEFT_CLICK) {
            if (material == Material.IRON_INGOT) {
                if (!left_cooldown.isCooldown()) {
                    ParticleLib.CLOUD.spawnParticle(getPlayer().getLocation(), 0.5, 0.5, 0.5, 100, 0.2);
                    final Location location = getPlayer().getLocation();
                    final Vector direction = location.getDirection().setY(0).normalize().multiply(5);
                    getPlayer().setVelocity(direction);
                    for (Location loc : Line.of(direction, 100).toLocations(location).add(0, 1, 0)) {
                        ParticleLib.REDSTONE.spawnParticle(loc, RGB.WHITE);
                    }
                    SoundLib.ENTITY_BAT_TAKEOFF.playSound(getPlayer(), 100, 1);
                    getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 40, 1));
                    left_cooldown.start();
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    @SubscribeEvent(priority = 5)
    private void onPlayerSetHealth(PlayerSetHealthEvent e) {
        if (e.getPlayer().equals(getPlayer()) && !e.isCancelled() && e.getHealth() <= 0) {
            if (respawn) {
                e.setCancelled(true);
                getPlayer().teleport(deathloc);
                getPlayer().setHealth(respawn_health);
                SoundLib.ENTITY_FIREWORK_ROCKET_LAUNCH.playSound(getPlayer());
                getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                respawn = false;
            }
        }
    }

    @SubscribeEvent(priority = 5)
    private void onEntityDamage(EntityDamageEvent e) {
        if (e.getEntity().equals(getPlayer()) && !e.isCancelled() && getPlayer().getHealth() - e.getDamage() <= 0) {
            if (respawn) {
                e.setCancelled(true);
                double damage = e.getFinalDamage();
                if (getPlayer().getHealth() - damage <= 0) {
                    e.setDamage(0);
                    getPlayer().teleport(deathloc);
                    getPlayer().setHealth(respawn_health);
                    SoundLib.ENTITY_FIREWORK_ROCKET_LAUNCH.playSound(getPlayer());
                    getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1));
                    respawn = false;
                }
            }
        }
    }

    @SubscribeEvent(priority = 5)
    private void onEntityDamageByBlock(EntityDamageByBlockEvent e) {
        onEntityDamage(e);
    }

    @SubscribeEvent(priority = 5)
    private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        onEntityDamage(e);
    }

    @Override
    protected void onUpdate(Update update) {
        if (Update.ABILITY_DESTROY == update) {
            rightclick = false;
        }
    }

    public class Bullet extends AbilityTimer {

        private final LivingEntity shooter;
        private final CustomEntity entity;
        private final Vector forward;
        private final Predicate<Entity> predicate;
        private final Set<Player> attacked = new HashSet<>();

        private Location lastLocation;

        private Bullet(LivingEntity shooter, Location startLocation, Vector arrowVelocity) {
            super(14);
            setPeriod(TimeUnit.TICKS, 1);
            Ninja.this.bullet = this;
            this.shooter = shooter;
            this.entity = new Bullet.ArrowEntity(startLocation.getWorld(), startLocation.getX(), startLocation.getY(), startLocation.getZ()).resizeBoundingBox(-2, -2, -2, 2, 2, 2);
            this.forward = arrowVelocity.normalize().multiply(0.5);
            this.lastLocation = startLocation;
            this.predicate = new Predicate<Entity>() {
                @Override
                public boolean test(Entity entity) {
                    if (entity instanceof ArmorStand) return false;
                    if (entity.equals(shooter)) return false;
                    if (entity instanceof Player) {
                        if (!getGame().isParticipating(entity.getUniqueId())
                                || (getGame() instanceof DeathManager.Handler && ((DeathManager.Handler) getGame()).getDeathManager().isExcluded(entity.getUniqueId()))
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

        @Override
        protected void run(int i) {
            final Location newLocation = lastLocation.clone().add(forward);
            for (Iterator<Location> iterator = new Iterator<Location>() {
                private final Vector vectorBetween = newLocation.toVector().subtract(lastLocation.toVector()), unit = vectorBetween.clone().normalize().multiply(.35);
                private final int amount = (int) (vectorBetween.length() / 0.35);
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
                final Block block = location.getBlock();
                final Material type = block.getType();
                if (type.isSolid()) {
                    stop(false);
                    return;
                }
                entity.setLocation(location);
                for (Player p : LocationUtil.getConflictingEntities(Player.class, entity.getWorld(), entity.getBoundingBox(), predicate)) {
                    if (!shooter.equals(p)) {
                        p.damage(4, shooter);
                        if (!getGame().getParticipant(p).hasEffect(Blind.registration) && !attacked.contains(p)) {
                            attacked.add(p);
                            Blind.apply(getGame().getParticipant(p), TimeUnit.SECONDS, 2);
                        }
                    }
                }
                final Location playerLocation = getPlayer().getLocation();
                final Vector direction = playerLocation.getDirection(), axis = VectorUtil.rotateAroundAxisY(direction, 90);
                for (Point2D point2D : imageVector) {
                    ParticleLib.REDSTONE.spawnParticle(location.clone().add(
                            VectorUtil.rotateAroundAxis(VectorUtil.rotateAroundAxisY(point2D, -playerLocation.getYaw()), axis, 90)
                    ), point2D.getColor());
                }
            }
            lastLocation = newLocation;
        }

        @Override
        protected void onEnd() {
            entity.remove();
            attacked.clear();
            Ninja.this.bullet = null;
        }

        @Override
        protected void onSilentEnd() {
            entity.remove();
            attacked.clear();
            Ninja.this.bullet = null;
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
                new Bullet(deflectedPlayer, lastLocation, newDirection).start();
            }

            @Override
            public ProjectileSource getShooter() {
                return shooter;
            }

            @Override
            protected void onRemove() {
            }

        }

    }
}
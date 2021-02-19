package KunE.Addon.KunEAbility;

import KunE.Addon.KunAddon;
import com.google.common.collect.Iterables;
import com.sun.istack.internal.NotNull;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.GameTimer;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.color.Gradient;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil.Vectors;
import daybreak.abilitywar.utils.base.math.geometry.Crescent;
import daybreak.abilitywar.utils.base.minecraft.boundary.CenteredBoundingBox;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.ParticleLib.ColouredParticle;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

@AbilityManifest(name = "루이스", rank = AbilityManifest.Rank.A, species = Species.HUMAN, explain = {
        "§7암흑의 공간§f과 §b현실세계§f를 공존하는 §8암흑의 §7대마법사 §f루이스.",
        "§7패시브 §8- §7어둠의 검기§f: 인게임의 시간이 밤일 경우 자신이 주는 근접 데미지는 1.2배로 들어갑니다.",
        "§7철괴 우클릭 §8- §8광란 파티§f: 자신의 위치를 기준으로 하늘의 마법구를 생성하며, 마법구에서 나오는 레이저의 맞을경우 $[RIGHT_DAMAGE]의 피해를 입힙니다. §c쿨타임 §7: §f$[RIGHT_CLICK_COOLDOWN_CONFIG]초"
})
public class Lewis extends AbilityBase implements ActiveHandler {
    public static final AbilitySettings.SettingObject<Integer> RIGHT_CLICK_COOLDOWN_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Lewis.class, "RIGHTCLICK_COOLDOWN", 80,
            "# 우클릭 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> RIGHT_CLICK_DURATION = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Lewis.class, "RIGHTCLICK_DURATION", 7,
            "# 우클릭 지속시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };

    public static final AbilitySettings.SettingObject<Integer> RIGHT_DAMAGE = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Lewis.class, "RIGHT_DAMAGE", 5,
            "# 마법구 레이저 피해량") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };

    private final Cooldown cooldown = new Cooldown(RIGHT_CLICK_COOLDOWN_CONFIG.getValue(), "광란 파티", 50);
    private int rightclickduration = RIGHT_CLICK_DURATION.getValue();
    private int rightdamage = RIGHT_DAMAGE.getValue();
    private static final List<RGB> gradation = Gradient.createGradient(25,
            new RGB(66, 66, 66),
            new RGB(18, 18, 18),
            new RGB(66, 66, 66));

    private static boolean isNight(long time) {
        return time > 12300 && time < 23850;
    }

    private final Iterator<RGB> iterator = Iterables.cycle(gradation).iterator();
    private DarkMagicZone MagicZone = null;
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

    public Lewis(Participant participant) {
        super(participant);
    }

    @Override
    public boolean ActiveSkill(@NotNull Material material, @NotNull ClickType clickType) {
        if (material == Material.IRON_INGOT) {
            if (clickType == ClickType.RIGHT_CLICK) {
                if (!cooldown.isCooldown()) {
                    if (MagicZone == null) {
                        new DarkMagicZone(rightclickduration, getPlayer().getLocation()).start();
                        SoundLib.ENTITY_WITHER_AMBIENT.playSound(getPlayer(), 1, 0.1f);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private class DarkMagicZone extends AbilityTimer implements Listener {

        private final ActionbarChannel Magicactionbar = getParticipant().actionbar().newChannel();

        private DarkMagicZone(final int duration, final Location center) {
            super(TaskType.REVERSE, duration * 10);
            setPeriod(TimeUnit.TICKS, 2);
            MagicZone = this;
        }

        @Override
        protected void onEnd() {
            onSilentEnd();
        }

        @Override
        protected void onSilentEnd() {
            Magicactionbar.unregister();
            MagicZone = null;
            SoundLib.ENTITY_GENERIC_EXPLODE.playSound(getPlayer());
        }

        @Override
        protected void run(int count) {
            Magicactionbar.update("§7광란 파티 지속시간 §f: " + (count / 10.0) + "초");
            if (count % 5 == 0) {
                final RGB color = iterator.next();
                final Location playerLocation = getPlayer().getLocation().clone().add(0, 10, 0);
                ParticleLib.SMOKE_NORMAL.spawnParticle(playerLocation, 1, 1, 1, 100, 1);
                DarkDiscoBallEffect effect = new DarkDiscoBallEffect(getGame(), 1, playerLocation);
                effect.sphereColor = color;
                effect.lineColor = color;
                effect.sphereRadius = 2.5f;
                effect.sphereParticles = 100;
                effect.start();
            }
        }
    }

    public class DarkDiscoBallEffect extends GameTimer {

        public float sphereRadius = .6f;
        public int max = 15;
        public ColouredParticle lineParticle = ParticleLib.REDSTONE;
        public ColouredParticle sphereParticle = ParticleLib.REDSTONE;
        public RGB sphereColor = null, lineColor = null;
        public int maxLines = 7;
        public int lineParticles = 100, sphereParticles = 50;
        private final Random random = new Random();
        private final Location location;
        private final CenteredBoundingBox boundingBox = CenteredBoundingBox.of(new Vector(), -0.75, -0.75, -0.75, 0.75, 0.75, 0.75);

        public DarkDiscoBallEffect(AbstractGame abstractGame, int iterations, Location location) {
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
                double y = random.nextInt(max * 2 - max) + max;
                double z = random.nextInt(max - max * (-1)) + max * (-1);
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
                    boundingBox.setCenter(loc);
                    for (LivingEntity livingEntity : LocationUtil.getConflictingEntities(LivingEntity.class, getPlayer().getWorld(), boundingBox, predicate)) {
                        livingEntity.damage(rightdamage, getPlayer());
                        SoundLib.ENTITY_WOLF_WHINE.playSound(getPlayer(), 0.7f, 2);
                    }
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

    }
    private int particleRight = 45;
    private int particleLeft = -45;


    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager().equals(getPlayer())) {
            final Entity entity = e.getEntity();
            if (getGame().isParticipating(entity.getUniqueId())) {
                final Participant target = getGame().getParticipant(entity.getUniqueId());
                if (isNight(getPlayer().getWorld().getTime())) {
                    e.setDamage(e.getDamage() * 1.2);
                    new CutParticle(particleRight).start();
                    new CutParticle(particleLeft).start();
                    SoundLib.ENTITY_WITHER_HURT.playSound(getPlayer(), 0.5f, 1);
                }
            }
        }
    }

    private static final RGB MOONLIGHT_COLOUR = RGB.of(81, 82, 80);
    private static final Crescent crescent = Crescent.of(1, 20);

    private class CutParticle extends AbilityTimer {

        private final Vector axis;
        private final Vector vector;
        private final Vectors crescentVectors;

        private CutParticle(double angle) {
            super(4);
            setPeriod(TimeUnit.TICKS, 1);
            this.axis = VectorUtil.rotateAroundAxis(VectorUtil.rotateAroundAxisY(getPlayer().getLocation().getDirection().setY(0).normalize(), 90), getPlayer().getLocation().getDirection().setY(0).normalize(), angle);
            this.vector = getPlayer().getLocation().getDirection().setY(0).normalize().multiply(0.5);
            this.crescentVectors = crescent.clone()
                    .rotateAroundAxisY(-getPlayer().getLocation().getYaw())
                    .rotateAroundAxis(getPlayer().getLocation().getDirection().setY(0).normalize(), (180 - angle) % 180)
                    .rotateAroundAxis(axis, -75);
        }

        @Override
        protected void run(int count) {
            Location baseLoc = getPlayer().getLocation().clone().add(vector).add(0, 1.3, 0);
            for (Location loc : crescentVectors.toLocations(baseLoc)) {
                ParticleLib.REDSTONE.spawnParticle(loc, MOONLIGHT_COLOUR);
            }
            crescentVectors.rotateAroundAxis(axis, 40);
        }

    }
}




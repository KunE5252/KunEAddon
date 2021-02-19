package KunE.Addon.KunEAbility;

import KunE.Addon.KunAddon;
import KunE.Addon.KunEAbility.AbilityEffect.Damp;
import KunE.Addon.KunEAbility.AbilityEffect.Fatigue;
import KunE.Addon.KunEAbility.AbilityEffect.Paralysis;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.game.manager.effect.Frost;
import daybreak.abilitywar.game.module.DeathManager;
import daybreak.abilitywar.game.team.interfaces.Teamable;
import daybreak.abilitywar.utils.base.color.Gradient;
import daybreak.abilitywar.utils.base.color.RGB;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import daybreak.abilitywar.utils.base.math.LocationUtil;
import daybreak.abilitywar.utils.base.math.VectorUtil;
import daybreak.abilitywar.utils.base.math.geometry.Circle;
import daybreak.abilitywar.utils.base.math.geometry.ImageVector;
import daybreak.abilitywar.utils.base.math.geometry.ImageVector.Point2D;
import daybreak.abilitywar.utils.base.minecraft.entity.health.Healths;
import daybreak.abilitywar.utils.library.ParticleLib;
import daybreak.abilitywar.utils.library.SoundLib;
import daybreak.google.common.collect.Iterables;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@AbilityManifest(name = "카일루스", rank = AbilityManifest.Rank.L, species = AbilityManifest.Species.GOD, explain = {
        "§7패시브 §8- §b구름 낙하§f: 낙하시 모든 낙하 데미지를 캔슬시킵니다.",
        "§7철괴 우클릭 §8- §e구름 경계§f: 자신의 위치를 기준으로 반지름 4칸의 원을 생성합니다.",
        " 이때 구름 경계의 상태에 따라 다른 디버프를 부여하며 구름 경계는 $[RIGHT_CLICK_DURATION]초 동안 지속됩니다. §c쿨타임 §7: §f$[RIGHT_CLICK_COOLDOWN_CONFIG]초",
        "§7철괴 쉬프트 우클릭 §8- §c모드 변경§f: 구름 경계의 상태를 스킬을 사용할때마다, 위에서 아래 순서로 변경됩니다. §c쿨타임 §7: §f$[RIGHT_CLICK_SNEAK_COOLDOWN]초",
        "§6✰ §f: §e구름 경계 능력 상태정보",
        "§6맑음§f: 경계 내부에 들어오는 대상 모두에게 체력을 1초에 0.5칸씩 회복되며, 피로 효과를 $[CLOUD_FATIGUE_TIME]초 동안 부여합니다.",
        "§e태풍§f: 경계 내부에 들어오는 대상에게 번개를 떨어뜨리며, 번개를 맞은 플레이어에게 마비 효과를 $[CLOUD_PARALYSIS_TIME]초 동안 부여합니다.",
        "§b폭우§f: 경계 내부에 들어오는 대상을 구역 밖으로 밀쳐내며, 습함 효과를 $[CLOUD_DAMP_TIME]초 동안 부여합니다.",
        "§7폭설§f: 경계 내부에 들어오는 대상을 빙결시켜 이동 불능 상태로 변경시킵니다.",
        "§7상태이상 §8- §d피로§f: 채굴속도가 매우 느리지며 움직일 경우 피해를 받습니다.",
        "§7상태이상 §8- §9습함§f: 이동이 어려워지고 공격시에 1의 추가 피해를 줍니다.",
        "§7상태이상 §8- §e마비§f: 움직일 수 없어지고 시야가 차단됩니다."
})
public class Caelus extends AbilityBase implements ActiveHandler {
    public static final AbilitySettings.SettingObject<Integer> RIGHT_CLICK_COOLDOWN_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Caelus.class, "RIGHTCLICK_COOLDOWN", 80,
            "# 우클릭 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> RIGHT_CLICK_DURATION = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Caelus.class, "RIGHTCLICK_DURATION", 5,
            "# 우클릭 지속시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> RIGHT_CLICK_SNEAK_COOLDOWN = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Caelus.class, "SNEAK_RIGHT_CLICK_COOLDOWN", 10,
            "# 쉬프트 우클릭 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> CLOUD_FATIGUE_TIME = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Caelus.class, "FATIGUE_DURATION", 5,
            "# 구름 경계 맑음 피로 지속시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> CLOUD_PARALYSIS_TIME = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Caelus.class, "PARALYSIS_DURATION", 1,
            "# 구름 경계 태풍 마비 지속시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> CLOUD_DAMP_TIME = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Caelus.class, "DAMP_DURATION", 5,
            "# 구름 경계 폭우 습함 지속시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };

    private final Cooldown cooldown = new Cooldown(RIGHT_CLICK_COOLDOWN_CONFIG.getValue(), "구름 경계", 65);
    private final Cooldown sneak_cooldown = new Cooldown(RIGHT_CLICK_SNEAK_COOLDOWN.getValue(), "모드 변경", 65);
    private final int rightClickDuration = RIGHT_CLICK_DURATION.getValue();
    private final int paralysistime = CLOUD_PARALYSIS_TIME.getValue();
    private final int damptime = CLOUD_DAMP_TIME.getValue();
    private final int fatiguetime = CLOUD_FATIGUE_TIME.getValue();
    private final Circle circleVectors = Circle.of(4, 70);
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

    private StateList state = StateList.SUN;
    private CaelusZone caelusZone = null;

    private static final String prefix = "§9[§b카일루스§9] §f";

    public Caelus(Participant participant) {
        super(participant);
    }

    private final ActionbarChannel stateab = getParticipant().actionbar().newChannel();

    @Override
    protected void onUpdate(Update update) {
        if (Update.RESTRICTION_CLEAR == update) {
            stateab.update("§b구름 경계 상태 §f: " + state.name);
        }
        if (Update.ABILITY_DESTROY == update) {
            stateab.unregister();
        }
    }


    @SubscribeEvent
    private void onEntityDamage(EntityDamageEvent e) {
        if (!e.isCancelled() && getPlayer().equals(e.getEntity()) && e.getCause().equals(DamageCause.FALL)) {
            e.setCancelled(true);
            getPlayer().sendMessage(prefix + "§f구름낙하를 성공하여 낙하데미지를 받지 않습니다.");
            SoundLib.ENTITY_ILLUSIONER_MIRROR_MOVE.playSound(getPlayer());

        }
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            if (getPlayer().isSneaking()) {
                if (!sneak_cooldown.isCooldown()) {
                    if (caelusZone == null) {
                        sneak_cooldown.start();
                        state = state.next();
                        getPlayer().sendMessage(prefix + "구름 경계의 상태가 " + state.name + "§f로 변경되었습니다.");
                        stateab.update("§b구름 경계 상태 §f: " + state.name);
                        sneak_cooldown.start();
                    }
                }
            } else {
                if (!cooldown.isCooldown()) {
                    if (caelusZone == null) {
                        new CaelusZone(rightClickDuration, getPlayer().getLocation()).start();
                        return true;
                    }
                }
            }
            return false;
        }
        return false;
    }

    public enum StateList {
        SUN ("§6맑음",Gradient.createGradient(25,
                new RGB(252, 252, 3),
                new RGB(219, 252, 3),
                new RGB(194, 252, 3),
                new RGB(169, 252, 3),
                new RGB(252, 252, 3)
        ), ImageVector.parse(Caelus.class.getResourceAsStream("/ImageParticle/flower.png")).addTransparent(0)) {
            @Override
            public StateList next() {
                return TYPHOON;
            }

            @Override
            public void run(Caelus caelus, int count) {
                if (count % 10 == 0) {
                    for (Player player : LocationUtil.getEntitiesInCircle(Player.class, caelus.getPlayer().getLocation(), 4, caelus.predicate)) {
                        Healths.setHealth(player, player.getHealth() + 1);
                        Fatigue.apply(caelus.getGame().getParticipant(player), TimeUnit.SECONDS, caelus.fatiguetime);
                    }
                }
            }
        }, TYPHOON ("§e태풍",Gradient.createGradient(25,
                new RGB(2, 89, 86),
                new RGB(245, 216, 0),
                new RGB(2, 89, 86)
        ), ImageVector.parse(Caelus.class.getResourceAsStream("/ImageParticle/TYPHOON.png")).addTransparent(0)) {
            @Override
            public StateList next() {
                return RAIN;
            }
            @Override
            public void run(Caelus caelus, int count) {
                if (count % 10 == 0) {
                    for (Player player : LocationUtil.getEntitiesInCircle(Player.class, caelus.getPlayer().getLocation(), 4, caelus.predicate)) {
                        player.getWorld().strikeLightningEffect(player.getLocation());
                        player.damage(1);
                        Paralysis.apply(caelus.getGame().getParticipant(player), TimeUnit.SECONDS, caelus.paralysistime);
                    }
                }
            }
        }, RAIN ("§b폭우",Gradient.createGradient(25,
                new RGB(0, 251, 255),
                new RGB(93, 103, 245),
                new RGB(0, 251, 255)
        ), ImageVector.parse(Caelus.class.getResourceAsStream("/ImageParticle/rain.png")).addTransparent(0)) {
            @Override
            public StateList next() {
                return SNOW;
            }
            @Override
            public void run(Caelus caelus, int count) {
                if (count % 2 == 0) {
                    final Vector center = caelus.getPlayer().getLocation().toVector();
                    for (Player player : LocationUtil.getEntitiesInCircle(Player.class, caelus.getPlayer().getLocation(), 4, caelus.predicate)) {
                        player.setVelocity(player.getLocation().toVector().subtract(center).normalize().setY(0));
                        Damp.apply(caelus.getGame().getParticipant(player), TimeUnit.SECONDS, caelus.damptime);
                    }
                }
            }
        }, SNOW ("§7폭설",Gradient.createGradient(25,
                new RGB(254, 254, 254),
                new RGB(196, 233, 242),
                new RGB(194, 221, 252),
                new RGB(176, 168, 254),
                new RGB(254, 254, 254)
        ), ImageVector.parse(Caelus.class.getResourceAsStream("/ImageParticle/snow.png")).addTransparent(0)) {
            @Override
            public StateList next() {
                return SUN;
            }
            @Override
            public void run(Caelus caelus, int count) {
                if (count % 10 == 0) {
                    for (Player player : LocationUtil.getEntitiesInCircle(Player.class, caelus.getPlayer().getLocation(), 4, caelus.predicate)) {
                        Frost.apply(caelus.getGame().getParticipant(player), TimeUnit.TICKS, 10);
                    }
                }
            }
        };

        private final String name;
        private final List<RGB> colors;
        private final ImageVector imageVector;

        StateList(String name, List<RGB> colors, ImageVector imageVector) {
            this.name = name;
            this.colors = colors;
            this.imageVector = imageVector;
        }

        public abstract StateList next();
        public abstract void run(Caelus caelus, int count);
    }

    private class CaelusZone extends AbilityTimer implements Listener {

        private final ActionbarChannel actionbar = getParticipant().actionbar().newChannel();
        private final Iterator<RGB> iterator = Iterables.cycle(state.colors).iterator();

        private CaelusZone(final int duration, final Location center) {
            super(TaskType.REVERSE, duration * 10);
            setPeriod(TimeUnit.TICKS, 2);
            caelusZone = this;
        }

        @Override
        protected void onEnd() {
            onSilentEnd();
        }

        @Override
        protected void onSilentEnd() {
            actionbar.unregister();
            cooldown.start();
            caelusZone = null;
        }

        @Override
        protected void run(int count) {
            final RGB color = iterator.next();
            for (final Location location : circleVectors.toLocations(getPlayer().getLocation()).floor(getPlayer().getLocation().getY())) {
                ParticleLib.REDSTONE.spawnParticle(location, color);
            }
            if (count % 5 == 0) {
                final Location playerLocation = getPlayer().getLocation().clone().add(0, 4, 0);
                for (Point2D point2D : state.imageVector) {
                    ParticleLib.REDSTONE.spawnParticle(playerLocation.clone().add(VectorUtil.rotateAroundAxisY(point2D.clone(), -playerLocation.getYaw())), point2D.getColor());
                }
            }
            state.run(Caelus.this, count);
            actionbar.update("§b구름 경계 지속시간 §f: " + (count / 10.0) + "초");
        }
    }
}

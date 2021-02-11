package KunEAbility;

import KunE.Addon.KunAddon;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.ActionbarChannel;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Random;

@AbilityManifest(name = "마루스", rank = AbilityManifest.Rank.S, species = Species.HUMAN, explain = {
        "§7패시브 §8- §c힘을 모아§f: 우클릭을 통하여 뽑은 데미지가 기존 데미지에 추가되어 들어갑니다. 또한 무적 해제시 랜덤으로 데미지를 배정받습니다.",
        "§7철괴 우클릭 §8- §d인생은 한방§f: 자신의 최대체력을 4칸으로 고정하며 0 ~ $[MAX_RANDOM_DAMAGE] 까지에 데미지를 랜덤으로 뽑습니다. §c쿨타임 §7: §f$[RIGHT_CLICK_COOLDOWN_CONFIG]초"

})
public class Mars extends AbilityBase implements ActiveHandler {
    public static final AbilitySettings.SettingObject<Integer> RIGHT_CLICK_COOLDOWN_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Mars.class, "RIGHTCLICK_COOLDOWN", 80,
            "# 우클릭 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> MAX_RANDOM_DAMAGE = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Mars.class, "MAX_RANDOM_DAMAGE", 7,
            "# 최대 랜덤 데미지") {
        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> MAX_HEALTH = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Mars.class, "MAX_HEALTH", 8,
            "# 최대 체력 ( 1 당 반칸 )") {
        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };

    private final Cooldown cooldown = new Cooldown(RIGHT_CLICK_COOLDOWN_CONFIG.getValue(), "인생은 한방", 50);
    private final int max_random_damage = MAX_RANDOM_DAMAGE.getValue();
    private final int max_health = MAX_HEALTH.getValue();

    private static final String prefix = "§d[§c마루스§d] §f";
    private final Random random = new Random();
    private int thisdamage = random.nextInt(max_random_damage + 1);

    public Mars(Participant participant) {
        super(participant);
    }

    private final ActionbarChannel actionbarChannel = getParticipant().actionbar().newChannel();

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager().equals(getPlayer())) {
            final Entity entity = e.getEntity();
            if (getGame().isParticipating(entity.getUniqueId())) {
                final Participant target = getGame().getParticipant(entity.getUniqueId());
                e.setDamage(e.getDamage() + thisdamage);
            }
        }
    }

    @Override
    protected void onUpdate(Update update) {
        if (Update.RESTRICTION_CLEAR == update) {
            actionbarChannel.update("§7현재 추가 데미지 §f: " + thisdamage);
        }
        if (Update.ABILITY_DESTROY == update) {
            actionbarChannel.unregister();
            getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20);
        }
    }


    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            if (!cooldown.isCooldown()) {
                this.thisdamage = random.nextInt(max_random_damage + 1);
                getPlayer().sendMessage(prefix + "랜덤으로 뽑은 숫자 : §c" + thisdamage);
                getPlayer().sendMessage(prefix + "당신은 이제부터 " + thisdamage + "의 데미지를 추가로 공격합니다.");
                SoundLib.UI_TOAST_CHALLENGE_COMPLETE.playSound(getPlayer());
                actionbarChannel.update("§7현재 추가 데미지 §f: " + thisdamage);
                getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(max_health);
                cooldown.start();
            }
        }
        return false;
    }
}
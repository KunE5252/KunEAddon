package KunEAbility;

import KunE.Addon.KunAddon;
import KunEAbility.AbilityEffect.Mouseattack;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Rank;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.SubscribeEvent;
import daybreak.abilitywar.config.ability.AbilitySettings.SettingObject;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import org.bukkit.entity.Entity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@AbilityManifest(name = "쥐", rank = Rank.C, species = Species.ANIMAL, explain = {
        "플레이어를 공격시에 대상을 $[DURATION_CONFIG]초간 깨물어 이동 불능 상태와 데미지를 줍니다.",
        "꺄아아악 우리집에 쥐가 나타났다 ! 도망쳐 !"
})
public class Mouse extends AbilityBase {

    public static final SettingObject<Integer> DURATION_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Mouse.class, "duration", 10,
            "# 지속 시간 (틱 단위)") {

        @Override
        public boolean condition(Integer value) {
            return value >= 0;
        }

        @Override
        public String toString() {
            return String.valueOf(getValue() / 20.0);
        }

    };

    public Mouse(Participant participant) {
        super(participant);
    }

    private final int mouseDuration = DURATION_CONFIG.getValue();

    @SubscribeEvent
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager().equals(getPlayer())) {
            final Entity entity = e.getEntity();
            if (getGame().isParticipating(entity.getUniqueId())) {
                final Participant target = getGame().getParticipant(entity.getUniqueId());
                e.setDamage(e.getDamage() + 1);
                Mouseattack.apply(target, TimeUnit.TICKS, mouseDuration);
            }
        }
    }

}
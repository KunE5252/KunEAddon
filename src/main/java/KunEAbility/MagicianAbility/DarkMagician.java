package KunEAbility.MagicianAbility;

import KunE.Addon.KunAddon;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityManifest;
import daybreak.abilitywar.ability.AbilityManifest.Species;
import daybreak.abilitywar.ability.decorator.ActiveHandler;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame.Participant;
import org.bukkit.Material;

@AbilityManifest(name = "암흑 마법사", rank = AbilityManifest.Rank.A, species = Species.HUMAN, explain = {
        "",
        ""

})
public class DarkMagician extends AbilityBase implements ActiveHandler {
    public static final AbilitySettings.SettingObject<Integer> RIGHT_CLICK_COOLDOWN_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(DarkMagician.class, "RIGHTCLICK_COOLDOWN", 80,
            "# 우클릭 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };

    private final Cooldown cooldown = new Cooldown(RIGHT_CLICK_COOLDOWN_CONFIG.getValue(), "", 50);

    private static final String prefix = "§d[§c§d] §f";

    public DarkMagician(Participant participant) {
        super(participant);
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
            if (!cooldown.isCooldown()) {
                cooldown.start();
            }
        }
        return false;
    }
}

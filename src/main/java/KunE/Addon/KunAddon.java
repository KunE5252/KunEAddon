package KunE.Addon;

import KunEAbility.Caelus;
import KunEAbility.MadMiner;
import KunEAbility.Mars;
import KunEAbility.Mayreel;
import KunEAbility.Mouse;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.addon.Addon;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class KunAddon extends Addon implements Listener {

    private static int abilityCount = 0;

    @Override
    public void onEnable() {
        Messager.sendConsoleMessage("§f쿤이 에드온이 활성화되었습니다.");
        registerAbility(MadMiner.class);
        registerAbility(Mayreel.class);
        registerAbility(Caelus.class);
        registerAbility(Mouse.class);
        registerAbility(Mars.class);
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
    }

    private static void registerAbility(Class<? extends AbilityBase> clazz) {
        abilityCount++;
        AbilityFactory.registerAbility(clazz);
        AbilityList.registerAbility(clazz);
    }

    @EventHandler
    private void onGameCredit(GameCreditEvent e) {
        e.addCredit(
                "§e✰ §f| §b쿤이 애드온§f이 적용되었습니다. §e" + abilityCount + "개§f의 능력이 적용되었습니다.",
                "§e✰ §f| §f개발자 : kuni_S2 [ §9Discord §f: 쿤이§7#§f3820 ]"
       );
    }

    public static final AbilitySettings KunEAbilitySetting = new AbilitySettings(FileUtil.newFile("KunEAddon/KunEAddonAbilitySetting.yml"));
    public static final AbilitySettings KunEsynergySetting = new AbilitySettings(FileUtil.newFile("KunEAddon/KunEAddonSynergySetting.yml"));
}



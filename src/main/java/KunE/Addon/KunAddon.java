package KunE.Addon;

import KunEAbility.*;
import KunESynergy.*;
import daybreak.abilitywar.ability.*;
import daybreak.abilitywar.ability.list.*;
import daybreak.abilitywar.addon.*;
import daybreak.abilitywar.config.ability.*;
import daybreak.abilitywar.game.event.*;
import daybreak.abilitywar.game.list.mix.synergy.*;
import daybreak.abilitywar.game.manager.*;
import daybreak.abilitywar.utils.base.*;
import daybreak.abilitywar.utils.base.io.*;
import org.bukkit.*;
import org.bukkit.event.*;

public class KunAddon extends Addon implements Listener {

    private static int abilityCount = 0;

    @Override
    public void onEnable() {
        Messager.sendConsoleMessage("§f쿤이 에드온이 활성화되었습니다.");
        registerAbility(MadMiner.class);
        registerAbility(Mayreel.class);
        registerAbility(Caelus.class);
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
        SynergyFactory.registerSynergy(Ferda.class, Mayreel.class, Elf.class);
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



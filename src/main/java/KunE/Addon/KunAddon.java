package KunE.Addon;

import KunE.Addon.KunEAbility.AbilityEffect.*;
import KunE.Addon.KunEAbility.Caelus;
import KunE.Addon.KunEAbility.Lewis;
import KunE.Addon.KunEAbility.MadMiner;
import KunE.Addon.KunEAbility.Mars;
import KunE.Addon.KunEAbility.Mayreel;
import KunE.Addon.KunEAbility.Mouse;
import KunE.Addon.KunEAbility.Ninja;
import daybreak.abilitywar.ability.AbilityBase;
import daybreak.abilitywar.ability.AbilityFactory;
import daybreak.abilitywar.addon.Addon;
import daybreak.abilitywar.config.ability.AbilitySettings;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.event.GameCreditEvent;
import daybreak.abilitywar.game.manager.AbilityList;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.io.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class KunAddon extends Addon implements Listener {
    private static KunAddon kunAddon;

    public KunAddon() {
        kunAddon = this;
    }

    public static KunAddon getKunAddon() {
        return kunAddon;
    }

    private static int abilityCount = 0;

    @Override
    public void onEnable() {
        Messager.sendConsoleMessage("§f쿤이 에드온이 활성화되었습니다.");
        // 기본 능력자 능력 등록
        registerAbility(MadMiner.class);
        registerAbility(Mayreel.class);
        registerAbility(Caelus.class);
        registerAbility(Mouse.class);
        registerAbility(Mars.class);
        registerAbility(Lewis.class);
        Bukkit.getPluginManager().registerEvents(this, getPlugin());
    }

    private static void registerAbility(Class<? extends AbilityBase> clazz) {
        abilityCount++;
        AbilityFactory.registerAbility(clazz);
        AbilityList.registerAbility(clazz);
    }

    public enum KunEEffectFactory {
        THORN(Thorn.class),
        FATIGUE(Fatigue.class),
        MOUSEATTACK(Mouseattack.class),
        PARALYSIS(Paralysis.class),
        DAMP(Damp.class),
        BLIND(Blind.class);

        Class<? extends AbstractGame.Effect> clazz;

        KunEEffectFactory(Class<? extends AbstractGame.Effect> clazz) {
            this.clazz = clazz;
        }

        public Class<? extends AbstractGame.Effect> getEffectClass() {
            return clazz;
        }

        public static void load() {
            for (KunEEffectFactory factory : KunEEffectFactory.values()) {
                try {
                    Class.forName(factory.getEffectClass().getName());
                } catch (Exception ignored) {

                }
            }
        }
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


/*
( 파티클 유틸 사용법 )

~~~틱 or ~~~초[ 클래스에 타임 ] 마다 소환, 몇초동안 소환할건지, [ 틱단위 ]
DiscoBallEffect effect = new DiscoBallEffect(getGame(), 100, getPlayer().getLocation());
effect.sphereColor = RGB.AQUA;
effect.lineColor = RGB.WHITE;
effect.start();

( 객체 수정법 )
effect.sphereRadius = 3f;\

! 사용시 타이머 돌려서 5 TICKS or 10 TICKS 로 조절해서 사용하기 [ 렉 ]
*/


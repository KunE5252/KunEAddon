package Gamemode;

import KunE.Addon.KunAddon;
import com.google.common.base.Strings;
import daybreak.abilitywar.config.Configuration.Settings;
import daybreak.abilitywar.game.Game;
import daybreak.abilitywar.game.GameManifest;
import daybreak.abilitywar.game.manager.object.DefaultKitHandler;
import daybreak.abilitywar.game.module.InfiniteDurability;
import daybreak.abilitywar.game.script.manager.ScriptManager;
import daybreak.abilitywar.utils.annotations.Beta;
import daybreak.abilitywar.utils.base.Messager;
import daybreak.abilitywar.utils.base.Seasons;
import daybreak.abilitywar.utils.base.minecraft.PlayerCollector;
import daybreak.abilitywar.utils.base.random.Random;
import daybreak.abilitywar.utils.library.SoundLib;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.List;


@GameManifest(name = "마법 능력자 전쟁", description = {
        "§f기존 능력자 전쟁에 모든 능력은 없다 !",
        "§f색다른 마법들을 보유중인 12명의 마법사들의 전쟁 !",
        "§f여러 속성의 마법중에 어떤 마법이 제일 좋을까?",
        "§f마법을 가지고 최후에 1인까지 살아남아라 !"
})
@Beta
public class MagicWar extends Game implements DefaultKitHandler {

    public MagicWar() {
        super(PlayerCollector.EVERY_PLAYER_EXCLUDING_SPECTATORS());
        setRestricted(Settings.InvincibilitySettings.isEnabled());
    }

    private final Random random = new Random();

    @Override
    protected void progressGame(int seconds) {
        switch (seconds) {
            case 1:
                List<String> lines = Messager.asList("§5==== §d게임 참여자 목록 §5====");
                int count = 0;
                for (Participant p : getParticipants()) {
                    count++;
                    lines.add("§d" + count + ". §f" + p.getPlayer().getName());
                }
                lines.add("§d총 인원수 : " + count + "명");
                lines.add("§5===========================");

                for (String line : lines) {
                    Bukkit.broadcastMessage(line);
                }

                if (getParticipants().size() < 1) {
                    stop();
                    Bukkit.broadcastMessage("§c최소 참가자 수를 충족하지 못하여 게임을 중지합니다. §8(§71명§8)");
                }
                break;
            case 3:
                lines = Messager.asList(
                        "§5MagicWar §f- §e마법 능력자 전쟁",
                        "§e버전 §7: §f" + KunAddon.getKunAddon().getDescription().getVersion(),
                        "§b개발자 §7: §fKunE 쿤이",
                        "§9디스코드 §7: §f쿤이§7#3820"
                );
                break;
            case 5:
                if (Settings.getDrawAbility()) {
                    for (String line : Messager.asList(
                            "§f해당 게임모드에 §b" + KunAddon.MagicWarAbility.size() + "개§f의 능력이 등록되어 있습니다.",
                            "§7능력을 무작위로 할당합니다...")) {
                        Bukkit.broadcastMessage(line);
                    }
                    Bukkit.broadcastMessage("§d마법 능력자 전쟁 §f모드에서는 모든 플레이어의 능력이 마법사로 추첨됩니다.");
                    Bukkit.broadcastMessage("§d* §f마법 능력자 모드에서는 능력을 재추첨 하실 수 없습니다.");
                    try {
                        for (Participant participant : getParticipants()) {
                            participant.setAbility(random.pick(KunAddon.MagicWarAbility));
                        }
                    } catch (ReflectiveOperationException ignored) {
                    }
                }
                break;
            case 6:
                break;
            case 7:
                Bukkit.broadcastMessage("§d=============================================");
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage("§f마법사 능력이 할당되었습니다. §d| §5/ §faw check");
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage("§d=============================================");
                SoundLib.ENTITY_EVOKER_PREPARE_SUMMON.broadcastSound();
                break;
            case 8:
                Bukkit.broadcastMessage("§e잠시 후 게임이 시작됩니다.");
                break;
            case 10:
                Bukkit.broadcastMessage("§e게임이 §c5§e초 후에 시작됩니다.");
                SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
                break;
            case 11:
                Bukkit.broadcastMessage("§e게임이 §c4§e초 후에 시작됩니다.");
                SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
                break;
            case 12:
                Bukkit.broadcastMessage("§e게임이 §c3§e초 후에 시작됩니다.");
                SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
                break;
            case 13:
                Bukkit.broadcastMessage("§e게임이 §c2§e초 후에 시작됩니다.");
                SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
                break;
            case 14:
                Bukkit.broadcastMessage("§e게임이 §c1§e초 후에 시작됩니다.");
                SoundLib.BLOCK_NOTE_BLOCK_HARP.broadcastSound();
                break;
            case 15:
                if (Seasons.isChristmas()) {
                    final String blocks = Strings.repeat("§c■§2■", 22);
                    Bukkit.broadcastMessage(blocks);
                    Bukkit.broadcastMessage("§f            §5MagicWar §f- §e마법 능력자 전쟁  ");
                    Bukkit.broadcastMessage("§f                   게임 시작                ");
                    Bukkit.broadcastMessage(blocks);
                } else {
                    for (String line : Messager.asList(
                            "§d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■",
                            "§f             §5MagicWar §f- §e마법 능력자 전쟁  ",
                            "§f                    게임 시작                ",
                            "§d■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■")) {
                        Bukkit.broadcastMessage(line);
                    }
                }

                giveDefaultKit(getParticipants());

                if (Settings.getSpawnEnable()) {
                    Location spawn = Settings.getSpawnLocation().toBukkitLocation();
                    for (Participant participant : getParticipants()) {
                        participant.getPlayer().teleport(spawn);
                    }
                }

                if (Settings.getNoHunger()) {
                    Bukkit.broadcastMessage("§2배고픔 무제한§a이 적용됩니다.");
                } else {
                    Bukkit.broadcastMessage("§4배고픔 무제한§c이 적용되지 않습니다.");
                }

                if (Settings.getInfiniteDurability()) {
                    addModule(new InfiniteDurability());
                } else {
                    Bukkit.broadcastMessage("§4내구도 무제한§c이 적용되지 않습니다.");
                }

                if (Settings.getClearWeather()) {
                    for (World world : Bukkit.getWorlds()) world.setStorm(false);
                }

                if (isRestricted()) {
                    getInvincibility().start(false);
                } else {
                    Bukkit.broadcastMessage("§4초반 무적§c이 적용되지 않습니다.");
                    setRestricted(false);
                }

                ScriptManager.runAll(this);

                startGame();
                break;
        }
    }

}
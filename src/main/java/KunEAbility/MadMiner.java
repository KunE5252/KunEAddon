package KunEAbility;

import KunE.Addon.*;
import daybreak.abilitywar.*;
import daybreak.abilitywar.ability.*;
import daybreak.abilitywar.ability.AbilityFactory.*;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.decorator.*;
import daybreak.abilitywar.config.ability.*;
import daybreak.abilitywar.game.AbstractGame.*;
import daybreak.abilitywar.game.AbstractGame.Participant.ActionbarNotification.*;
import daybreak.abilitywar.game.list.mix.*;
import daybreak.abilitywar.utils.base.color.*;
import daybreak.abilitywar.utils.base.concurrent.*;
import daybreak.abilitywar.utils.base.language.korean.*;
import daybreak.abilitywar.utils.base.language.korean.KoreanUtil.*;
import daybreak.abilitywar.utils.base.minecraft.nms.*;
import daybreak.abilitywar.utils.library.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;

import java.util.*;

@AbilityManifest(name = "도굴꾼", rank = Rank.S, species = AbilityManifest.Species.HUMAN, explain = {
        "§7철괴 우클릭 §8- §7도굴§f: 플레이어의 잔상을 도굴합니다. 이때 도굴꾼은 움직일 수 없으며,",
        " 도굴에 성공할경우 플레이어의 능력이 바닥에 떨어집니다. 바닥에 떨어진 능력은 강탈 스킬을 통하여 능력을 획득하실 수 있습니다. §c쿨타임 §7: §f$[RIGHTCLICK_COOLDOWN_CONFIG]초",
        "§7철괴 좌클릭 §8- §4강탈§f: 채굴한 플레이어의 능력을 자신의 능력으로 변경하며,",
        " 도굴꾼 능력은 영원히 사라집니다. 또한 변경된 자신의 능력을 모든 플레이어들에게 알립니다. §c쿨타임 §7: §f$[LEFTCLICK_COOLDOWN_CONFIG]초",
        "§7플레이어 사망 §8- §c주시§f: 무적이 해제된후 먼저 사망한 플레이어가 $[PLAYER_CONFIG]명으로 설정됩니다. 같은 도굴꾼끼리는 해당 플레이어가 공유됩니다.",
        "§7플레이어 사망 §8- §e잔상§f: 사망한 플레이어 위치에 해당 플레이어의 능력에 잔상을 생성합니다. 또한 사망 위치의 좌표를 도굴꾼에게 안내합니다.",
        "§7시너지 강탈 §8- §d트리플 믹스§f: 도굴 대상의 능력이 시너지일 경우 시너지 능력 자체를 강탈할 수 있습니다."
})
public class MadMiner extends AbilityBase implements ActiveHandler {
    public static final AbilitySettings.SettingObject<Integer> RIGHTCLICK_COOLDOWN_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(MadMiner.class, "우클릭 쿨타임", 60,
            "# 도굴 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };

    public static final AbilitySettings.SettingObject<Integer> LEFTCLICK_COOLDOWN_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(MadMiner.class, "좌클릭 쿨타임", 15,
            "# 강탈 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }

    };

    public static final AbilitySettings.SettingObject<Integer> PLAYER_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(MadMiner.class, "Player", 3,
            "# 타깃 플레이어") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };

    private static final String prefix = "§4[§c도굴꾼§4] §f";

    private final int playerint = PLAYER_CONFIG.getValue();
    private final Cooldown Left_cooldown = new Cooldown(LEFTCLICK_COOLDOWN_CONFIG.getValue(), "강탈");
    private final Cooldown Right_cooldown = new Cooldown(RIGHTCLICK_COOLDOWN_CONFIG.getValue(), "도굴");
    private final List<Deathloc> deathlocs = new ArrayList<>();
    private Theft theft;

    public MadMiner(Participant participant) {
        super(participant);
    }

    @SubscribeEvent
    public void onPlayerDeath(PlayerDeathEvent e) {
        if (deathlocs.size() >= playerint) {
            return;
        }
        Player dead = e.getEntity();
        Participant participant = getGame().getParticipant(dead);
        if (participant != null) {
            AbilityBase abilityB = participant.getAbility(), myAbility = getParticipant().getAbility();
            if (myAbility instanceof Mix) {
                Mix myMix = (Mix) myAbility;
                Mix youMix = (Mix) abilityB;
                if (youMix.hasSynergy()) {
                    abilityB = youMix.getSynergy();
                } else {
                    if (this.equals(myMix.getFirst())) {
                        abilityB = youMix.getFirst();
                    } else {
                        abilityB = youMix.getSecond();
                    }
                }
            }
            if (abilityB != null && abilityB.getClass() != MadMiner.class) {
                Location deadLocation = dead.getLocation();
                getPlayer().sendMessage(prefix + "§c사망한 플레이어 §f" + dead.getName() + "님의 잔상이 " + "§5X§f: " + ((int) deadLocation.getX()) + " §5Y§f: " + ((int) deadLocation.getY()) + " §5Z§f: " + ((int) deadLocation.getZ()) + " §f에 생성되었습니다.");
                deathlocs.add(new Deathloc(participant, dead.getLocation(), abilityB));
            }
        }
    }

    @Override
    protected void onUpdate(Update update) {
        if (Update.ABILITY_DESTROY == update) {
            for (Deathloc removeDeathloc : deathlocs) {
                removeDeathloc.hologram.unregister();
            }
        }
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT) {
            if (clickType == ClickType.RIGHT_CLICK) {
                if (!Right_cooldown.isCooldown()) {
                    boolean exists = false;
                    for (Deathloc rangecheck : deathlocs) {
                        Location playerLoc = getPlayer().getLocation(), deathLoc = rangecheck.location;
                        if (playerLoc.getWorld() == deathLoc.getWorld()) {
                            if (playerLoc.distance(deathLoc) <= 3) {
                                exists = true;
                                if (rangecheck.CheckD) {
                                    getPlayer().sendMessage(prefix + "이미 도굴이 완료된 잔상입니다.");
                                    continue;
                                }
                                if (theft != null) {
                                    getPlayer().sendMessage(prefix + "여러 잔상을 동시에 도굴하실 수 없습니다.");
                                    continue;
                                }
                                getPlayer().sendMessage(prefix + "3칸 이내에 플레이어의 잔상이 존재하여 도굴을 시작합니다.");
                                new Theft(rangecheck).start();
                                Right_cooldown.start();
                                break;
                            }
                        }
                    }
                    if (!exists) {
                        getPlayer().sendMessage(prefix + "3칸 이내에 잔상이 존재하지 않습니다.");
                    }
                }
            } else if (clickType == ClickType.LEFT_CLICK) {
                if (!Left_cooldown.isCooldown()) {
                    boolean exists = false;
                    for (Deathloc rangecheck : deathlocs) {
                        Location playerLoc = getPlayer().getLocation(), deathLoc = rangecheck.location;
                        if (playerLoc.getWorld() == deathLoc.getWorld()) {
                            if (playerLoc.distance(deathLoc) <= 3) {
                                exists = true;
                                if (!rangecheck.CheckD) {
                                    getPlayer().sendMessage(prefix + "도굴이 되지 않은 잔상입니다.");
                                    continue;
                                }
                                try {
                                    AbilityBase myAbility = getParticipant().getAbility();
                                    if (myAbility instanceof Mix) {
                                        Mix myMix = (Mix) myAbility;
                                        if (this.equals(myMix.getFirst())) {
                                            myMix.setAbility(rangecheck.AbilityR, myMix.getSecond().getRegistration());
                                        } else {
                                            myMix.setAbility(myMix.getFirst().getRegistration(), rangecheck.AbilityR);
                                        }
                                    } else {
                                        getParticipant().setAbility(rangecheck.AbilityR);
                                    }
                                } catch (ReflectiveOperationException e) {
                                    e.printStackTrace();
                                }
                                final String abilityName = rangecheck.AbilityR.getManifest().name();
                                getPlayer().sendMessage(prefix + "강탈을 성공하여 §7" + abilityName + "§f" + KoreanUtil.getJosa(abilityName, Josa.을를) + " 부여받았습니다.");
                                Bukkit.broadcastMessage("도굴꾼 §e" + getPlayer().getName() + "§f님이 §c" + abilityName + "§f" + KoreanUtil.getJosa(abilityName, Josa.을를) + " §f강탈했습니다.");
                                SoundLib.ENTITY_ILLUSIONER_PREPARE_BLINDNESS.playSound(getPlayer());
                                Left_cooldown.start();
                                break;
                            }
                        }
                    }
                    if (!exists) {
                        getPlayer().sendMessage(prefix + "3칸 이내에 잔상이 존재하지 않습니다.");
                    }
                }
            }
            return false;
        }
        return false;
    }

    public class Theft extends AbilityTimer implements Listener {

        private final ActionbarChannel channel = getParticipant().actionbar().newChannel();
        private final Deathloc deathloc;

        private Theft(Deathloc deathloc) {
            super(TaskType.REVERSE, 50);
            setPeriod(TimeUnit.TICKS, 2);
            this.deathloc = deathloc;
            theft = this;
        }

        @EventHandler
        public void onPlayerMoveEvent(PlayerMoveEvent e) {
            if (getPlayer().equals(e.getPlayer())) {
                e.setTo(e.getFrom());
            }
        }

        @Override
        protected void onStart() {
            Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
        }

        @Override
        protected void run(int count) {
            channel.update("§c도굴§7 : §f" + (count / 10.0) + "초");
        }

        @Override
        protected void onEnd() {
            onSilentEnd();
            deathloc.hologram.setText("§4⚑ §c도굴한 능력§7: §f" + deathloc.AbilityR.getManifest().name() + " §4⚑");
            deathloc.CheckD = true;
            ParticleLib.REDSTONE.spawnParticle(getPlayer(), deathloc.location.clone().add(0, 1, 0), RGB.of(99, 16, 10));
            new AbilityTimer(TaskType.NORMAL, 6) {
                @Override
                protected void run(int count) {
                    SoundLib.BLOCK_STONE_BREAK.playSound(getPlayer());
                    SoundLib.BLOCK_GRASS_BREAK.playSound(getPlayer());
                }
            }.setPeriod(TimeUnit.TICKS, 3).start();
        }

        @Override
        protected void onSilentEnd() {
            HandlerList.unregisterAll(this);
            channel.unregister();
            theft = null;
        }
    }

    public class Deathloc {

        private final Location location;
        private final IHologram hologram;
        private final AbilityRegistration AbilityR;
        private boolean CheckD = false;

        private Deathloc(Participant dead, Location location, AbilityBase abilityB) {
            this.location = location;
            this.hologram = NMS.newHologram(location.getWorld(), location.getX(), location.getY(), location.getZ(), "§4⚑ §c" + dead.getPlayer().getName() + "§f의 잔상 §4⚑");
            this.AbilityR = abilityB.getRegistration();
            hologram.display(getPlayer());
        }
    }
}

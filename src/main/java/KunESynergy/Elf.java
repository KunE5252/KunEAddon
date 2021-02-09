package KunESynergy;

import KunE.Addon.*;
import daybreak.abilitywar.ability.*;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.decorator.*;
import daybreak.abilitywar.config.ability.*;
import daybreak.abilitywar.game.AbstractGame.*;
import daybreak.abilitywar.game.list.mix.synergy.*;
import daybreak.abilitywar.utils.annotations.*;
import org.bukkit.*;

@AbilityManifest(name = "엘프",rank = AbilityManifest.Rank.L, species = Species.UNDEAD, explain = {
        "§7패시브 §8- §b엘프의 화살§f: 화살을 발사할 때마다 1단계씩 올라가며 총 3단계까지 올라가며, 3단계 화살을 발사할 경우, 화살이 재장전되며, 1단계 화살로 돌아옵니다. 또한 단계에 따라 강도와 화살의 능력이 변경되며, 재장전 시간동안은 일반 화살을 사용할 수 있습니다. §c화살 재장전 시간 §7: §f$[ARROW_RELOADING_TIME]초",
        "§d✿ §f: §a엘프의 화살 목록",
        "§a수호의 화살 §f: 3단계 중 1단계의 화살인 수호의 화살, 화살에 맞은 대상은 5칸 뒤로 밀려가며, 해당 대상에게 엘프의 결박 효과를 $[GUARD_ARROW_BINDING_ING]초 동안 부여합니다.",
        "§2결박의 화살 §f: 3단계 중 2단계의 화살인 결박의 화살, 화살에 맞은 대상을 본인의 위치로 끌어당기며, 해당 대상에게 엘프의 결박 효과를 $[BINDING_ARROW_BINDING_ING]초 동안 부여합니다.",
        "§b생명의 화살 §f: 3단계 중 3단계의 화살인 생명의 화살, 화살은 맞은 대상에 위치에 반지름 4칸인 생명의 구간을 생성합니다. 또한 해당 구간은 $[LIFE_ARROW_DURATION]초동안 지속되며, 해당 구간 내부에 들어오는 모든 대상이 회복하는 체력은 엘프에게 돌아갑니다. ",
        "§7철괴 우클릭 §8- §a페어리의 실드§f: 자신의 위치를 기준으로 반지름이 5칸인 원을 생성하며, 해당 구역 내에 엘프가 존재할 경우 실드 사용 시간이 1초씩 증가하며 실드가 진행되는 도중에는 데미지를 받지 않습니다.",
        " 해당 구역은 총 $[SHIELD_DURATION_TIME]초 동안 지속되며, 구역 내부에 있을 경우 실드의 사용시간은 줄어들지 않습니다. §c쿨타임 §7: §f$[RIGHT_COOLDOWN_CONFIG]초",
        "§7상태이상 §8- §2엘프의 결박§f: 엘프의 결박 효과를 보유중인 대상이 회복하는 모든 체력은 엘프에게 회복됩니다."
})
@Beta
public class Elf extends Synergy implements ActiveHandler {
    public static final AbilitySettings.SettingObject<Integer> RIGHT_COOLDOWN_CONFIG = KunAddon.KunEsynergySetting.new SettingObject<Integer>(Elf.class, "RIGHT_COOLDOWN", 120,
            "# 우클릭 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> ARROW_RELOADING_TIME = KunAddon.KunEsynergySetting.new SettingObject<Integer>(Elf.class, "ELF_ARROW_RELOADING", 15,
            "# 엘프의 화살 재장전 시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> GUARD_ARROW_BINDING_ING = KunAddon.KunEsynergySetting.new SettingObject<Integer>(Elf.class, "GUARD_ARROW_BINDING_ING", 1,
            "# 수호의 화살 엘프의 결박 지속시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> BINDING_ARROW_BINDING_ING = KunAddon.KunEsynergySetting.new SettingObject<Integer>(Elf.class, "BINDING_ARROW_BINDING_ING", 3,
            "# 결박의 화살 엘프의 결박 지속시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> LIFE_ARROW_DURATION = KunAddon.KunEsynergySetting.new SettingObject<Integer>(Elf.class, "LIFE_ARROW_DURATION", 5,
            "# 생명의 화살 생명의 구간 지속시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> SHIELD_DURATION_TIME = KunAddon.KunEsynergySetting.new SettingObject<Integer>(Elf.class, "SHIELD_DURATION_TIME", 6,
            "# 우클릭 페어리의 쉴드 구역 지속시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };

    private final Cooldown right_cooldown = new Cooldown(RIGHT_COOLDOWN_CONFIG.getValue());
    private final Duration right_duration = new Duration(SHIELD_DURATION_TIME.getValue()) {
        @Override
        protected void onDurationProcess(int i) {
        }
    };

    private final int reloading_arrow = ARROW_RELOADING_TIME.getValue();
    private final int guard_arrow = GUARD_ARROW_BINDING_ING.getValue();
    private final int binding_arrow = BINDING_ARROW_BINDING_ING.getValue();
    private final int life_arrow = LIFE_ARROW_DURATION.getValue();


    public Elf(Participant participant) {
        super(participant);
    }

    @Override
    public boolean ActiveSkill(Material material, ClickType clickType) {
        if (material == Material.IRON_INGOT && clickType == ClickType.RIGHT_CLICK) {
                return true;
            }
            return false;
    }
}
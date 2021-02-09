package KunEAbility;

import KunE.Addon.*;
import daybreak.abilitywar.ability.*;
import daybreak.abilitywar.ability.AbilityManifest.*;
import daybreak.abilitywar.ability.decorator.*;
import daybreak.abilitywar.config.ability.*;
import daybreak.abilitywar.game.AbstractGame.*;
import daybreak.abilitywar.utils.annotations.*;
import org.bukkit.*;

@AbilityManifest(name = "메이릴",rank = Rank.L, species = Species.GOD, explain = {
        "§7패시브 §8- §a요정의 수호§f: 요정의 호수 내부에 있을 경우, 자신이 받는 모든 피해량을 20%만큼 줄여서 받으며, 자신을 공격한 플레이어에게 속박의 가시를 $[THORN_CONFIG]초 만큼 부여합니다.",
        "§7근접 공격 §8- §a하프 연주§f: 메이릴이 주는 모든 근접 데미지는 무시되며, 근접 공격시 해당 플레이어에게 3초동안 데미지를 나누어 주며, $[HAF_CONFIG]% 확률로 주는 데미지의 2배의 데미지로 공격합니다.",
        "§7철괴 우클릭 §8- §b요정의 호수§f: 하프를 사용하여 자신의 지대를 생성합니다.",
        " 이때 호수에 존재하는 모든 플레이어는 속박의 가시에 휩싸입니다. §c쿨타임 §7: §f$[RIGHT_CLICK_COOLDOWN_CONFIG]초",
        "§7철괴 좌클릭 §8- §2페어리의 도움§f: 메이릴의 주변에 페어리가 생성되며,",
        " 2마리의 페어리가 메이릴이 바라보는 방향으로 투사체를 발사합니다. §c쿨타임 §7: §f$[LEFT_CLICK_COOLDOWN_CONFIG]초",
        "§7상태이상 §8- §2속박의 가시§f: 속박에 가시 효과를 보유 중인 대상은 이동 불능 상태에 돌입하며, 움직임을 시도할 경우 독에 감염됩니다."
})
@Beta
public class Mayreel extends AbilityBase implements ActiveHandler {
    public static final AbilitySettings.SettingObject<Integer> RIGHT_CLICK_COOLDOWN_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Mayreel.class, "RIGHT_COOLDOWN", 120,
            "# 우클릭 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> LEFT_CLICK_COOLDOWN_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Mayreel.class, "LEFT_COOLDOWN", 60,
            "# 좌클릭 쿨타임") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> THORN_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Mayreel.class, "THORN_DURATION", 2,
            "# 패시브 속박의 가시 지속시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> HAF_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Mayreel.class, "HAF_CONFIG", 3,
            "# 기본 공격 데미지 배율 확률") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };
    public static final AbilitySettings.SettingObject<Integer> PEARL_CONFIG = KunAddon.KunEAbilitySetting.new SettingObject<Integer>(Mayreel.class, "PEARL_DURATION", 5,
            "# 쉬프트 좌클릭 수정구슬 폭파시간") {

        @Override
        public boolean condition(Integer value) {
            return value >= 1;
        }
    };

    private final Cooldown right_cooldown = new Cooldown(RIGHT_CLICK_COOLDOWN_CONFIG.getValue());
    private final Cooldown left_cooldown = new Cooldown(LEFT_CLICK_COOLDOWN_CONFIG.getValue());
    private final int thornint = THORN_CONFIG.getValue();
    private final int pearlint = PEARL_CONFIG.getValue();
    private final int hafint = HAF_CONFIG.getValue();



    public Mayreel(Participant participant) {
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

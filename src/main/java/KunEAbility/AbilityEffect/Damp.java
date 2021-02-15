package KunEAbility.AbilityEffect;

import daybreak.abilitywar.*;
import daybreak.abilitywar.game.*;
import daybreak.abilitywar.game.AbstractGame.*;
import daybreak.abilitywar.game.manager.effect.registry.*;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.*;
import daybreak.abilitywar.utils.base.concurrent.*;
import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.*;
import org.bukkit.potion.*;

@EffectManifest(name = "습함", displayName = "§9습함", method = ApplicationMethod.UNIQUE_LONGEST, type = EffectType.MOVEMENT_RESTRICTION, description = {
        "이동이 어려워지고 공격시에 1의 추가 피해를 줍니다."
})

public class Damp extends AbstractGame.Effect implements Listener {

    public static final EffectRegistration<Damp> registration = EffectRegistry.registerEffect(Damp.class);

    public static void apply(Participant participant, TimeUnit timeUnit, int duration) {
        registration.apply(participant, timeUnit, duration);
    }

    private final Participant participant;

    public Damp(Participant participant, TimeUnit timeUnit, int duration) {
        participant.getGame().super(registration, participant, timeUnit.toTicks(duration) / 2);
        setPeriod(TimeUnit.TICKS, 2);
        this.participant = participant;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
    }

    @EventHandler
    private void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (e.getDamager().equals(participant.getPlayer())) {
            e.setDamage(e.getDamage() + 1);
        }
    }

    @Override
    protected void run(int count) {
        super.run(count);
        participant.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 5));
    }

    @Override
    protected void onEnd() {
        super.onEnd();
        HandlerList.unregisterAll(this);
    }

    @Override
    protected void onSilentEnd() {
        super.onSilentEnd();
        HandlerList.unregisterAll(this);
    }
}
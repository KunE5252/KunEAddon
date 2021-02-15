package KunEAbility.AbilityEffect;

import daybreak.abilitywar.AbilityWar;
import daybreak.abilitywar.game.AbstractGame;
import daybreak.abilitywar.game.AbstractGame.Participant;
import daybreak.abilitywar.game.manager.effect.registry.ApplicationMethod;
import daybreak.abilitywar.game.manager.effect.registry.EffectManifest;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.EffectRegistration;
import daybreak.abilitywar.utils.base.concurrent.TimeUnit;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@EffectManifest(name = "쥐에게 물림!", displayName = "§7쥐에게 물림!", method = ApplicationMethod.UNIQUE_LONGEST, description = {
        "이동속도를 매우 느리게 만듭니다."
})
public class Mouseattack extends AbstractGame.Effect implements Listener {

    public static final EffectRegistration<Mouseattack> registration = EffectRegistry.registerEffect(Mouseattack.class);

    public static void apply(Participant participant, TimeUnit timeUnit, int duration) {
        registration.apply(participant, timeUnit, duration);
    }

    private final Participant participant;

    public Mouseattack(Participant participant, TimeUnit timeUnit, int duration) {
        participant.getGame().super(registration, participant, timeUnit.toTicks(duration) / 2);
        setPeriod(TimeUnit.TICKS, 2);
        this.participant = participant;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Bukkit.getPluginManager().registerEvents(this, AbilityWar.getPlugin());
    }

    @Override
    protected void run(int count) {
        super.run(count);
        participant.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 2));
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



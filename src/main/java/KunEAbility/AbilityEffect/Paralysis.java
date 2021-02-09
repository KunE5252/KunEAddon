package KunEAbility.AbilityEffect;

import daybreak.abilitywar.*;
import daybreak.abilitywar.game.*;
import daybreak.abilitywar.game.AbstractGame.*;
import daybreak.abilitywar.game.manager.effect.registry.*;
import daybreak.abilitywar.game.manager.effect.registry.EffectRegistry.*;
import daybreak.abilitywar.utils.base.concurrent.*;
import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.event.player.*;
import org.bukkit.potion.*;

@EffectManifest(name = "마비", displayName = "§e마비", method = ApplicationMethod.UNIQUE_LONGEST)
public class Paralysis extends AbstractGame.Effect implements Listener {

    public static final EffectRegistration<Paralysis> registration = EffectRegistry.registerEffect(Paralysis.class);

    public static void apply(Participant participant, TimeUnit timeUnit, int duration) {
        registration.apply(participant, timeUnit, duration);
    }

    private final Participant participant;

    public Paralysis(Participant participant, TimeUnit timeUnit, int duration) {
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
    private void onPlayerMove(final PlayerMoveEvent e) {
        if (e.getPlayer().getUniqueId().equals(participant.getPlayer().getUniqueId())) {
            final Location from = e.getFrom(), to = e.getTo();
            if (to != null) {
                to.setX(from.getX());
                to.setY(from.getY());
                to.setZ(from.getZ());
            }
        }
    }

    @Override
    protected void run(int count) {
        super.run(count);
        participant.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));
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
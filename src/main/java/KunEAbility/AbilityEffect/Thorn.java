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

@EffectManifest(name = "속박의 가시", displayName = "§2속박의 가시", method = ApplicationMethod.UNIQUE_LONGEST, description = {
        "대상은 이동 불능 상태에 돌입하며, 움직임을 시도할 경우 독에 감염됩니다."
})
public class Thorn extends AbstractGame.Effect implements Listener {

    public static final EffectRegistration<Thorn> registration = EffectRegistry.registerEffect(Thorn.class);

    public static void apply(Participant participant, TimeUnit timeUnit, int duration) {
        registration.apply(participant, timeUnit, duration);
    }

    private final Participant participant;

    public Thorn(Participant participant, TimeUnit timeUnit, int duration) {
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
            participant.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.POISON, 20, 1));
            if (to != null) {
                to.setX(from.getX());
                to.setY(from.getY());
                to.setZ(from.getZ());
            }
        }
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
// +10 DEF for 2 rounds :current round and the next
// doesn't touch the base defense field; Combatant.getDefense() adds the boost dynamically
public class DefenseBoost implements StatusEffect {
    private int duration;
    private static final int BOOST_AMOUNT = 10;
    private static final String EFFECT_NAME = "DefenseBoost";

    public DefenseBoost() {
        this.duration = 2;
    }

    @Override
    public int getDuration() { return duration; }

    @Override
    public void decrementDuration() {
        if (duration > 0) duration--;
    }

    @Override
    public boolean isExpired() { return duration <= 0; }

    @Override
    public void apply(Combatant combatant) {
        // getDefense() picks this up automatically while the effect is active
    }

    @Override
    public void remove(Combatant combatant) {
        // removing from activeEffects is enough , no base stat was changed
    }

    @Override
    public String getName() { return EFFECT_NAME; }

    public int getBoostAmount() { return BOOST_AMOUNT; }
}

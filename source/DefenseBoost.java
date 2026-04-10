/**
 * Defense boost status effect - increases defense by 10.
 * Duration: 2 turns (current round + next round).
 */
public class DefenseBoost implements StatusEffect {
    private int duration;
    private static final int BOOST_AMOUNT = 10;
    private static final String EFFECT_NAME = "DefenseBoost";
    
    public DefenseBoost() {
        // Initialize duration to 2 (current round + next round)
        this.duration = 2;
    }
    
    @Override
    public int getDuration() {
        // Return the current duration value
        return this.duration;
    }
    
    @Override
    public void decrementDuration() {
        // Check if duration is greater than 0
        if (duration > 0)
        {
            // If so, decrement duration by 1
            duration--;
        }
    }
    
    @Override
    public boolean isExpired() {
        // Return true if duration is less than or equal to 0, false otherwise
        return (duration <= 0);
    }
    
    @Override
    public void apply(Combatant combatant) {
        // Boost is applied in Combatant.getDefense() while this effect is active.
    }
    
    @Override
    public void remove(Combatant combatant) {
        // Boost is applied in Combatant.getDefense(); removing the effect restores base behavior.
    }
    
    @Override
    public String getName() {
        // Return the predefined effect name constant
        return EFFECT_NAME;
    }
    
    /**
     * Gets the defense boost amount.
     * 
     * @return The amount to add to defense
     */
    public int getBoostAmount() {
        // Return the predefined boost amount constant
        return BOOST_AMOUNT;
    }
}

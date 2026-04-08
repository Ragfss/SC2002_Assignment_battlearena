// Tracks cooldown for a combatant's special skill
// Cooldown lasts 3 turns including the turn it was used
public class CooldownTracker {
    private int turnsRemaining;
    private static final int COOLDOWN_DURATION = 3;
    
    public CooldownTracker() {
        this.turnsRemaining = 0; 
    }

    public boolean isAvailable() {
        return turnsRemaining <= 0;
    }
    
    public void startCooldown() {
        this.turnsRemaining = COOLDOWN_DURATION;
    }

    public void decrementCooldown() {
        if (turnsRemaining > 0) {
            turnsRemaining--;
        }
    }
    
    public int getTurnsRemaining() {
        return turnsRemaining;
    }
    
    public void reset() {
        this.turnsRemaining = 0;
    }
}

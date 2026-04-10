import java.util.ArrayList;
import java.util.List;

// Snapshot of battle at given moment
// Used to pass state to UI without showing the actual game objects
public class GameState {
    private final List<CombatantSnapshot> combatants;
    private final int currentRound;
    private final BattleResult result;
    
    public GameState(List<CombatantSnapshot> combatants, int currentRound, BattleResult result) {
        this.combatants = new ArrayList<>(combatants);
        this.currentRound = currentRound;
        this.result = result;
    }
    
    public List<CombatantSnapshot> getCombatants() {
        return new ArrayList<>(combatants);
    }
    
    public int getCurrentRound() {
        return currentRound;
    }
    
    public BattleResult getResult() {
        return result;
    }
    
    // Copy of a combatant's stats at one point in time
    public static class CombatantSnapshot {
        private final String name;
        private final int currentHP;
        private final int maxHP;
        private final int attack;
        private final int defense;
        private final int speed;
        private final List<String> activeEffects;
        private final boolean isAlive;
        
        public CombatantSnapshot(Combatant combatant) {
            this.name = combatant.getName();
            this.currentHP = combatant.getCurrentHP();
            this.maxHP = combatant.getMaxHP();
            this.attack = combatant.getAttack();
            this.defense = combatant.getDefense();
            this.speed = combatant.getSpeed();
            this.isAlive = combatant.isAlive();

            this.activeEffects = new ArrayList<>();
            for (StatusEffect effect : combatant.getActiveEffects()) {
                activeEffects.add(effect.getName());
            }
        }
        
        public String getName() {return name;}
        public int getCurrentHP() {return currentHP;}
        public int getMaxHP() {return maxHP;}
        public int getAttack() {return attack;}
        public int getDefense() {return defense;}
        public int getSpeed() {return speed;}
        public boolean isAlive() { return isAlive; }
        public List<String> getActiveEffects() {
            return new ArrayList<>(activeEffects);
        }
    }

    public enum BattleResult {
        ONGOING,
        VICTORY,
        DEFEAT
    }
}

import java.util.List;

public class Wolf extends Combatant {
    private static final int WOLF_HP = 40;
    private static final int WOLF_ATK = 45;
    private static final int WOLF_DEF = 5;
    private static final int WOLF_SPD = 35;
    
    public Wolf() {
        super("Wolf", WOLF_HP, WOLF_ATK, WOLF_DEF, WOLF_SPD);
    }
    
    @Override
    public Action getAction(List<Combatant> availableTargets) {
        return null;
    }
}
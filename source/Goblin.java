import java.util.List;

public class Goblin extends Combatant {
    private static final int GOBLIN_HP  = 55;
    private static final int GOBLIN_ATK = 35;
    private static final int GOBLIN_DEF = 15;
    private static final int GOBLIN_SPD = 25;

    public Goblin() {
        super("Goblin", GOBLIN_HP, GOBLIN_ATK, GOBLIN_DEF, GOBLIN_SPD);
    }

    @Override
    public Action getAction(List<Combatant> availableTargets) {
        // enemy actions are handled by BasicEnemyStrategy via BattleEngine.getEnemyAction()
        return null;
    }
}

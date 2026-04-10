import java.util.List;

public class Wizard extends Combatant {
    private static final int WIZARD_HP = 200;
    private static final int WIZARD_ATK = 50;
    private static final int WIZARD_DEF = 15;
    private static final int WIZARD_SPD = 28;

    public Wizard() {
        super("Wizard", WIZARD_HP, WIZARD_ATK, WIZARD_DEF, WIZARD_SPD);
    }

    @Override
    public Action getAction(List<Combatant> availableTargets) {
        return null;
    }
}

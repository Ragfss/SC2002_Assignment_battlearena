import java.util.List;

public class Potion implements Item {
    private static final int    HEAL_AMOUNT = 100;
    private static final String ITEM_NAME   = "Potion";

    @Override
    public void use(Combatant user, List<Combatant> targets) {
        if (user == null) return;
        // just heals , GameManager's useItem() handles the display via CLIHandler
        user.heal(HEAL_AMOUNT);
    }

    @Override
    public String getName() { return ITEM_NAME; }

    @Override
    public boolean isConsumable() { return true; }
}

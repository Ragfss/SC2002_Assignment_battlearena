import java.util.List;

public class PowerStone implements Item {
    private static final String ITEM_NAME = "Power Stone";

    @Override
    public void use(Combatant user, List<Combatant> targets) {
        // actual skill execution is done in GameManager.useItem()
        // this method satisfies the Item interface contract
    }

    @Override
    public String getName() { return ITEM_NAME; }

    @Override
    public boolean isConsumable() { return true; }

    // fires the special skill without touching the cooldown timer
    public void executeSkillWithoutCooldown(Combatant user, Action skillAction, List<Combatant> targets) {
        if (user != null && skillAction != null) {
            skillAction.execute(user, targets);
        }
    }
}

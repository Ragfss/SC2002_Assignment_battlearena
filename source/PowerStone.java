import java.util.List;

public class PowerStone implements Item {
    private static final String ITEM_NAME = "Power Stone";
    
    @Override
    public void use(Combatant user, List<Combatant> targets) {
        if (user == null) return;
       
        System.out.println(user.getName() + " used a Power Stone to ready a special skill!");
    }
    
    @Override
    public String getName() {
        return ITEM_NAME;
    }
    
    @Override
    public boolean isConsumable() {
        return true;
    }
    
    public void executeSkillWithoutCooldown(Combatant user, Action skillAction, List<Combatant> targets) {
        if (user != null && skillAction != null) {
            
            skillAction.execute(user, targets);
            
        }
    }
}
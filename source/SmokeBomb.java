import java.util.List;

public class SmokeBomb implements Item {
    private static final String ITEM_NAME = "Smoke Bomb";
    
    @Override
    public void use(Combatant user, List<Combatant> targets) {
        if (user == null) return;
        
        user.addStatusEffect(new DamageZeroEffect()); 
        System.out.println(user.getName() + " used a Smoke Bomb! Enemy attacks will do 0 damage for 2 turns.");
    }
    
    @Override
    public String getName() {
        return ITEM_NAME;
    }
    
    @Override
    public boolean isConsumable() {
        return true;
    }
}

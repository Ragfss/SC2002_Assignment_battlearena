import java.util.ArrayList;
import java.util.List;

/**
 * Basic enemy AI strategy - enemies always use BasicAttack on the first available target.
 */
public class BasicEnemyStrategy implements EnemyAction {
    @Override
    public Action getAction(Combatant enemy, List<Combatant> availableTargets) {
        // Validate that enemy and availableTargets are not null and targets list is not empty  
        if (enemy !=null && availableTargets !=null && !availableTargets.isEmpty()) {

            List<Combatant> targets = new ArrayList<>();
            for (Combatant target : availableTargets) {
                if (target !=null && target.isAlive()) {
                    targets.add(target);
                    break;
                }
            }
            if (targets.isEmpty()) {
                return null;
            }

            return new BasicAttack();
        }
        return null;
    }
}

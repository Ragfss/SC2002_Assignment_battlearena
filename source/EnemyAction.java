import java.util.List;

public interface EnemyAction {
    Action getAction(Combatant enemy, List<Combatant> availableTargets);
}

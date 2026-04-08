import java.util.List;

// Interface for sorting combatants into turn order
public interface TurnOrder {
    List<Combatant> sort(List<Combatant> combatants);
}

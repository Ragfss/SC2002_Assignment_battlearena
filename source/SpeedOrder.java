import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// Sorts combatants by speed in descending order
public class SpeedOrder implements TurnOrder {
    @Override
    public List<Combatant> sort(List<Combatant> combatants) {
        List<Combatant> sorted = new ArrayList<>(combatants);
        sorted.sort(Comparator.comparingInt(Combatant::getSpeed).reversed());
        return sorted;
    }
}

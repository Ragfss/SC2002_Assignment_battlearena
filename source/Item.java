import java.util.List;

public interface Item {
    void use(Combatant user, List<Combatant> targets);
    String getName();
    boolean isConsumable();
}

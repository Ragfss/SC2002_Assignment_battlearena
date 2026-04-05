public interface Item {
    void use(Combatant user, java.util.List<Combatant> targets);
    String getName();
    boolean isConsumable();
}
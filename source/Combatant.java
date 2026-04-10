import java.util.ArrayList;
import java.util.List;

public abstract class Combatant {
    protected int maxHP;
    protected int currentHP;
    protected int attack;
    protected int defense;
    protected int speed;
    protected String name;
    protected List<StatusEffect> activeEffects;

    public Combatant(String name, int maxHP, int attack, int defense, int speed) {
        this.name = name;
        this.maxHP = maxHP;
        this.currentHP = maxHP;
        this.attack = attack;
        this.defense = defense;
        this.speed = speed;
        this.activeEffects = new ArrayList<>();
    }

    public int getMaxHP() { return maxHP; }
    public int getCurrentHP() { return currentHP; }
    public int getAttack() { return attack; }

    protected void setAttack(int attack) {
        this.attack = attack;
    }
    public int getSpeed() { return speed; }
    public String getName() { return name; }
    
    public int getDefense() {
        int totalDefense = this.defense;
        for (StatusEffect effect : activeEffects) {
            if (effect instanceof DefenseBoost) {
                totalDefense += ((DefenseBoost) effect).getBoostAmount();
            }
        }
        return totalDefense;
    }

    public List<StatusEffect> getActiveEffects() {
        return new ArrayList<>(activeEffects);
    }

    protected void setCurrentHP(int hp) {
        this.currentHP = Math.max(0, Math.min(hp, maxHP));
    }

    public void takeDamage(int damage) {
        if (hasStatusEffect("DamageZeroEffect") || hasStatusEffect("SmokeBombInvulnerability")) {
            damage = 0;
        }
        int effectiveDamage = Math.max(0, damage - getDefense()); 
        setCurrentHP(this.currentHP - effectiveDamage);
    }

    public void heal(int amount) {
        setCurrentHP(this.currentHP + amount);
    }

    public boolean isAlive() {
        return currentHP > 0;
    }

    public void addStatusEffect(StatusEffect effect) {
        if (effect != null && !activeEffects.contains(effect)) {
            activeEffects.add(effect);
            effect.apply(this);
            System.out.println(name + " is now affected by " + effect.getName() + "!");
        }
    }

    public void removeStatusEffect(StatusEffect effect) {
        if (effect != null && activeEffects.contains(effect)) {
            activeEffects.remove(effect);
            effect.remove(this);
        }
    }

    public boolean hasStatusEffect(String effectName) {
        return activeEffects.stream()
                .anyMatch(e -> e.getName().equalsIgnoreCase(effectName));
    }

    public void updateStatusEffects() {
        List<StatusEffect> expiredEffects = new ArrayList<>();
        for (StatusEffect effect : activeEffects) {
            effect.decrementDuration();
            if (effect.isExpired()) {
                expiredEffects.add(effect);
            }
        }
        for (StatusEffect expired : expiredEffects) {
            removeStatusEffect(expired);
        }
    }

    public abstract Action getAction(List<Combatant> availableTargets);

    public boolean canAct() {
        return isAlive() && !hasStatusEffect("Stun");
    }
}
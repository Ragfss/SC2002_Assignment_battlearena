import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Main game loop that linkes BattleEngine, CLIHandler and LevelManager
// Handles player turns, enemy turns, items and backup spawns

public class GameManager {
    private BattleEngine battleEngine;
    private CLIHandler cliHandler;
    private LevelManager levelManager;
    private Map<Combatant, CooldownTracker> cooldownTrackers;
    private Map<Combatant, List<Item>> playerInventories;
    private int roundsSurvived;
    private Combatant player;
    private Difficulty currentDifficulty;
    private boolean backupSpawned;
    private List<Combatant> allEnemiesEver; // Track all enemies including eliminated ones
    private List<Combatant> initialEnemies; // Track initial enemies separately
    
    public GameManager() {
        this.battleEngine = new BattleEngine(new SpeedOrder(), new BasicEnemyStrategy());
        this.cliHandler = new CLIHandler();
        this.levelManager = new LevelManager();
        this.cooldownTrackers = new HashMap<>();
        this.playerInventories = new HashMap<>();
    }
    
    public void start() {
        boolean playing = true;

        while (playing) {
            cliHandler.displayLoadingScreen();
            cliHandler.displayNewline();
            player = cliHandler.selectPlayer();
            cliHandler.displayNewline();
            Difficulty difficulty = cliHandler.selectDifficulty();

            initializeGame(player, difficulty);
            runBattle();
            displayCompletion();

            playing = cliHandler.promptReplay();
        }

        cliHandler.close();
    }
    
    private void initializeGame(Combatant player, Difficulty difficulty) {
        this.currentDifficulty = difficulty;
        this.backupSpawned = false;

        // set up starting items based on class
        List<Item> inventory = new ArrayList<>();
        if (player instanceof Warrior) {
            inventory.add(new Potion());
            inventory.add(new SmokeBomb());
        } 
        else if (player instanceof Wizard) {
            inventory.add(new PowerStone());
            inventory.add(new Potion());
        }
        playerInventories.put(player, inventory);

        cooldownTrackers.put(player, new CooldownTracker());

        // spawn initial enemies for this difficulty
        List<Combatant> enemies = levelManager.getInitialWave(difficulty);
        cliHandler.initializeEnemyLabels(enemies);
        this.initialEnemies = enemies;
        this.allEnemiesEver = new ArrayList<>(enemies);

        List<Combatant> playerList = new ArrayList<>();
        playerList.add(player);
        battleEngine.initialize(playerList, enemies);

        this.roundsSurvived = 0;
    }
    
    private void runBattle() {
        while (!battleEngine.isBattleOver()) {
            battleEngine.startRound();
            roundsSurvived = battleEngine.getGameState().getCurrentRound();

            if (battleEngine.isBattleOver()) break;

            cliHandler.displayRoundHeader(roundsSurvived);

            List<Combatant> turnOrder = battleEngine.getTurnOrder();

            for (Combatant combatant : turnOrder) {
                if (battleEngine.isBattleOver()) break;

                if (!combatant.isAlive()) {
                    cliHandler.displaySkippedTurn(getDisplayName(combatant), "ELIMINATED");
                    continue;
                }

                if (!combatant.canAct()) {
                    if (combatant.hasStatusEffect("Stun") || combatant.hasStatusEffect("StunEffect")) {
                        cliHandler.displaySkippedTurn(getDisplayName(combatant), "STUNNED");
                        checkStatusEffectExpiration(combatant);
                    } 
                    else if (!combatant.isAlive()) {
                        cliHandler.displaySkippedTurn(getDisplayName(combatant), "ELIMINATED");
                        checkStatusEffectExpiration(combatant);
                        checkBackupSpawn();
                    }
                    continue;
                }

                if (battleEngine.isBattleOver()) break;

                // player vs enemy turn
                if (combatant instanceof Warrior || combatant instanceof Wizard) {
                    processPlayerTurn(combatant);
                    checkBackupSpawn();
                } else {
                    processEnemyTurn(combatant);
                    checkBackupSpawn();
                }
            }

            // shows the summary before ticking cooldowns
            displayEndOfRound();
            updateCooldowns();
        }
    }

    private void processPlayerTurn(Combatant player) {
        CooldownTracker tracker = cooldownTrackers.get(player);
        List<Item> inventory = playerInventories.get(player);
        boolean canUseSkill = (tracker != null && tracker.isAvailable());
        boolean hasItems = (inventory != null && !inventory.isEmpty());

        int actionChoice = cliHandler.selectAction(canUseSkill, hasItems);
        Action action = null;
        List<Combatant> targets = new ArrayList<>();
        String actionName = null;

        switch (actionChoice) {
            case 1: // basic attack
                Combatant target = cliHandler.selectEnemyTarget(battleEngine.getAliveEnemies());
                targets.add(target);
                action = new BasicAttack();
                actionName = "BasicAttack";
                break;

            case 2: // defend
                action = new Defend();
                targets.add(player);
                actionName = "Defend";
                break;

            case 3: // special skill
                if (canUseSkill) {
                    action = getSpecialSkillAction(player);
                    if (action instanceof ShieldBash) {
                        actionName = "Shield Bash";
                        Combatant enemyTarget = cliHandler.selectEnemyTarget(battleEngine.getAliveEnemies());
                        targets.add(enemyTarget);
                    } 
                    else if (action instanceof ArcaneBlast) {
                        actionName = "Arcane Blast";
                        targets = battleEngine.getAliveEnemies();
                    }
                    tracker.startCooldown();
                }
                break;

            case 4: // use item
                if (hasItems) {
                    int itemIndex = cliHandler.selectItem(inventory);
                    Item selectedItem = inventory.get(itemIndex);
                    useItem(player, selectedItem);
                    if (selectedItem.isConsumable()) {
                        inventory.remove(itemIndex);
                    }
                    return; // items don't go through the normal action flow
                }
                break;
        }

        if (action != null) {
            executeActionWithDisplay(player, action, actionName, targets);
        }
    }
  
    private void processEnemyTurn(Combatant enemy) {
        Action action = battleEngine.getEnemyAction(enemy);
        if (action != null) {
            List<Combatant> targets = battleEngine.getAlivePlayers();
            if (!targets.isEmpty()) {
                executeActionWithDisplay(enemy, action, "BasicAttack", targets.subList(0, 1));
            }
        }
    }
    
    private void executeActionWithDisplay(Combatant source, Action action, String actionName, List<Combatant> targets) {
        String sourceName = getDisplayName(source);

        if (action instanceof BasicAttack) {
            // save HP before so we can show the change
            Map<Combatant, Integer> hpBefore = new HashMap<>();
            for (Combatant t : targets) {
                if (t != null && t.isAlive()) {
                    hpBefore.put(t, t.getCurrentHP());
                }
            }

            battleEngine.executeTurn(source, action, targets);

            for (Combatant t : targets) {
                if (t == null || !hpBefore.containsKey(t)) continue;

                int before = hpBefore.get(t);
                int after = t.isAlive() ? t.getCurrentHP() : 0;
                int damage = Math.max(0, source.getAttack() - t.getDefense());

                // smoke bomb nullifies damage
                if (t.hasStatusEffect("DamageZeroEffect") || t.hasStatusEffect("SmokeBombInvulnerability")) {
                    damage = 0;
                }

                cliHandler.displayAction(sourceName, "BasicAttack", getDisplayName(t),
                        before, after, source.getAttack(), t.getDefense(), damage);

                if (damage == 0 && t.isAlive()) {
                    cliHandler.displayNullifiedBasicAttack(sourceName, getDisplayName(t),
                            getDisplayName(t), t.getCurrentHP());
                }
            }

        } else if (action instanceof ShieldBash) {
            Combatant target = targets.get(0);
            if (target != null && target.isAlive()) {
                int hpBefore = target.getCurrentHP();
                int damage = Math.max(0, source.getAttack() - target.getDefense());

                battleEngine.executeTurn(source, action, targets);

                int hpAfter = target.isAlive() ? target.getCurrentHP() : 0;
                cliHandler.displayAction(sourceName, "Shield Bash", getDisplayName(target),
                        hpBefore, hpAfter, source.getAttack(), target.getDefense(), damage);

                if (target.isAlive() && (target.hasStatusEffect("Stun") || target.hasStatusEffect("StunEffect"))) {
                    cliHandler.displayStatusEffect(getDisplayName(target), "Stun", 2);
                }

                CooldownTracker tracker = cooldownTrackers.get(source);
                if (tracker != null) {
                    cliHandler.displayCooldownSet(tracker.getTurnsRemaining());
                } 
                else {
                    cliHandler.displayNewline();
                }
            }

        } else if (action instanceof ArcaneBlast) {
            int attackerATK = source.getAttack();

            Map<Combatant, Integer> hpBefore = new HashMap<>();
            for (Combatant t : targets) {
                if (t != null && t.isAlive()) {
                    hpBefore.put(t, t.getCurrentHP());
                }
            }

            battleEngine.executeTurn(source, action, targets);

            int kills = 0;
            List<String> resultStrings = new ArrayList<>();
            boolean goblinSurvives = false;

            for (Combatant t : targets) {
                if (t == null || !hpBefore.containsKey(t)) continue;

                int before = hpBefore.get(t);
                int after = t.isAlive() ? t.getCurrentHP() : 0;
                int damage = Math.max(0, attackerATK - t.getDefense());
                String damageCalc = attackerATK + "-" + t.getDefense() + "=" + damage;

                if (!t.isAlive()) {
                    resultStrings.add(getDisplayName(t) + " " + damageCalc + " X Eliminated");
                    kills++;
                }
                else {
                    resultStrings.add(getDisplayName(t) + " " + damageCalc + " HP:" + before + "vs" + after);
                    if (t instanceof Goblin) {
                        goblinSurvives = true;
                    }
                }
            }

            CooldownTracker tracker = cooldownTrackers.get(source);
            Integer cooldownRounds = (tracker != null) ? tracker.getTurnsRemaining() : null;

            cliHandler.displayArcaneBlastSummary(sourceName, "Arcane Blast", resultStrings,
                    attackerATK, kills, goblinSurvives, cooldownRounds);

        } 
        else if (action instanceof Defend) {
            battleEngine.executeTurn(source, action, targets);

        } 
        else {
            // fallback for anything else
            battleEngine.executeTurn(source, action, targets);
        }
    }
    
    private Action getSpecialSkillAction(Combatant player) {
        if (player instanceof Warrior) {
            return new ShieldBash();
        } 
        else if (player instanceof Wizard) {
            return new ArcaneBlast();
        }
        return null;
    }
    
    private void useItem(Combatant player, Item item) {
        String displayName = getDisplayName(player);

        if (item instanceof Potion) {
            int hpBefore = player.getCurrentHP();
            item.use(player, new ArrayList<>());
            int hpAfter = player.getCurrentHP();
            int healed = hpAfter - hpBefore;
            cliHandler.displayItemUsage(displayName, "Potion",
                    "HP " + hpBefore + " -> " + hpAfter + " (+" + healed + ")");

        } else if (item instanceof SmokeBomb) {
            item.use(player, new ArrayList<>());
            cliHandler.displayItemUsage(displayName, "Smoke Bomb",
                    "Enemy attacks deal 0 damage for 2 turns");

        } else if (item instanceof PowerStone) {
            Action skillAction = getSpecialSkillAction(player);
            if (skillAction != null) {
                List<Combatant> targets;
                String skillName;

                if (skillAction instanceof ShieldBash) {
                    skillName = "Shield Bash";
                    targets = new ArrayList<>();
                    Combatant enemyTarget = cliHandler.selectEnemyTarget(battleEngine.getAliveEnemies());
                    targets.add(enemyTarget);
                } 
                else if (skillAction instanceof ArcaneBlast) {
                    skillName = "Arcane Blast";
                    targets = battleEngine.getAliveEnemies();
                } 
                else {
                    return;
                }

                cliHandler.displayPowerStoneTriggered(displayName, skillName);

                // save hp before for display
                Map<Combatant, Integer> hpBefore = new HashMap<>();
                for (Combatant t : targets) {
                    if (t != null && t.isAlive()) {
                        hpBefore.put(t, t.getCurrentHP());
                    }
                }

                // execute directly, skip executeTurn so cooldown doesn't trigger
                skillAction.execute(player, targets);

                if (skillAction instanceof ShieldBash) {
                    Combatant target = targets.get(0);
                    if (target != null && hpBefore.containsKey(target)) {
                        int before = hpBefore.get(target);
                        int after = target.isAlive() ? target.getCurrentHP() : 0;
                        int damage = Math.max(0, player.getAttack() - target.getDefense());
                        cliHandler.displayAction(displayName, "Shield Bash", getDisplayName(target),
                                before, after, player.getAttack(), target.getDefense(), damage);
                        if (target.isAlive() && (target.hasStatusEffect("Stun") || target.hasStatusEffect("StunEffect"))) {
                            cliHandler.displayStatusEffect(getDisplayName(target), "Stun", 2);
                        }
                    }
                } else if (skillAction instanceof ArcaneBlast) {
                    int kills = 0;
                    List<String> resultStrings = new ArrayList<>();
                    boolean allDefeated = true;

                    for (Combatant t : targets) {
                        if (t == null || !hpBefore.containsKey(t)) continue;
                        int before = hpBefore.get(t);
                        int after = t.isAlive() ? t.getCurrentHP() : 0;
                        int damage = Math.max(0, player.getAttack() - t.getDefense());
                        String calc = damage + " dmg";

                        if (!t.isAlive()) {
                            resultStrings.add(getDisplayName(t) + ": " + calc + " X Eliminated");
                            kills++;
                        } 
                        else {
                            resultStrings.add(getDisplayName(t) + ": " + calc + " HP:" + before + "->" + after);
                            allDefeated = false;
                        }
                    }

                    cliHandler.displayArcaneBlastPowerStone(displayName, player.getAttack(),
                            resultStrings, kills, allDefeated);
                }

                CooldownTracker tracker = cooldownTrackers.get(player);
                int cd = (tracker != null) ? tracker.getTurnsRemaining() : 0;
                cliHandler.displayPowerStoneConsumedAndCooldownUnchanged(cd);

                // manually tick status effects since we didn't go through executeTurn
                player.updateStatusEffects();
                for (Combatant t : targets) {
                    if (t != null) {
                        t.updateStatusEffects();
                    }
                }
            }
        }
    }
    private void checkBackupSpawn() {
        if (!backupSpawned && initialEnemies != null) {
            boolean allDead = true;
            for (Combatant e : initialEnemies) {
                if (e.isAlive()) {
                    allDead = false;
                    break;
                }
            }

            if (allDead) {
                List<Combatant> backupEnemies = levelManager.getBackupWave(currentDifficulty);
                if (!backupEnemies.isEmpty()) {
                    cliHandler.displayMessage("Backup enemies arrive: " + formatEnemyList(backupEnemies));
                    cliHandler.addEnemyLabels(backupEnemies);
                    allEnemiesEver.addAll(backupEnemies);
                    battleEngine.addEnemies(backupEnemies);
                    backupSpawned = true;
                }
            }
        }
    }
    
    private String formatEnemyList(List<Combatant> enemies) {
        List<String> parts = new ArrayList<>();
        for (Combatant e : enemies) {
            parts.add(e.getName() + " (HP: " + e.getMaxHP() + ")");
        }
        return String.join(" + ", parts);
    }
    
    private void checkStatusEffectExpiration(Combatant combatant) {
        boolean hadStun = combatant.hasStatusEffect("Stun") || combatant.hasStatusEffect("StunEffect");
        boolean hadSmokeBomb = combatant.hasStatusEffect("DamageZeroEffect") || combatant.hasStatusEffect("SmokeBombInvulnerability");

        combatant.updateStatusEffects();

        boolean hasStunNow = combatant.hasStatusEffect("Stun") || combatant.hasStatusEffect("StunEffect");
        boolean hasSmokeBombNow = combatant.hasStatusEffect("DamageZeroEffect") || combatant.hasStatusEffect("SmokeBombInvulnerability");

        if (hadStun && !hasStunNow) {
            cliHandler.displayStunExpires();
        }
        if (hadSmokeBomb && !hasSmokeBombNow) {
            cliHandler.displaySmokeBombExpires();
        }
    }
    
    private void updateCooldowns() {
        for (CooldownTracker tracker : cooldownTrackers.values()) {
            tracker.decrementCooldown();
        }
    }
    
    private void displayEndOfRound() {
        List<Combatant> everyone = new ArrayList<>();
        everyone.add(player);
        everyone.addAll(allEnemiesEver);

        int potionCount = 0, smokeBombCount = 0, powerStoneCount = 0;
        boolean hasItems = false;

        List<Item> inventory = playerInventories.get(player);
        if (inventory != null) {
            for (Item item : inventory) {
                if (item instanceof Potion) potionCount++;
                else if (item instanceof SmokeBomb) smokeBombCount++;
                else if (item instanceof PowerStone) powerStoneCount++;
            }
            hasItems = !inventory.isEmpty();
        }

        CooldownTracker tracker = cooldownTrackers.get(player);
        int cooldownRounds = (tracker != null) ? tracker.getTurnsRemaining() : 0;

        Map<Combatant, String> labelMap = new HashMap<>();
        for (Combatant e : allEnemiesEver) {
            String label = cliHandler.getEnemyLabel(e);
            if (label != null && !label.equals(e.getName())) {
                labelMap.put(e, label);
            }
        }

        cliHandler.displayEndOfRound(roundsSurvived, everyone, labelMap,
                potionCount, smokeBombCount, powerStoneCount, cooldownRounds, hasItems, player);
    }
    
    private String getDisplayName(Combatant combatant) {
        if (combatant instanceof Goblin || combatant instanceof Wolf) {
            String label = cliHandler.getEnemyLabel(combatant);
            return combatant.getClass().getSimpleName() + " " + label;
        }
        return combatant.getName();
    }
    
    private void displayCompletion() {
        GameState finalState = battleEngine.getGameState();
        int remainingHP = (player != null) ? player.getCurrentHP() : 0;
        int maxHP = (player != null) ? player.getMaxHP() : 0;

        int potionCount = 0, smokeBombCount = 0, powerStoneCount = 0;
        List<Item> inventory = playerInventories.get(player);
        if (inventory != null) {
            for (Item item : inventory) {
                if (item instanceof Potion) potionCount++;
                else if (item instanceof SmokeBomb) smokeBombCount++;
                else if (item instanceof PowerStone) powerStoneCount++;
            }
        }
        
        cliHandler.displayCompletionScreen(finalState, roundsSurvived, remainingHP, maxHP,
                potionCount, smokeBombCount, powerStoneCount, player);
    }
}

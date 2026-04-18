import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ties BattleEngine (logic), CLIHandler (I/O), and LevelManager (spawning) together
// the only class allowed to know about all three
public class GameManager {
    private BattleEngine battleEngine;
    private CLIHandler   cliHandler;
    private LevelManager levelManager;
    private Map<Combatant, CooldownTracker> cooldownTrackers;
    private Map<Combatant, List<Item>>      playerInventories;
    private int      roundsSurvived;
    private Combatant player;
    private Difficulty currentDifficulty;
    private boolean  backupSpawned;
    private List<Combatant> allEnemiesEver;  // all enemies including eliminated, for end-of-round display
    private List<Combatant> initialEnemies;  // first wave only, used to detect when backup should spawn

    public GameManager() {
        this.battleEngine     = new BattleEngine(new SpeedOrder(), new BasicEnemyStrategy());
        this.cliHandler       = new CLIHandler();
        this.levelManager     = new LevelManager();
        this.cooldownTrackers  = new HashMap<>();
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
            cliHandler.displayNewline();

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

        // player chooses their 2 items — duplicates allowed per spec
        List<Item> inventory = cliHandler.selectItems();
        playerInventories.put(player, inventory);

        cooldownTrackers.put(player, new CooldownTracker());

        // spawn initial enemies and assign A/B/C display labels
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
                    // show stun expiry if combatant was stunned when eliminated
                    checkStatusEffectExpiration(combatant);
                    continue;
                }

                if (!combatant.canAct()) {
                    // alive but stunned , skip and tick stun down once more
                    // (startRound already ticked once; stunned combatants need two ticks total per round)
                    cliHandler.displaySkippedTurn(getDisplayName(combatant), "STUNNED");
                    checkStatusEffectExpiration(combatant);
                    continue;
                }

                // player vs enemy branch
                if (combatant instanceof Warrior || combatant instanceof Wizard) {
                    processPlayerTurn(combatant);
                    checkBackupSpawn();
                } else {
                    processEnemyTurn(combatant);
                    checkBackupSpawn();
                }
            }

            // print summary before ticking cooldowns down
            displayEndOfRound();
            updateCooldowns();
        }
    }

    private void processPlayerTurn(Combatant player) {
        CooldownTracker tracker  = cooldownTrackers.get(player);
        List<Item> inventory     = playerInventories.get(player);
        boolean canUseSkill      = (tracker != null && tracker.isAvailable());
        boolean hasItems         = (inventory != null && !inventory.isEmpty());

        int actionChoice = cliHandler.selectAction(canUseSkill, hasItems);
        Action action    = null;
        List<Combatant> targets = new ArrayList<>();
        String actionName = null;

        switch (actionChoice) {
            case 1:
                // basic attack — player picks the target
                targets.add(cliHandler.selectEnemyTarget(battleEngine.getAliveEnemies()));
                action     = new BasicAttack();
                actionName = "BasicAttack";
                break;

            case 2:
                // defend targets self
                action     = new Defend();
                actionName = "Defend";
                targets.add(player);
                break;

            case 3:
                if (canUseSkill) {
                    action = getSpecialSkillAction(player);
                    if (action instanceof ShieldBash) {
                        actionName = "Shield Bash";
                        targets.add(cliHandler.selectEnemyTarget(battleEngine.getAliveEnemies()));
                    } else if (action instanceof ArcaneBlast) {
                        actionName = "Arcane Blast";
                        targets    = battleEngine.getAliveEnemies();
                    }
                    tracker.startCooldown();
                }
                break;

            case 4:
                if (hasItems) {
                    int itemIndex   = cliHandler.selectItem(inventory);
                    Item selectedItem = inventory.get(itemIndex);
                    useItem(player, selectedItem);
                    if (selectedItem.isConsumable()) inventory.remove(itemIndex);
                    return;  // items bypass the normal action flow
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

    private void executeActionWithDisplay(Combatant source, Action action, String actionName,
                                          List<Combatant> targets) {
        String sourceName = getDisplayName(source);

        if (action instanceof BasicAttack) {
            // snapshot HP before attacking so display shows the correct before/after
            Map<Combatant, Integer> hpBefore = new HashMap<>();
            for (Combatant t : targets) {
                if (t != null && t.isAlive()) hpBefore.put(t, t.getCurrentHP());
            }

            battleEngine.executeTurn(source, action, targets);

            for (Combatant t : targets) {
                if (t == null || !hpBefore.containsKey(t)) continue;

                int before = hpBefore.get(t);
                int after  = t.isAlive() ? t.getCurrentHP() : 0;

                // check AFTER the turn so we reflect what actually happened
                boolean nullified = t.hasStatusEffect("DamageZeroEffect");
                int damage = nullified ? 0 : Math.max(0, source.getAttack() - t.getDefense());

                if (nullified) {
                    cliHandler.displayNullifiedBasicAttack(sourceName, getDisplayName(t),
                            getDisplayName(t), t.getCurrentHP());
                } else {
                    cliHandler.displayAction(sourceName, "BasicAttack", getDisplayName(t),
                            before, after, source.getAttack(), t.getDefense(), damage);
                }
            }

        } else if (action instanceof ShieldBash) {
            Combatant target = targets.get(0);
            if (target != null && target.isAlive()) {
                int hpBefore = target.getCurrentHP();
                int damage   = Math.max(0, source.getAttack() - target.getDefense());

                battleEngine.executeTurn(source, action, targets);

                int hpAfter = target.isAlive() ? target.getCurrentHP() : 0;
                cliHandler.displayAction(sourceName, "Shield Bash", getDisplayName(target),
                        hpBefore, hpAfter, source.getAttack(), target.getDefense(), damage);

                if (target.hasStatusEffect("Stun")) {
                    cliHandler.displayStatusEffect(getDisplayName(target), "Stun", 2);
                }

                CooldownTracker tracker = cooldownTrackers.get(source);
                if (tracker != null) cliHandler.displayCooldownSet(tracker.getTurnsRemaining());
            }

        } else if (action instanceof ArcaneBlast) {
            int atk = source.getAttack();

            Map<Combatant, Integer> hpBefore = new HashMap<>();
            for (Combatant t : targets) {
                if (t != null && t.isAlive()) hpBefore.put(t, t.getCurrentHP());
            }

            battleEngine.executeTurn(source, action, targets);

            int kills = 0;
            List<String> results = new ArrayList<>();
            boolean goblinSurvives = false;

            for (Combatant t : targets) {
                if (t == null || !hpBefore.containsKey(t)) continue;

                int before = hpBefore.get(t);
                int after  = t.isAlive() ? t.getCurrentHP() : 0;
                int damage = Math.max(0, atk - t.getDefense());
                String calc = atk + "-" + t.getDefense() + "=" + damage;

                if (!t.isAlive()) {
                    results.add(getDisplayName(t) + " HP: " + before + " -> 0 X ELIMINATED (dmg: " + calc + ")");
                    kills++;
                } else {
                    results.add(getDisplayName(t) + " HP: " + before + " -> " + after + " (dmg: " + calc + ")");
                    if (t instanceof Goblin) goblinSurvives = true;
                }
            }

            CooldownTracker tracker  = cooldownTrackers.get(source);
            Integer cooldownRounds   = (tracker != null) ? tracker.getTurnsRemaining() : null;
            cliHandler.displayArcaneBlastSummary(sourceName, "Arcane Blast", results,
                    atk, kills, goblinSurvives, cooldownRounds);

        } else if (action instanceof Defend) {
            battleEngine.executeTurn(source, action, targets);
            // defend is silent , effect shows up in stats, no action line needed

        } else {
            battleEngine.executeTurn(source, action, targets);
        }
    }

    private Action getSpecialSkillAction(Combatant player) {
        if (player instanceof Warrior) return new ShieldBash();
        if (player instanceof Wizard)  return new ArcaneBlast();
        return null;
    }

    private void useItem(Combatant player, Item item) {
        String displayName = getDisplayName(player);

        if (item instanceof Potion) {
            int hpBefore = player.getCurrentHP();
            item.use(player, new ArrayList<>());
            int hpAfter = player.getCurrentHP();
            cliHandler.displayItemUsage(displayName, "Potion",
                    "HP " + hpBefore + " -> " + hpAfter + " (+" + (hpAfter - hpBefore) + ")");

        } else if (item instanceof SmokeBomb) {
            item.use(player, new ArrayList<>());
            cliHandler.displayItemUsage(displayName, "Smoke Bomb",
                    "Enemy attacks deal 0 damage this turn + next");

        } else if (item instanceof PowerStone) {
            Action skillAction = getSpecialSkillAction(player);
            if (skillAction == null) return;

            List<Combatant> targets;
            String skillName;

            if (skillAction instanceof ShieldBash) {
                skillName = "Shield Bash";
                targets = new ArrayList<>();
                targets.add(cliHandler.selectEnemyTarget(battleEngine.getAliveEnemies()));
            } else if (skillAction instanceof ArcaneBlast) {
                skillName = "Arcane Blast";
                targets   = battleEngine.getAliveEnemies();
            } else {
                return;
            }

            cliHandler.displayPowerStoneTriggered(displayName, skillName);

            Map<Combatant, Integer> hpBefore = new HashMap<>();
            for (Combatant t : targets) {
                if (t != null && t.isAlive()) hpBefore.put(t, t.getCurrentHP());
            }

            // execute directly , bypassing executeTurn so cooldown stays unchanged
            skillAction.execute(player, targets);

            if (skillAction instanceof ShieldBash) {
                Combatant target = targets.get(0);
                if (target != null && hpBefore.containsKey(target)) {
                    int before = hpBefore.get(target);
                    int after  = target.isAlive() ? target.getCurrentHP() : 0;
                    int damage = Math.max(0, player.getAttack() - target.getDefense());
                    cliHandler.displayAction(displayName, "Shield Bash", getDisplayName(target),
                            before, after, player.getAttack(), target.getDefense(), damage);
                    if (target.hasStatusEffect("Stun")) {
                        cliHandler.displayStatusEffect(getDisplayName(target), "Stun", 2);
                    }
                }

            } else if (skillAction instanceof ArcaneBlast) {
                int kills = 0;
                List<String> results = new ArrayList<>();
                boolean allDefeated  = true;

                for (Combatant t : targets) {
                    if (t == null || !hpBefore.containsKey(t)) continue;
                    int before = hpBefore.get(t);
                    int after  = t.isAlive() ? t.getCurrentHP() : 0;
                    int damage = Math.max(0, player.getAttack() - t.getDefense());

                    if (!t.isAlive()) {
                        results.add(getDisplayName(t) + ": " + damage + " dmg X Eliminated");
                        kills++;
                    } else {
                        results.add(getDisplayName(t) + ": " + damage + " dmg HP: " + before + " -> " + after);
                        allDefeated = false;
                    }
                }
                cliHandler.displayArcaneBlastPowerStone(displayName, player.getAttack(),
                        results, kills, allDefeated);
            }

            CooldownTracker tracker = cooldownTrackers.get(player);
            int cd = (tracker != null) ? tracker.getTurnsRemaining() : 0;
            cliHandler.displayPowerStoneConsumedAndCooldownUnchanged(cd);

            // manually tick effects since we skipped executeTurn
            player.updateStatusEffects();
            for (Combatant t : targets) {
                if (t != null) t.updateStatusEffects();
            }
        }
    }

    private void checkBackupSpawn() {
        if (backupSpawned || initialEnemies == null) return;

        boolean allDead = true;
        for (Combatant e : initialEnemies) {
            if (e.isAlive()) { allDead = false; break; }
        }

        if (allDead) {
            List<Combatant> backup = levelManager.getBackupWave(currentDifficulty);
            if (!backup.isEmpty()) {
                cliHandler.addEnemyLabels(backup);
                allEnemiesEver.addAll(backup);
                battleEngine.addEnemies(backup);
                backupSpawned = true;
                cliHandler.displayMessage("All initial enemies eliminated -> Backup Spawn triggered! "
                        + formatEnemyList(backup) + " enter simultaneously");
            }
        }
    }

    private String formatEnemyList(List<Combatant> enemies) {
        List<String> parts = new ArrayList<>();
        for (Combatant e : enemies) {
            String label = cliHandler.getEnemyLabel(e);
            parts.add(e.getName() + " " + label + " (HP: " + e.getMaxHP() + ")");
        }
        return String.join(" + ", parts);
    }

    // startRound() already ticked once; this second tick is the intended second tick for stun
    // (stun needs 2 ticks to expire: one at round start, one when turn is skipped)
    // dead combatants don't tick : they just display the expiry message
    private void checkStatusEffectExpiration(Combatant combatant) {
        boolean hadStun      = combatant.hasStatusEffect("Stun");
        boolean hadSmokeBomb = combatant.hasStatusEffect("DamageZeroEffect");

        if (combatant.isAlive()) {
            // second tick for stunned-alive combatants — brings stun from 1 to 0 and expires it
            combatant.updateStatusEffects();

            if (hadStun      && !combatant.hasStatusEffect("Stun"))           cliHandler.displayStunExpires();
            if (hadSmokeBomb && !combatant.hasStatusEffect("DamageZeroEffect")) cliHandler.displaySmokeBombExpires();
        } else {
            // eliminated : effects expire naturally, just show the messages
            if (hadStun)      cliHandler.displayStunExpires();
            if (hadSmokeBomb) cliHandler.displaySmokeBombExpires();
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
        List<Item> inventory = playerInventories.get(player);
        if (inventory != null) {
            for (Item item : inventory) {
                if (item instanceof Potion)     potionCount++;
                else if (item instanceof SmokeBomb)  smokeBombCount++;
                else if (item instanceof PowerStone) powerStoneCount++;
            }
        }
        boolean hasItems = (inventory != null && !inventory.isEmpty());

        CooldownTracker tracker = cooldownTrackers.get(player);
        int cooldownRounds      = (tracker != null) ? tracker.getTurnsRemaining() : 0;

        // build label map for the display method
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
        int maxHP       = (player != null) ? player.getMaxHP()     : 0;

        int potionCount = 0, smokeBombCount = 0, powerStoneCount = 0;
        List<Item> inventory = playerInventories.get(player);
        if (inventory != null) {
            for (Item item : inventory) {
                if (item instanceof Potion)     potionCount++;
                else if (item instanceof SmokeBomb)  smokeBombCount++;
                else if (item instanceof PowerStone) powerStoneCount++;
            }
        }

        cliHandler.displayCompletionScreen(finalState, roundsSurvived, remainingHP, maxHP,
                potionCount, smokeBombCount, powerStoneCount, player);
    }
}

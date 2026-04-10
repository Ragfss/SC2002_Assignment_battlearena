import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Command Line Interface handler for the battle arena game.
 * Follows SRP - handles only I/O, no game logic.
 * Translates between user input and engine actions.
 */
public class CLIHandler {
    private Scanner scanner;
    private Map < Combatant, String > enemyLabels; // Maps enemies to labels (A, B, C, etc.)
    private int nextEnemyLabel;
    
    public CLIHandler() {
        // Initialize scanner to read from System.in
        // Initialize enemyLabels as a new empty HashMap
        // Initialize nextEnemyLabel to 0
        this.scanner = new Scanner(System.in);
        this.enemyLabels = new HashMap<>();
        this.nextEnemyLabel = 0;

    }

    /**
     * Initializes enemy labels for display.
     */
    public void initializeEnemyLabels(List<Combatant> enemies) {
        // Clear all existing enemy labels
        // Reset nextEnemyLabel to 0
        // Start with label character 'A'
        // Iterate through each enemy in the list
            // Map enemy to current label character (convert char to String)
            // Increment label character to next letter
        enemyLabels.clear();
        nextEnemyLabel = 0;
        char label ='A';

        for (Combatant enemy: enemies) {
            enemyLabels.put(enemy,String.valueOf(label));
            label++;
        }

    }
    
    /**
     * Adds new enemy labels (for backup spawns).
     */
    public void addEnemyLabels(List<Combatant> newEnemies) {
        // Start with label character 'A'
        // Iterate through each new enemy
            // Check if enemy is not already in the labels map
                // Find next available label by incrementing label character while it's already used
                // Map enemy to the found available label character

        char label = 'A';

        for (Combatant enemy: newEnemies) {
            if (!enemyLabels.containsKey(enemy)){
                // Find next avail label
                while (enemyLabels.containsValue(String.valueOf(label))) {
                    label++;
                }

                enemyLabels.put(enemy,String.valueOf(label));
            }
        }
    }
    
    /**
     * Gets the display label for an enemy.
     */
    public String getEnemyLabel(Combatant enemy) {
        // Return the label for the enemy from the map, or enemy's name if not found
        return enemyLabels.getOrDefault(enemy,enemy.getName());
    }
    
    /**
     * Displays the loading screen with available options.
     */
    public void displayLoadingScreen() {
        // Display formatted header with box drawing characters
        // Display empty line
        // Display "Available Players:" section with Warrior and Wizard stats
        // Display empty line
        // Display "Difficulty Levels:" section with Easy, Medium, Hard descriptions
        // Display empty line
        // Display "Enemy Types:" section with Goblin and Wolf stats
        // Display empty line
        System.out.println("____________________");
        System.out.println("    Battle Arena    ");
        System.out.println("____________________");
        System.out.println();
        System.out.println(" Available Players: ");
        System.out.println();
        System.out.println("        Warrior                     Wizard");
        System.out.println();
        System.out.println("           ^                          .");
        System.out.println("           |                          |");
        System.out.println("         ==+==                        |");
        System.out.println("           |                          |");
        System.out.println("         sword                      wand");
        System.out.println();
        System.out.println(" 1. Warrior - High HP/DEF, Special: Shield Bash (stuns enemy) ");
        System.out.println(" 2. Wizard - High ATK, Special: Arcane Blast (hits all enemies, +10 ATK per kill) ");
        System.out.println();
        System.out.println(" Difficulty Levels: ");
        System.out.println(" 1. Easy ");
        System.out.println(" 2. Medium ");
        System.out.println(" 3. Hard ");
        System.out.println();
        System.out.println("Enemy Types:");
        System.out.println();
        System.out.println(" Goblin - Low HP/DEF, fast");
        System.out.println();
        System.out.println("             ,      ,");
        System.out.println("            /(.-\"\"-.)\\");
        System.out.println("        |\\  \\/      \\/  /|");
        System.out.println("        | \\ / =.  .= \\ / |");
        System.out.println("        \\( \\   o\\/o   / )/");
        System.out.println("         \\_, '-/  \\-' ,_/");
        System.out.println("           /   \\__/   \\");
        System.out.println("           \\ \\__/\\__/ /");
        System.out.println("         ___\\ \\|--|/ /___");
        System.out.println("       /`    \\      /    `\\");
        System.out.println("     /       '----'       \\");
        System.out.println();
        System.out.println(" Wolf - Higher ATK/SPD, more aggressive");
        System.out.println();
        System.out.println("                     .");
        System.out.println("                    / V\\");
        System.out.println("                  / `  /");
        System.out.println("                 <<   |");
        System.out.println("                 /    |");
        System.out.println("               /      |");
        System.out.println("             /        |");
        System.out.println("           /    \\  \\ /");
        System.out.println("          (      ) | |");
        System.out.println("  ________|   _/_  | |");
        System.out.println("<__________\\______)\\__)");
        System.out.println();

    }
    
    /**
     * Prompts user to select a player character.
     */
    public Combatant selectPlayer() {
        // Prompt user to select player (1-2)
        // Read integer input in range 1-2 using readInt method
        // Use switch statement on choice
        // Case 1: return new Warrior instance
        // Case 2: return new Wizard instance
        // Default: return null

        System.out.println("Select A Player (1 or 2):");
        System.out.println();
        System.out.println(" 1. Warrior ");
        System.out.println(" 2. Wizard ");
        System.out.println();
        int choice = readInt(1, 2);

        switch (choice) {
            case 1:
                return new Warrior();
            case 2:
                return new Wizard();
            default:
                return null;

        }

    }
    /**
     * Prompts user to select a difficulty level.
     */
    public Difficulty selectDifficulty() {
        // Prompt user to select difficulty (1-3)
        // Read integer input in range 1-3 using readInt method
        // Use switch statement on choice
        // Case 1: return Difficulty.EASY
        // Case 2: return Difficulty.MEDIUM
        // Case 3: return Difficulty.HARD
        // Default: return Difficulty.EASY

        System.out.println(" Select Difficulty (1-3):");
        System.out.println();
        System.out.println(" 1. Easy");
        System.out.println(" 2. Medium");
        System.out.println(" 3. Hard");
        System.out.println();
        int choice = readInt(1, 3);

        switch (choice) {
            case 1:
                return Difficulty.EASY;
            case 2:
                return Difficulty.MEDIUM;
            case 3:
                return Difficulty.HARD;
            default:
                return Difficulty.EASY;
        }
    }
    /**
     * Displays round header.
     */
    public void displayRoundHeader(int round) {
        // Display empty line
        // Display formatted round header with round number
        System.out.println();
        System.out.println("Round " + round);
        System.out.println();
    }
    
    /**
     * Displays an action with proper formatting.
     */
    public void displayAction(String actor, String actionName, String target, int hpBefore, int hpAfter, int attackerATK, int targetDEF, int damage) {
        // Format damage calculation string as "attackerATK-targetDEF=damage"
        // Check if target HP after is less than or equal to 0
            // If eliminated, display formatted message with "X ELIMINATED" and damage calculation
        // Otherwise, display formatted message with HP before and after, and damage calculation

        String damage_calc = attackerATK + "-" + targetDEF + "=" + damage;
        if ( hpAfter <= 0) {
            System.out.println(actor + " - " + actionName + " - " + target + ": " + damage_calc + " X Eliminated");
        }
        else {
            System.out.println(actor + " - " + actionName + " - " + target + ": " + damage_calc + " | " + target + " HP: " + hpBefore + " vs " + hpAfter);
        }
        System.out.println();
        System.out.println();
    }
    
    /**
     * Displays cooldown message after using special skill.
     */
    public void displayCooldownSet(int cooldownRounds) {
        // Display formatted message showing cooldown set to the given number of rounds

        System.out.println("Cooldown set :" + cooldownRounds + "rounds");
    }
    
    /**
     * Displays cooldown unchanged message for Power Stone.
     */
    public void displayCooldownUnchanged(int cooldownRounds) {
        // Display formatted message showing cooldown unchanged with explanation about Power Stone

        System.out.println("Cooldown unchanged (Power Stone used): " + cooldownRounds + "rounds left");
    }
    
    /**
     * Displays that Basic Attack damage was nullified by Smoke Bomb.
     */
    public void displayNullifiedBasicAttack(String actor, String target, String targetDisplay, int targetHP) {
        // Display formatted message like:
        // "Actor -> BasicAttack -> Target: 0 damage (Smoke Bomb active) | TargetDisplay HP: targetHP"
        System.out.println(actor + "BasicAttack -> " + target
                + ": 0 damage (Smoke Bomb active) | " + targetDisplay + " HP: " + targetHP);
    }
    
    /**
     * Displays status effect application.
     */
    public void displayStatusEffect(String target, String effect, int duration) {
        // Display formatted message showing target is STUNNED with duration in turns
        System.out.println(target + " is STUNNED for " + duration + " turns");
    }
    
    /**
     * Displays that stun has expired.
     */
    public void displayStunExpires() {
        // Display formatted "Stun expires" message (used by GameManager, not printed directly there)
        System.out.println("Stun expires");
    }
    
    /**
     * Displays that Smoke Bomb effect has expired.
     */
    public void displaySmokeBombExpires() {
        // Display formatted "Smoke Bomb effect expires" message (used by GameManager, not printed directly there)
        System.out.println("Smoke Bomb effect expires");
    }
    
    /**
     * Displays a skipped turn message.
     */
    public void displaySkippedTurn(String combatant, String reason) {
        // Check if reason equals "STUNNED"
            // If so, display formatted message showing combatant's turn was skipped due to stun
        // Otherwise, check if reason equals "ELIMINATED"
            // If so, display formatted message showing combatant's turn was skipped due to elimination

        if (reason.equals("STUNNED")) {
            System.out.println(combatant + "'s turn skipped (Stunned)");
        } else if (reason.equals("ELIMINATED")) {
            System.out.println(combatant + "'s turn skipped (Eliminated)");
        }
    }
    
    /**
     * Displays item usage.
     */
    public void displayItemUsage(String actor, String itemName, String effect) {
        // Display formatted message showing actor used item with effect description
        System.out.println(actor + ": Item : " + itemName + ": " + effect);
    }
    
    /**
     * Displays end of round summary.
     */
    public void displayEndOfRound(int round, List<Combatant> combatants, Map<Combatant, String> enemyLabels,
                                  int potionCount, int smokeBombCount, int powerStoneCount, int cooldownRounds, boolean hasItems, Combatant player) {
        // Display "End of Round X: " header
        
        // Create a list to store status parts for joining
        
        // Iterate through combatants to find players (Warrior or Wizard instances)
            // For each player, format status string with name and HP (alive: "Name HP: X/Y", eliminated: "Name HP: X")
            // Add status string to status parts list
        
        // Create a list for enemy combatants
        // Iterate through combatants to find enemies (Goblin or Wolf instances)
            // Add each enemy to the enemy list
        // Sort enemies by their labels (get label from map, compare alphabetically)
        
        // Iterate through sorted enemies
            // Get enemy type name and label from map
            // Create full label string (enemy type + label, or just enemy type if no label)
            // Format status string based on whether enemy is alive
                // If eliminated: "FullLabel HP: X"
                // If alive: "FullLabel HP: X" and append "[STUNNED]" if has Stun effect
            // Add status string to status parts list
        
        // Handle inventory display based on player type
        // If player is Warrior:
            // Format potion text (show "consumed" if count is 0, otherwise show count)
            // Format smoke bomb text (show "consumed" if count is 0, otherwise show count)
            // Add both to status parts
            // If no items available and both counts are 0, add "Item action no longer available"
        // Else if player is Wizard:
            // Format power stone text (show "consumed" if count is 0, otherwise show count)
            // Format potion text (show "consumed" if count is 0, otherwise show count)
            // Add both to status parts
            // If no items available and both counts are 0, add "Item action no longer available"
            // If player attack is greater than 50, add formatted ATK display
        
        // Format cooldown text (0 -> "0 Round", 1 -> "1 round", else -> "X rounds")
        // Add formatted cooldown string to status parts
        
        // Join all status parts with " | " separator and display

        List<String> parts = new ArrayList<>();

        // iterating through players
        for (Combatant c : combatants) {
            if (c instanceof Warrior || c instanceof Wizard) {
                String status;

                if (c.isAlive()) {
                    status = c.getName() + " HP: " + c.getCurrentHP() + "/" + c.getMaxHP();
                }

                else {
                    status = c.getName() + " HP: " + c.getCurrentHP();
                }
                parts.add(status);
            }
        }

        // creation of enemy list
        List<Combatant> enemyList = new ArrayList<>();

        for (Combatant c : combatants) {
            if (c instanceof Goblin || c instanceof Wolf) {
                enemyList.add(c);
            }

        }
        // sorting of enermy list
        enemyList.sort((a, b) -> {
            String aa = enemyLabels.getOrDefault(a, "");
            String bb = enemyLabels.getOrDefault(b, "");
            return aa.compareTo(bb);
        });

        for (Combatant e : enemyList) {
            String type = e.getClass().getSimpleName();
            String label = enemyLabels.getOrDefault(e, "");
            String fullLabel = label.isEmpty() ? type : type + label;

            String status;
            if (!e.isAlive()) {
                status = fullLabel + " HP: " + e.getCurrentHP();
            }

            else {
                status = fullLabel + " HP: " + e.getCurrentHP() + "/" + e.getMaxHP();

                if (e.hasStatusEffect("Stun") || e.hasStatusEffect("StunEffect")) {
                    status = "[STUNNED]";
                }
            }
            parts.add(status);
        }

        if (player instanceof Warrior) {

            String potionText  = (potionCount  == 0) ? "Potion: consumed" : "Potion: "   + potionCount;
            String smokeText   = (smokeBombCount == 0) ? "Smoke Bomb: consumed" : "Smoke Bomb: " + smokeBombCount;

            parts.add(potionText);

            parts.add(smokeText);

            if (!hasItems && potionCount == 0 && smokeBombCount == 0) {
                parts.add("Item action no longer available");
            }
        }

        else if (player instanceof Wizard) {

            String stoneText  = (powerStoneCount == 0) ? "Power Stone: consumed" : "Power Stone: " + powerStoneCount;
            String potionText = (potionCount    == 0) ? "Potion: consumed"      : "Potion: "      + potionCount;


            parts.add(stoneText);
            parts.add(potionText);


            if (!hasItems && powerStoneCount == 0 && potionCount == 0) {
                parts.add("Item action no longer available");
            }
            if (player.getAttack() > 50) {
                parts.add("ATK: " + player.getAttack());
            }
        }

        String cooldownText;

        if (cooldownRounds == 0) {
            cooldownText = "Cooldown: 0 Round";
        }

        else if (cooldownRounds == 1) {
            cooldownText = "Cooldown: 1 round";
        }

        else {
            cooldownText = "Cooldown: " + cooldownRounds + " rounds";
        }

        parts.add(cooldownText);

        System.out.println();
        System.out.println("End of Round " + round + ": " + String.join(" | ", parts));
        System.out.println();






        }
    
    /**
     * Displays Arcane Blast summary across all enemies including kill-based ATK progression and cooldown.
     */
    public void displayArcaneBlastSummary(String actor, String actionName, List<String> enemyResults,
                                          int initialATK, int kills, boolean goblinSurvives, Integer cooldownRounds) {
        // Display "Actor -> Arcane Blast -> All Enemies: " header
        // Join and display enemyResults
        // If kills > 0, display ATK progression (+10 per kill)
        // If goblinSurvives is true, append "Goblin survives"
        // If cooldownRounds is not null, display cooldown set message
        // Otherwise, print newline only

        System.out.print(actor + " -> Arcane Blast -> All Enemies: ");
        System.out.print(String.join(", ", enemyResults));


        if (kills > 0) {
            int finalATK = initialATK + (kills * 10);
            System.out.print("ATK: " + initialATK + "+" + (kills * 10) + "=" + finalATK);
        }


        if (goblinSurvives) {
            System.out.print("Goblin survives");
        }

        if (cooldownRounds != null) {
            System.out.println();
            displayCooldownSet(cooldownRounds);
        }
        else {
            System.out.println();
        }



    }
    
    /**
     * Displays Power Stone trigger header.
     */
    public void displayPowerStoneTriggered(String actor, String skillName) {
        // Display formatted "Actor -> Item -> Power Stone used -> SkillName triggered" message
        System.out.println(actor + " -> Item -> Power Stone used -> " + skillName + " triggered");
    }
    
    /**
     * Displays Arcane Blast result when triggered via Power Stone.
     */
    public void displayArcaneBlastPowerStone(String actor, int attackerATK, List<String> enemyResults, int kills, boolean allDefeated) {
        // Display "-> All Enemies (ATK: X): " prefix
        // Join and display enemy results
        // If kills > 0, display ATK progression (+10 per kill)
        // If allDefeated is true, append "All enemies defeated"

        System.out.print("-> All Enemies (ATK: " + attackerATK + "): ");
        System.out.print(String.join(", ", enemyResults));
        if (kills > 0) {
            int finalATK = attackerATK + (kills * 10);
            System.out.print("ATK: " + attackerATK + "+" + (kills * 10) + "=" + finalATK);
        }
        if (allDefeated) {
            System.out.print("All enemies defeated");
        }
        System.out.println();


    }
    
    /**
     * Displays Power Stone consumed with unchanged cooldown.
     */
    public void displayPowerStoneConsumedAndCooldownUnchanged(int cooldownRounds) {
        // Display "Power Stone consumed" text
        // Then display cooldown unchanged message through existing cooldown format
        System.out.print("Power Stone consumed ");
        displayCooldownUnchanged(cooldownRounds);
    }
    
    /**
     * Displays a newline.
     */
    public void displayNewline() {
        // Print a newline (used to keep formatting logic inside UI layer)
        System.out.println();
    }
    
    /**
     * Displays available actions and prompts user to select one.
     */
    public int selectAction(boolean canUseSkill, boolean hasItems) {
        // Display "Select Action:" header
        // Display "1. Basic Attack"
        // Display "2. Defend"
        // If canUseSkill is true, display "3. Special Skill"
        // If hasItems is true, display "4. Use Item"
        
        // Calculate max option (4 if hasItems, 3 if canUseSkill, 2 otherwise)
        // Prompt user for choice in range 1 to maxOption
        // Read and return integer input using readInt method

        System.out.println("Select Action:");
        System.out.println();
        System.out.println("1. Basic Attack");
        System.out.println("2. Defend");


        if (canUseSkill) {
            System.out.println("3. Special Skill");
        }

        if (hasItems) {
            System.out.println("4. Use Item");
        }

        System.out.println();

        int maxOption = hasItems ? 4 : (canUseSkill ? 3 : 2);
        return readInt(1, maxOption);

    }
    
    /**
     * Prompts user to select a target from available enemies.
     */
    public Combatant selectEnemyTarget(List<Combatant> enemies) {
        // Display "Select Target:" header
        // Create a list for alive enemies
        // Iterate through enemies list
            // If enemy is alive, add to alive enemies list
        
        // Iterate through alive enemies with index
            // Get enemy label and type name
            // Display formatted option with index, enemy type, label, and HP (current/max)
        
        // Prompt user for choice
        // Read integer input in range 1 to alive enemies size
        // Return the enemy at (choice - 1) index from alive enemies list

        System.out.println("Select Target:");
        System.out.println();
        List<Combatant> aliveEnemies = new ArrayList<>();

        for (Combatant e : enemies) {
            if (e.isAlive()) {
                aliveEnemies.add(e);
            }

        }

        for (int i = 0; i < aliveEnemies.size(); i++) {
            Combatant e = aliveEnemies.get(i);
            String label = enemyLabels.getOrDefault(e, "");
            String type  = e.getClass().getSimpleName();

            System.out.printf(" %d. %s%s  HP: %d/%d%n", i + 1, type, label, e.getCurrentHP(), e.getMaxHP());
        }

        System.out.println();

        int choice = readInt(1, aliveEnemies.size());
        System.out.println();
        return aliveEnemies.get(choice - 1);


    }
    
    /**
     * Prompts user to select an item.
     */
    public int selectItem(List<Item> availableItems) {
        // Display "Select Item:" header
        // Iterate through available items with index
            // Display formatted option with index and item name
        
        // Prompt user for choice
        // Read integer input in range 1 to available items size
        // Return (choice - 1) to convert to 0-based index

        System.out.println("Select Item:");

        for (int i = 0; i < availableItems.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + availableItems.get(i).getName());
        }

        int choice = readInt(1, availableItems.size());
        return choice - 1;

    }
    
    /**
     * Displays the game completion screen.
     */
    public void displayCompletionScreen(GameState state, int roundsSurvived, int remainingHP, int maxHP, 
                                        int potionCount, int smokeBombCount, int powerStoneCount, Combatant player) {
        // Display empty line
        // Display "**Victory**" header
        
        // If player is Warrior:
            // Format potion text (show "unused" if count > 0, otherwise "0")
            // Format smoke bomb text (show "unused" if count > 0, otherwise "0")
            // Display formatted victory message with HP, rounds, and remaining items
        // Else if player is Wizard:
            // Format power stone text (show "unused" if count > 0, otherwise "0")
            // Format potion text (show "unused" if count > 0, otherwise "0")
            // Format ATK text (show final ATK if greater than 50, otherwise empty string)
            // Display formatted victory message with HP, rounds, remaining items, and ATK if applicable

        System.out.println();
        System.out.println("**Victory**");


        if (player instanceof Warrior) {
            String potionText = (potionCount > 0) ? potionCount + " unused" : "0";
            String smokeBombText = (smokeBombCount > 0) ? smokeBombCount + " unused" : "0";
            System.out.println("HP: " + remainingHP + "/" + maxHP + " : Rounds: " + roundsSurvived + " : Potion: " + potionText + " : Smoke Bomb: " + smokeBombText);

        }

        else if (player instanceof Wizard) {
            String stoneText = (powerStoneCount > 0) ? powerStoneCount + " unused" : "0";

            String potionText = (potionCount     > 0) ? potionCount     + " unused" : "0";

            String atkText = (player.getAttack() > 50) ? "Final ATK: " + player.getAttack() : "";

            System.out.println("HP: " + remainingHP + "/" + maxHP + ": Rounds: " + roundsSurvived + ": Power Stone: " + stoneText + ": Potion: " + potionText + atkText);
        }

    }
    
    /**
     * Prompts user to replay or exit.
     */
    public boolean promptReplay() {
        // Prompt user "Play again? (y/n): "
        // Read input line, trim whitespace, and convert to lowercase
        // Return true if input equals "y" or "yes", false otherwise

        System.out.print("Play again? (y/n): ");
        String input = scanner.nextLine().trim().toLowerCase();
        return input.equals("y") || input.equals("yes");


    }
    
    /**
     * Displays a message.
     */
    public void displayMessage(String message) {
        // Display the message string
        System.out.println(message);
    }
    
    /**
     * Reads an integer from user input within a valid range.
     */
    private int readInt(int min, int max) {
        // Loop indefinitely until valid input is received
            // Try to read input:
                // Read next line from scanner, trim whitespace
                // Parse input as integer
                // Check if value is within min and max range
                    // If valid, return the value
                    // Otherwise, display error message prompting for valid range
            // Catch NumberFormatException:
                // Display error message prompting for valid range

        while (true) {

            try {
                String input = scanner.nextLine().trim();
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("Please enter a number between " + min + " and " + max);

            }

            catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number between " + min + " and " + max + ".");
            }

        }

    }

    
    /**
     * Closes the scanner.
     */
    public void close() {
        // Close the scanner
        scanner.close();
    }
}

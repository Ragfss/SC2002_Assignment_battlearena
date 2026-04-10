import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages difficulty levels and enemy spawning configurations.
 * Maps difficulty levels to enemy spawn configurations.
 */
public class LevelManager {
    private Map<Difficulty, EnemySpawnConfig> difficultyConfigs;

    public LevelManager() {
        this.difficultyConfigs = new HashMap<>();
        initializeConfigurations();
    }

    /**
     * Initializes the difficulty configurations according to PRD:
     * - Easy: Initial: 3 Goblins, Backup: None
     * - Medium: Initial: 1 Goblin + 1 Wolf, Backup: 2 Wolves
     * - Hard: Initial: 2 Goblins, Backup: 1 Goblin + 2 Wolves
     */
    private void initializeConfigurations() {
        List<EnemySpawnConfig.EnemyType> easyInitial = new ArrayList<>();
        easyInitial.add(EnemySpawnConfig.EnemyType.GOBLIN);
        easyInitial.add(EnemySpawnConfig.EnemyType.GOBLIN);
        easyInitial.add(EnemySpawnConfig.EnemyType.GOBLIN);
        difficultyConfigs.put(Difficulty.EASY, new EnemySpawnConfig(easyInitial, new ArrayList<>()));

        List<EnemySpawnConfig.EnemyType> mediumInitial = new ArrayList<>();
        mediumInitial.add(EnemySpawnConfig.EnemyType.GOBLIN);
        mediumInitial.add(EnemySpawnConfig.EnemyType.WOLF);
        List<EnemySpawnConfig.EnemyType> mediumBackup = new ArrayList<>();
        mediumBackup.add(EnemySpawnConfig.EnemyType.WOLF);
        mediumBackup.add(EnemySpawnConfig.EnemyType.WOLF);
        difficultyConfigs.put(Difficulty.MEDIUM, new EnemySpawnConfig(mediumInitial, mediumBackup));

        List<EnemySpawnConfig.EnemyType> hardInitial = new ArrayList<>();
        hardInitial.add(EnemySpawnConfig.EnemyType.GOBLIN);
        hardInitial.add(EnemySpawnConfig.EnemyType.GOBLIN);
        List<EnemySpawnConfig.EnemyType> hardBackup = new ArrayList<>();
        hardBackup.add(EnemySpawnConfig.EnemyType.GOBLIN);
        hardBackup.add(EnemySpawnConfig.EnemyType.WOLF);
        hardBackup.add(EnemySpawnConfig.EnemyType.WOLF);
        difficultyConfigs.put(Difficulty.HARD, new EnemySpawnConfig(hardInitial, hardBackup));
    }

    /**
     * Gets the spawn configuration for a difficulty level.
     *
     * @param difficulty The difficulty level
     * @return The enemy spawn configuration
     */
    public EnemySpawnConfig getSpawnConfig(Difficulty difficulty) {
        return difficultyConfigs.get(difficulty);
    }

    /**
     * Creates enemy combatants from a list of enemy types.
     *
     * @param enemyTypes List of enemy types to create
     * @return List of created enemy combatants
     */
    public List<Combatant> createEnemies(List<EnemySpawnConfig.EnemyType> enemyTypes) {
        List<Combatant> enemies = new ArrayList<>();
        for (EnemySpawnConfig.EnemyType type : enemyTypes) {
            switch (type) {
                case GOBLIN:
                    enemies.add(new Goblin());
                    break;
                case WOLF:
                    enemies.add(new Wolf());
                    break;
            }
        }
        return enemies;
    }

    /**
     * Gets the initial wave of enemies for a difficulty level.
     *
     * @param difficulty The difficulty level
     * @return List of initial enemy combatants
     */
    public List<Combatant> getInitialWave(Difficulty difficulty) {
        EnemySpawnConfig config = getSpawnConfig(difficulty);
        if (config == null) {
            return new ArrayList<>();
        }
        return createEnemies(config.getInitialWave());
    }

    /**
     * Gets the backup wave of enemies for a difficulty level.
     *
     * @param difficulty The difficulty level
     * @return List of backup enemy combatants
     */
    public List<Combatant> getBackupWave(Difficulty difficulty) {
        EnemySpawnConfig config = getSpawnConfig(difficulty);
        if (config == null) {
            return new ArrayList<>();
        }
        return createEnemies(config.getBackupWave());
    }
}

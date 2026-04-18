import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// maps difficulty levels to enemy wave configurations
// the only class that knows which enemies spawn where
public class LevelManager {
    private Map<Difficulty, EnemySpawnConfig> difficultyConfigs;

    public LevelManager() {
        this.difficultyConfigs = new HashMap<>();
        initializeConfigurations();
    }

    // per spec:
    // Easy   — 3 Goblins, no backup
    // Medium — 1 Goblin + 1 Wolf, backup: 2 Wolves
    // Hard   — 2 Goblins, backup: 1 Goblin + 2 Wolves
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

    public EnemySpawnConfig getSpawnConfig(Difficulty difficulty) {
        return difficultyConfigs.get(difficulty);
    }

    public List<Combatant> createEnemies(List<EnemySpawnConfig.EnemyType> enemyTypes) {
        List<Combatant> enemies = new ArrayList<>();
        for (EnemySpawnConfig.EnemyType type : enemyTypes) {
            switch (type) {
                case GOBLIN: enemies.add(new Goblin()); break;
                case WOLF:   enemies.add(new Wolf());   break;
            }
        }
        return enemies;
    }

    public List<Combatant> getInitialWave(Difficulty difficulty) {
        EnemySpawnConfig config = getSpawnConfig(difficulty);
        return (config == null) ? new ArrayList<>() : createEnemies(config.getInitialWave());
    }

    public List<Combatant> getBackupWave(Difficulty difficulty) {
        EnemySpawnConfig config = getSpawnConfig(difficulty);
        return (config == null) ? new ArrayList<>() : createEnemies(config.getBackupWave());
    }
}

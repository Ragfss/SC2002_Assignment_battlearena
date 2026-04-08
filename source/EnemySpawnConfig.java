import java.util.ArrayList;
import java.util.List;

// Holds enemy wave setup for a given difficulty
// Each difficult has an initial wave and an optional backup wave
public class EnemySpawnConfig {
    private List<EnemyType> initialWave;
    private List<EnemyType> backupWave;
    
    public EnemySpawnConfig(List<EnemyType> initialWave, List<EnemyType> backupWave) {
        this.initialWave = new ArrayList<>(initialWave);
        this.backupWave = new ArrayList<>(backupWave);
    }
    
    public List<EnemyType> getInitialWave() {
        return new ArrayList<>(initialWave);
    }
    
    public List<EnemyType> getBackupWave() {
        return new ArrayList<>(backupWave);
    }
    
    public enum EnemyType {
        GOBLIN,
        WOLF
    }
}

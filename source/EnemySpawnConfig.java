import java.util.ArrayList;
import java.util.List;

// holds which enemies spawn for a given difficulty, split into initial and backup waves
public class EnemySpawnConfig {
    private List<EnemyType> initialWave;
    private List<EnemyType> backupWave;

    public EnemySpawnConfig(List<EnemyType> initialWave, List<EnemyType> backupWave) {
        this.initialWave = new ArrayList<>(initialWave);
        this.backupWave  = new ArrayList<>(backupWave);
    }

    public List<EnemyType> getInitialWave() { return new ArrayList<>(initialWave); }
    public List<EnemyType> getBackupWave()  { return new ArrayList<>(backupWave); }

    public enum EnemyType {
        GOBLIN,
        WOLF
    }
}

package north.autonomous;

import edu.wpi.first.wpilibj.Timer;

public class CommandState {
    public double startTime;
    public double elapsedTime;
    public boolean init = true;

    public CommandState() {
        startTime = Timer.getFPGATimestamp();
    }

    public void update() {
        elapsedTime = Timer.getFPGATimestamp() - startTime;
    }
}

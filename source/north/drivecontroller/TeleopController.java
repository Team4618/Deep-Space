package north.drivecontroller;

import java.util.function.Supplier;

import north.North;
import north.util.DriveControls;

public class TeleopController implements IDriveController {
   Supplier<DriveControls> drive_func;

   public TeleopController(Supplier<DriveControls> drive_func) {
      this.drive_func = drive_func;
   }

   @Override
   public void init() {}

   @Override
   public void periodic() {
      DriveControls values = drive_func.get();
      North.drive.setMotorPercents(values.left, values.right); 
   }
}
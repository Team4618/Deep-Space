package north.drivecontroller;

import north.North;
import north.IDriveAndNavigation.EncoderData;

public class HoldController implements IDriveController {
   public static HoldController I = new HoldController();

   @Override
   public void init() {
      North.drive.zeroEncoders();
   }

   @Override
   public void periodic() {
      EncoderData encoders = North.drive.getEncoders();
      
      //TODO: run p controller to hold at 0
      float left = 0;
      float right = 0;
      
      North.drive.setMotorPercents(left, right);
   }
}
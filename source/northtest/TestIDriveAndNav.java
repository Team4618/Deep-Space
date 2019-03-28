package northtest;

import north.IDriveAndNavigation;
import north.RobotState;

public class TestIDriveAndNav implements IDriveAndNavigation {
   public static TestIDriveAndNav INSTANCE = new TestIDriveAndNav();

   @Override
   public void setState(RobotState state) {

   }

   @Override
   public RobotState getState(double dt) {
      return new RobotState(0, 0, 0, 0, 0, 0);
   }

   @Override
   public void zeroEncoders() {

   }

   @Override
   public EncoderData getEncoders() {
      return new EncoderData();
   }

   @Override
   public void setMotorPercents(double left, double right) {

	}

}
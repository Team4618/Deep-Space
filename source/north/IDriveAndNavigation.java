package north;

public interface IDriveAndNavigation {
   void setState(RobotState state);
   RobotState getState(double dt);

   public static class EncoderData {
      public double left_p;
      public double left_v;
      public double right_p;
      public double right_v;
   }

   void zeroEncoders();
   EncoderData getEncoders();
   void setMotorPercents(double left, double right);
}

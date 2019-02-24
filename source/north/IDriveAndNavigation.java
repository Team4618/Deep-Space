package north;

import north.curves.*;

public interface IDriveAndNavigation {
   void setState(RobotState state);
   RobotState getState(double dt);

   //when going into automatic control it zeros the drive encoders & puts it in PID control (equivalent to "hold")
   //when going into manual/power control it sets the motors to 0% output
   void setAutomaticControl(boolean set_automatic);
   void zeroEncoders();

   void setDriveSetpoints(double left, double right);

   //returns amount of degrees turned
   default PathProgress followPivot(PivotPlan pivot) {
      setDriveSetpoints(pivot.getLeftSetpoint(), pivot.getRightSetpoint());
      return pivot.getProgress();
   }

   //returns distance driven along the path
   default PathProgress followPath(PathPlan path) {
      setDriveSetpoints(path.getLeftSetpoint(), path.getRightSetpoint());
      return path.getProgress();
   }

   //NOTE: IDrive stuff
   /*----------------------------------------
   void zeroEncoders();
   void setMotorPercents(double left, double right);
   void updateEncoderData();
   PID stuff??
   ----------------------------------------*/
}

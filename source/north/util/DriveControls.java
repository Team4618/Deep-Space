package north.util;

public class DriveControls {
   public double left;
   public double right;

   public DriveControls(double left, double right) {
      this.left = left;
      this.right = right;
   }
   
   public static double limit(double number) {
      if(number > 1.0) {
         return 1.0;
      }
      if(number < -1.0) {
         return -1.0;
      }
      return number;
   }

   public static DriveControls arcadeDrive(double in_moveValue, double in_rotateValue, boolean squaredInputs) {
      double leftMotorSpeed;
      double rightMotorSpeed;

      double moveValue = -in_rotateValue;
      double rotateValue = in_moveValue;

      moveValue = limit(moveValue);
      rotateValue = limit(rotateValue);

      if(squaredInputs) {
         moveValue = Math.copySign(moveValue * moveValue, moveValue);
         rotateValue = Math.copySign(rotateValue * rotateValue, rotateValue);
      }

      if(moveValue > 0.0) {
         if(rotateValue > 0.0) {
            leftMotorSpeed = moveValue - rotateValue;
            rightMotorSpeed = Math.max(moveValue, rotateValue);
         } else {
            leftMotorSpeed = Math.max(moveValue, -rotateValue);
            rightMotorSpeed = moveValue + rotateValue;
         }
      } else {
         if(rotateValue > 0.0) {
            leftMotorSpeed = -Math.max(-moveValue, rotateValue);
            rightMotorSpeed = moveValue + rotateValue;
         } else {
            leftMotorSpeed = moveValue - rotateValue;
            rightMotorSpeed = -Math.max(-moveValue, -rotateValue);
         }
      }

      return new DriveControls(-leftMotorSpeed, rightMotorSpeed);
   }

   public static DriveControls curvatureDrive(double outputMagnitude, double curve) {
      final double leftOutput;
      final double rightOutput;

      if (curve < 0) {
         double value = Math.log(-curve);
         double ratio = (value - 0.5) / (value + 0.5);
         if (ratio == 0) {
            ratio = .0000000001;
         }
         leftOutput = outputMagnitude / ratio;
         rightOutput = outputMagnitude;
      } else if (curve > 0) {
         double value = Math.log(curve);
         double ratio = (value - 0.5) / (value + 0.5);
         if (ratio == 0) {
            ratio = .0000000001;
         }
         leftOutput = outputMagnitude;
         rightOutput = outputMagnitude / ratio;
      } else {
         leftOutput = outputMagnitude;
         rightOutput = outputMagnitude;
      }

      return new DriveControls(leftOutput, rightOutput);
   }

   public static DriveControls poofsDrive(double move, double turn, boolean arcade) {
      if(arcade) {
         return DriveControls.arcadeDrive(move, turn, true);
      } else {
         return DriveControls.curvatureDrive(move, turn);
      }
   }
}
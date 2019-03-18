package team4618.robot;

import edu.wpi.first.wpilibj.RobotBase;

public final class Main {
   private Main() { }

   public static void main(String... args) {
      RobotBase.startRobot(Robot::new);
   }

   //TODO: write a wrapper here so we can make the
   //      robot class & all the subsystem stuff static
   //      using kotlin singletons
}

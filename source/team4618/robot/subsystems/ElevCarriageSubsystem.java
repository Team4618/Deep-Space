package team4618.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;

import static team4618.robot.IDs.*;

public class ElevCarriageSubsystem {
   public WPI_VictorSPX ball_conveyor = new WPI_VictorSPX(BALL_CONVEYOR);
   public DoubleSolenoid disc_holder = new DoubleSolenoid(DISC_HOLDER_EXTEND, DISC_HOLDER_RETRACT);
   public DoubleSolenoid disc_arm = new DoubleSolenoid(DISC_ARM_EXTEND, DISC_ARM_RETRACT);
   DigitalInput ball_sensor = new DigitalInput(BALL_CONVEYOR_SENSOR);

   public boolean hasBall() {
      return !ball_sensor.get();
   }

   public void startConveyorForHandOff() {
      ball_conveyor.set(-0.5);
   }

   public void startConveyorForFrontShoot() {
      ball_conveyor.set(-1);
   }

   public void startConveyorForBackShoot() {
      ball_conveyor.set(1);
   }

   public void stopConveyor() {
      ball_conveyor.set(0);
   }
}
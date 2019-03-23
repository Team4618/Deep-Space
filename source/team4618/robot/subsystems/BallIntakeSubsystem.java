package team4618.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import north.parameters.*;

import static team4618.robot.IDs.*;

public class BallIntakeSubsystem {
   public final String NAME = "Ball Intake";
   public DoubleSolenoid arm = new DoubleSolenoid(BALL_INTAKE_EXTEND, BALL_INTAKE_RETRACT);
   public WPI_VictorSPX roller = new WPI_VictorSPX(BALL_INTAKE);
   DigitalInput ball_sensor = new DigitalInput(BALL_INTAKE_SENSOR);

   Parameter intake_speed = new Parameter(NAME, "Intake Speed");

   public boolean hasBall() {
      return !ball_sensor.get();
   }

   public void startRollerForIntake() {
      roller.set(-0.75 /*intake_speed.get()*/);
   }

   public void startRollerForBackShoot() {
      roller.set(1);
   }

   public void stopRoller() {
      roller.set(0);
   }

   public void goDown() {
      arm.set(Value.kForward);
   }

   public void goUp() {
      arm.set(Value.kReverse);
   }
}
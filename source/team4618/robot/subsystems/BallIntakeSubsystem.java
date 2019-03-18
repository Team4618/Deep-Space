package team4618.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import north.Subsystem;
import north.reflection.Parameter;
import team4618.robot.Robot;

import static team4618.robot.IDs.*;

public class BallIntakeSubsystem extends Subsystem {
   public DoubleSolenoid arm = new DoubleSolenoid(BALL_INTAKE_EXTEND, BALL_INTAKE_RETRACT);
   public WPI_VictorSPX roller = new WPI_VictorSPX(BALL_INTAKE);
   DigitalInput ball_sensor = new DigitalInput(BALL_INTAKE_SENSOR);

   Parameter intake_speed;
   Parameter arm_change_time;

   //???
   // Parameter intake_speed = new Parameter(this::onchanged)

   public boolean hasBall() {
      return !ball_sensor.get();
   }

   public void startRollerForIntake() {
      roller.set(-0.75);
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

   @Override
   public void init() { }

   @Override
   public String name() { return "Ball Intake"; }
}
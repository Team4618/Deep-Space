package team4618.robot;

import team4618.robot.subsystems.DriveSubsystem;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import north.North;
import north.Subsystem;
import north.autonomous.PivotExecutable;
import north.reflection.Logic;
import north.util.Button;
import north.util.DriveControls;
import north.util.NorthUtils;
import north.util.ToggleButton;

import static team4618.robot.IDs.*;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import static north.util.Button.IDs.*;

public class Robot extends TimedRobot {
   public static Joystick driver = new Joystick(0);
   public static Joystick op = new Joystick(1);

   public static DriveSubsystem drive = new DriveSubsystem();
   
   DoubleSolenoid ball_intake_arms = new DoubleSolenoid(BALL_INTAKE_EXTEND, BALL_INTAKE_RETRACT);
   WPI_VictorSPX ball_intake_roller = new WPI_VictorSPX(BALL_INTAKE);

   public void robotInit() {
      //NOTE: init(name, size, logic provider, drive & nav)
      North.init(/*NorthUtils.readText("name.txt")*/ "testbot", 24/12, 24/12, getClass(), drive);
   }

   public void robotPeriodic() {
      North.tick();

      if(op.getRawButton(8)) {
         drive.navx.zeroYaw();
      }
   }

   public void autonomousInit() {
      if(North.auto_starting_node != null)
         North.auto_starting_node.reset();

      North.execute(North.auto_starting_node);
      North.subsystems.values().forEach(Subsystem::reset);
   }

   public void autonomousPeriodic() {
      North.tickExecution();
      North.tickSubsystems();
   }

   Button stopAuto = new Button(op, LOGI_STICK_TRIGGER);
   Button pivotButton = new Button(driver, LOGI_PAD_RB);
   ToggleButton intakeDown = new ToggleButton(driver, LOGI_PAD_X, false);

   public void teleopInit() { }

   public void teleopPeriodic() {
      if(North.executionDone()) {
         drive.setAutomaticControl(false);

         teleopControl();
      } else {
         if(stopAuto.released) {
            North.stopExecution();
         }

         North.tickExecution();
      }

      North.tickSubsystems();
   }

   public void teleopControl() {
      drive.teleop(DriveControls.poofsDrive(-driver.getRawAxis(1), driver.getRawAxis(4), pivotButton.isDown()));

      if(intakeDown.state) {
         ball_intake_arms.set(Value.kForward);
         ball_intake_roller.set(-1);
      } else {
         ball_intake_arms.set(Value.kReverse);
         ball_intake_roller.set(0);
      }
   }

//     public void testInit() {
//         teleopInit();
//         elevatorOverride.state = true;
//         intakeOverride.state = true;
//     }

//     public void testPeriodic() {
//         teleopPeriodic();
//     }

//     public void disabledInit() {
//         intakeSubsystem.wristPID.disable();
//     }

   // @Logic public boolean leftSwitchOurs() { return DriverStation.getInstance().getGameSpecificMessage().charAt(0) == 'L'; }
   // @Logic public boolean rightSwitchOurs() { return DriverStation.getInstance().getGameSpecificMessage().charAt(0) == 'R'; }
   // @Logic public boolean leftScaleOurs() { return DriverStation.getInstance().getGameSpecificMessage().charAt(1) == 'L'; }
   // @Logic public boolean rightScaleOurs() { return DriverStation.getInstance().getGameSpecificMessage().charAt(1) == 'R'; }
}

package team4618.robot;

import team4618.robot.subsystems.BallIntakeSubsystem;
import team4618.robot.subsystems.DriveSubsystem;
import team4618.robot.subsystems.ElevCarriageSubsystem;
import team4618.robot.subsystems.ElevatorSubsystem;
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

import static north.util.Button.IDs.*;

public class Robot extends TimedRobot {
   public static Joystick driver = new Joystick(0);
   public static Joystick op = new Joystick(1);

   public static DriveSubsystem drive = new DriveSubsystem();
   public static BallIntakeSubsystem ball_intake = new BallIntakeSubsystem();
   public static ElevatorSubsystem elevator = new ElevatorSubsystem();
   public static ElevCarriageSubsystem carriage = new ElevCarriageSubsystem();
   
   public void robotInit() {
      //NOTE: init(name, size, logic provider, drive & nav)
      North.init(/*NorthUtils.readText("name.txt")*/ "lawn chair", 24/12, 24/12, getClass(), drive);
   }

   // public static enum DesiredState {
   //    Idle,
   //    Intaking,
   //    BackShoot,
   //    FrontShoot,
   // }

   // boolean desired_state_set = false;
   // public DesiredState desired_state = DesiredState.Idle;
   // public void setState(DesiredState state) {
   //    desired_state_set = true;
   //    desired_state = state;
   // }

   public void robotPeriodic() {
      North.tick();
      
      // if(!desired_state_set &&
      //    ((desired_state == DesiredState.BackShoot) ||
      //     (desired_state == DesiredState.FrontShoot))) 
      // {
      //    desired_state = DesiredState.Idle;
      // }
      // desired_state_set = false;

      // if(op.getRawButton(8)) {
      //    drive.navx.zeroYaw();
      // }
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

   Button toggleBallIntaking = new Button(driver, LOGI_PAD_X);
   Button ballBackShoot = new Button(driver, LOGI_PAD_Y);

   Button ballConveyor = new Button(driver, LOGI_PAD_B);
   // DriveController teleopController = new TeleopController();

   ToggleButton discHolder = new ToggleButton(driver, LOGI_PAD_A, false);
   ToggleButton discArm = new ToggleButton(driver, LOGI_PAD_LB, false);

   public void teleopInit() { }

   public void teleopPeriodic() {
      if(North.executionDone()) {
         // North.setDriveController(teleopController);
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

   //---------------------------------------
   public void toggleIntaking() {
      if(ball_intake.desired_state != BallIntakeSubsystem.DesiredState.Intaking) {
         ball_intake.setState(BallIntakeSubsystem.DesiredState.Intaking);
         carriage.setState(ElevCarriageSubsystem.DesiredState.Intaking);
      } else {
         ball_intake.setState(BallIntakeSubsystem.DesiredState.Idle);
         carriage.setState(ElevCarriageSubsystem.DesiredState.Idle);
      }
   }

   public void backShoot() {
      ball_intake.setState(BallIntakeSubsystem.DesiredState.BackShoot);
      carriage.setState(ElevCarriageSubsystem.DesiredState.BackShoot);
   }
   //---------------------------------------

   public void teleopControl() {
      // teleopController.drive(DriveControls.poofsDrive(-driver.getRawAxis(1), driver.getRawAxis(4), pivotButton.isDown()));
      drive.teleop(DriveControls.poofsDrive(-driver.getRawAxis(1), driver.getRawAxis(4), pivotButton.isDown()));

      if(toggleBallIntaking.released) {
         toggleIntaking();
      }

      if(ballBackShoot.isDown()) {
         backShoot();
      }

      if(stopAuto.isDown()) {
         elevator.__periodic();
      } else {
         double elev_voltage = 12 * (0.85 * op.getRawAxis(1));
         double elev_percent = NorthUtils.getPercent(elev_voltage);      
         elevator.elev_talon.set(elev_percent);
         System.out.println(elev_voltage + "V : " + elev_percent + "%");
      }

      if(elevator.readyForHandOff()) {
         System.out.println("Ready for handoff");
      }

      carriage.disc_holder.set(discHolder.state);
      carriage.disc_arm.set(discArm.state ? Value.kForward : Value.kReverse);
   }

//     public void testInit() {
//         teleopInit();
//         elevatorOverride.state = true;
//         intakeOverride.state = true;
//     }

//     public void testPeriodic() {
//         teleopPeriodic();
//     }
}

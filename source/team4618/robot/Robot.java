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
import north.NorthSequence;
import north.Subsystem;
import north.drivecontroller.HoldController;
import north.drivecontroller.TeleopController;
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
      North.init(/*NorthUtils.readText("name.txt")*/ "lawn chair", 24/12, 24/12, drive);
      North.default_drive_controller = HoldController.I;
   }

   Button recalibrate = new Button(op, LOGI_STICK_8);
   Button toggleElevatorManual = new Button(op, LOGI_STICK_9);

   public void robotPeriodic() {
      North.tick();
      
      if(isEnabled()) {
         North.tickExecution();
         North.tickSubsystems();
      }

      if(recalibrate.released) {
         drive.navx.zeroYaw();
         elevator.calibrated = false;
      }

      if(toggleElevatorManual.released) {
         elevator.manual = !elevator.manual;
      }
   }

   public void autonomousInit() {
      North.default_drive_controller = HoldController.I;
      North.execute(North.auto_starting_node);

      North.subsystems.values().forEach(Subsystem::reset);
   }

   public void autonomousPeriodic() { }

   Button stopAuto = new Button(op, LOGI_STICK_TRIGGER);
   Button pivotButton = new Button(driver, LOGI_PAD_RB);

   TeleopController TELEOP = new TeleopController(() -> {
      return DriveControls.poofsDrive(-driver.getRawAxis(1), driver.getRawAxis(4), pivotButton.isDown());
   });

   Button toggleBallIntaking = new Button(driver, LOGI_PAD_X);
   Button ballFrontShoot = new Button(driver, LOGI_PAD_A);
   Button ballBackShoot = new Button(driver, LOGI_PAD_B);

   ToggleButton discHolder = new ToggleButton(driver, LOGI_PAD_Y, false);
   ToggleButton discArm = new ToggleButton(driver, LOGI_PAD_LB, false);

   public void teleopInit() { 
      North.default_drive_controller = TELEOP;
   }

   NorthSequence intake_sequence = NorthSequence.Begin()
                                                .Do(() -> elevator.setSetpoint(elevator.handoff_height) )
                                                .Wait(elevator::atSetpoint)
                                                .Do(carriage::startConveyorForHandOff)
                                                .Do(ball_intake::startRollerForIntake)
                                                .Do(ball_intake::goDown)
                                                .Wait(ball_intake::hasBall)
                                                .Do(ball_intake::goUp)
                                                .Wait(carriage::hasBall)
                                                .Do(carriage::stopConveyor)
                                                .Do(ball_intake::stopRoller)
                                                .End();

   public void teleopPeriodic() {
      if(toggleBallIntaking.released) {
         if(intake_sequence.isExecuting()) {
            North.stopExecution();
         } else {
            North.execute(intake_sequence);
         }
      }

      //NOTE: only control the ball intake & carriage if its not being automatically controlled
      if(North.executionDone()) {
         ball_intake.goUp();

         if(ballFrontShoot.isDown()) {
            ball_intake.stopRoller();
            carriage.startConveyorForFrontShoot();
         } else if(ballBackShoot.isDown()) {
            ball_intake.startRollerForBackShoot();
            carriage.startConveyorForBackShoot();
         } else {
            ball_intake.stopRoller();
            carriage.stopConveyor();;
         }
      }

      if(elevator.manual) {
         double elev_voltage = 12 * (0.85 * op.getRawAxis(1));
         double elev_percent = NorthUtils.getPercent(elev_voltage);      
         elevator.elev_talon.set(elev_percent);
         System.out.println(elev_voltage + "V : " + elev_percent + "%");
      }

      carriage.disc_holder.set(discHolder.state ? Value.kForward : Value.kReverse);
      carriage.disc_arm.set(discArm.state ? Value.kForward : Value.kReverse);

      if(stopAuto.released) {
         North.stopExecution();
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
}

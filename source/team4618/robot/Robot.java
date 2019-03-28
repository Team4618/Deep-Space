package team4618.robot;

import team4618.robot.subsystems.BallIntakeSubsystem;
import team4618.robot.subsystems.Climber;
import team4618.robot.subsystems.DriveSubsystem;
import team4618.robot.subsystems.ElevCarriageSubsystem;
import team4618.robot.subsystems.ElevatorSubsystem;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import north.North;
import north.NorthSequence;
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
   // public static Climber climber = new Climber();  
   
   public void robotInit() {
      // North.registerCommand("setSetpoint", (params) -> ??, ["Setpoint"]);
      // North.registerCondition("atSetpoint", elevator::atSetpoint);

      //NOTE: init(name, size, logic provider, drive & nav)
      North.init(/*NorthUtils.readText("name.txt")*/ "lawn chair", 24/12, 24/12, drive);
      North.default_drive_controller = HoldController.I;

      UsbCamera camera = CameraServer.getInstance().startAutomaticCapture();
      camera.setResolution(640, 480);
   }

   Button recalibrate = new Button(op, LOGI_STICK_8);
   Button toggleElevatorManual = new Button(op, LOGI_STICK_9);

   public void robotPeriodic() {
      North.tick();
      
      if(isEnabled()) {
         North.tickExecution();
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
      // North.execute(North.auto_starting_node);

      //TODO: REMOVE THIS IS TEMPORARY
      teleopInit();
   }

   public void autonomousPeriodic() {
      teleopPeriodic();
   }

   Button stopAuto = new Button(op, LOGI_STICK_TRIGGER);
   Button pivotButton = new Button(driver, LOGI_PAD_RB);

   TeleopController TELEOP = new TeleopController(() -> {
      double move = -driver.getRawAxis(1);
      double turn = driver.getRawAxis(4);
      
      if(pivotButton.isDown()) {
         return DriveControls.arcadeDrive(0.5 * move, 0.5 * turn, true);
      } else {
         return DriveControls.arcadeDrive(move, turn, true); //DriveControls.curvatureDrive(move, turn);
      }
   });

   Button toggleBallIntaking = new Button(driver, LOGI_PAD_X);
   Button ballFrontShoot = new Button(driver, LOGI_PAD_A);
   Button ballBackShoot = new Button(driver, LOGI_PAD_B);

   int setpoint = 0;
   Button upSetpoint = new Button(op, LOGI_STICK_6);
   Button downSetpoint = new Button(op, LOGI_STICK_7);

   ToggleButton discHolder = new ToggleButton(driver, LOGI_PAD_Y, false);
   ToggleButton discArm = new ToggleButton(driver, LOGI_PAD_LB, false);

   boolean was_executing = false;
   boolean just_finished_executing = false;

   public void teleopInit() { 
      North.default_drive_controller = TELEOP;
      was_executing = !North.executionDone();
   }

   NorthSequence intake_sequence = NorthSequence.Begin()
                                                .Do(() -> elevator.setSetpoint(elevator.handoff_height) )
                                                // .Wait(elevator::atSetpoint)
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
      just_finished_executing = (North.executionDone() && was_executing);
      was_executing = !North.executionDone();

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
            carriage.stopConveyor();
         }
      }

      if(just_finished_executing) {
         setpoint = elevator.getClosestSetpoint();
      }

      if(North.executionDone()) {
         if(upSetpoint.released && (setpoint < (elevator.ball_setpoints.length - 1) )) {
            setpoint++;
         }

         if(downSetpoint.released && (setpoint > 0)) {
            setpoint--;
         }

         //TODO: dont put this in an executionDone condition, itll just change setpoint as soon as it finishes executing and not be smooth
         elevator.setSetpoint(elevator.ball_setpoints[setpoint] + op.getRawAxis(1));
      }

      if(elevator.manual) {
         double elev_voltage = 12 * (0.85 * op.getRawAxis(1));
         double elev_percent = NorthUtils.getPercent(elev_voltage);      
         elevator.elev_talon.set(elev_percent);
         // System.out.println(elev_voltage + "V : " + elev_percent + "%");
      }

      carriage.disc_holder.set(discHolder.state ? Value.kForward : Value.kReverse);
      carriage.disc_arm.set(discArm.state ? Value.kForward : Value.kReverse);

      if(stopAuto.released) {
         North.stopExecution();
         North.default_drive_controller = TELEOP;
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

package team4618.robot.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.*;

import north.IDriveAndNavigation;
import north.North;
import north.RobotState;
import north.util.NorthUtils;
import static team4618.robot.IDs.*;

public class DriveSubsystem implements IDriveAndNavigation {
   public static final double feet_per_pulse = NorthUtils.getFeetPerPulse(6, 4096);

   public class DriveSide {
      public WPI_TalonSRX talon;
      public WPI_VictorSPX[] victors;
      public boolean flipDirection;

      public DriveSide(boolean flipped, int talon_id, int... victor_ids) {
         flipDirection = flipped;
         talon = new WPI_TalonSRX(talon_id);
         talon.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 0, 0);
         
         victors = new WPI_VictorSPX[victor_ids.length];
         for(int i = 0; i < victor_ids.length; i++) {
            WPI_VictorSPX victor = new WPI_VictorSPX(victor_ids[i]);
            victor.follow(talon);
            victors[i] = victor;
         }
      }

      //NOTE: multiply by 10 because it provides in ticks/100ms and we want ticks/sec
      public double getRate() { return (flipDirection ? -1 : 1) * feet_per_pulse * 10 * talon.getSensorCollection().getQuadratureVelocity(); }
      public double getDistance() { return (flipDirection ? -1 : 1) * feet_per_pulse * talon.getSensorCollection().getQuadraturePosition(); }
      public void setPositionSetpoint(double setpoint) { talon.set(ControlMode.Position, setpoint * (1 / feet_per_pulse)); }
      
      public void sendDiagnostics(String prefix) {
         // sendDiagnostic(prefix + " Speed", FeetPerSecond, getRate());
         // sendDiagnostic(prefix + " Position", Feet, getDistance());
         // sendDiagnostic(prefix + " Raw Position", Unitless, talon.getSensorCollection().getQuadraturePosition());
         // // sendDiagnostic(prefix + " Velocity Setpoint", FeetPerSecond, (shepherd.getControlMode() == ControlMode.Velocity) ? (feet_per_pulse * 10 * shepherd.getClosedLoopTarget(0)) : 0);
         // sendDiagnostic(prefix + " Position Setpoint", Feet, (talon.getControlMode() == ControlMode.Position) ? (feet_per_pulse * talon.getClosedLoopTarget(0)) : 0);
         // sendDiagnostic(prefix + " Power", Percent, talon.getMotorOutputPercent());
         // sendDiagnostic(prefix + " Current", Percent, talon.getOutputCurrent());
      }
   }

   public DriveSide left = new DriveSide(true, LEFT_TALON, LEFT_VICTOR_1, LEFT_VICTOR_2);
   public DriveSide right = new DriveSide(false, RIGHT_TALON, RIGHT_VICTOR_1, RIGHT_VICTOR_2);
   public AHRS navx = new AHRS(SPI.Port.kMXP);

   public DriveSubsystem() {
      //---------------------Can we move this inside the DriveSide contructor?
      left.talon.setSafetyEnabled(false);
      left.talon.setSensorPhase(true);

      right.talon.setSafetyEnabled(false);
      right.talon.setSensorPhase(true); //NOTE: we have to set this here but not while using software PID because the talon ignores setInverted
      right.talon.setInverted(true);
      for(WPI_VictorSPX victor : right.victors) {
         victor.setInverted(true);
      }
      //---------------------

      navx.zeroYaw();

      North.addDiagnosticsCallback(this::sendDiagnostics);
   }

   public void sendDiagnostics() {
      left.sendDiagnostics("Left");
      right.sendDiagnostics("Right");
      // sendDiagnostic("Speed", FeetPerSecond, getSpeed());

      // sendDiagnostic("Angle (Yaw/Z)", Degrees, getAngle());
      // sendDiagnostic("Pitch/X", Degrees, navx.getPitch());
      // sendDiagnostic("Roll/Y", Degrees, navx.getRoll());
      
      // //NOTE: testing (only navx things im not using is the temp sensor, quaternion stuff & raw data)
      //    sendDiagnostic("TEST G getAngle", Degrees, navx.getAngle());
      //    sendDiagnostic("TEST G getYaw", Degrees, navx.getYaw());
      //    sendDiagnostic("TEST M getCompassHeading", Degrees, navx.getCompassHeading());
      //    sendDiagnostic("TEST F getFusedHeading", Degrees, navx.getFusedHeading());

      //    // if(!navx.isMagnetometerCalibrated()) North.sendMessage(Warning, "Magnetometer Not Calibrated");
      //    // if(navx.isMagneticDisturbance()) North.sendMessage(Warning, "Magnetic Disturbance");

      //    //TODO: fix the units on these
      //    sendDiagnostic("X Accel", Unitless, navx.getWorldLinearAccelX());
      //    sendDiagnostic("Y Accel", Unitless, navx.getWorldLinearAccelY());
      //    sendDiagnostic("X Vel", Unitless, navx.getVelocityX());
      //    sendDiagnostic("Y Vel", Unitless, navx.getVelocityY());
      //    sendDiagnostic("X Pos", Unitless, navx.getDisplacementX());
      //    sendDiagnostic("Y Pos", Unitless, navx.getDisplacementY());

      // North.sendMessage(Message, currently_automatic ? "Drive PID Control" : "Drive Power Control");
   }

   public double getSpeed() {
      return (left.getRate() + right.getRate()) / 2.0;
   }

   public double getAngle() {
      return NorthUtils.canonicalizeAngle(navx.getAngle());
   }

   @Override
   public void zeroEncoders() {
      left.talon.setSelectedSensorPosition(0, 0, 0);
      right.talon.setSelectedSensorPosition(0, 0, 0);
   }

   @Override
   public EncoderData getEncoders() {
      EncoderData result = new EncoderData();
      result.left_p = left.getDistance();
      result.left_v = left.getRate();
      result.right_p = right.getDistance();
      result.right_p = right.getRate();
      return result;
   }

   @Override
   public void setMotorPercents(double left_percent, double right_percent) {
      left.talon.set(left_percent);
      right.talon.set(right_percent);
   }

   RobotState curr_state = new RobotState(0, 0, 0, 0, 0, 0);

   @Override
   public void setState(RobotState state) {
      navx.setAngleAdjustment(state.angle);
      navx.zeroYaw();

      curr_state = state;
   }

   @Override
   public RobotState getState(double dt) {
      double speed = getSpeed();
      double angle = getAngle();
      
      //TODO: change this to something pose based & dt independant
      double new_x = curr_state.posx + dt * speed * Math.cos(Math.toRadians(angle));
      double new_y = curr_state.posy + dt * speed * Math.sin(Math.toRadians(angle));

      return new RobotState(new_x, new_y, speed, angle, 
                            left.getDistance(), right.getDistance());
   }
}
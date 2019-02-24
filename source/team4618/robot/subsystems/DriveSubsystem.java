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
import north.Subsystem;
import north.reflection.*;
import north.util.DriveControls;
import north.util.NorthUtils;
import static team4618.robot.IDs.*;
import north.curves.*;

import static north.network.NetworkDefinitions.*;

public class DriveSubsystem extends Subsystem implements IDriveAndNavigation {
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
         sendDiagnostic(prefix + " Speed", FeetPerSecond, getRate());
         sendDiagnostic(prefix + " Position", Feet, getDistance());
         sendDiagnostic(prefix + " Raw Position", Unitless, talon.getSensorCollection().getQuadraturePosition());
         // sendDiagnostic(prefix + " Velocity Setpoint", FeetPerSecond, (shepherd.getControlMode() == ControlMode.Velocity) ? (feet_per_pulse * 10 * shepherd.getClosedLoopTarget(0)) : 0);
         sendDiagnostic(prefix + " Position Setpoint", Feet, (talon.getControlMode() == ControlMode.Position) ? (feet_per_pulse * talon.getClosedLoopTarget(0)) : 0);
         sendDiagnostic(prefix + " Power", Percent, talon.getMotorOutputPercent());
         sendDiagnostic(prefix + " Current", Percent, talon.getOutputCurrent());
      }
   }

   public DriveSide left = new DriveSide(true, LEFT_TALON, LEFT_VICTOR_1, LEFT_VICTOR_2);
   public DriveSide right = new DriveSide(false, RIGHT_TALON, RIGHT_VICTOR_1, RIGHT_VICTOR_2);
   public AHRS navx = new AHRS(SPI.Port.kMXP);

   @Override
   public void init() {
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
   }

   @Override
   public void sendDiagnostics() {
      left.sendDiagnostics("Left");
      right.sendDiagnostics("Right");
      sendDiagnostic("Speed", FeetPerSecond, getSpeed());

      sendDiagnostic("Angle (Yaw/Z)", Degrees, getAngle());
      sendDiagnostic("Pitch/X", Degrees, navx.getPitch());
      sendDiagnostic("Roll/Y", Degrees, navx.getRoll());
      
      //NOTE: testing (only navx things im not using is the temp sensor, quaternion stuff & raw data)
         sendDiagnostic("TEST G getAngle", Degrees, navx.getAngle());
         sendDiagnostic("TEST G getYaw", Degrees, navx.getYaw());
         sendDiagnostic("TEST M getCompassHeading", Degrees, navx.getCompassHeading());
         sendDiagnostic("TEST F getFusedHeading", Degrees, navx.getFusedHeading());

         if(!navx.isMagnetometerCalibrated()) North.sendMessage(Warning, "Magnetometer Not Calibrated");
         if(navx.isMagneticDisturbance()) North.sendMessage(Warning, "Magnetic Disturbance");

         //TODO: fix the units on these
         sendDiagnostic("X Accel", Unitless, navx.getWorldLinearAccelX());
         sendDiagnostic("Y Accel", Unitless, navx.getWorldLinearAccelY());
         sendDiagnostic("X Vel", Unitless, navx.getVelocityX());
         sendDiagnostic("Y Vel", Unitless, navx.getVelocityY());
         sendDiagnostic("X Pos", Unitless, navx.getDisplacementX());
         sendDiagnostic("Y Pos", Unitless, navx.getDisplacementY());

      North.sendMessage(Message, currently_automatic ? "Drive PID Control" : "Drive Power Control");
   }

   public double getSpeed() {
      return (left.getRate() + right.getRate()) / 2.0;
   }

   public double getAngle() {
      return NorthUtils.canonicalizeAngle(navx.getAngle());
   }

   boolean currently_automatic = false;

   @Override
   public void zeroEncoders() {
      left.talon.setSelectedSensorPosition(0, 0, 0);
      right.talon.setSelectedSensorPosition(0, 0, 0);
   }

   public void teleop(DriveControls values) {
      assert(!currently_automatic);
      left.talon.set(values.left);
      right.talon.set(values.right);
   }

   @Override
   public void setAutomaticControl(boolean set_automatic) {
      if(set_automatic && !currently_automatic) {
         zeroEncoders();

         left.setPositionSetpoint(0);
         right.setPositionSetpoint(0);
      } else if(!set_automatic && currently_automatic) {
         left.talon.set(0);
         right.talon.set(0);
      }

      if(set_automatic) {
         assert(left.talon.getControlMode() == ControlMode.Position);
         assert(right.talon.getControlMode() == ControlMode.Position);
      } else {
         assert(left.talon.getControlMode() == ControlMode.PercentOutput);
         assert(right.talon.getControlMode() == ControlMode.PercentOutput);
      }
   }

   @Override
   public void setDriveSetpoints(double left_val, double right_val) {
      assert(currently_automatic);
      left.setPositionSetpoint(left_val);
      right.setPositionSetpoint(right_val);
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

   @Override
   public String name() { return "Drive"; }
}
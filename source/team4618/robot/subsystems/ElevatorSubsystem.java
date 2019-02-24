package team4618.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;
import north.Subsystem;
import north.reflection.Command;
import north.reflection.Parameter;
import north.util.NorthUtils;
import team4618.robot.planning.ElevatorMotionPlan;

import static north.network.NetworkDefinitions.*;
import static team4618.robot.IDs.*;

public class ElevatorSubsystem extends Subsystem {
   Parameter hold_voltage;
   Parameter p_gain;
   Parameter max_vel;
   Parameter max_accel;

   public double setpoint = 0;
   public WPI_TalonSRX elev_talon = new WPI_TalonSRX(ELEV_TALON);

   public WPI_VictorSPX ball_conveyor = new WPI_VictorSPX(BALL_CONVEYOR);
   public Solenoid disc_holder = new Solenoid(DISC_HOLDER);
   public DoubleSolenoid disc_arm = new DoubleSolenoid(DISC_ARM_EXTEND, DISC_ARM_RETRACT);

   @Override
   public void init() {
      WPI_VictorSPX victor1 = new WPI_VictorSPX(ELEV_VICTOR_1);
      victor1.follow(elev_talon);
      WPI_VictorSPX victor2 = new WPI_VictorSPX(ELEV_VICTOR_2);
      victor2.follow(elev_talon);
   }

   public static final double ft_per_tick = ( (1.751 / 12.0) * Math.PI ) / 4096.0;
   public double getHeight() { return ft_per_tick * elev_talon.getSensorCollection().getQuadraturePosition(); }
   public double getSpeed() { return ft_per_tick * 10 * elev_talon.getSensorCollection().getQuadratureVelocity(); }

   @Override
   public void sendDiagnostics() {
      sendDiagnostic("Height", Feet, getHeight());
   }

   ElevatorMotionPlan curr_plan;
   // @Override
   public void __periodic() {
      if((curr_plan == null) || (curr_plan.setpoint != setpoint)) {
         curr_plan = new ElevatorMotionPlan(getHeight(), getSpeed(), setpoint, 
                                            max_vel.get(), max_accel.get());
      }

      double error = curr_plan.getSetpoint() - getHeight();
      double voltage = hold_voltage.get() + p_gain.get() * error;
      elev_talon.set(NorthUtils.getPercent(voltage));
   }

   // @Command
   // public boolean calibrate() {
   //    return false;
   // }

   @Override
   public String name() { return "Elevator"; }
}
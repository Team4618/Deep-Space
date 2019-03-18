package team4618.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import north.North;
import north.NorthSequence;
import north.parameters.*;
import north.util.NorthUtils;
import team4618.robot.Robot;
import team4618.robot.planning.ElevatorMotionPlan;

import static north.network.NetworkDefinitions.*;
import static team4618.robot.IDs.*;

public class ElevatorSubsystem {
   Parameter hold_voltage;
   Parameter p_gain;
   Parameter max_vel;
   Parameter max_accel;

   public WPI_TalonSRX elev_talon = new WPI_TalonSRX(ELEV_TALON);
   public DigitalInput bottom_switch = new DigitalInput(ELEV_BOTTOM_SWITCH);

   public ElevatorSubsystem() {
      WPI_VictorSPX victor1 = new WPI_VictorSPX(ELEV_VICTOR_1);
      victor1.follow(elev_talon);
      WPI_VictorSPX victor2 = new WPI_VictorSPX(ELEV_VICTOR_2);
      victor2.follow(elev_talon);

      North.addPeriodic(this::periodic);
      North.addDiagnosticsCallback(this::sendDiagnostics);
   }

   public static final int number_of_stages = 2;
   public static final double ft_per_tick = number_of_stages * ( (1.751 / 12.0) * Math.PI ) / 4096.0;
   public double getHeight() { return ft_per_tick * elev_talon.getSensorCollection().getQuadraturePosition(); }
   public double getSpeed() { return ft_per_tick * 10 * elev_talon.getSensorCollection().getQuadratureVelocity(); }

   public boolean manual = true;
   public boolean calibrated = false;

   public final double handoff_height = 0;
   public final double max_height = 5;
   public final double[] ball_setpoints = new double[]{
      0, 1, 2, 3, 4
   };

   ElevatorMotionPlan curr_plan;
   double setpoint = 0;
   
   public boolean auto_home() {
      elev_talon.set(NorthUtils.getPercent(2.5 /*volts, positive is down*/));
         
      if(!bottom_switch.get()) {
         System.out.println("Elev Homed");
         elev_talon.setSelectedSensorPosition(0, 0, 0);
         return true;
      }

      return false;
   }

   public boolean auto_calibrate() {
      //TODO

      return true;
   }

   NorthSequence calibration_sequence = NorthSequence.Begin()
                                          .Do(this::auto_home)
                                          .Do(this::auto_calibrate)
                                          .Do(() -> calibrated = true)
                                          .End();

   public void periodic() {
      if(!manual) {
         if(calibrated) {
            if((curr_plan == null) || ((curr_plan.setpoint - setpoint) > 0.001)) {
               curr_plan = new ElevatorMotionPlan(getHeight(), getSpeed(), setpoint, 
                                                  1 /*max_vel.get()*/, 1 /*max_accel.get()*/);
            }
      
            double error = curr_plan.getSetpoint() /*setpoint*/ - getHeight();
            double voltage = /*hold_voltage.get()*/1.5 + /*p_gain.get()*/ 6 * error;
            elev_talon.set(NorthUtils.getPercent(-voltage));

            // System.out.println("Curr Setpoint: " + curr_plan.getSetpoint() + " V: " + voltage + " Error: " + error);
         } else if(!calibration_sequence.isExecuting()) {
            System.out.println("Calibrating");
            North.execute(calibration_sequence);
         }
      }
   }

   public void setSetpoint(double val) {
      setpoint = NorthUtils.clamp(0, max_height, val);
   }

   public boolean atSetpoint() {
      boolean stopped = Math.abs(getSpeed()) < 0.1;
      boolean at_position = Math.abs(getHeight() - setpoint) < 0.25;
      return stopped && at_position;
   }

   public boolean readyForHandOff() {
      return (setpoint == handoff_height) && atSetpoint();
   }

   public static final String NAME = "Elevator";

   public void sendDiagnostics() {
   //    sendDiagnostic("Height", Feet, getHeight());
      // North.sendMessage(NAME, Message, "Elev State: " + curr_state.toString());

      // System.out.println("Height " + getHeight());
      // System.out.println("Bottom Switch " + bottom_switch.get());
   }
}
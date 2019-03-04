package team4618.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import north.North;
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

   public WPI_TalonSRX elev_talon = new WPI_TalonSRX(ELEV_TALON);
   public DigitalInput bottom_switch = new DigitalInput(ELEV_BOTTOM_SWITCH);

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

   public static enum State {
      Unzeroed, //elevator doesnt know where 0 is
      Uncalibrated, //elevator hasnt calibrated hold voltage

      HandOff, //Lined up with the ball intake
      Ball,
      
   }

   final double handoff_height = 0;

   State curr_state = State.Unzeroed;
   ElevatorMotionPlan curr_plan;
   // @Override
   public void __periodic() {
      double setpoint = 0;

      switch(curr_state) {
         case Unzeroed: {
            // elev_talon.set(NorthUtils.getPercent(voltage));
            
            if(bottom_switch.get()) {
               elev_talon.setSelectedSensorPosition(0, 0, 0);
               curr_state = State.Uncalibrated;
            }
         } break;
         
         case Uncalibrated: {
            //TODO: automatic calibration
            curr_state = State.HandOff;
         } break;

         case HandOff: {
            setpoint = handoff_height;
         } break;

         case Ball: {
            
         } break;
      }

      if(curr_state != State.Uncalibrated) {
         if((curr_plan == null) || (curr_plan.setpoint != setpoint)) {
            curr_plan = new ElevatorMotionPlan(getHeight(), getSpeed(), setpoint, 
                                               max_vel.get(), max_accel.get());
         }
   
         double error = curr_plan.getSetpoint() - getHeight();
         double voltage = hold_voltage.get() + p_gain.get() * error;
         elev_talon.set(NorthUtils.getPercent(voltage));
      } 
   }

   public boolean readyForHandOff() {
      boolean stopped = Math.abs(getSpeed()) < 0.01;
      boolean at_position = Math.abs(getHeight() - handoff_height) < 0.01;
      return (curr_state == State.HandOff) && stopped && at_position;
   }

   @Override
   public void sendDiagnostics() {
      sendDiagnostic("Height", Feet, getHeight());
      North.sendMessage(Message, "Elev State: " + curr_state.toString());
   }

   @Override
   public String name() { return "Elevator"; }
}
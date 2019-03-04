package team4618.robot.subsystems;

import north.Subsystem;
import team4618.robot.Robot;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;

import static team4618.robot.IDs.*;

public class ElevCarriageSubsystem extends Subsystem {
   public WPI_VictorSPX ball_conveyor = new WPI_VictorSPX(BALL_CONVEYOR);
   public Solenoid disc_holder = new Solenoid(DISC_HOLDER);
   public DoubleSolenoid disc_arm = new DoubleSolenoid(DISC_ARM_EXTEND, DISC_ARM_RETRACT);
   DigitalInput ball_sensor = new DigitalInput(BALL_CONVEYOR_SENSOR);

   public static enum DesiredState {
      Idle,
      Intaking,
      BackShoot,
      FrontShoot,
   }

   boolean desired_state_set = false;
   public DesiredState desired_state = DesiredState.Idle;
   public void setState(DesiredState state) {
      desired_state_set = true;
      desired_state = state;
   }

   public boolean hasBall() {
      return !ball_sensor.get();
   }

   public static enum State {
      Idle, 
      HandOff,
      Holding, 
      BackShoot,
      FrontShoot,
   }

   State curr_state = State.Idle;

   @Override
   public void periodic() {
      switch(curr_state) {
         case Idle: {
            ball_conveyor.set(0);

            if(hasBall()) {
               curr_state = State.Holding;
            } else if((desired_state == DesiredState.Intaking) &&
                      Robot.elevator.readyForHandOff())
            {
               curr_state = State.HandOff;
            } else if(desired_state == DesiredState.FrontShoot) {
               curr_state = State.FrontShoot;
            } else if(desired_state == DesiredState.BackShoot) {
               curr_state = State.BackShoot;
            }
         } break;

         case HandOff: {
            ball_conveyor.set(-1);

            if(hasBall()) {
               curr_state = State.Holding;
               desired_state = DesiredState.Idle;
            }
         } break;
         
         case Holding: {
            ball_conveyor.set(0);

            if(!hasBall()) {
               curr_state = State.Idle;
            }
         } break; 
         
         case BackShoot: {
            ball_conveyor.set(1);

            if(desired_state == DesiredState.Idle) {
               curr_state = State.Idle;
            }
         } break;

         case FrontShoot: {
            ball_conveyor.set(-1);
            
            if(desired_state == DesiredState.Idle) {
               curr_state = State.Idle;
            }
         } break;
      }

      if(!desired_state_set && 
         ((desired_state == DesiredState.BackShoot) || 
          (desired_state == DesiredState.FrontShoot)))
      {
         desired_state = DesiredState.Idle;
      }
      desired_state_set = false;
   }

   @Override
   public void init() { }

   @Override
   public String name() { return "Elev Carriage"; }
}
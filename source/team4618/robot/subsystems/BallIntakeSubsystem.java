package team4618.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import north.Subsystem;
import north.reflection.Parameter;

import static team4618.robot.IDs.*;

public class BallIntakeSubsystem extends Subsystem {
   public DoubleSolenoid arm = new DoubleSolenoid(BALL_INTAKE_EXTEND, BALL_INTAKE_RETRACT);
   public WPI_VictorSPX roller = new WPI_VictorSPX(BALL_INTAKE);
   DigitalInput ball_sensor = new DigitalInput(BALL_INTAKE_SENSOR);

   Parameter intake_speed;
   Parameter arm_change_time;

   public static enum DesiredState {
      Idle,
      Intaking,
      FrontShoot,
   }

   boolean desired_state_set = false;
   DesiredState desired_state = DesiredState.Idle;
   public void setState(DesiredState state) {
      desired_state_set = true;
      desired_state = state;
   }
   
   public void toggleIntaking() {
      if(desired_state != DesiredState.Intaking) {
         setState(DesiredState.Intaking);
      } else {
         setState(DesiredState.Idle);
      }
   }

   public void frontShoot() {
      setState(DesiredState.FrontShoot);
   }

   public static enum State {
      Idle, //arms up, no ball
      Intaking, //arm down, no ball
      GoingUp, //arm going up, has ball
      Hold,  //arm up, has ball (waiting on elev or something) 
      PassOff, //arms up, has ball & elev in position
      FrontShoot,
   }

   State curr_state = State.Idle;
   double last_arm_change = 0;

   @Override
   public void periodic() {
      switch(curr_state) {
         case Idle: {
            arm.set(Value.kReverse);
            roller.set(0);

            if(ball_sensor.get()) {
               curr_state = State.Hold;
            } else if(desired_state == DesiredState.Intaking) {
               curr_state = State.Intaking;
            } else if(desired_state == DesiredState.FrontShoot) {
               curr_state = State.FrontShoot;
            }
         } break;
         
         case Intaking: {
            arm.set(Value.kForward);
            roller.set(-1 /*intake_speed.get()*/);

            if(desired_state == DesiredState.Idle) {
               curr_state = State.Idle;
            } else if(ball_sensor.get()) {
               curr_state = State.GoingUp;
            }
         } break;

         case GoingUp: {
            if(arm.get() == Value.kForward) {
               last_arm_change = Timer.getFPGATimestamp();
            }

            arm.set(Value.kReverse);
            roller.set(0);

            if((Timer.getFPGATimestamp() - last_arm_change) > arm_change_time.get()) {
               curr_state = State.Hold;
            }
         } break;

         case Hold: {
            arm.set(Value.kReverse);
            roller.set(0);
            
            if(!ball_sensor.get()) {
               curr_state = State.Idle;
            } else if(desired_state == DesiredState.FrontShoot) {
               curr_state = State.FrontShoot;
            } /* else if(???) {
               curr_state = State.PassOff;
            } */
            //TODO: if the sensor fails we want a way to intake while it thinks its holding a ball
         } break;

         case PassOff: {
            //TODO
            arm.set(Value.kReverse);
            roller.set(0);
         } break;

         case FrontShoot: {
            arm.set(Value.kReverse);
            roller.set(1);

            if(desired_state == DesiredState.Idle) {
               curr_state = State.Idle;
            } else if(desired_state == DesiredState.Intaking) {
               curr_state = State.Intaking;
            }
         } break;
      }

      if(!desired_state_set && (desired_state == DesiredState.FrontShoot)) {
         desired_state = DesiredState.Idle;
      }
      desired_state_set = false;
   }

   @Override
   public void init() { }

   @Override
   public String name() { return "Ball Intake"; }
}
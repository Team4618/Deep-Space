package team4618.robot.subsystems;

import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;

import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.DoubleSolenoid.Value;
import north.Subsystem;
import north.reflection.Parameter;
import team4618.robot.Robot;

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
      BackShoot,
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
      Idle, //arms up, no ball
      Intaking, //arm down, roller spinning, waiting for ball
      HandOff, //arms up/going up, has ball & elev in position
      BackShoot //
   }

   State curr_state = State.Idle;

   @Override
   public void periodic() {
      switch(curr_state) {
         case Idle: {
            arm.set(Value.kReverse);
            roller.set(0);

            if((desired_state == DesiredState.Intaking) && 
               Robot.elevator.readyForHandOff())
            {
               curr_state = State.Intaking;
            } else if(desired_state == DesiredState.BackShoot) {
               curr_state = State.BackShoot;
            }
         } break;
         
         case Intaking: {
            arm.set(Value.kForward);
            roller.set(-0.75 /*intake_speed.get()*/);

            if(desired_state == DesiredState.Idle) {
               curr_state = State.Idle;
            } else if(hasBall()) {
               curr_state = State.HandOff;
            }
         } break;

         case HandOff: {
            arm.set(Value.kReverse);
            roller.set(-0.75);

            if(Robot.carriage.hasBall()) {
               curr_state = State.Idle;
               desired_state = DesiredState.Idle;
            }
         } break;

         case BackShoot: {
            arm.set(Value.kReverse);
            roller.set(-1);
            
            if(desired_state == DesiredState.Idle) {
               curr_state = State.Idle;
            }
         } break;
      }

      if(!desired_state_set && (desired_state == DesiredState.BackShoot)) {
         desired_state = DesiredState.Idle;
      }
      desired_state_set = false;
   }

   @Override
   public void init() { }

   @Override
   public String name() { return "Ball Intake"; }
}
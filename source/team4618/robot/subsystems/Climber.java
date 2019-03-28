package team4618.robot.subsystems;

import edu.wpi.first.wpilibj.Solenoid;
import north.North;
import north.NorthSequence;

import static team4618.robot.IDs.*;

import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;

public class Climber {
   Solenoid popsocket = new Solenoid(POP_SOCKET_EXTEND);
   WPI_TalonSRX wrist = new WPI_TalonSRX(DGI_WRIST);

   //NOTE: climb part 1 gets the front 2 wheels on the step using the wrist
   //NOTE: climb part 2 gets us all the way up using the pop socket

   public boolean climb_1_done = false;
   public NorthSequence climb_sequence_1 = NorthSequence.Begin()
                                                        .Do(() -> wrist.set(0.1)) //go down slow
                                                        .Wait(5)
                                                        .Do(() -> wrist.set(0.1)) //push up
                                                        .Wait(1)
                                                        .Do(() -> wrist.set(0)) //hold wrist
                                                        .Do(() -> climb_1_done = true)
                                                        .End();

   /*
   if(part_1_button) {
      if(climb_1_done) {
         drive & hold wrist
      } else if(!clibm_sequence_1.isExecuting()) {
         North.execute(climb_sequence_1);
      }
   } else if(part_2_button) {
      drive
      wrist.set(0);
      pop_socket.set(true)
   } else {
      climb_1_done = false;
      
      wrist.set(0);
      pop_socket.set(false);
   }
   */
}
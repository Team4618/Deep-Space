package north.autonomous;

import north.autonomous.commands.GenericCommandInstance;

public class DiscreteEvent {
   double distance;
   GenericCommandInstance command;
   boolean triggered = false;

   public void tick(double curr_distance) {
      if((curr_distance >= distance) && !triggered) {
         command.invoke();
         triggered = true;
      }
   }

   public void reset() {
      triggered = false;
   }
}
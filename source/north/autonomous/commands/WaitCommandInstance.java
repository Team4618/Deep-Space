package north.autonomous.commands;

import north.autonomous.CommandState;

public class WaitCommandInstance implements ICommandInstance {
   double duration;
   CommandState state = null;

   @Override
   public void reset() {
      state = null;
   }

   @Override
   public boolean invoke() {
      if(state == null)
         state = new CommandState();

      state.update();
      return (state.elapsedTime >= duration);
   }
}
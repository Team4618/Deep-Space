package north.autonomous.commands;

import north.autonomous.CommandState;

public class GenericCommandInstance implements ICommandInstance {
   Command command;
   double[] parameters;

   CommandState state = null;

   public GenericCommandInstance(Command command, double[] parameters) {
      this.command = command;
      this.parameters = parameters;
   }

   @Override
   public void reset() {
      state = null;
   }

   @Override
   public boolean invoke() {
      if(state == null) {
         state = new CommandState();
      }

      state.update();
      boolean result = command.invoke(state, parameters);
      state.init = false;

      return result;
   }
}

package north;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.function.Supplier;

import north.autonomous.Command;
import north.autonomous.IExecutable;

public class NorthSequence implements IExecutable {
   public static class Builder {
      ArrayList<Function<Double, Boolean>> sequence = new ArrayList<>();

      public Builder Do(Runnable exec) {
         sequence.add((t) -> {
            exec.run();
            return true;
         });
         return this;
      }
   
      public Builder Do(Function<Double, Boolean> exec) {
         sequence.add(exec);
         return this;
      }
   
      public Builder Wait(Supplier<Boolean> cond) {
         sequence.add((t) -> cond.get());
         return this;
      }

      public Builder Wait(double time) {
         sequence.add((t) -> t > time);
         return this;
      }
   
      public NorthSequence End() {
         return new NorthSequence(sequence);
      }
   }

   public static Builder Begin() {
      return new Builder();
   }

   final Command[] commands;

   public NorthSequence(ArrayList<Function<Double, Boolean>> sequence) {
      assert(sequence.size() > 0);
      commands = new Command[sequence.size()];
      Command curr_command = null;

      for(int i = sequence.size() - 1; i >= 0; i--) {
         curr_command = new Command(sequence.get(i), curr_command);
         commands[i] = curr_command;
      }
   }

   public boolean isExecuting() {
      for(Command command : commands) {
         if(command == North.current_executable)
            return true;
         
      }
      return false;
   }

   @Override
   public IExecutable execute() { return commands[0]; }

   @Override
   public void init() { }
}
package north.autonomous;

import java.util.function.Function;
import edu.wpi.first.wpilibj.Timer;

public class Command implements IExecutable {
   final Function<Double, Boolean> func;
   final IExecutable next;

   //if func returns true the command finishes
   public Command(Function<Double, Boolean> func, IExecutable next) {
      this.func = func;
      this.next = next;
   }

   public static Command Wait(double time, IExecutable next) {
      return new Command(t -> t >= time, next);
   }

   double start_time = 0;

   @Override
   public void init() {
      start_time = Timer.getFPGATimestamp();
   }

   @Override
   public IExecutable execute() {
      boolean done = func.apply(Timer.getFPGATimestamp() - start_time);
      return done ? next : this;
   }
}
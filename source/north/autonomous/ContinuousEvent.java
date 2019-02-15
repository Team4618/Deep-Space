package north.autonomous;

import java.util.List;

import north.autonomous.commands.Command;
import north.util.InterpolatingMap;
import north.util.Vector2;

public class ContinuousEvent {
   Command command;
   InterpolatingMap<Double> valueAt = new InterpolatingMap<>(InterpolatingMap::doubleLerp);

   public ContinuousEvent(Command command, List<Vector2> samples) {
      this.command = command;
      for (Vector2 sample : samples) {
         valueAt.put(sample.x, sample.y);
      }
   }

   public void update(double distance) {
      double value = valueAt.getInterpolated(distance);
      command.invoke(null, new double[]{ value });
   }
}
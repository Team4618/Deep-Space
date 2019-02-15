package north.autonomous.commands;

import java.util.ArrayList;

import north.North;
import north.autonomous.ContinuousEvent;
import north.autonomous.DiscreteEvent;
import north.curves.PathProgress;
import north.curves.PivotPlan;

public class PivotCommandInstance implements ICommandInstance {
   PivotPlan plan;
   ArrayList<DiscreteEvent> devents;
   ArrayList<ContinuousEvent> cevents;
   
   @Override
   public void reset() {
      devents.forEach(DiscreteEvent::reset);
      plan.reset();
   }

   @Override
   public boolean invoke() {
      plan.update();
      PathProgress progress = North.drive.followPivot(plan);
      
      for(DiscreteEvent devent : devents) {
         devent.tick(progress.distance);
      }

      for(ContinuousEvent cevent : cevents) {
         cevent.update(progress.distance);
      }

      return progress.done;
   }
}
package north.autonomous;

import java.util.ArrayList;

import north.North;
import north.curves.PathPlan;
import north.curves.PathProgress;

public class Path implements IExecutable {
   Node in_node;
   String condition;
   Node out_node;
   
   double initial_angle;

   ArrayList<DiscreteEvent> devents;
   ArrayList<ContinuousEvent> cevents;

   PathPlan plan;
   
   @Override
   public void init() {
      // plan = create plan
      devents.forEach(DiscreteEvent::reset);
   }

   @Override
   public IExecutable execute() {
      plan.update();
      PathProgress progress = new PathProgress(true, 0) /*North.drive.followPath(plan)*/;

      for(DiscreteEvent devent : devents) {
         devent.tick(progress.distance);
      }

      for(ContinuousEvent cevent : cevents) {
         cevent.update(progress.distance);
      }

      return progress.done ? out_node : this;
   }
}
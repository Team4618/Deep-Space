package north.autonomous;

import north.North;
import north.curves.PivotPlan;

public class PivotExecutable implements IExecutable {
   IExecutable next;
   PivotPlan plan;

   public PivotExecutable(double dest_angle, IExecutable next) {
      this.next = next;
      this.plan = PivotPlan.generatePivot(dest_angle);
   } 

   @Override
   public IExecutable execute() {
      plan.update();
      return North.drive.followPivot(plan).done ? next : this;
   }
}
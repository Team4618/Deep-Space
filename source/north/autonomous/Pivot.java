package north.autonomous;

import north.North;
import north.curves.PivotPlan;

public class Pivot implements IExecutable {
   final double dest_angle;
   final IExecutable next;

   public Pivot(double dest_angle, IExecutable next) {
      this.dest_angle = dest_angle;
      this.next = next;
   } 

   PivotPlan plan;
   
   @Override
   public void init() {
      plan = PivotPlan.generatePivot(dest_angle);
   }

   @Override
   public IExecutable execute() {
      plan.update();
      return /*North.drive.followPivot(plan).done*/ false ? next : this;
   }
}
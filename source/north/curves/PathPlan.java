package north.curves;

import north.util.InterpolatingMap;
import north.util.Vector2;

public class PathPlan {
   InterpolatingMap<Vector2> len_to_pos = new InterpolatingMap<>(Vector2::lerp);

   public void update() {
      //adjust path 
      //update setpoints
   }

   public void reset() {

   }

   //NOTE: plan-drive interface
   public double getLeftSetpoint() {
      return 0;
   }

   public double getRightSetpoint() {
      return 0;
   }
   
   public boolean zeroEncoders() {
      return false;
   }

   public PathProgress getProgress() {
      return new PathProgress(false, 0);
   }
}
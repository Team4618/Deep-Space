package north.curves;

import edu.wpi.first.wpilibj.Timer;
import north.North;
import north.util.InterpolatingMap;
import north.util.NorthUtils;
import north.util.Vector2;

public class PivotPlan {
   public static class RobotSample {
      public double left_pos;
      public double right_pos;
      public double time;
      public double angle_distance;
      
      public RobotSample(double left_pos, double right_pos, double time, double angle_distance) {
         this.left_pos = left_pos;
         this.right_pos = right_pos;
         this.time = time;
         this.angle_distance = angle_distance;
      }

      public boolean isAt() {
         return (Math.abs(North.state.left_pos - left_pos) < 0.05) &&
                (Math.abs(North.state.right_pos - right_pos) < 0.05);
      }
   }

   //NOTE: dont get reset
   public Vector2 pos;
   public double end_angle;
   public boolean clockwise;
   public double start_angle;
   public InterpolatingMap<Double> velocity_map;

   //NOTE: get reset
   public RobotSample[] samples; 
   public double distance;
   public int target_sample;
   public boolean init;
   public double start_time;

   public PivotPlan(double start_angle, double end_angle, 
                    boolean clockwise, InterpolatingMap<Double> velocity_map,
                    Vector2 pivot_point) 
   {
      this.pos = pivot_point;
      this.start_angle = start_angle;
      this.end_angle = end_angle;
      this.clockwise = clockwise;
      this.velocity_map = velocity_map;
      
      reset();
   }

   public RobotSample[] generateSamples() {
      double pivot_length = NorthUtils.AngleBetween(start_angle, end_angle, clockwise);
      int sample_count = (int)(2 * Math.abs(pivot_length));
      RobotSample[] results = new RobotSample[sample_count];
      double step = Math.abs(pivot_length / (sample_count - 1));

      double wheelbase = North.width;

      double time = 0;
      double left_pos = 0;
      double right_pos = 0;
      for(int i = 0; i < sample_count; i++) {
         double pivot_distance = step * i;
         double velocity = (Math.PI * wheelbase / 360) * velocity_map.getInterpolated(pivot_distance);
         
         results[i] = new RobotSample(left_pos, right_pos, time, pivot_distance);

         double dt = (1 / velocity_map.getInterpolated(pivot_distance)) * step;
         time += dt;
         left_pos += (clockwise ? 1 : -1) * velocity * dt;
         right_pos += (clockwise ? -1 : 1) * velocity * dt;
      }

      return results;
   }

   public void update() {
      if(!init) {
         start_time = Timer.getFPGATimestamp();
         init = true;
      }
      double running_time = Timer.getFPGATimestamp() - start_time;
      
      double new_distance = NorthUtils.clamp(0, NorthUtils.AngleBetween(start_angle, end_angle, clockwise), NorthUtils.AngleBetween(start_angle, end_angle, clockwise) - NorthUtils.AngleBetween(North.state.angle, end_angle, clockwise));
      distance = Math.max(distance, new_distance);

      for(int i = 0; i < samples.length; i++) {
         if((samples[i].time < running_time) &&
            (Math.abs(samples[i].angle_distance - new_distance) < 10))
         {
            target_sample = i;
         }
      }

      System.out.println("Progress: " + distance + " i=" + target_sample);

      //TODO: adjust plan (eg. if we didnt turn on the spot add a bit of movement to bring us back) 
   }

   public void reset() {
      samples = generateSamples();
      distance = 0;
      target_sample = 0;
      init = false;
   }

   //NOTE: plan-drive interface
   public double getLeftSetpoint() {
      return samples[target_sample].left_pos;
   }

   public double getRightSetpoint() {
      return samples[target_sample].right_pos;
   }
   
   public PathProgress getProgress() {
      return new PathProgress(samples[samples.length - 1].isAt(), distance);
   }

   //NOTE: helper function
   public static PivotPlan generatePivot(double destAngle) {
      boolean clockwise = NorthUtils.IsClockwiseShorter(North.state.angle, destAngle);
      double pivot_length = Math.abs(NorthUtils.AngleBetween(North.state.angle, destAngle, clockwise));

      //TODO: pass a max velocity & accel here
      InterpolatingMap<Double> velocity_map = new InterpolatingMap<>(InterpolatingMap::doubleLerp);
      velocity_map.put(0.0, 0.0);
      velocity_map.put(pivot_length * 0.1, 20.0);
      velocity_map.put(pivot_length * 0.9, 20.0);
      velocity_map.put(pivot_length, 0.0);

      return new PivotPlan(North.state.angle, destAngle, clockwise, 
                           velocity_map, new Vector2(North.state.posx, North.state.posy));
   }
}
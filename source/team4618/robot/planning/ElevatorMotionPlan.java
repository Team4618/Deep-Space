package team4618.robot.planning;

import edu.wpi.first.wpilibj.Timer;
import north.util.InterpolatingMap;
import north.util.NorthUtils;

public class ElevatorMotionPlan {
   double start_time;
   public double setpoint;
   InterpolatingMap<Double> plan = new InterpolatingMap<>(InterpolatingMap::doubleLerp);

   public ElevatorMotionPlan(double d_curr, double v_i, double d_dest,
                             double v_max, double a_max)
   {
      start_time = Timer.getFPGATimestamp();

      double sign = (d_dest - d_curr) / Math.abs(d_dest - d_curr);
      v_i *= sign;
      
      double d = Math.abs(d_dest - d_curr);
      double d_accel = (v_max - v_i) * (v_max - v_i) / (2 * a_max);
      double d_deccel = v_max * v_max / (2 * a_max); 

      if(d > (d_accel + d_deccel)) {
         double t_accel = (v_max - v_i) / a_max;
         double t_deccel = v_max / a_max;
         double t_coast = (d - d_accel - d_accel) / v_max;
         
         plan.put(0.0,                          d_curr);
         plan.put(t_accel,                      d_curr + sign * d_accel);
         plan.put(t_accel + t_coast,            d_dest - sign * d_deccel);
         plan.put(t_accel + t_coast + t_deccel, d_dest);
      } else {
         v_max = ( (2 * v_i - 1) + Math.sqrt((1-2*v_i)*(1-2*v_i) - 4*(v_i*v_i - 2*a_max*d)) ) / 2;
         
         d_accel = (v_max - v_i) * (v_max - v_i) / (2 * a_max);
         d_deccel = v_max * v_max / (2 * a_max); 

         double t_accel = (v_max - v_i) / a_max;
         double t_deccel = v_max / a_max;
         
         plan.put(0.0,                d_curr);
         plan.put(t_accel,            d_curr + sign * d_accel);
         plan.put(t_accel + t_deccel, d_dest);
      }
   }

   public double getSetpoint() {
      double t = Timer.getFPGATimestamp() - start_time;
      t = NorthUtils.clamp(plan.firstKey(), plan.lastKey(), t);

      return plan.getInterpolated(t);
   }
}
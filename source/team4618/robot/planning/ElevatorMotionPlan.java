package team4618.robot.planning;

import java.util.ArrayList;

import edu.wpi.first.wpilibj.Timer;
import north.util.InterpolatingMap;
import north.util.NorthUtils;

public class ElevatorMotionPlan {
   double start_time;
   public double setpoint;
   
   public class PlanPart {
      double start_time;
      double end_time;

      double d_0;
      double a;
      double v_0;

      public PlanPart(double start_time, double end_time,
                      double d_0, double a, double v_0)
      {
         this.start_time = start_time;
         this.end_time = end_time;

         this.d_0 = d_0;
         this.a = a;
         this.v_0 = v_0;
      }

      public double getSetpoint(double t) {
         return d_0 + v_0*t + 0.5*a*t*t;
      }
   }
   
   ArrayList<PlanPart> plan = new ArrayList<>();
   // InterpolatingMap<Double> plan = new InterpolatingMap<>(InterpolatingMap::doubleLerp);

   public ElevatorMotionPlan(double d_curr, double v_i, double d_dest,
                             double v_max, double a_max)
   {
      setpoint = d_dest;
      start_time = Timer.getFPGATimestamp();

      double sign = (d_dest - d_curr) / Math.abs(d_dest - d_curr);
      v_i *= sign;
      
      double d = Math.abs(d_dest - d_curr);
      double d_accel = (v_max - v_i) * (v_max - v_i) / (2 * a_max);
      double d_deccel = v_max * v_max / (2 * a_max); 

      System.out.println("Generating profile from " + d_curr + " (at " + v_i + " ft/s) to " + d_dest);

      if(d > (d_accel + d_deccel)) {
         double t_accel = (v_max - v_i) / a_max;
         double t_deccel = v_max / a_max;
         double t_coast = (d - d_accel - d_accel) / v_max;
         
         plan.add(new PlanPart(0.0,               t_accel,                      d_curr,                   sign * a_max,  sign * v_i));
         plan.add(new PlanPart(t_accel,           t_accel + t_coast,            d_curr + sign * d_accel,  0,             sign * v_max));
         plan.add(new PlanPart(t_accel + t_coast, t_accel + t_coast + t_deccel, d_dest - sign * d_deccel, sign * -a_max, sign * v_max));
         
         // plan.put(0.0,                          d_curr);
         // plan.put(t_accel,                      d_curr + sign * d_accel);
         // plan.put(t_accel + t_coast,            d_dest - sign * d_deccel);
         // plan.put(t_accel + t_coast + t_deccel, d_dest);
         
         System.out.println("Trapazoidal Profile (T, D)");
         System.out.println(0.0 + " : " + d_curr);
         System.out.println(t_accel  + " : " + (d_curr + sign * d_accel));
         System.out.println((t_accel + t_coast)  + " : " + (d_dest - sign * d_accel));
         System.out.println((t_accel + t_coast + t_deccel)  + " : " + d_dest);
      } else {
         v_max = ( (2 * v_i - 1) + Math.sqrt((1-2*v_i)*(1-2*v_i) - 4*(v_i*v_i - 2*a_max*d)) ) / 2;
         
         d_accel = (v_max - v_i) * (v_max - v_i) / (2 * a_max);
         d_deccel = v_max * v_max / (2 * a_max); 

         double t_accel = (v_max - v_i) / a_max;
         double t_deccel = v_max / a_max;
         
         plan.add(new PlanPart(0.0,       t_accel,            d_curr,                  sign * a_max,  sign * v_i));
         plan.add(new PlanPart(t_accel,   t_accel + t_deccel, d_curr + sign * d_accel, sign * -a_max, sign * v_max));
         
         // plan.put(0.0,                d_curr);
         // plan.put(t_accel,            d_curr + sign * d_accel);
         // plan.put(t_accel + t_deccel, d_dest);

         System.out.println("Triangular Profile (T, D)");
         System.out.println(0.0 + " : " + d_curr);
         System.out.println(t_accel  + " : " + (d_curr + sign * d_accel));
         System.out.println((t_accel + t_deccel)  + " : " + d_dest);
      }

      System.out.println("vel " + v_max + " ft/s");
      System.out.println("accel " + a_max + " ft/s^2");
   }

   public double getSetpoint() {
      double t = Timer.getFPGATimestamp() - start_time;

      t = NorthUtils.clamp(plan.get(0).start_time, plan.get(plan.size() - 1).end_time, t);
      PlanPart part = null;
      for(PlanPart p : plan) {
         if((p.start_time <= t) && (t <= p.end_time)) {
            part = p;
            break;
         }
      }
      assert(part != null);
      return part.getSetpoint(t);

      // t = NorthUtils.clamp(plan.firstKey(), plan.lastKey(), t);
      // return plan.getInterpolated(t);
   }
}
package north;

public class RobotState {
   public double posx;
   public double posy;
   public double angle;

   public double speed;
   //NOTE: these are the left & right drive encoder values
   public double left_pos;
   public double right_pos;
   
   public RobotState(double posx, double posy, double speed, double angle,
                     double left_pos, double right_pos)
   {
      this.posx = posx;
      this.posy = posy;
      this.angle = angle;
      
      this.speed = speed;
      this.left_pos = left_pos;
      this.right_pos = right_pos;
   }
}
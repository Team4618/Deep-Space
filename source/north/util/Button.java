package north.util;

import edu.wpi.first.wpilibj.Joystick;

import java.util.ArrayList;
import java.util.function.Supplier;

public class Button {
   public static ArrayList<Button> buttons = new ArrayList<>();

   boolean wasDown = false;
   public boolean released = false;
   boolean pressBegin = false;
   Supplier<Boolean> down;

   public Button(Joystick joystick, int buttonIndex) {
      this(() -> joystick.getRawButton(buttonIndex));
   }

   public Button(Supplier<Boolean> isDown) {
      this.down = isDown;
      buttons.add(this);
   }

   public boolean isDown() {
      return down.get();
   }

   public void tick() {
      released = wasDown && !isDown();
      pressBegin = !wasDown && isDown();
      wasDown = isDown();
   }

   public void reset() {}

   public static void tickAll() { buttons.forEach(Button::tick); }
   public static void resetAll() { buttons.forEach(Button::reset); }

   public static class IDs {
      //NOTE: buttons for logitech gamepad
      public static final int LOGI_PAD_A = 1;
      public static final int LOGI_PAD_B = 2;
      public static final int LOGI_PAD_X = 3;
      public static final int LOGI_PAD_Y = 4;

      public static final int LOGI_PAD_LB = 5;
      public static final int LOGI_PAD_RB = 6;

      //NOTE: buttons for logitech joystick
      public static final int LOGI_STICK_TRIGGER = 1;
   }
}

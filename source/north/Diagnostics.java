package north;

import java.util.ArrayList;
import java.util.HashMap;

import north.parameters.Parameter;
import north.util.Vector2;

public class Diagnostics {
   public static class StateMessage {
      String text;
      byte type;

      public StateMessage(String _text, byte _type) {
         text = _text;
         type = _type;
      }
   }

   public static class StateMarker {
      String text;
      Vector2 pos;

      public StateMarker(String _text, Vector2 _pos) {
         text = _text;
         pos = _pos;
      }
   }

   public static class StatePath {
      String text;
      Vector2[] path;

      public StatePath(String _text, Vector2[] _path) {
         text = _text;
         path = _path;
      }
   }

   public static class StateDiagnostic {
      byte unit;
      double value;

      public StateDiagnostic(byte _unit, double _value) {
         unit = _unit;
         value = _value;
      }
   }   
}
package north;

import north.util.Vector2;

public class Diagnostics {
   public static class StateMessage {
      public String text;
      public byte type;

      public StateMessage(String _text, byte _type) {
         text = _text;
         type = _type;
      }
   }

   public static class StateMarker {
      public String text;
      public Vector2 pos;

      public StateMarker(String _text, Vector2 _pos) {
         text = _text;
         pos = _pos;
      }
   }

   public static class StatePath {
      public String text;
      public Vector2[] path;

      public StatePath(String _text, Vector2[] _path) {
         text = _text;
         path = _path;
      }
   }

   public static class StateDiagnostic {
      public byte unit;
      public double value;

      public StateDiagnostic(byte _unit, double _value) {
         unit = _unit;
         value = _value;
      }
   }   
}
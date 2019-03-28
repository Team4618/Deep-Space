package north.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import north.util.NorthUtils;

import static north.network.NetworkDefinitions.*;

public class Network {
   public static final double TIMEOUT = 2;
   public static Selector selector;
   public static ArrayList<ConnectedClient> connections = new ArrayList<>();

   public static class ConnectedClient {
      public double last_recv_time;
      public Selector selector;
      public SocketChannel channel;
      public boolean wantsState;
      
      public ConnectedClient(SocketChannel in_channel) {
         channel = in_channel;
         last_recv_time = NorthUtils.getTimestamp();
         
         try {
            selector = Selector.open();
            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_READ);
         } catch(Exception e) { e.printStackTrace(); }
      }

      public void send(ByteBuffer data) {
         System.out.println(data.toString());
         try {
            channel.write(data);
         } catch (IOException e) { e.printStackTrace(); }
      }
   }

   public static void init() {
      try {
         selector = Selector.open();
         ServerSocketChannel channel = ServerSocketChannel.open();
         channel.configureBlocking(false);
         channel.bind(new InetSocketAddress(5800));
         channel.register(selector, SelectionKey.OP_ACCEPT);

         System.out.println("Network.init: Opened server socket " + channel.toString());
      } catch(Exception e) { e.printStackTrace(); }
   }

   public static void send(ByteBuffer data) {
      for(ConnectedClient client : connections) {
         client.send(data);
      }
   }

   public static void HandlePacket(ConnectedClient client, ByteBuffer data, byte type) {
      client.last_recv_time = NorthUtils.getTimestamp();
      
      switch(type) {
         case SetConnectionFlags: {
            
         } break;

         case ParameterOp: {
            
         } break;

         case SetState: {
               //TODO: set state
         } break;

         case UploadAutonomous: {

         } break;

         default:
               System.err.println("Invalid packet type " + type + " received");
      }
   }

   public static ByteBuffer buffer = ByteBuffer.allocate(16384);
   public static void tick() {
      try {
            //Check for incoming connections
            int incoming_connections = selector.selectNow();
            if(incoming_connections != 0) {
               System.out.println(incoming_connections + " incoming connections");

               Set<SelectionKey> readyKeys = selector.selectedKeys();
               Iterator<SelectionKey> iterator = readyKeys.iterator();
               while (iterator.hasNext()) {
                  SelectionKey key = iterator.next();
                  iterator.remove();
   
                  if(key.isAcceptable()) {
                     ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
                     SocketChannel channel = serverChannel.accept();
                     Socket clientSocket = channel.socket();
                     
                     System.out.println(clientSocket.getInetAddress().toString() + " has connected");
                     
                     ConnectedClient client = new ConnectedClient(channel);
                     connections.add(client);
                     client.send(NetworkDefinitions.createWelcomePacket());
                     //TODO: send welcome & currentparams
                  }
               }
            }

            //check last ping times of connected things, remove from list if timed out
            Iterator<ConnectedClient> clients = connections.iterator();
            while(clients.hasNext()) {
               ConnectedClient client = clients.next();
               
               //TODO: check if there are any complete packets recieved from this client
               client.selector.selectNow();
               for(SelectionKey key : client.selector.selectedKeys()) {
                  if(key.isReadable()) {
                     // client.channel.read();
                     // HandlePacket(sender, buffer);
                  }
               }

               if((NorthUtils.getTimestamp() - client.last_recv_time) > TIMEOUT) {
                  System.out.println(client.channel.getLocalAddress().toString() + " has disconnected");
                  clients.remove();
               }
            }

            //send state
            // ByteBuffer statePacket = createStatePacket();
            // send(statePacket);
      } catch (Exception e) { e.printStackTrace(); }
   }
}

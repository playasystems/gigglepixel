package playasystems.gigglepixel;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class GPListenerTask {
  private static DatagramSocket socket = null;
  private static final int GP_MAXLEN = 1000;

  public GPListenerTask(String listenIP, int port) {
    try {
      socket = new DatagramSocket(null);
      socket.setReuseAddress(true);
      socket.setBroadcast(true);
      socket.bind(new InetSocketAddress(port));
      socket.setSoTimeout(1);
    } catch (SocketException e) {
      throw new Error(e);
    }
  }

  public GPPacket loop() throws GPException, IOException {
    byte[] buf = new byte[GP_MAXLEN];
    DatagramPacket packet = new DatagramPacket(buf, GP_MAXLEN);
    try {
      socket.receive(packet);
    } catch (SocketTimeoutException e) {
      return null;
    }
    byte[] minibuf = Arrays.copyOfRange(buf, 0, packet.getLength());
    GPPacket gp = GPPacket.decode(minibuf);
    return gp;
  }
}

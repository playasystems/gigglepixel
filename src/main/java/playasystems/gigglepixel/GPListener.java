package playasystems.gigglepixel;

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

import static java.net.StandardSocketOptions.SO_REUSEPORT;
import static playasystems.gigglepixel.GPUDP.*;

public class GPListener {
  private DatagramSocket socket = null;

  // Passes null, which means "0.0.0.0" or "Listen for packets on all available interfaces"
  public GPListener() throws IOException {
    this(null, GP_DEFAULT_PORT);
  }

  public GPListener(InetAddress listenIP) throws IOException {
    this(listenIP, GP_DEFAULT_PORT);
  }

  public GPListener(InetAddress listenIP, int port) throws IOException {
    // We want to listen on the provided IP+port, but we can't bind it right away.
    // We need to first pass null, then enable reuse, *then* bind.
    SocketAddress sockAddr = new InetSocketAddress(listenIP, port);
    this.socket = new DatagramSocket(null);
    this.socket.setReuseAddress(true);
    socket.setOption(SO_REUSEPORT, true);
    this.socket.bind(sockAddr);
    this.socket.setSoTimeout(1);
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

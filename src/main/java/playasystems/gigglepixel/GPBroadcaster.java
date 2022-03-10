package playasystems.gigglepixel;

import java.io.IOException;
import java.net.*;

import static playasystems.gigglepixel.GPUDP.GP_DEFAULT_PORT;

public class GPBroadcaster {
  private DatagramSocket socket = null;
  private final InetAddress defaultDestinationIP;
  private final int defaultDestinationPort;

  // Set to >= 0 and all packets sent out through this broadcaster will have their
  // source set to this, regardless of the source in the packet
  public int sourceOverride;

  // Default to 255.255.255.255, which means "broadcast everywhere"
  private static InetAddress broadcastAddr() {
    try {
      return InetAddress.getByName("255.255.255.255");
    } catch (UnknownHostException e) {
      throw new Error(e);  // Should never happen
    }
  }

  public GPBroadcaster() throws SocketException {
    this(broadcastAddr(), GP_DEFAULT_PORT);
  }

  public GPBroadcaster(InetAddress destinationIP) throws SocketException {
    this(destinationIP, GP_DEFAULT_PORT);
  }

  public GPBroadcaster(InetAddress defaultDestinationIP, int defaultDestinationPort) throws SocketException {
    // We don't want to bind the socket to any particular IP/port. We want to
    // leave it up to the OS to, at send time, pick the interface IP based on
    // the destination IP, and to assign a random port.
    this.socket = new DatagramSocket(null);
    this.socket.setReuseAddress(true);
    this.socket.setBroadcast(true);
    this.socket.setSoTimeout(1);
    this.defaultDestinationIP = defaultDestinationIP;
    this.defaultDestinationPort = defaultDestinationPort;
    this.sourceOverride = 0;
  }

  public void send(GPPacket packet, InetAddress destinationIP, int destinationPort) throws IOException {
    byte[] bytes = packet.encode(this.sourceOverride);
    DatagramPacket dgp = new DatagramPacket(bytes, bytes.length, destinationIP, destinationPort);
    this.socket.send(dgp);
  }

  public void send(GPPacket packet) throws IOException {
    this.send(packet, this.defaultDestinationIP, this.defaultDestinationPort);
  }
}

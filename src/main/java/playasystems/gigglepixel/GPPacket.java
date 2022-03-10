package playasystems.gigglepixel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

public abstract class GPPacket {
  private static Map<Integer, Class<? extends GPPacket>> typesByInt = Map.of(
          1, GPPalettePacket.class,
          3, GPIdentificationPacket.class
  );
  private static Map<Class<? extends GPPacket>, Integer> intsByType =
          typesByInt.entrySet().stream().collect(Collectors.toMap(
                  Map.Entry::getValue, Map.Entry::getKey));

  private static final byte[] MAGIC = "GLPX".getBytes();
  private static final int LATEST_VERSION = 2;
  public int version;
  public int priority;
  public int flags;
  public int source;
  public int dest;
  public boolean hasCrc;

  protected GPPacket() {
    this.version = LATEST_VERSION;
    this.priority = 0;
    this.flags = 0;
    this.source = 0;
    this.dest = 0;
    this.hasCrc = false;
  }

  public static GPPacket decode(byte[] encoded) throws GPException {
    if (encoded.length < 14) throw new GPException("Header too short");

    for (int i = 0; i < 4; i++)
      if (encoded[i] != MAGIC[i])
        throw new GPException("Bad magic number");

    int version = unpack8(encoded[4]);

    // Go ahead and try to parse packets from the future
    //if (version > LATEST_VERSION) throw new GPException("Version from the future");

    int length = unpack16(encoded[5], encoded[6]);

    if (length < 14) throw new GPException("Length too short");

    int type = unpack8(encoded[7]);

    Class<? extends GPPacket> typeClass = typesByInt.get(type);
    if (typeClass == null) throw new GPException("Unknown packet type");

    GPPacket packet;

    try {
      Constructor<? extends GPPacket> cons = typeClass.getConstructor();
      packet = cons.newInstance();
    } catch (NoSuchMethodException | InstantiationException |
            IllegalAccessException | InvocationTargetException e) {
      throw new Error(e);
    }

    packet.version = version;
    packet.priority = unpack8(encoded[8]);
    packet.flags = unpack8(encoded[9]);
    packet.source = unpack16(encoded[10], encoded[11]);
    packet.dest = unpack16(encoded[12], encoded[13]);

    if (encoded.length == length) {
      packet.hasCrc = false;
    } else if (encoded.length == length + 2) {
      packet.hasCrc = true;
      byte[] sansPayload = Arrays.copyOfRange(encoded, 0, length);
      int calcCrc = GPCRC.forBytes(sansPayload);
      int foundCrc = unpack16(encoded[length], encoded[length + 1]);
      if (calcCrc != foundCrc) throw new GPException("Bad CRC");
    } else {
      throw new GPException("Length mismatch");
    }

    packet.decodePayload(Arrays.copyOfRange(encoded, 14, length));

    return packet;
  }

  protected static int unpack8(byte b) {
    int rv = b;
    if (rv < 0) rv += 256;
    return rv;
  }

  protected static int unpack16(byte b0, byte b1) {
    return unpack8(b0) << 8 | unpack8(b1);
  }

  public abstract void decodePayload(byte[] buf) throws GPException;

  protected static byte pack8(int i) {
    assert i >= 0;
    assert i <= 255;
    if (i > 127) i -= 256;
    return (byte) i;
  }

  protected static void pack16(ByteArrayOutputStream bytes, int i) {
    assert i >= 0;
    assert i < 65536;
    byte b0 = pack8(i / 256);
    byte b1 = pack8(i % 256);
    bytes.write(b0);
    bytes.write(b1);
  }

  public byte[] encode() {
    return this.encode(-1);
  }

  public byte[] encode(int sourceOverride) {
    try {
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      bytes.write(MAGIC);
      bytes.write(this.version);

      // Length; will be overwritten later
      bytes.write(0);
      bytes.write(0);
      Integer typeInt = intsByType.get(this.getClass());
      if (typeInt == null) {
        throw new Error("Internal error");
      }
      bytes.write(typeInt);
      bytes.write(this.priority);
      bytes.write(this.flags);

      if (sourceOverride < 0) {
        pack16(bytes, this.source);
      } else {
        pack16(bytes, sourceOverride);
      }
      pack16(bytes, this.dest);

      this.encodePayload(bytes);

      byte[] rv = bytes.toByteArray();

      // Overwrite length, now that we know it
      int length = rv.length;
      assert length < 65536;
      byte lengthHi = pack8(length / 256);
      byte lengthLo = pack8(length % 256);
      rv[5] = lengthHi;
      rv[6] = lengthLo;

      if (this.hasCrc) {
        int crc = GPCRC.forBytes(rv);
        pack16(bytes, crc);
        rv = bytes.toByteArray();
        rv[5] = lengthHi;
        rv[6] = lengthLo;
      }

      return rv;
    } catch (IOException e) {
      throw new Error(e);
    }
  }

  public abstract void encodePayload(ByteArrayOutputStream bytes) throws IOException;
}
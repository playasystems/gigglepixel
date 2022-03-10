package playasystems.gigglepixel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GPIdentificationPacket extends GPPacket {
  public static final int NAME_LENGTH = 30;
  public String name;

  // Used by the decoder, which calls decodePayload(), which sets the name
  public GPIdentificationPacket() {
    this(null);
  }

  public GPIdentificationPacket(String name) {
    assert name == null || name.length() <= NAME_LENGTH;
    this.name = name;
  }

  @Override
  public void decodePayload(byte[] payload) throws GPException {
    if (payload.length < NAME_LENGTH) throw new GPException("Short name");
    byte[] nameBytes = Arrays.copyOfRange(payload, 0, NAME_LENGTH);
    this.name = new String(nameBytes, StandardCharsets.UTF_8);
    this.name = this.name.replace("\0", "");
    // There may be more added to the packet in the future, so to be future-proof,
    // we're going to ignore any trailing bytes.
  }

  @Override
  public void encodePayload(ByteArrayOutputStream bytes) throws IOException {
    assert this.name.length() <= NAME_LENGTH : "Name too long";
    byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
    int padding = NAME_LENGTH - nameBytes.length;
    assert padding >= 0;
    bytes.write(nameBytes);
    for (int i = 0; i < padding; i++) {
      bytes.write(0);
    }
  }
}

package playasystems.gigglepixel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class GPPalettePacket extends GPPacket {
  public List<GPColor> entries;

  // Used by the decoder, which calls decodePayload(), which sets entries
  public GPPalettePacket() {
    this(null);
  }

  public GPPalettePacket(List<GPColor> entries) {
    super();
    this.entries = entries;
  }

  @Override
  public void decodePayload(byte[] payload) throws GPException {
    if (payload.length < 2) throw new GPException("Short palette header");
    int numEntries = unpack16(payload[0], payload[1]);
    if (payload.length != 2 + 4 * numEntries)
      throw new GPException("Palette length mismatch");
    this.entries = new ArrayList<>();

    for (int i = 0; i < numEntries; i++) {
      int startIndex = 2 + 4 * i;
      int frac = unpack8(payload[startIndex]);
      int r = unpack8(payload[startIndex+1]);
      int g = unpack8(payload[startIndex+2]);
      int b = unpack8(payload[startIndex+3]);
      GPColor gpc = new GPColor(r, g, b, frac);
      this.entries.add(gpc);
    }
  }

  @Override
  public void encodePayload(ByteArrayOutputStream bytes) throws IOException {
    pack16(bytes, this.entries.size());
    for (GPColor color : this.entries) {
      bytes.write(pack8(color.frac));
      bytes.write(pack8(color.r));
      bytes.write(pack8(color.g));
      bytes.write(pack8(color.b));
    }
  }
}
package playasystems.gigglepixel;

public class GPCRC {
  public int value;

  public GPCRC() {
    this.value = 0xFFFF;
  }

  public void update(byte curByte) {
    this.value ^= curByte << 8;
    for (int i = 0; i < 8; i++) {
      boolean hiSet = (this.value & 0x8000) > 0;
      this.value <<= 1;
      if (hiSet) this.value ^= 0x1021;
    }
    this.value &= 0xffff;
  }

  public static int forBytes(byte[] bytes) {
    GPCRC crc = new GPCRC();
    for (byte b : bytes) {
      crc.update(b);
    }
    return crc.value;
  }
}


package playasystems.gigglepixel;

public class GPCRC {
  public int value;

  public GPCRC() {
    this.value = 0;
  }

  public void update(byte curByte) {
    int a, b;

    a = curByte;
    for (int count = 7; count >= 0; count--) {
      a = a << 1;
      b = (a >>> 8) & 1;
      if ((this.value & 0x8000) != 0) {
        this.value = ((this.value << 1) + b) ^ 0x1021;
      } else {
        this.value = (this.value << 1) + b;
      }
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


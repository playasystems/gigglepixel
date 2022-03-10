package playasystems.gigglepixel;

public class GPColor {
  public int r;
  public int g;
  public int b;
  public int frac;  // Fraction of 256

  public GPColor(int r, int g, int b, int frac) {
    assert r >= 0;
    assert r <= 255;
    assert g >= 0;
    assert g <= 255;
    assert b >= 0;
    assert b <= 255;
    assert frac >= 0;
    assert frac <= 255;

    this.r = r;
    this.g = g;
    this.b = b;
    this.frac = frac;
  }

  public String repr() {
    return r + "," + g + "," + b + " frac";
  }
}

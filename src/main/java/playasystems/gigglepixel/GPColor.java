package playasystems.gigglepixel;

public class GPColor {
  public int r;
  public int g;
  public int b;
  public int frac;  // Fraction of 256

  public GPColor(int r, int g, int b, int frac) {
    this.r = r;
    this.g = g;
    this.b = b;
    this.frac = frac;
  }

  public String repr() {
    return r + "," + g + "," + b + " frac";
  }
}

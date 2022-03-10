package playasystems.gigglepixel;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static playasystems.gigglepixel.GPPacket.*;

class GigglePixelTest {
  @Test
  public void Crc() {
    byte[] empty = {};
    assertEquals(65535, GPCRC.forBytes(empty));

    byte[] justZero = {0};
    assertEquals(57840, GPCRC.forBytes(justZero));

    byte[] justOne = {1};
    assertEquals(61905, GPCRC.forBytes(justOne));

    byte[] oneTwoThree = {1, 2, 3};
    assertEquals(44461, GPCRC.forBytes(oneTwoThree));

    byte[] three255s = {-1, -1, -1};
    assertEquals(7920, GPCRC.forBytes(three255s));
  }

  @Test
  public void Pack() {
    assertEquals(0, pack8(0));
    assertEquals(1, pack8(1));
    assertEquals(127, pack8(127));
    assertEquals(-128, pack8(128));
    assertEquals(-1, pack8(255));
  }

  @Test
  public void Unpack() {
    assertEquals(0, unpack8((byte) 0));
    assertEquals(1, unpack8((byte) 1));
    assertEquals(127, unpack8((byte) 127));
    assertEquals(128, unpack8((byte) -128));
    assertEquals(255, unpack8((byte) -1));
  }

  private static GPPalettePacket makePalettePacket() {
    List<GPColor> entries = new ArrayList<>();

    entries.add(new GPColor(255, 0, 0, 1));
    entries.add(new GPColor(0, 180, 0, 1));
    entries.add(new GPColor(0, 0, 10, 1));

    return new GPPalettePacket(entries);
  }

  @Test
  public void EncodePaletteWithoutCrc() {
    GPPalettePacket packet = makePalettePacket();
    byte[] bytes = packet.encode();

    String actual = Base64.getEncoder().encodeToString(bytes);
    String expected = "R0xQWAIAHAEAAAAAAAAAAwH/AAABALQAAQAACg==";
    assertEquals(expected, actual);
  }

  @Test
  public void EncodePaletteWithCrc() {
    GPPalettePacket packet = makePalettePacket();
    packet.hasCrc = true;
    byte[] bytes = packet.encode();
    String actual = Base64.getEncoder().encodeToString(bytes);
    String expected = "R0xQWAIAHAEAAAAAAAAAAwH/AAABALQAAQAACmRd";
    assertEquals(expected, actual);
  }

  @Test
  public void DecodePaletteWithCrc() {
    byte[] bytes = Base64.getDecoder().decode("R0xQWAIAHAEAAAAAAAAAAwH/AAABALQAAQAACmRd");
    GPPacket packet = null;
    try {
      packet = GPPacket.decode(bytes);
    } catch (GPException e) {
      fail();
    }
    assertTrue(packet.hasCrc);
    assertTrue(packet instanceof GPPalettePacket);
    GPPalettePacket palettePacket = (GPPalettePacket) packet;
    assertEquals(3, palettePacket.entries.size());
    assertEquals(255, palettePacket.entries.get(0).r);
    assertEquals(0, palettePacket.entries.get(0).g);
    assertEquals(0, palettePacket.entries.get(0).b);
    assertEquals(180, palettePacket.entries.get(1).g);
  }

  @Test
  public void DecodePaletteWithBadCrc() {
    byte[] bytes = Base64.getDecoder().decode("R0xQWAIAHAEAAAAAAAAAAwH/AAABALQAAQAACmRe");
    GPPacket packet = null;
    try {
      packet = GPPacket.decode(bytes);
    } catch (GPException e) {
      assertEquals("Bad CRC", e.getMessage());
      return;
    } catch (Exception e) {
      fail();
    }
    fail();
  }


  @Test
  public void EncodeIdentification() {
    GPIdentificationPacket packet = new GPIdentificationPacket("Aztec Spleen");
    byte[] bytes = packet.encode();

    String actual = Base64.getEncoder().encodeToString(bytes);
    String expected = "R0xQWAIALAMAAAAAAABBenRlYyBTcGxlZW4AAAAAAAAAAAAAAAAAAAAAAAA=";
    assertEquals(expected, actual);
  }

  @Test
  public void DecodeIdentification() {
    byte[] bytes = Base64.getDecoder().decode(
            "R0xQWAIALAMAAAAAAABBenRlYyBTcGxlZW4AAAAAAAAAAAAAAAAAAAAAAAA=");

    GPPacket packet = null;
    try {
      packet = GPPacket.decode(bytes);
    } catch (GPException e) {
      fail();
    }
    assertTrue(packet instanceof GPIdentificationPacket);
    GPIdentificationPacket idPacket = (GPIdentificationPacket) packet;
    assertEquals("Aztec Spleen", idPacket.name);
  }

}
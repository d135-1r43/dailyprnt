package com.dailyprnt.image;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DitherTest
{
	private static final int STRIP_WIDTH = 384;

	@Test
	void shouldProduceOnlyBlackAndWhitePixels() throws IOException
	{
		// given a smooth grey gradient, which has no pure black or white to copy
		byte[] gradient = gradient(800, 400);

		// when
		BufferedImage dithered = read(Dither.toPng(gradient, STRIP_WIDTH));

		// then every pixel is one of the two tones a thermal printer can put down
		Set<Integer> tones = new HashSet<>();
		for (int y = 0; y < dithered.getHeight(); y++)
		{
			for (int x = 0; x < dithered.getWidth(); x++)
			{
				tones.add(dithered.getRGB(x, y) & 0xffffff);
			}
		}
		assertEquals(Set.of(0x000000, 0xffffff), tones, "dithered output must be pure 1-bit");
	}

	@Test
	void shouldScaleToTheStripWidthKeepingAspectRatio()
	{
		// given an image twice as wide as it is tall
		byte[] source = gradient(800, 400);

		// when
		BufferedImage dithered = read(Dither.toPng(source, STRIP_WIDTH));

		// then
		assertEquals(STRIP_WIDTH, dithered.getWidth());
		assertEquals(STRIP_WIDTH / 2, dithered.getHeight());
	}

	@Test
	void shouldCarryTheGradientAcrossAsVaryingDotDensity()
	{
		// given a gradient running from black on the left to white on the right
		byte[] gradient = gradient(800, 400);

		// when
		BufferedImage dithered = read(Dither.toPng(gradient, STRIP_WIDTH));

		// then the dark end keeps far more ink than the light end
		assertTrue(blackRatio(dithered, 0, STRIP_WIDTH / 4) > 0.8,
				"the dark end should be nearly solid");
		assertTrue(blackRatio(dithered, STRIP_WIDTH * 3 / 4, STRIP_WIDTH) < 0.2,
				"the light end should be nearly bare");
	}

	@Test
	void shouldEmitADataUriAnImgTagCanUse()
	{
		// when
		String uri = Dither.toDataUri(gradient(100, 100), STRIP_WIDTH);

		// then
		assertTrue(uri.startsWith("data:image/png;base64,"));
		assertTrue(uri.length() > "data:image/png;base64,".length());
	}

	@Test
	void shouldRejectSomethingThatIsNotAnImage()
	{
		// given
		byte[] notAnImage = "certainly not a png".getBytes();

		// when & then
		assertThrows(IllegalArgumentException.class, () -> Dither.toPng(notAnImage, STRIP_WIDTH));
	}

	private static double blackRatio(BufferedImage image, int fromX, int toX)
	{
		int black = 0;
		int total = 0;
		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = fromX; x < toX; x++)
			{
				total++;
				if ((image.getRGB(x, y) & 0xffffff) == 0)
				{
					black++;
				}
			}
		}
		return black / (double) total;
	}

	private static byte[] gradient(int width, int height)
	{
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = image.createGraphics();
		for (int x = 0; x < width; x++)
		{
			int tone = Math.round(255f * x / (width - 1));
			graphics.setColor(new Color(tone, tone, tone));
			graphics.drawLine(x, 0, x, height);
		}
		graphics.dispose();
		return png(image);
	}

	private static byte[] png(BufferedImage image)
	{
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(image, "png", out);
			return out.toByteArray();
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}

	private static BufferedImage read(byte[] png)
	{
		try
		{
			return ImageIO.read(new ByteArrayInputStream(png));
		}
		catch (IOException e)
		{
			throw new IllegalStateException(e);
		}
	}
}

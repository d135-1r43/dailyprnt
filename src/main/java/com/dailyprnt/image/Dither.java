package com.dailyprnt.image;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Base64;

/**
 * Reduces an image to the 1-bit black and white a thermal printer can actually put on
 * paper, using Atkinson diffusion.
 *
 * <p>Atkinson passes on only six eighths of each pixel's error instead of all of it, so
 * highlights stay white rather than silting up into grey. On thermal paper that reads as
 * crisp; full diffusion muddies the empty areas and burns ink for no detail.
 */
public final class Dither
{
	/** Error weights as (dx, dy), each carrying one eighth of the error. */
	private static final int[][] ATKINSON = {{1, 0}, {2, 0}, {-1, 1}, {0, 1}, {1, 1}, {0, 2}};

	private static final float DIVISOR = 8f;
	private static final float THRESHOLD = 0.5f;

	private Dither()
	{
	}

	/**
	 * Dithers a PNG or JPEG to the given width and returns it as a {@code data:} URI ready
	 * to drop into an {@code <img>} tag.
	 */
	public static String toDataUri(byte[] image, int width)
	{
		return "data:image/png;base64," + Base64.getEncoder().encodeToString(toPng(image, width));
	}

	static byte[] toPng(byte[] image, int width)
	{
		try
		{
			BufferedImage source = ImageIO.read(new ByteArrayInputStream(image));
			if (source == null)
			{
				throw new IllegalArgumentException("Not a readable image");
			}
			BufferedImage dithered = dither(grayscale(source, width), width);

			ByteArrayOutputStream png = new ByteArrayOutputStream();
			ImageIO.write(dithered, "png", png);
			return png.toByteArray();
		}
		catch (IOException e)
		{
			throw new UncheckedIOException("Could not dither image", e);
		}
	}

	/** Scales to the strip width and flattens to luminance in the range 0..1. */
	private static float[] grayscale(BufferedImage source, int width)
	{
		int height = Math.max(1, Math.round(source.getHeight() * (width / (float) source.getWidth())));
		BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = scaled.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		graphics.drawImage(source, 0, 0, width, height, null);
		graphics.dispose();

		float[] luminance = new float[width * height];
		for (int i = 0; i < luminance.length; i++)
		{
			int rgb = scaled.getRGB(i % width, i / width);
			float value = (0.299f * ((rgb >> 16) & 0xff)
					+ 0.587f * ((rgb >> 8) & 0xff)
					+ 0.114f * (rgb & 0xff)) / 255f;
			luminance[i] = clamp(value);
		}
		return luminance;
	}

	private static BufferedImage dither(float[] pixels, int width)
	{
		int height = pixels.length / width;
		BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_BINARY);

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				int i = y * width + x;
				float before = pixels[i];
				float after = before < THRESHOLD ? 0f : 1f;
				pixels[i] = after;
				out.setRGB(x, y, after == 0f ? 0x000000 : 0xffffff);

				float error = (before - after) / DIVISOR;
				for (int[] offset : ATKINSON)
				{
					int nx = x + offset[0];
					int ny = y + offset[1];
					if (nx >= 0 && nx < width && ny < height)
					{
						int n = ny * width + nx;
						pixels[n] = clamp(pixels[n] + error);
					}
				}
			}
		}
		return out;
	}

	private static float clamp(float value)
	{
		return value < 0f ? 0f : Math.min(value, 1f);
	}
}

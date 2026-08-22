package com.dailyprnt.modules.woodcut;

import com.dailyprnt.image.Dither;
import com.dailyprnt.modules.Module;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.Base64;
import java.util.List;

/**
 * A daily illustration, generated as a woodcut and dithered for the printer.
 *
 * <p>The style is not decoration: a carved print is already pure black on white, built
 * from lines and hatching, so it survives the reduction to 1-bit almost untouched.
 * Photographic or painted images turn to mud at this width.
 */
@ApplicationScoped
@CheckedTemplate(basePath = "com/dailyprnt/modules/woodcut")
public class WoodcutModule implements Module
{
	/** The strip is 384 dots wide, and the illustration runs the full width of it. */
	private static final int STRIP_WIDTH = 384;

	private static final String STYLE = """
			A woodcut print in the style of a 19th century book illustration: %s. \
			Pure black ink on white paper, no grey, no colour, no gradients. Bold \
			confident carved lines, dense parallel hatching for shadow, large areas of \
			clean untouched white. Flat, graphic, high contrast. No text, no border, \
			no frame.""";

	public static native TemplateInstance module(String title, String image);

	@Inject
	WoodcutSubjectAiService subjects;

	@Inject
	@RestClient
	OpenAiImagesClient images;

	@ConfigProperty(name = "dailyprnt.woodcut.theme")
	String theme;

	@ConfigProperty(name = "dailyprnt.woodcut.model")
	String model;

	@ConfigProperty(name = "dailyprnt.woodcut.size")
	String size;

	@ConfigProperty(name = "dailyprnt.woodcut.quality")
	String quality;

	@Override
	public String id()
	{
		return "woodcut";
	}

	@Override
	public String title()
	{
		return "Today's Cut";
	}

	@Override
	public String render()
	{
		WoodcutSubject subject = subjects.propose(theme);
		byte[] generated = generate(STYLE.formatted(subject.scene()));
		return module(subject.title(), Dither.toDataUri(generated, STRIP_WIDTH)).render();
	}

	private byte[] generate(String prompt)
	{
		List<GeneratedImages.Item> items = images.generate(
				new ImageRequest(model, prompt, size, quality, 1)).data();
		if (items == null || items.isEmpty())
		{
			throw new IllegalStateException("Image generation returned nothing");
		}
		return Base64.getDecoder().decode(items.get(0).b64Json());
	}
}

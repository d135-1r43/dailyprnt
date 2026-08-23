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

	/**
	 * Asking for an isolated subject on a blank page is what produces the margin. Left to
	 * itself the model fills the canvas edge to edge with hatched sky and ground, which
	 * both crops the subject and roughly triples the ink a thermal head has to lay down.
	 */
	private static final String STYLE = """
			A woodcut print in the style of a 19th century natural history plate: %s. \
			A single isolated specimen on a completely blank white page, small and \
			centred, taking up about half the height of the picture, with wide empty \
			white space all around it and especially at the edges. No scenery, no \
			background, no ground line, no sky, no filled areas behind the subject. \
			Pure black ink on white paper, no grey, no colour, no gradients. Bold \
			confident carved lines with restrained hatching on the subject itself. \
			Flat, graphic, high contrast. No text. No drawn border, frame line or box.""";

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

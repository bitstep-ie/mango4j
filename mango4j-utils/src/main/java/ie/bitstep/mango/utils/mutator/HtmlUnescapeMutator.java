package ie.bitstep.mango.utils.mutator;

import org.apache.commons.text.StringEscapeUtils;

import java.lang.annotation.Annotation;

public class HtmlUnescapeMutator implements ValueMutator {
	/**
	 * Unescapes HTML in the supplied value.
	 *
	 * @param a the annotation triggering this mutator
	 * @param in the input value
	 * @return the unescaped value
	 */
	@Override
	public Object process(Annotation a, Object in) {
		return StringEscapeUtils.unescapeHtml4(in.toString());
	}
}

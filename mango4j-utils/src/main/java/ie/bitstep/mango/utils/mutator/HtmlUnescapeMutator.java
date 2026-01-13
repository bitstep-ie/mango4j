package ie.bitstep.mango.utils.mutator;

import org.apache.commons.text.StringEscapeUtils;

import java.lang.annotation.Annotation;

public class HtmlUnescapeMutator implements ValueMutator {
	@Override
	public Object process(Annotation a, Object in) {
		return StringEscapeUtils.unescapeHtml4(in.toString());
	}
}

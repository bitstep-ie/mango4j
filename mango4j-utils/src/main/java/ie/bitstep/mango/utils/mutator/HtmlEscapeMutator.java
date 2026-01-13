package ie.bitstep.mango.utils.mutator;

import org.apache.commons.text.StringEscapeUtils;

import java.lang.annotation.Annotation;

public class HtmlEscapeMutator implements ValueMutator {
	@Override
	public Object process(Annotation a, Object in) {
		return StringEscapeUtils.escapeHtml4(in.toString());
	}
}

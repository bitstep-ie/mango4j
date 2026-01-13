package ie.bitstep.mango.utils.mutator;

import ie.bitstep.mango.utils.mutator.exceptions.MutatorException;
import ie.bitstep.mango.utils.mutator.exceptions.MutatorUnsupportedTypeException;
import ie.bitstep.mango.utils.test.data.ObjectMutatorTestDataPrivate;
import ie.bitstep.mango.utils.mutator.annotations.Text;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ObjectMutatorTest {
	public static final String HTML_ESCAPED_TEXT = "&lt;script&gt;Hello&lt;/script&gt;";
	public static final String HTML_PARTLY_ESCAPED_TEXT = "&lt;script>Hello</script&gt;";
	public static final String HTML_TEXT = "<script>Hello</script>";
	public static final String PLAIN_TEXT = "Hello";

	enum Enum {
		ONE
	}

	public class Child {
		@Text
		public String text;

		@Text
		public ObjectMutatorTest objectMutatorTest;

		public Child(String text) {
			this.text = text;
		}

		public Child(String text, ObjectMutatorTest objectMutatorTest) {
			this.text = text;
			this.objectMutatorTest = objectMutatorTest;
		}
	}


	@Text
	private String nullString = null;

	@Text
	private Child nullChild = null;

	@Text
	private String text;

	private Map<String, Child> map = new LinkedHashMap<>();

	@Text
	private Map<String, String> primitiveMap = new LinkedHashMap<>();

	private List<Child> list = new ArrayList<>();

	@Text
	private List<String> primitiveList = new ArrayList<>();

	@Text
	byte[] ba = null;

	@Text
	String[] sa = null;

	private String notAnnotatedString = "not annotated";
	private Number notAnnotatedNumber = 200;
	private Boolean notAnnotatedBoolean = true;
	private Enum notAnnotatedEnumeration = Enum.ONE;


	private Child child = new Child("hello", this);

	public String getNullString() {
		return nullString;
	}

	public void setNullString(String nullString) {
		this.nullString = nullString;
	}

	public Child getNullChild() {
		return nullChild;
	}

	public void setNullChild(Child nullChild) {
		this.nullChild = nullChild;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Map<String, Child> getMap() {
		return map;
	}

	public void setMap(Map<String, Child> map) {
		this.map = map;
	}

	public Map<String, String> getPrimitiveMap() {
		return primitiveMap;
	}

	public void setPrimitiveMap(Map<String, String> primitiveMap) {
		this.primitiveMap = primitiveMap;
	}

	public List<Child> getList() {
		return list;
	}

	public void setList(List<Child> list) {
		this.list = list;
	}

	public List<String> getPrimitiveList() {
		return primitiveList;
	}

	public void setPrimitiveList(List<String> primitiveList) {
		this.primitiveList = primitiveList;
	}

	public byte[] getBa() {
		return ba;
	}

	public void setBa(byte[] ba) {
		this.ba = ba;
	}

	public String[] getSa() {
		return sa;
	}

	public void setSa(String[] sa) {
		this.sa = sa;
	}

	public String getNotAnnotatedString() {
		return notAnnotatedString;
	}

	public void setNotAnnotatedString(String notAnnotatedString) {
		this.notAnnotatedString = notAnnotatedString;
	}

	public Number getNotAnnotatedNumber() {
		return notAnnotatedNumber;
	}

	public void setNotAnnotatedNumber(Number notAnnotatedNumber) {
		this.notAnnotatedNumber = notAnnotatedNumber;
	}

	public Boolean getNotAnnotatedBoolean() {
		return notAnnotatedBoolean;
	}

	public void setNotAnnotatedBoolean(Boolean notAnnotatedBoolean) {
		this.notAnnotatedBoolean = notAnnotatedBoolean;
	}

	public Enum getNotAnnotatedEnumeration() {
		return notAnnotatedEnumeration;
	}

	public void setNotAnnotatedEnumeration(Enum enumeration) {
		this.notAnnotatedEnumeration = enumeration;
	}

	public Child getChild() {
		return child;
	}

	public void setChild(Child child) {
		this.child = child;
	}

	@Test
	void testMutateFieldException() throws MutatorException {
		ObjectMutatorTestDataPrivate testData = new ObjectMutatorTestDataPrivate();
		ObjectMutator objectMutator = new ObjectMutator()
			.on(Text.class, new HtmlEscapeMutator());

		MutatorException exception = assertThrows(MutatorException.class, () -> objectMutator.mutate(testData));

		assertThat(exception.getLocalizedMessage()).isEqualTo("java.lang.IllegalAccessException: class ie.bitstep.mango.reflection.accessors.PropertyAccessor cannot access a member of class ie.bitstep.mango.utils.test.data.ObjectMutatorTestDataPrivate with modifiers \"private\"");
		assertThat(exception.getCause()).isInstanceOf(IllegalAccessException.class);
	}

	@Test
	void testNoRegisteredMutators() throws MutatorException {
		text = HTML_TEXT;
		child.text = HTML_TEXT;
		map.put("msg", new Child(HTML_TEXT));
		map.put("msg2", null);
		primitiveMap.put("msg", HTML_TEXT);
		list.add(new Child(HTML_TEXT));
		primitiveList.add(HTML_TEXT);
		primitiveList.add(PLAIN_TEXT);

		ObjectMutator objectMutator = new ObjectMutator();

		objectMutator.mutate(this);

		assertThat(text).isEqualTo(HTML_TEXT);
		assertThat(child.text).isEqualTo(HTML_TEXT);
		assertThat(primitiveMap).containsEntry("msg", HTML_TEXT);
		assertThat(map.get("msg").text).isEqualTo(HTML_TEXT);
		assertThat(map.get("msg2")).isNull();
		assertThat(list.get(0).text).isEqualTo(HTML_TEXT);
		assertThat(primitiveList.get(0)).isEqualTo(HTML_TEXT);
		assertThat(primitiveList.get(1)).isEqualTo(PLAIN_TEXT);
	}

	@Test
	void testHtmlEscape() throws MutatorException {
		text = HTML_TEXT;
		child.text = HTML_TEXT;
		map.put("msg", new Child(HTML_TEXT));
		map.put("msg2", null);
		primitiveMap.put("msg", HTML_TEXT);
		list.add(new Child(HTML_TEXT));
		primitiveList.add(HTML_TEXT);
		primitiveList.add(PLAIN_TEXT);

		ObjectMutator objectMutator = new ObjectMutator()
			.on(Text.class, new HtmlEscapeMutator());

		objectMutator.mutate(this);

		assertThat(text).isEqualTo(HTML_ESCAPED_TEXT);
		assertThat(child.text).isEqualTo(HTML_ESCAPED_TEXT);
		assertThat(primitiveMap).containsEntry("msg", HTML_ESCAPED_TEXT);
		assertThat(map.get("msg").text).isEqualTo(HTML_ESCAPED_TEXT);
		assertThat(map.get("msg2")).isNull();
		assertThat(list.get(0).text).isEqualTo(HTML_ESCAPED_TEXT);
		assertThat(primitiveList.get(0)).isEqualTo(HTML_ESCAPED_TEXT);
		assertThat(primitiveList.get(1)).isEqualTo(PLAIN_TEXT);
	}

	@Test
	void testHtmlUnescape() throws MutatorException {
		text = HTML_ESCAPED_TEXT;
		child.text = HTML_ESCAPED_TEXT;
		map.put("msg", new Child(HTML_ESCAPED_TEXT));
		list.add(new Child(HTML_ESCAPED_TEXT));

		ObjectMutator objectMutator = new ObjectMutator()
			.on(Text.class, new HtmlUnescapeMutator());

		objectMutator.mutate(this);

		assertThat(text).isEqualTo(HTML_TEXT);
		assertThat(child.text).isEqualTo(HTML_TEXT);
		assertThat(map.get("msg").text).isEqualTo(HTML_TEXT);
		assertThat(list.get(0).text).isEqualTo(HTML_TEXT);
	}

	@Test
	void testHtmlPartlyEscapedUnescapeEscape() throws MutatorException {
		text = HTML_PARTLY_ESCAPED_TEXT;
		child.text = HTML_PARTLY_ESCAPED_TEXT;
		map.put("msg", new Child(HTML_PARTLY_ESCAPED_TEXT));
		list.add(new Child(HTML_PARTLY_ESCAPED_TEXT));

		ObjectMutator objectMutator = new ObjectMutator()
			.on(Text.class, new HtmlUnescapeMutator())
			.on(Text.class, new HtmlEscapeMutator());

		objectMutator.mutate(this);

		assertThat(text).isEqualTo(HTML_ESCAPED_TEXT);
		assertThat(child.text).isEqualTo(HTML_ESCAPED_TEXT);
		assertThat(map.get("msg").text).isEqualTo(HTML_ESCAPED_TEXT);
		assertThat(list.get(0).text).isEqualTo(HTML_ESCAPED_TEXT);
	}

	@Test
	void testProcessByteArraySubObject() throws NoSuchFieldException {
		ObjectMutator objectMutator = new ObjectMutator()
			.on(Text.class, new HtmlUnescapeMutator())
			.on(Text.class, new HtmlEscapeMutator());
		Field field = this.getClass().getDeclaredField("ba");
		ba = new byte[]{(byte) 0, (byte) 0, (byte) 2};

		MutatorUnsupportedTypeException thrown = assertThrows(MutatorUnsupportedTypeException.class, () -> objectMutator.mutateObject(this, field, ba));

		assertThat(thrown.getMessage()).isEqualTo("ba, unsupported type byte[]");
	}

	@Test
	void testProcessArraySubObject() throws NoSuchFieldException {
		ObjectMutator objectMutator = new ObjectMutator()
			.on(Text.class, new HtmlUnescapeMutator())
			.on(Text.class, new HtmlEscapeMutator());
		Field field = this.getClass().getDeclaredField("sa");
		sa = new String[]{""};

		MutatorUnsupportedTypeException thrown = assertThrows(MutatorUnsupportedTypeException.class, () -> objectMutator.mutateObject(this, field, sa));

		assertThat(thrown.getMessage()).isEqualTo("sa, unsupported type java.lang.String[]");
	}
}

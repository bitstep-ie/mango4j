package ie.bitstep.mango.reflection.accessors;

import ie.bitstep.mango.reflection.annotations.Mask;
import ie.bitstep.mango.reflection.annotations.MaskCard;
import ie.bitstep.mango.reflection.annotations.MaskUID;
import ie.bitstep.mango.reflection.annotations.Modifier;
import ie.bitstep.mango.reflection.classes.Base;
import ie.bitstep.mango.reflection.classes.Derived;
import ie.bitstep.mango.reflection.utils.AccessorsPrivateField;
import ie.bitstep.mango.reflection.utils.BadGetterAccessorPrivateField;
import ie.bitstep.mango.reflection.utils.BadSetterAccessorPrivateField;
import ie.bitstep.mango.reflection.utils.NoAccessorsPrivateField;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertyAccessorTest {
	@MaskCard
	@MaskUID
	@Mask
	@Deprecated
	public String name = "Hello";
	private String privateName = "Goodbye";
	private String privateNameAnnotatedMethods = "Goodbye";

	private boolean bindable;
	private int i = 20;

	private UUID uuid = UUID.randomUUID();

	public String getPrivateName() {
		return privateName;
	}

	public void setPrivateName(String privateName) {
		this.privateName = privateName;
	}

	@PropertyGetter("privateNameAnnotatedMethods")
	public String getPrivateNameAnnotatedMethod() {
		return privateNameAnnotatedMethods;
	}

	@PropertySetter("privateNameAnnotatedMethods")
	public void setPrivateNameAnnotatedMethod(String privateNameAnnotatedMethods) {
		this.privateNameAnnotatedMethods = privateNameAnnotatedMethods;
	}

	public void setI(int i) {
		this.i = i;
	}

	public int getI() {
		return i;
	}

	public boolean isBindable() {
		return bindable;
	}

	public void setBindable(boolean bindable) {
		this.bindable = bindable;
	}

	@Test
	void testConstructor() throws NoSuchFieldException {
		PropertyAccessor<String> pa = new PropertyAccessor<>(PropertyAccessorTest.class, "name");

		assertThat(pa.getClazz()).isEqualTo(PropertyAccessorTest.class);
		assertThat(pa.getFieldName()).isEqualTo("name");
		assertThat(pa.getField().getName()).isEqualTo("name");
	}

	@Test
	void testDerivedClass() throws NoSuchFieldException, InvocationTargetException, IllegalAccessException {
		PropertyAccessor<String> pa = new PropertyAccessor<>(Derived.class, "errorCode");
		Derived derived = new Derived(
			"AX101233",
			"Error",
			"AXON",
			"AX-UPC-0003");

		assertDoesNotThrow(() -> pa.set(derived, "OB170001"),
			"Setting errorCode via PropertyAccessor failed");

		assertThat(pa.get(derived)).isEqualTo("OB170001");

		assertThat(pa.getClazz()).isEqualTo(Base.class);
		assertThat(pa.getFieldName()).isEqualTo("errorCode");
		assertThat(pa.getField().getName()).isEqualTo("errorCode");
	}

	@Test
	void testBaseClassNoSuchField() {
		NoSuchFieldException thrown = assertThrows(
			NoSuchFieldException.class, () -> new PropertyAccessor<>(Base.class, "ziggy")
		);

		assertThat(thrown.getMessage()).isEqualTo("ziggy");
	}

	@Test
	void testPublicField() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<String> pa = new PropertyAccessor<>(PropertyAccessorTest.class, "name");

		assertThat(pa.getAnnotations()).hasSize(4);

		assertThat(pa.hasAnnotation(Modifier.class)).isTrue();
		assertThat(pa.hasAnnotation(Mask.class)).isTrue();
		assertThat(pa.hasAnnotation(MaskCard.class)).isTrue();
		assertThat(pa.hasAnnotation(MaskUID.class)).isTrue();
		assertThat(pa.hasAnnotation(Override.class)).isFalse();

		assertThat(pa.getAnnotation(Modifier.class)).isInstanceOf(Modifier.class);
		assertThat(pa.getAnnotation(Mask.class)).isInstanceOf(Mask.class);
		assertThat(pa.getAnnotation(MaskCard.class)).isInstanceOf(MaskCard.class);
		assertThat(pa.getAnnotation(MaskUID.class)).isInstanceOf(MaskUID.class);

		assertThat(pa.get(this)).isEqualTo("Hello");

		pa.set(this, "Adios");

		assertThat(pa.get(this)).isEqualTo("Adios");
	}

	@Test
	void testBooleanWithIsMethod() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Boolean> pa = new PropertyAccessor<>(PropertyAccessorTest.class, "bindable");

		assertThat(pa.getGetter()).isEqualTo(this.getClass().getMethod("isBindable"));
		assertThat(pa.getSetter()).isEqualTo(this.getClass().getMethod("setBindable", boolean.class));

		pa.set(this, true);

		assertThat(pa.get(this)).isTrue();
	}

	@Test
	void testPrivateFieldNoAnnotatedGetSet() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<String> pa = new PropertyAccessor<>(PropertyAccessorTest.class, "privateName");

		assertThat(pa.getGetter()).isEqualTo(this.getClass().getMethod("getPrivateName"));
		assertThat(pa.getSetter()).isEqualTo(this.getClass().getMethod("setPrivateName", String.class));

		pa.set(this, "Adios");

		assertThat(pa.get(this)).isEqualTo("Adios");
	}

	@Test
	void testPrivateFieldAnnotatedGetSet() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<String> pa = new PropertyAccessor<>(PropertyAccessorTest.class, "privateNameAnnotatedMethods");

		assertThat(pa.getGetter()).isEqualTo(this.getClass().getMethod("getPrivateNameAnnotatedMethod"));
		assertThat(pa.getSetter()).isEqualTo(this.getClass().getMethod("setPrivateNameAnnotatedMethod", String.class));

		pa.set(this, "Adios");
		assertThat(pa.get(this)).isEqualTo("Adios");
	}

	@Test
	void testIntFieldNoAnnotatedGetSet() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<Integer> pa = new PropertyAccessor<>(PropertyAccessorTest.class, "i");

		pa.set(this, 100);
		assertThat(pa.get(this)).isEqualTo(100);
	}

	@Test
	void testAccessors() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		PropertyAccessor<String> pa = new PropertyAccessor<>(AccessorsPrivateField.class, "s");
		AccessorsPrivateField accessorsPrivateField = new AccessorsPrivateField();

		pa.set(accessorsPrivateField, "Hello Dolly");
		assertThat(pa.get(accessorsPrivateField)).isEqualTo("Hello Dolly");
	}

	@Test
	void testBadGetterAccessors() {
		Exception thrown = assertThrows(RuntimeException.class, () -> {
			PropertyAccessor<String> pa = new PropertyAccessor<>(BadGetterAccessorPrivateField.class, "s");
		});

		assertThat(thrown.getCause()).isInstanceOf(NoSuchMethodException.class);
		assertThat(thrown.getCause().getMessage()).isEqualTo("ie.bitstep.mango.reflection.utils.BadGetterAccessorPrivateField.getMessage()");
	}

	@Test
	void testBadSetterAccessors() {
		Exception thrown = assertThrows(RuntimeException.class, () -> {
			PropertyAccessor<String> pa = new PropertyAccessor<>(BadSetterAccessorPrivateField.class, "s");
		});

		assertThat(thrown.getCause()).isInstanceOf(NoSuchMethodException.class);
		assertThat(thrown.getCause().getMessage()).isEqualTo("ie.bitstep.mango.reflection.utils.BadSetterAccessorPrivateField.setMessage(java.lang.String)");
	}

	@Test
	void testNoAccessors() throws NoSuchFieldException {
		PropertyAccessor<String> pa = new PropertyAccessor<>(NoAccessorsPrivateField.class, "s");
		NoAccessorsPrivateField noAccessorsPrivateField = new NoAccessorsPrivateField();

		Exception thrown = assertThrows(IllegalAccessException.class, () -> {
			pa.set(noAccessorsPrivateField, "Hello Dolly");
		});
	}

	@Test
	void isCoreType() throws NoSuchFieldException {
		PropertyAccessor<String> privateNamePA = new PropertyAccessor<>(PropertyAccessorTest.class, "privateName");

		assertThat(privateNamePA.isCoreType()).isTrue();

		PropertyAccessor<String> uuidPA = new PropertyAccessor<>(PropertyAccessorTest.class, "uuid");

		assertThat(uuidPA.isCoreType()).isFalse();
	}

	@Test
	void forceGetWithGetter() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		// GIVEN
		PropertyAccessor<Integer> pa = new PropertyAccessor<>(PropertyAccessorTest.class, "i");

		// WHEN
		this.setI(200);

		// THEN
		assertThat(pa.forceGet(this)).isEqualTo(200);
	}


	@Test
	void forceGetWithoutGetter() throws InvocationTargetException, IllegalAccessException, NoSuchFieldException {
		// GIVEN
		PropertyAccessor<String> pa = new PropertyAccessor<>(PropertyAccessorTest.class, "name");

		// WHEN
		this.name = "Woof";

		// THEN
		assertThat(pa.forceGet(this)).isEqualTo("Woof");
	}
}

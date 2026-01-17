package ie.bitstep.mango.crypto.core.domain;

import ie.bitstep.mango.crypto.core.encryption.EncryptionServiceDelegate;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Contains all the information needed to specify a cryptographic key.
 * <br>
 * i.e. for If an application were using AWS KMS then they would assign the AWS KMS key ID (or key ARN)
 * to the {@link #configuration} field.
 */
public class CryptoKey {

	/**
	 * Unique ID (UUID) of the key
	 */
	private String id;

	/**
	 * The cryptographic mechanism which this key will use to carry out its operations. This library supports 2 dummy
	 * key types &quot;BASE_64&quot; and &quot;IDENTITY&quot;, for development and testing purposes
	 * <p>
	 * This field is used in conjunction with
	 * {@link EncryptionServiceDelegate#supportedCryptoKeyType() EncryptionServiceDelegate.supportedCryptoKeyType()}
	 * to figure out which {@link EncryptionServiceDelegate} to use at runtime.
	 * So applications which supply their own {@link EncryptionServiceDelegate} implementation must make sure that this field
	 * matches the value returned by {@link EncryptionServiceDelegate#supportedCryptoKeyType() EncryptionServiceDelegate.supportedCryptoKeyType()}.
	 * </p>
	 * <p>
	 * e.g. As a developer you use a
	 * HSM to perform your cryptographic operations so you've created an implementation of {@link EncryptionServiceDelegate}
	 * called HsmEncryptionServiceDelegate to do this. You would then set this field on you {@link CryptoKey} object to &quot;HSM&quot;
	 * and also return the value &quot;HSM&quot;
	 * from {@link EncryptionServiceDelegate#supportedCryptoKeyType() HsmEncryptionServiceDelegate.supportedCryptoKeyType()}.
	 * </p>
	 * <p>
	 * Note: See the Base64EncryptionService implementation for details.
	 * </P>
	 */
	private String type;

	/**
	 * The cryptographic function this key will be used to perform.
	 * Currently supported options are {@link CryptoKeyUsage#ENCRYPTION ENCRYPTION} and {@link CryptoKeyUsage#HMAC HMAC}
	 */
	private CryptoKeyUsage usage;

	/**
	 * Contains information needed for this key to actually perform it's cryptographic function.
	 * i.e. for an AWS KMS key this would contain the key ID or key ARN.
	 * <p>
	 * Obviously you don't put the actual bytes of the key in this field! Unless they were encrypted with some master key
	 * or something but that's not approved.
	 * </p>
	 * <p>
	 * NOTE: This should be a JSON blob
	 * </p>
	 */
	private Map<String, Object> configuration;

	/**
	 * Optional field to support
	 * {@code SingleHmacFieldStrategyForTimeBasedCryptoKey} the strategy (see mango4j-crypto for explanation)
	 * When using this strategy and when a new HMAC key is being added to a tenant, this field should be set to a future
	 * moment in time. This moment in time should be set to a longer period of time than the length of time that
	 * tenant/HMAC key information is cached for.
	 * <p>
	 * e.g. If your application instances cache tenant/key information for 15 minutes. Then this value should be set
	 * to 'now + 15 minutes plus at least 1 second'. Where 'now' is the exact time this key is added to the tenant.
	 * Better to add more than 1 second of course.
	 * So say you set this field to now plus 24 hours. The library will only begin using this HMAC key for write
	 * operations around 24 hours from the date the key was added to the tenant. As long as applications use all
	 * tenant HMAC keys to perform searches (which should always be the case anyway) then this would allow the
	 * {@code SingleHmacFieldStrategyForTimeBasedCryptoKey} to successfully support unique constraint functionality
	 * during HMAC key rotation.
	 * </p>
	 */
	private Instant keyStartTime;

	/**
	 * Semi-optional field. Only needed for applications which use tenant segregation of their data. This field isn't needed for normal
	 * operations, but it is needed for automatic re-key functionality if your applications uses tenants.
	 */
	private String tenantId;

	/**
	 * Optional Field, only used for rekey functionality. This field can be left null. But if an
	 * application needs to rekey records which use/do not use certain keys then this must be set to
	 * the corresponding value.
	 */
	private RekeyMode rekeyMode;

	/**
	 * Mandatory field. All {@link CryptoKey CryptoKeys} must set this value to a valid (immutable) date.
	 * If this field is null on any {@link CryptoKey CryptoKeys} rekey functionality will not work.
	 */
	private Instant createdDate;

	/**
	 * Returns the key ID.
	 *
	 * @return the key ID
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the key ID.
	 *
	 * @param id the key ID
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the key type.
	 *
	 * @return the key type
	 */
	public String getType() {
		return type;
	}

	/**
	 * Sets the key type.
	 *
	 * @param type the key type
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Returns the key usage.
	 *
	 * @return the key usage
	 */
	public CryptoKeyUsage getUsage() {
		return usage;
	}

	/**
	 * Sets the key usage.
	 *
	 * @param usage the key usage
	 */
	public void setUsage(CryptoKeyUsage usage) {
		this.usage = usage;
	}

	/**
	 * Returns the key start time.
	 *
	 * @return the key start time
	 */
	public Instant getKeyStartTime() {
		return keyStartTime;
	}

	/**
	 * Sets the key start time.
	 *
	 * @param keyStartTime the key start time
	 */
	public void setKeyStartTime(Instant keyStartTime) {
		this.keyStartTime = keyStartTime;
	}

	/**
	 * Returns the tenant ID.
	 *
	 * @return the tenant ID
	 */
	public String getTenantId() {
		return tenantId;
	}

	/**
	 * Sets the tenant ID.
	 *
	 * @param tenantId the tenant ID
	 */
	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}

	/**
	 * Returns the rekey mode.
	 *
	 * @return the rekey mode
	 */
	public RekeyMode getRekeyMode() {
		return rekeyMode;
	}

	/**
	 * Sets the rekey mode.
	 *
	 * @param rekeyMode the rekey mode
	 */
	public void setRekeyMode(RekeyMode rekeyMode) {
		this.rekeyMode = rekeyMode;
	}

	/**
	 * Returns the key created date.
	 *
	 * @return the created date
	 */
	public Instant getCreatedDate() {
		return createdDate;
	}

	/**
	 * Sets the key created date.
	 *
	 * @param createdDate the created date
	 */
	public void setCreatedDate(Instant createdDate) {
		this.createdDate = createdDate;
	}

	/**
	 * Equals and hashcode by default only compare the ID field for equality. This is all that's necessary.
	 *
	 * @param o Other {@link CryptoKey} object to compare with this one.
	 * @return true, if the ids of the 2 {@link CryptoKey CryptoKeys} are equal, false otherwise.
	 */
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CryptoKey cryptoKey = (CryptoKey) o;
		return Objects.equals(id, cryptoKey.id);
	}

	/**
	 * Returns the hash code for this key.
	 *
	 * @return the hash code
	 */
	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	/**
	 * Returns a string representation of this key.
	 *
	 * @return the string representation
	 */
	@Override
	public String toString() {
		return "CryptoKey{" +
				"id='" + id + '\'' +
				", type=" + type +
				", usage=" + usage +
				", keyStartTime=" + keyStartTime +
				", tenantId='" + tenantId + '\'' +
				", rekeyMode=" + rekeyMode +
				", createdDate=" + createdDate +
				'}';
	}

	/**
	 * Returns the configuration payload for this key.
	 *
	 * @return the configuration map
	 */
	public Map<String, Object> getConfiguration() {
		return configuration;
	}

	/**
	 * Sets the configuration payload for this key.
	 *
	 * @param configuration the configuration map
	 */
	public void setConfiguration(Map<String, Object> configuration) {
		this.configuration = configuration;
	}

	public enum RekeyMode {
		KEY_OFF,
		KEY_ON
	}
}

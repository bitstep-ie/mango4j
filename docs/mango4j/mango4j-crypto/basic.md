# mango4j-crypto - Documentation

The goal of this library is to facilitate developers with implementing Application Level Encryption (ALE) in a quick, easy and safe manner. This library is based on using simple
annotations to mark fields on your entity which the library will then generate the appropriate ciphertext for (encrypted/HMACed/both). This library is built on top of
the [mango4j-crypto-core](TBD) library so it's strongly advised to read the documentation
for [mango4j-crypto-core](/mango4j-crypto-core/basic.md) also to get a better background on how this library works but
most
importantly for a general understanding of common challenges implementing Application Level Encryption. You can also check out
the [mango4j-crypto-example](TBD) module in the mango4j-examples repository for a
working Springboot application which shows how to use this library with each HMAC strategy (explained further in this document).

This library is a framework for implementing ALE in Java applications. It is up to applications to choose which EncryptionServiceDelegates should be used for the actual
cryptographic operations. EncryptionServiceDelegates are separate modules which can be included as dependencies for whatever cryptographic providers you want to support.

## Annotations

The 2 main annotations that developers will use will be the @Encrypt and @Hmac annotations

### @Encrypt

The @Encrypt annotation should be placed on fields which must be encrypted. This annotation also requires the @EncryptedBlob partner annotation to be placed on the (single) field
where the library should put the resulting ciphertext (which is generated in one go for all fields), so you only need one @EncryptodBlob field regardless of the number of @Encrypt
fields. This is shown in the example entity code below.

**Note: All fields marked with @Encrypt must be transient or the library will throw an error on registration of the entity.**

### @Hmac

The @Hmac annotation should be placed on fields which must be HMACed for either lookup or unique constraint purposes. Depending on the HmacStrategy that your entity is using there
needs to be corresponding fields where the library should write the HMACs to. Since the below example uses the @SingleHmacStrategy (generally not recommended but for now it's the
easiest to understand) each field annotated with @Hmac must also have accompanying fields where the library should place the generated HMAC. For @SingleHmacStrategy each @Hmac
field must be accompanied by a field with the suffix 'Hmac'. In the below example you can see that pan has an associated field called 'panHmac'. There are currently 4 HMAC
strategies supported by the library and each one has slightly different approaches related to the design of your entity. This will most certainly seem strange, but they will be
discussed at length further in this documentation when it will make more sense. Also, if you're familiar with the challenges mentioned in the official Mango4J ALE documentation
they will make more sense.

**Note: All fields marked with @Hmac must be transient or the library will throw an error on registration of the entity.**

Other annotations that you need to know about are:

### @EncryptedBlob

As discussed above, if you have any fields marked with @Encrypt then you must have a single field marked with @EncryptedBlob where the library will store the ciphertext for all
encrypted source fields.

### @EncryptionKeyId

This is an optional annotation (for now) which you can place on a String field in your entity and the library will set it to the ID of the crypto key that was used to perform the
encryption. This is not necessary for decryption purposes (it's also stored inside the ciphertext blob anyway) but is useful for rekey purposes. If you use the rekey functionality
from this library then you must have a field on your entity with this annotation. It's recommended to have this anyway as it won't hurt and can be useful later.
It's basically used to find the records which are (or aren't) using a certain encryption key so that they can be rekeyed with the current encryption key.

An example Entity is as follows (notice the @SingleHmacStrategy annotation on the class, this is only needed if we have HMAC fields which we do in this example entity):

```java language=java
import ie.bitstep.mango.crypto.annotations.Encrypt;
import ie.bitstep.mango.crypto.annotations.EncryptedBlob;
import ie.bitstep.mango.crypto.annotations.Hmac;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity(name = "USER_PROFILE")
@SingleHmacStrategy
public class UserProfileEntity {

	@Encrypt
	@Hmac
	private transient String pan;

	@Encrypt
	@Hmac
	private transient String userName;

	@Encrypt
	private transient String ethnicity;

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEthnicity() {
		return ethnicity;
	}

	public void setEthnicity(String ethnicity) {
		this.ethnicity = ethnicity;
	}

	@Id
	@Column(name = "ID")
	private String id;

	@Column(name = "FAVOURITE_COLOR")
	private String favouriteColor;

	@Column(name = "USERNAME_HMAC", unique = true)
	private String userNameHmac;

	@Column(name = "PAN_HMAC")
	private String panHmac;

	@Column(name = "ENCRYPTED_DATA")
	@EncryptedBlob
	private String encryptedData;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFavouriteColor() {
		return favouriteColor;
	}

	public void setFavouriteColor(String favouriteColor) {
		this.favouriteColor = favouriteColor;
	}

}
```

To make this work, all you have to do is follow these instructions:

* Create your implementation of the CryptoKeyProvider interface. If you store your CryptoKey objects in a database it might look something like this:

```java language=java
package ie.bitstep.mango.examples.crypto.example.common;

import ie.bitstep.mango.crypto.core.domain.CryptoKey;
import ie.bitstep.mango.crypto.core.domain.CryptoKeyUsage;
import ie.bitstep.mango.crypto.core.providers.CryptoKeyProvider;
import ie.bitstep.mango.crypto.example.domain.entities.CryptoKeyEntity;
import ie.bitstep.mango.examples.crypto.example.repositories.CryptoKeyRepository;
import ie.bitstep.mango.examples.crypto.example.utils.CryptoKeyUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ApplicationCryptoKeyProvider implements CryptoKeyProvider {

	private final CryptoKeyRepository cryptoKeyRepository;
	private final CryptoKeyUtils cryptoKeyUtils;

	public ApplicationCryptoKeyProvider(CryptoKeyRepository cryptoKeyRepository, CryptoKeyUtils cryptoKeyUtils) {
		this.cryptoKeyRepository = cryptoKeyRepository;
		this.cryptoKeyUtils = cryptoKeyUtils;
	}

	@Override
	public CryptoKey getById(String id) {
		CryptoKeyEntity cryptoKeyEntity = cryptoKeyRepository.findById(id).orElseThrow(RuntimeException::new);
		return cryptoKeyUtils.convert(cryptoKeyEntity);
	}

	@Override
	public CryptoKey getCurrentEncryptionKey() {
		return cryptoKeyUtils.convert(cryptoKeyRepository.findTopByUsageOrderByCreatedDateDesc(CryptoKeyUsage.ENCRYPTION));
	}

	@Override
	public List<CryptoKey> getCurrentHmacKeys() {
		return cryptoKeyRepository.findAllByUsage(CryptoKeyUsage.HMAC).stream()
				.map(cryptoKeyUtils::convert)
				.collect(Collectors.toList());
	}

	@Override
	public List<CryptoKey> getAllCryptoKeys() {
		return cryptoKeyRepository.findAll().stream()
				.map(cryptoKeyUtils::convert)
				.collect(Collectors.toList());
	}

}
```

* Create a bean for your CryptoKeyProvider in your application config (shown below).
* Create a bean for EncryptionService (passing in whatever encryptionServiceDelegates you need) in your application config (shown below).
* Create a bean for CryptoShield in your application config, passing in a list of all your application entities which use @Encrypt or @Hmac. As shown below.

e.g. in Spring your config class might look like this:

```java language=java

@Bean
public CryptoShield cryptoShield(ObjectMapper objectMapper,
                                 CiphertextFormatter cipherTextFormatter,
                                 CryptoKeyProvider cryptoKeyProvider,
                                 EncryptionService encryptionService) {
	return new CryptoShield(List.of(UserProfileEntity.class),
			objectMapper,
			new CiphertextFormatter(cryptoKeyProvider),
			cryptoKeyProvider,
			encryptionService);
}

@Bean
public EncryptionService encryptionService(CryptoKeyProvider cryptoKeyProvider,
                                           CiphertextFormatter cipherTextFormatter) {
	return new EncryptionService(List.of(new Base64EncryptionService(),
			new IdentityEncryptionService()),
			cryptoKeyProvider);
}
```

*Note:* In this example we're passing in instances of Base64EncryptionService and IdentityEncryptionService. These come with the library for test purposes and should never be
available in a production deployment. To minimise this risk it's advised to have separate Spring config classes which run with 'prod' and 'dev' Spring profiles.

You can create your own EncryptionService classes by creating your own subclass of EncryptionServiceDelegate (just like Base64EncryptionService and IdentityEncryptionService) which
carries out encryption operations using a cryptographic provider that you use.

Then in your application code which performs write operations you should call

```java language=java
        CryptoShield.encrypt(userProfile);
```

before storing, so that the library encrypts/HMACs all your entity fields.

Likewise, after retrieving an entity from storage you can call:

```java language=java
        CryptoShield.decrypt(userProfile);
```

To reset (decrypt) all the original values in your entity. That's pretty much all there is to it.

## A Quick word about Crypto Keys and Encryption Service Delegates

This entire library revolves around the idea of CryptoKeys and EncryptionServiceDelegates. These are linked together via the CryptoKey.type field. When you call
CryptoShield.encrypt(), the library calls your CryptoKeyProvider.getCurrentEncryptionKey() to get the CryptoKey it should use to carry out the encryption operation, and then it
looks for the EncryptionServiceDelegate implementation which is associated with that CryptoKey.type. For example, If you look at Base64EncryptionService.supportedCryptoKeyType() it
returns "BASE_64". This means that if your CryptoKeyProvider.getCurrentEncryptionKey() returns a CryptoKey whose type field has a value of "BASE_64", then the
Base64EncryptionService will be used to carry out the encryption. This design provides high flexibility as it allows applications to support a wide variety of cryptographic
providers at the same time if necessary. And add more over time without needing to write any application code. This could be useful for when applications need to move from one cryptographic provider to another seamlessly and without writing any application code. This section applies to HMAC CryptoKeys also.
This design will be useful for applications which currently use some cryptographic provider but want to move over to a different one (i.e. from AWS KMS over to Azure Dedicated HMS). All they need to do is add the new encryption/HMAC cryptokeys with the type field for the new provider (a key rotation). The application will
continue to function just fine. Using the new keys to encrypt/HMAC and old cryptokeys to decrypt/search for old records. The application can even perform an automatic rekey if
necessary (this library currently has an experimental rekey feature built in).

## HMAC Strategies

A core concept in the mango4j-crypto library is that of HMAC strategies. There are various ways that an application could choose to implement key-rotation friendly HMAC
functionality and this library provides 4 of them out of the box.

You can choose which ones to apply to your application entities by using the corresponding class level annotation. The library authors strongly advise application developers to use
the @ListHmacStrategy unless there are very strong reasons not to. Currently, the library supports the following (in order of preference): +
@ListHmacStrategy +
@SingleHmacStrategy +
@SingleHmacStrategyForTimeBaseCryptoKey +
@DoubleHmacStrategy +

```java language=java
        AnnotatedEntityManager.register(UserProfileEntity .class, ListHmacFieldStrategy .class);
```

### ListHmacFieldStrategy

This is the most flexible and probably powerful strategy the library uses. However, it might not be the most suitable for relational DBs. The reason for this is that it will
generate a list of lookup HMACs for fields marked with LOOKUP and another separate list of unique value HMACs for fields marked with UNIQUE. This will probably mean that the
application has to store the lookup list in a separate table and the unique values in a unique values table (with the correct unique constraint definitions). This could require
from 1 to 3 inserts for each write operation on a record and would involve the associated search queries needing an SQL JOIN (back to the actual entity). This strategy is very
suitable for document DBs such as MongoDB because these lists can be stored in the actual entity which will have a negligible impact on performance (and probably makes for nicer
looking search code). But even if using an SQL DB this HMAC strategy should still be strongly considered.

This strategy is the most powerful because the lists on an entity can support any number of HMAC values for each field which means that applications/tenants could have as many HMAC
keys as they need over time, or we could generate multiple HMACs per field for search purposes (e.g. for a PAN we could generate the original PAN, the PAN without dashes, the last
4 digits of the PAN...all for a single field). This is known as HMAC Tokenization and is supported by the library for entities using the ListHmacFieldStrategy. HMAC Tokenization is
discussed further in this document.

This strategy can support passive HMAC key rotation since there could be a ton of HMAC keys on an application/tenant over time (although that's not recommended!) plus it would
allow updating to a new HMAC key even in the middle of a HMAC rekey job (in case of a mistake with new HMAC key assignment) without any functional impact to the application
whatsoever. Even if the new HMAC key is for a completely different cryptographic provider! Some applications may need multiple HMAC keys in use at all times for various reasons and
this HMAC Strategy supports that while keeping all search and unique constraint functionality intact.

In order to use this strategy your entity definition will be quite different from the other strategies. For example the equivalent entity shown previously in this diagram will look
like this if it were using the @ListHmacStrategy.

```java language=java
import ie.bitstep.mango.crypto.annotations.Encrypt;
import ie.bitstep.mango.crypto.annotations.EncryptedBlob;
import ie.bitstep.mango.crypto.annotations.EncryptionKeyId;
import ie.bitstep.mango.crypto.annotations.Hmac;
import ie.bitstep.mango.crypto.annotations.strategies.ListHmacStrategy;
import ie.bitstep.mango.crypto.domain.CryptoShieldHmacHolder;
import ie.bitstep.mango.crypto.domain.Lookup;
import ie.bitstep.mango.crypto.domain.Unique;
import ie.bitstep.mango.crypto.tokenizers.PanTokenizer;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ListHmacStrategy
@Document(collection = "UserProfile")
public class UserProfileEntityForListHmacStrategy implements Lookup, Unique {

	@Encrypt
	@Hmac
	private transient String pan;

	@Encrypt
	@Hmac(purposes = {Hmac.Purposes.LOOKUP, Hmac.Purposes.UNIQUE})
	private transient String userName;

	@Encrypt
	private transient String ethnicity;

	private Collection<CryptoShieldHmacHolder> lookups;

	private Collection<CryptoShieldHmacHolder> uniqueValues;

	public String getPan() {
		return pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEthnicity() {
		return ethnicity;
	}

	public void setEthnicity(String ethnicity) {
		this.ethnicity = ethnicity;
	}

	@Id
	private String id;

	private String favouriteColor;

	@EncryptedBlob
	private String encryptedData;

	@EncryptionKeyId
	private String encryptionKeyId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFavouriteColor() {
		return favouriteColor;
	}

	public void setFavouriteColor(String favouriteColor) {
		this.favouriteColor = favouriteColor;
	}

	@Override
	public void setLookups(Collection<CryptoShieldHmacHolder> lookups) {
		this.lookups = lookups;
	}

	@Override
	public List<CryptoShieldHmacHolder> getLookups() {
		return lookups;
	}

	@Override
	public void setUniqueValues(Collection<CryptoShieldHmacHolder> uniqueValues) {
		this.uniqueValues = uniqueValues;
	}

	@Override
	public List<CryptoShieldHmacHolder> getUniqueValues() {
		return uniqueValues;
	}

}
```

This example entity is designed for MongoDB (as it's the most suitable DB for this HMAC strategy). If you are using it with an SQL DB check out
the [mango4j-crypto-example](TBD) demo application which does the exact same for an
SQL DB.

There are several things to note with this entity definition. Since this entity is annotated with @ListHmacStrategy:

* The username field annotation with @Hmac has a 'purposes' definition. This can have the values of Purposes.LOOKUP or Purposes.UNIQUE, or both depending on what purpose that field
  is being HMACed for. If no value is specified then it defaults to Purposes.LOOKUP (as you can see with the 'pan' field).
* The entity class must implement either the Lookup interface, the Unique interface or both. Since this entity uses HMACs for both purposes it implements both interfaces.
* Unlike the other HMAC strategies this one doesn't have associated target HMAC fields with the 'Hmac' suffix. Instead, it implements the methods getLookups() and setLookups() from
  the Lookup interface and the getUniqueValues() and setUniqueValues() from the Unique interface. The library calls back to these methods to get and set the HMACs. This is what
  makes this the most powerful HMAC strategy, we can have as many HMACS for as many keys or tokenized values as needed.
* If you are using HMACs for unique purposes, make sure to create the appropriate unique constraint definitions on your DB. So the uniqueValues

**Note:** When updating existing entities, make sure that the setLookup() and getLookup() methods _completely replace_ the existing list! Do not append to the existing lists!!!

### Steps to perform a HMAC key rotation when using the 'ListFieldHmacStrategy' HMAC strategy.

1. Add the new HMAC key into the applications/tenant's list of HMAC keys. Do not remove or replace existing HMAC cryptokeys as they must still be used!!!

That's it! And since the ListHmacStrategy has experimental rekey functionality we'll document that here:

1. Implement the RekeyCryptoKeyManager interface
2. For each entity that uses this library create a corresponding implementation of the RekeyService interface.
3. Configure a RekeyScheduler in your config class, e.g.:

```java language=java

@Bean
public RekeyScheduler rekeyScheduler(
		CryptoShield cryptoShield,
		ObjectMapper objectMapper,
		Clock clock,
		RekeyCryptoKeyManager rekeyCryptoKeyManager,
		Collection<RekeyService<?>> rekeyServices) {

	RekeyScheduler rekeyScheduler = new RekeyScheduler();
	rekeyScheduler
			.withCryptoShield(cryptoShield)
			.withRekeyCryptoManager(rekeyCryptoKeyManager)
			.withRekeyServices(rekeyServices)
			.withObjectMapper(objectMapper)
			.withClock(clock)
			.withBatchInterval(Duration.ofSeconds(10))
			.withCryptoKeyCachePeriod(Duration.ofMinutes(3))
			.withMaximumToleratedFailuresPerExecution(1000)
			.withRekeyCheckInterval(60, 15, TimeUnit.SECONDS)
			.start();
	return rekeyScheduler;

}
```

The above is a once off config. Once done, it allows the application to perform rekey jobs with no extra code. In order to make this periodic RekeyScheduler start rekeying entities
you need to make use of the CryptoKey.rekeyMode field.
Mango4j-crypto supports 2 types of rekey modes:

KEY_OFF: This type of rekey is where we have some encryption/HMAC that we want to completely remove from our system but there might still be some entities which used that
encryption/HMAC key. If we set the rekeyMode to 'KEY_OFF' on this CryptoKey then when the RekeyScheduler job runs it will look for all entities that used that CryptoKey and start
re-encrypting/re-hmacing them with the current key. In the case of a HMAC CryptoKey the library will calculate HMACs with all remaining HMAC keys that don't appear in the current
lookup/unique HMAC list for the entity. This is how we can rekey to different HMAC keys without functional outage.

*Note:* If rekeyMode is set to 'KEY_OFF' on the current encryption or current HMAC CryptoKey it is ignored. It makes no sense to key off the current key.

KEY_ON: This type of rekey is where we want all other encryption/HMAC keys (apart from this one) to be completely removed from the system. If we set the rekeyMode to 'KEY_ON' on
the current encryption/HMAC key then when the RekeyScheduler job runs it will look for all entities that did not use this CryptoKey and start re-encrypting/re-hmacing them with the
current key. This type of rekeying should be strongly reconsidered before initiating it because it will most likely rekey a very large portion of your entities. Also, keep in mind that you may need to make
sure any external cryptographic providers that you use are aware that they will experience a higher volume of traffic for a period.

*Note:* If rekeyMode is set to 'KEY_ON' on a key that is not the current encryption or current HMAC CryptoKey it is ignored. It makes no sense to rekey everything onto some old
key.

### SingleHmacStrategy

The first example entity at the beginning of this document uses the SingleHmacStrategy. To use this strategy you just need to each field annotated with @Hmac to also have a
corresponding field with the same name but with the suffix 'Hmac'. This is where the library will place the generated HMAC. Using the SingleHmacStrategy will leave applications
open to the 2 core HMAC challenges so should only be used if your application can tolerate the resulting outcomes of those challenges. As such we don't really recommend anybody
uses this strategy.

### SingleHmacStrategyForTimeBasedCryptoKey

Exactly the same as the SingleHmacStrategy but will not use the current HMAC key for write operations until the CryptoKey.startTime (which you need to make sure to set on the new
CryptoKey) has passed.

### DoubleHmacFieldStrategy

This strategy is a less powerful strategy than the ListHmacStrategy. It uses an approach which requires each source HMAC field on an entity to have 2 accompanying persisted fields
which must have the suffixes 'hmac1' and 'hmac2'.

If using an SQL DB this strategy will probably give better performance than the more powerful ListHmacFieldStrategy which for an SQL database would most likely mean lower
performance (as explained in the ListHmacFieldStrategy section). If using the ListHmacFieldStrategy would lower application performance in an unacceptable way, then this
DoubleHmacFieldStrategy is more compatible while still allowing your applications to overcome both of the main documented HMAC challenges.

During normal application running (only a single HMAC key in use for the tenant) the library will write the same HMAC value into both the HMAC fields. But during HMAC key rotation
when there are now 2 HMAC keys it will write the HMAC generated with the old key into the 'HMAC_1' field and the HMAC generated with the new key into the 'HMAC_2' field.

**The following are the prescribed HMAC Key Rotation steps when using this HMAC strategy. If you use this HMAC strategy you must implement your key rotation process exactly this
way (and in the order of these steps):**

1. Add the new HMAC key into the tenant's list of HMAC keys
2. Wait until the background HMAC key rotation job is complete (this may take some time)
3. Wait and make sure that all application instances can see the updated tenant information (with both the old and new keys). This is a *_very important_* consideration in
   environments where it's common practice to cache tenant/key information, it may take a while for this result to be true and if the key rotation happened to finish quickly
   proceeding to the next step will cause problems. You need to make sure to wait until this condition *_and_* the condition in step 2 are *_both_* true.
4. Remove the old key from your tenant
5. Wait and make sure that all application instances are now *_only_* using the tenant's new HMAC key. Again this is the same consideration as in step 3
6. Copy HMAC_2 column values into HMAC_1 columns for all fields in all records, something like:
   [,sql]

```sql language=sql
UPDATE table
SET USERNAME_HMAC_1 = USERNAME_HMAC_2
```

**Some notes for applications using this strategy for an entity:**

If your application is distributed and caches tenant/key information (which is standard since this information rarely changes), then it should always search for values in both the
HMAC_1 and the HMAC_2 fields even when there is currently only 1 tenant HMAC key in use (and not assume that the correct value will be in the HMAC_1 column). The reason is that
when a key changes are made to a tenant, some instances will see this before others (instances will update their caches at different times). Between step 5 and 6 above the
instances which are seeing only the new key will only find matching values in the HMAC_2 column. Likewise until step 3 is complete some instances will still only see the old key
until their cache has expired and will only find matching values in the HMAC_1 column. An application instance has no idea if it is at steps 1-3 or steps 5-6

### SingleHMACFieldStrategy

This HMAC Strategy is pretty straightforward. For each field marked with @Hmac you need one corresponding HMAC field with the suffix 'Hmac'. e.g. for a field named pan you need
another (persisted) field named 'panHmac'.
Applications should rarely (almost never) use this strategy.
An application should only use this strategy for an entity if the answer to the following 2 questions is **'NO!'**:

1. Does any encrypted field in this entity need to be guaranteed unique (has an associated unique constraint)?
2. Would it have a negative impact on the business if the application experienced functional problems with search operations on this entity during a key rotation?

The reasoning behind these questions are related to the challenges with HMAC key rotation which are outlined extensively in this and the mango4j-crypto-core official documentation.

## HMAC Tokenizers

If using the ListHmacStrategy for an entity you can make use of HMAC Tokenizers by specifying them in the @Hmac annotation's HmacTokenizers method. Like:

```java language=java
@Hmac(HMACTokenizers = {PanTokenizer.class})
private transient String pan;
```

The library will then generate a series of alternative HMACs for that field using those HmacTokenizer classes. For example the PanTokenizer (which is included in the library) in
the sample code above will result in the lookup HMAC list for that entity including the HMAC of the last 4 digit of the PAN, the HMAC of the first 6 digits of the PAN, the HMAC of
the PAN without dashes or spaces (if there are any) and the HMAC of the full original PAN that was supplied. The library has some standard HMAC tokenizers, please see the javadocs
for each one to learn what HMAC representations they generate. Applications can supply their own HmacTokenizers with whatever tokenization logic they need by implementing the
HmacTokenizer interface. If you have created a HmacTokenizer you think would be generally useful to others please let us know and we'll add it to the library. Using HMAC Tokenizers
will help applications with more flexible searching functionality and is another reason that the ListHmacFieldStrategy is the most powerful of the 4 core HMAC strategies.


***
*Please see the accompanying [mango4j-crypto-example](TBD) module in
the [mango4j-examples](TBD) repository for a full Springboot application using this library (and which
demonstrates all 4 HMAC strategies).*
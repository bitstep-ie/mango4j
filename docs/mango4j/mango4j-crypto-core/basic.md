# mango4j-crypto-core - Base Documentation

If you have any questions/feedback or would like a consultation/discussion regarding the following subject matter, please reach out
to [Ashley Waldron](mailto:ashley.waldron@gmail.com) and [John Allen.](mailto:john.allen@gmail.com)

## Background

This documentation is aimed at providing a general overview of implementing application level encryption (ALE) in your code. Most of the discussion here will focus on general
challenges and only a small portion will talk about the actual library itself since the main library that is intended to be used by application developers is
the [mango4j-crypto](mango4j-crypto/basic.md) library (which is built on top of this). The contents in this document can be
used as a reference for developers who need to implement ALE regardless of whether they use this library (or the main mango4j-crypto library) or not. Developers should consider the
guidelines documented in each section when building their own solutions.

This documentation purposely avoids talking about specific cryptographic implementations. or standards (AES, PKCS#11, AWS KMS, etc.) because the core focus here is to talk about how
to implement ALE (whatever cryptographic methods, standards or providers you use) and what general challenges exist along with guidance on overcoming them in your application.

The following documentation uses some terms regularly which we'll define upfront:

*Tenant:* A term used to describe logical isolation in your system for a particular client entity. e.g. Some issuers have requirements that any confidential data of theirs that you
store in your system should be segregated from data belonging to other entities. An application could spin up a full environment just for that issuer client or they can choose to
introduce the concept of 'tenants' in the system. With each client entity being a 'tenant' and each 'tenant' using their own encryption keys, the application meets the data
segregation guidelines. This 'tenant' approach is the usual one followed to enforce the data segregation rules.

*HMAC key:* A [HMAC](#What'-s-a-HMAC?) is just a [hash](#What's-a-hash?) which uses a secret key to create the hash. HMACs are used for 2 main purposes in an application:

1. To support searching on fields that are encrypted
2. To support unique constraint enforcement on fields that are encrypted.
   The [FAQ](#Why-do-I-need-to-HMAC-data-in-order-to-make-it-searchable?) section at the bottom of this document explains this in more detail.

Often when designing applications which have to implement Application Level Encryption it might seem to make sense to design only with some specific
cryptographic provider in mind (e.g. Azure Dedicated HSM) but doing this could introduce complexities later if your application needs the potential to use different providers (different regions might have
different cryptographic providers or regulations). You may need to perform a [key rotation](#What-is-a-key-rotation?) from one key which uses some cryptographic provider to a new
key which uses some other cryptographic provider. So a more flexible design should be used to support a more generic approach to implementing application level encryption. We call
the approach that the mango4j-crypto-core takes "Key Driven Design". The term 'Key Driven Design' comes from the fact that each tenant has a group of encryption/HMAC keys (referred
to in this documentation as CryptoKeys) and those CryptoKeys define how the encryption operation is carried out. i.e. If a CryptoKey has a type of 'AWS_KMS' then this library would use
AWS KMS and its SDK to perform the encryption operation, if a CryptoKey has a type of 'HSM' then this library will use some matching HSM implementation to perform the encryption operation. This
approach hides the details from the application code which is generally good practice and allows for more flexibility if an application needs to use a different method to perform
encryption later without needing any changes to existing application code.

This library is very simple at its core and is intended to force application code into a tried and tested design for application level encryption which removes references to
specific cryptographic providers from your code and allows you to handle encryption key rotation and [HMAC](#What's-a-HMAC?) key rotation more robustly. HMAC key rotation has a
long section all of its own in this document which should be considered mandatory reading for all developers who need to HMAC data for lookup or unique constraint purposes.

## Key objects

Everything in mango4j-crypto-core revolves around the CryptoKey object. These are objects that you will pass to the generic EncryptionService interface with your data in order to
encrypt/HMAC it.
We often see applications representing their encryption/HMAC keys in their code with simple Strings (representing an AWS KMS key ID or key ARN for example) rather than key objects, but you
should strongly consider representing your key information with objects instead. For example, this library represents keys in the code with the following object:

```java language=java
public class CryptoKey {
	private String id;
	private CryptoKeyUsage usage;
	private String type;
	private String keyMaterial;
	private Instant keyStartTime;
}
```

*id:* Just a plain old random GUID

*usage:* What this key will be used for (either encryption or HMAC)

*type:* The encryption implementation mechanism that this key will use to carry out its cryptographic operations. For example: maybe you use Azure Dedicated HSM, in that case you could set this
value to 'AZURE_DEDICATED_HSM'. If you use Amazon KMS with the SDK, you could set it to 'AWS_KMS'. You can define whatever types you like, they're the application's convention.

*keyMaterial:* This field stores the information about the key that the encryption implementation/mechanism uses to carry out its operations. It would never contain the actual
bytes of the key or related confidential information. Instead, it would contain a reference to the key. For example: if the 'type' of this key was AWS_KMS then this would just be set
to the Key ID or Key ARN.

*keyStartTime:* We can ignore this for now but it's related to the [Single Time Based HMAC Strategy](#Single-Time-Based-HMAC-Strategy) that's described further in this document.

Using key objects like the above to represent cryptographic keys instead of just simple Strings is really no extra work and can keep your code flexible when it comes to supporting
multiple approaches to encryption. One advantage is that it will allow you to easily support multiple cryptographic providers while keeping code clean and encryption code
abstracted. It could also allow you to rotate from one type of key to another (e.g. AWS_KMS to AZURE_DEDICATED_HSM) with no code application code changes. Or you could support different
encryption mechanisms in different regions without having to write any custom application code to support that.

## Encryption logic abstraction

Abstract all encryption related code behind an interface. This is just good practice anyway, and coupled with the CryptoKey objects will allow your application to support multiple
types of cryptographic providers or different cryptographic approaches depending on application requirements. You can have a look at the EncryptionService interface in this library
as a reference.

## Ciphertext representation

This library also has a standardised way of representing the final ciphertext (for encryption, not HMACs) which your application will store. Instead of just returning the straight
ciphertext output it will return it in the following format:

```json language=json
JSON={
	"cryptoKeyId": "someKeyId",
	"iv": "someInitializationVector",
	"cipherText": "generatedCipherText"
}
```

where:

- *cryptoKeyId* is the identifier of the crypto key object (e.g. the CryptoKey.id field) in your system that was used to carry out the cryptographic operation
- *iv* is the [Initialization Vector](#What's-an-IV?) that was used for the cryptographic operation
- *cipherText* is the actual ciphertext that was returned from the cryptographic operation

Storing the ciphertext in this format should give your application all the information it needs to decrypt the ciphertext. If your application needs to store the generated
ciphertext in a different format you can register your own implementation of CipherTextFormatter using:

```java language=java
CipherTextFormatter.register(MyCipherTextFormatterImpl);
```

If designing your own ALE solution, consider representing your ciphertext in a format which gives you all the information you need to parse it properly, easily and decrypt it.

## HMAC Key Rotation Challenges

The following section documents the 2 core challenges when it comes to supporting HMAC key rotation in your application. It's advised for all developers not familiar with this
particular topic to read this section very carefully. It's often easy to not give much attention to the scenario of rotating HMAC keys, because it's tempting to think "when are we
really going to need to change a [tenant's](#What-is-a-tenant?) HMAC key?!" or "I can worry about it later". But, as you can see below in order to support this functionality there
are very important considerations to take into account for the way that you design your application. If you don't design your application with an approach to overcome these
specific challenges then you most likely cannot say that you support tenant HMAC key rotation without _serious_ impacts to system functionality.

### Searching operations during the rotation of a HMAC key

If your application has the concept of a single HMAC key per tenant, it's extremely likely that you'll need to stop doing this and change this design approach. Unless for some
reason you don't have to support HMAC key rotation in your application. The reason being that if you change the tenant's HMAC key during normal application operation you won't be
able to find any records. The only way that you can ever change a tenant's HMAC key is if it is acceptable to take your system offline until all records have been re-keyed with the
new key by some background job, or (for some reason) you are able to tolerate a lot of search misses for existing records until such a background job was complete. Both of these
scenarios are very unlikely to be tolerated by the business.

To break down this sample scenario into steps:

1. You change the HMAC key for your tenant.
2. All application searches going forward will use the new HMAC key.
3. Immediately you won't be able to find any records in your system because all existing HMACs have been calculated with the old HMAC key
4. You kick off a background job which goes from record to record decrypting and re-keying the fields with the new HMAC key.
5. Your application searches gradually start to become more and more successful over time.
6. Once the background rotation job is complete your application search functionality is now back online.

The functional search outage from your application is most likely unacceptable. re-keying all records in your system may take a very large amount of time. You may have a very large
number of records in your system and even with a modest number of records, it's generally not acceptable to have this background re-keying job run as fast as it can due to
performance degradation of your system. Increased number of calls to the DB (2 extra calls in the background for every record). Not to mention if you're using an external
cryptographic provider (like Azure Dedicated HSM) it might not handle (or even allow) the extra traffic without its own performance problems.
It may be tempting to think along the lines of running the background job until all records are re-keyed with the new HMAC key before changing the key on the tenant. But this is
just the same scenario in reverse. Your application searches will begin successfully but immediately will become less and less successful over time until the background job is
finished and you change the tenant's HMAC key to the new one.

To remedy this you need to implement 2 simple concepts:

1. Change your design approach so that a tenant has a _list_ of HMAC keys rather than a single one.
   Depending on your application requirements this list may contain anywhere from a single HMAC key to N HMAC keys. During HMAC key rotations you will add in the new key, instead
   of replacing the old key.
   If your application only supports passive key rotation then this list of HMAC keys will keep getting larger until one of the following 2 scenarios happen:
   a. Older data that has HMACs calculated with the old key is deleted due to data retention rules. Once there are no more HMACs in the DB that were calculated with the old HMAC
   key, you can remove it.
   b. If your data is long-lived then you may need to consider re-keying your data so that all older records that have HMACs calculated with the old HMAC key are re-keyed with the
   new HMAC key. You can then remove the old HMAC key once all older records are re-keyed with the new HMAC key.

2. In your application's search operations on some field change the logic to look for matches
   with a _list_ of HMACs instead of try ing to match a single HMAC.
   The number of HMACs generated for the search will be equal to the number of HMAC keys in the list. So your code needs to be prepared for this scenario by matching across a list
   of HMACs rather than a single HMAC.

So the steps for converting your design approach to accommodate HMAC key rotation become:

1. Update your tenant record to have a list of HMACs
2. Modify your application search code in such a way that instead of looking for a HMAC field value equal to the generated HMAC (e.g. in JpaRepository):

```java language=java
findByUsernameHmac(String userNameHmac);
```

Instead, it needs to generate a list of HMAC values (using the tenant's _list_ of HMAC keys) and look for a HMAC field value which is in that list of HMACs (e.g. in JpaRepository):

```java language=java
findByUsernameHmacIn(Collection<String> userNameHmacs);
```

3. Modify your normal application HMAC code for *_write_* operations to choose the active HMAC key for the HMAC operations in the case where the tenant's HMAC keys list has more
   than one element (*important:* this library assumes that the new HMAC key is the first one in the list).

Actually, if your application is a multi-instance application and caches tenant information (both very likely) then this solution still has very big issues. Even with the above
solution consider the following scenario:

#### Cached Keys Search Scenario

1. You have 2 instances of your application, and they both cache tenant information (common practice because tenant information rarely changes)
2. You add the new HMAC key to the tenant's list of HMAC keys
3. You kick off the key rotation job
4. Instance 1 of your application has not updated its cache and is still only seeing the tenant with the old HMAC key
5. Instance 2 of your application has updated its cache and is now seeing the tenant with both the old HMAC key and new HMAC key
6. Instance 2 inserts a new record with an email of `john.doe@test.com` and uses the new HMAC key to do it (since going
   forward the application should use the new HMAC key)
7. A search comes into instance 1, and it uses the old HMAC key to HMAC `john.doe@test.com` and look for it.
8. The record is not found because the HMAC for `john.doe@test.com` was calculated and inserted by instance 2 using the new HMAC key

To solve this issue, another design change needs to be considered. This change is described in the next section
and is also needed for another (possibly much more serious) challenge regarding unique constraints. Implementing the design consideration in the next section will also solve the
above shortcoming.

### Unique constraint enforcement

*This problem is much more important than the previous search problem! If your application has unique constraints on confidential fields then there's a chance that changing a HMAC
key could leave it in a broken state!*

If you have a confidential field which you need to be unique for your tenant let's say....a username field. Then you will have a DB column with a unique constraint where you store
the HMAC of this username. Now, if you switch HMAC keys it will be possible to create accounts with the same username.
Since an existing account username was calculated with the old HMAC key then a new account could be created with the same username since now that the new HMAC key is being used it
will calculate a different HMAC for the same username value and the DB unique constraint will not be enforced. Your system would now be broken.

To break down this sample scenario into steps:

1. A user exists in your application with username `john.doe@test.com`
2. You change your tenant's HMAC key to the new HMAC key (or if you've followed the [above](#Searching-operations-during-the-rotation-of-a-HMAC-key) approach
   to solve the search challenge, you've added the new HMAC key into your tenant's list of HMAC keys)
3. A request to create a new user (or change an existing username) is sent to your application with the username `john.doe@test.com`.
4. Your application calculates the HMAC of `john.doe@test.com` using the tenant's new HMAC key. This HMAC will be different than the username HMAC for the existing
   `john.doe@test.com`
   record.
5. The new record is created (or existing record is updated) successfully as the unique constraint on that DB column won't have been triggered.
6. There now exists 2 users in the application with the username `john.doe@test.com`

It may be tempting to think that you can offset this somewhat by always searching for the username first before write operations but this impacts performance and makes the DB
unique constraint a little bit redundant. However, in any case it doesn't solve the problem. There would still exist a race condition in your code which can result in 2 usernames
with the same value and once this happens your system is broken. If you think this race condition is too improbable to care much about, just know that this has already happened
more than once in existing applications! Also, if your application is multi-instance and caches tenant/key information this increases this window considerably.

Imagine at [point 7](#Cached-Keys-Search-Scenario) in the previous cached keys search scenario that instead of _searching_ for a record, instance 1 _creates_ a new record with the
username `john.doe@test.com`. It can even search for any existing records but it won't find them (because it doesn't yet have the latest HMAC key). This situation will result in a
duplicate record being created by instance 1.

To remedy this you need to implement particular approaches to calculating and storing the HMACs for your entity fields. The following sections detail some of these approaches, all
of which are supported in the [mango4j-crypto](/mango4j-crypto/basic.md) library.

## List HMAC strategy

This strategy documents the most general and powerful design that application developers can use to take care of all problems mentioned above. Using this design guarantees your
application will accommodate HMACs correctly regardless of your use case or application requirements.
The List HMAC strategy has just 2 fairly elegant concepts:

1. Have a list of HMAC keys on your tenant, rather than a single HMAC key. Exactly as already discussed in this document.
2. For all write operations write the entire list of generated HMACs into your DB.

Part 2 is the tricky bit because when you use an SQL DB, it forces you into a particular entity design. If you take our previous examples where we HMAC the username. This would
mean that we would have a table for the actual record (maybe a USER_PROFILE table) with the encrypted ciphertext and whatever other information, a table for lookup HMACs if we are
using the username HMAC for search purposes (e.g. USER_PROFILE_LOOKUPS) and a 3rd table for unique values (USER_PROFILE_UNIQUE_VALUES) if we are using the username HMAC to enforce
uniqueness per tenant.

For example the USER_PROFILE table might look like this:

| ID                                   | TENANT_ID  | CIPHER_TEXT                                                          | SOME_OTHER_NON_CONFIDENTIAL_ATTRIBUTE | ENCRYPTION_KEY_ID                    |
|--------------------------------------|------------|----------------------------------------------------------------------|---------------------------------------|--------------------------------------|
| 9c7e275e-3729-421c-a7c5-cf02bba17f2d | MyTenantID | QmFzZTY0RW5jb2RpbmdPZlVzZXJQcm9maWxlQ29uZmlkZW50aWFsQXR0cmlidXRlcw== | some value In the clear               | 23a0a1b4-3897-4eaa-b8fa-1818a9540f0c |

And the USER_PROFILE_LOOKUPS table would look like this:

| ID                                   | TENANT_ID  | ALIAS    | VALUE                        | HMAC_KEY_ID                          | USER_PROFILE_ID                      |
|--------------------------------------|------------|----------|------------------------------|--------------------------------------|--------------------------------------|
| 2f2ab2f4-57cb-441a-84c8-44eee23e3693 | MyTenantID | username | QmFzZTY0VmFsdWVPZlVzZXJuYW1l | 31ae30a7-4228-40a9-9078-d5e994491981 | 9c7e275e-3729-421c-a7c5-cf02bba17f2d |

And the USER_PROFILE_UNIQUE_VALUES tables would look like this:

| ID                                   | TENANT_ID  | ALIAS    | VALUE                        | HMAC_KEY_ID                          | USER_PROFILE_ID                      |
|--------------------------------------|------------|----------|------------------------------|--------------------------------------|--------------------------------------|
| eeddbee1-34db-4554-ada4-a23e04c69846 | MyTenantID | username | QmFzZTY0VmFsdWVPZlVzZXJuYW1l | 31ae30a7-4228-40a9-9078-d5e994491981 | 9c7e275e-3729-421c-a7c5-cf02bba17f2d |

The USER_PROFILE_LOOKUPS and USER_PROFILE_UNIQUE_VALUES look exactly the same because they are. The only reason they need to be separate is because the USER_PROFILE_UNIQUE_VALUES
table needs a compound unique constraint placed on the TENANT_ID, ALIAS, VALUE and HMAC_KEY_ID columns, whereas the lookup does not.

With the above entity design when inserting or updating records in the USER_PROFILE table, your application code would generate a list of HMACs (1 for each HMAC key in use) and
insert them all into both the USER_PROFILE_LOOKUPS and USER_PROFILE_UNIQUE_VALUES tables (1 row per HMAC entry). If you now go back to the HMAC challenge scenarios described
earlier in this document (even the cached keys scenario), you'll see that this design guarantees to overcome both.
If you're using a document DB such as Mongo, you should just implement this design anyway as it won't interfere with your entity design. The USER_PROFILE_LOOKUPS and
USER_PROFILE_UNIQUE_VALUES tables just become separate lists inside the USER_PROFILE record. Unfortunately when using relational DBs this strategy will require inserting/updating
from 1 to 3 separate tables for each write operation and for searching it will require a join from the USER_PROFILE_LOOKUPS table back to the original record in the USER_PROFILE
table.

### Process for re-keying data with the List HMAC Strategy

1. Add your new HMAC key to the tenant's list of HMAC keys
2. Wait until the HMAC key cache expiry time has passed (if applicable), so that all application instances are using the new HMAC key.
3. Kick off the re-keying job.
4. The re-keying job should find each record it needs to re-key. This depends on application requirements but will probably fall into one of 2 criteria:
   a. Any record which doesn't yet have HMACs calculated with the new HMAC key - **common for full re-keying of all data**
   b. Any record which has HMACs calculated with some old HMAC key (that you're trying to remove from the system), but does not have HMACs calculated with the new HMAC key -
   **common for passive key rotation**
5. For each record found, decrypt the record, calculate the HMAC(s) with the new key, insert the HMAC(s) into the lookup/unique constraint tables (or **_add_** them to the lists if
   using a document DB and save the record). Existing HMACs should remain as they are, untouched.
6. Wait for all applicable records to be re-keyed
7. Remove the old HMAC key from the tenant's list of HMAC keys
8. Wait until the HMAC key cache expiry time has passed (if applicable), so that all application instances are no longer using the old HMAC key.
9. For every record, remove all HMACs from the lookup/unique constraint tables (or document HMAC entries) that were calculated with the old HMAC key. Leaving them there won't
   affect functionality so this cleanup step is optional from a functionality perspective, but leaving old HMACs in your system is a potential security hazard so applications
   should perform this step at least for long-lived data.
10. You can now delete the old HMAC key from wherever it was stored.



**Pros of the list HMAC strategy:**

- Bulletproof HMAC design which will work for all combinations of application requirements
- The only general HMAC design which supports passive key rotation for long-lived data which uses HMACS for unique value enforcement
- Application search code becomes standardized and is cleaner (because all lookups tables will have the same definition)
- Excellent fit for document DBs (Mongo)
- Supports zero-outage search
- Supports unique constraint integrity
- Supports applications which cache HMAC keys
- Supports all passive key rotation designs
- Easiest strategy for supporting re-keying of data with no impact to application functionality
- Can have as many HMAC keys on a tenant as you want. You can add a new HMAC key anytime you want, even in the middle of a re-keying job, and it won't affect application
  functionality.
- Easily supports [HMAC tokenization.](#What-is-HMAC-tokenization?)

**Cons of the list HMAC strategy:**

- Forces relational DBs into a multi-table entity design which can negatively impact performance
- Always adds/updates N HMACs for each write operation, where N is the number of HMAC keys in use. So depending on the rate at which an application deletes old keys this will have
  a performance impact if the list of HMAC keys keeps growing.

## Double HMAC strategy

The Double HMAC strategy is a compromise between the List HMAC strategy and the (mostly not recommended) Single HMAC Strategy (or its sister strategy the Single Time Based HMAC
strategy) that allows for a more normal entity design.

This strategy trades the generality of the List HMAC strategy to a more simple relational design and has an added downside of only allowing 2 HMAC keys to be in use for write
operations at any time. So there will be some applications which this strategy won't suit, particularly those that need to have 3 or more HMAC keys active for write operations at
any time. Applications which use HMACs for unique value enforcement for long-lived data and only support passive key rotation (no re-keying) cannot use this strategy. For
applications which require unique value enforcement, using this strategy will mean that you must support re-keying old data because if you need to introduce a new HMAC key (once
you already have 2 HMAC keys) then you're forced to re-key any HMACs that were calculated with the oldest HMAC key (1rst HMAC key) onto the most recent HMAC key (2nd HMAC key)
before you can introduce the new (3rd HMAC key)....and so on.

The way the strategy works is the following. You just have the USER_PROFILE table, but it looks like this:

| ID                                   | TENANT_ID  | CIPHER_TEXT                                                          | SOME_OTHER_NON_CONFIDENTIAL_ATTRIBUTE | USERNAME_HMAC_1              | USERNAME_HMAC_2              | ENCRYPTION_KEY_ID                    | HMAC_KEY_ID_1                        | HMAC_KEY_ID_2                        |
|--------------------------------------|------------|----------------------------------------------------------------------|---------------------------------------|------------------------------|------------------------------|--------------------------------------|--------------------------------------|--------------------------------------|
| 9c7e275e-3729-421c-a7c5-cf02bba17f2d | MyTenantID | QmFzZTY0RW5jb2RpbmdPZlVzZXJQcm9maWxlQ29uZmlkZW50aWFsQXR0cmlidXRlcw== | some value In the clear               | QmFzZTY0VmFsdWVPZlVzZXJuYW1l | QmFzZTY0VmFsdWVPZlVzZXJuYW1l | 23a0a1b4-3897-4eaa-b8fa-1818a9540f0c | 31ae30a7-4228-40a9-9078-d5e994491981 | 31ae30a7-4228-40a9-9078-d5e994491981 |

Notice that there are 2 separate columns for the username HMAC.
With this strategy, your application will assume that there could be up to 2 write keys (max) in use at any time so that it can be prepared to insert 2 HMACs for the username for
each write operation. With the HMAC generated with the old key inserted into the USERNAME_HMAC_1 column and the HMAC generated with the new key inserted into the USERNAME_HMAC_2
column. If there is only a single key in use then it should insert the same username HMAC twice into both the USERNAME_HMAC_1 and USERNAME_HMAC_2 columns so that they match.
This keeps the entity design simple and for relational DBs it eliminates the need for multiple tables per entity as is necessary for the List HMAC strategy. It's important to note
that all search code must search for the HMAC in both columns at all times though, as it could be in either one. This leads to messier search code.

### Process for re-keying data with the Double HMAC Strategy

1. Add your new HMAC key to the tenant's list of HMAC keys
2. Wait until the HMAC key cache expiry time has passed (if applicable), so that all application instances are using the new HMAC key.
3. Kick off the re-keying job.
4. The re-keying job should find each record it needs to re-key. This depends on application requirements but will probably fall into one of 2 criteria:
   a. Any record which doesn't yet have HMACs calculated with the new HMAC key - **common for full re-keying of all data**
   b. Any record which has HMACs calculated with some old HMAC key (that you're trying to remove from the system), but does not have HMACs calculated with the new HMAC key -
   **common for passive key rotation where no unique constraint support is required**
5. For each record found, decrypt the record, calculate the HMAC(s) with the new key, overwrite the X_HMAC_2 columns (e.g. the USERNAME_HMAC_2 column above) with the new HMAC(s).
   X_HMAC_1 columns (e.g. the USERNAME_HMAC_1 column above) values should remain as they are, untouched.
6. Wait for all applicable records to be re-keyed
7. Remove the old HMAC key from the tenant's list of HMAC keys
8. Wait until the HMAC key cache expiry time has passed (if applicable), so that all application instances are no longer using the old HMAC key.
9. Copy X_HMAC_2 column values into X_HMAC_1 columns for all fields in all records, something like:

```sql language=sql
UPDATE table
SET USERNAME_HMAC_1 = USERNAME_HMAC_2
```

**Pros of the Double HMAC strategy:**

- Fairly solid HMAC design which will probably work for a good number of application requirements
- Relational DB friendly, single table design
- Negligible performance impact. Only calculates and inserts a max of 2 HMACs (per attribute) for each write operation
- Supports zero-outage search
- Supports unique constraint integrity
- Supports both the previous points for applications which cache HMAC keys

**Cons of the Double HMAC strategy:**

- For applications which use HMACs for unique value enforcement and only support passive key rotation (don't re-key data), it potentially only supports ones with relatively short
  data retention
- For applications which use HMACs for unique constraint support and have long-lived data, this strategy requires re-keying all data (by some background task) once a new HMAC key
  is introduced. Once this task finishes the old key can be removed
- Introduces a small window for mistakes to be made during HMAC key rotation. A code defect could mix up the HMAC columns which would cause issues. Or if the order of the HMAC keys
  in the list of HMAC keys is wrong then this could cause serious problems
- Application search code looks awkward as searches have to look in both HMAC columns (OR query)
- Supporting HMAC tokenization would be very clunky

<!--name=admonition;type=info;title=Note;body=You don't need to stop at 2 HMAC keys/columns. You can have a Triple HMAC Strategy or a Quadruple HMAC strategy if that suits your application requirements. The concept is
the same, but at that point you may as well go with the List HMAC Strategy instead. -->

## Single HMAC Strategy

This strategy is only recommended for a very specific set of application requirements which will most likely only apply to a small number of applications.
The Single HMAC strategy isn't really much of a strategy, it's just the way that many applications (unfortunately in many cases) default to using HMACs. So for each HMAC you have a
single column in your table/record where you store the HMAC. So our USER_PROFILE table would simply look like this:

| ID                                   | TENANT_ID  | CIPHER_TEXT                                                          | SOME_OTHER_NON_CONFIDENTIAL_ATTRIBUTE | USERNAME_HMAC                | ENCRYPTION_KEY_ID                    | HMAC_KEY_ID                          |
|--------------------------------------|------------|----------------------------------------------------------------------|---------------------------------------|------------------------------|--------------------------------------|--------------------------------------|
| 9c7e275e-3729-421c-a7c5-cf02bba17f2d | MyTenantID | QmFzZTY0RW5jb2RpbmdPZlVzZXJQcm9maWxlQ29uZmlkZW50aWFsQXR0cmlidXRlcw== | some value In the clear               | QmFzZTY0VmFsdWVPZlVzZXJuYW1l | 23a0a1b4-3897-4eaa-b8fa-1818a9540f0c | 31ae30a7-4228-40a9-9078-d5e994491981 |

And (as usual) you have a list of HMAC keys on a tenant.
However, there are serious limitations with this 'strategy' when it comes to key rotation. If your application caches HMAC keys then you will have some period of time after
changing the HMAC key where you intermittently won't find records that have just been created/updated with the new HMAC key. The result will depend on which application instance
the search is executed on and the state of its HMAC key cache. This strategy cannot support applications which require both key rotation and HMAC unique constraint enforcement
under any circumstances.

### Process for re-keying data with the Single HMAC Strategy

1. Add your new HMAC key to the tenant's list of HMAC keys. Single HMAC Strategy convention dictates that the list should be sorted in descending order by date created. So the new
   HMAC key should be inserted at the start of the list.
2. Wait until the HMAC key cache expiry time has passed (if applicable), so that all application instances are using the new HMAC key.
3. Kick off the re-keying job.
4. The re-keying job should find each record it needs to re-key. This depends on application requirements but will probably fall into one of 2 criteria:
   a. Any record which doesn't yet have HMACs calculated with the new HMAC key - **common for full re-keying of all data**
   b. Any record which has HMACs calculated with some old HMAC key (that you're trying to remove from the system) - **common for passive key rotation**
5. For each record found, decrypt the record, calculate the HMAC(s) with the new key, overwrite all HMAC columns (e.g. the USERNAME_HMAC column above) with the new HMAC(s).
6. Wait for all applicable records to be re-keyed
7. Remove the old HMAC key from the tenant's list of HMAC keys

**Pros of the Single HMAC strategy:**

- Probably the simplest design possible for supporting HMACs in an application
- Relational DB friendly, single table design
- No performance impact. Only calculates and inserts a single HMAC (per attribute) for each write operation
- Not much of a window for anything to go wrong during a key rotation or re-keying job.

**Cons of the Single HMAC strategy:**

- Cannot support applications which require both unique constraint enforcement and key rotation under any circumstances
- Will cause intermittent search outages during key rotation for applications which cache HMAC keys

## Single Time Based HMAC Strategy

This strategy is almost identical to the regular Single HMAC strategy documented previously with one small difference that solves the search outage window associated with key
rotation using cached HMAC keys.
All HMAC keys have a 'start time' attribute, which is set to some time in the future. If the application caches keys then the start time must be longer than that time period from
the moment that the HMAC key is added to the tenants list of HMAC keys. i.e. if your application HMAC key cache period is 15 minutes and the new HMAC key is added to the tenant at
15:00 3rd April 2030, then the start time needs to be set to at least 15:16 3rd April 2030. This will then guarantee that no application instances will start to use the key to
insert HMACs until that start time has passed. This solves the search problem for applications which cache HMAC keys because it means that all searches will work regardless of when
each instance updates their HMAC key cache.
However, just like the Single HMAC Strategy, this one cannot support applications which require both key rotation and unique constraint enforcement under any circumstances.

### Process for re-keying data with the Single Time Based HMAC Strategy

The process for re-keying data is the same as the Single HMAC Strategy

**Pros of the Single Time Based HMAC strategy:**

- Solves the intermittent search outage problem for applications which cache HMAC keys that the Single HMAC Strategy has.

**Cons of the Single Time Based HMAC strategy:**

- Cannot support applications which require both unique constraint enforcement and key rotation under any circumstances
- Will cause intermittent search outages during key rotation for applications which cache HMAC keys
- Introduces a bigger window where things can go wrong. e.g. If the start time of the HMAC key is not set correctly or there are other problems with application instances not
  refreshing their HMAC keys correctly then this strategy reduces to the Single HMAC Strategy.

### Some final considerations for application designs

Although corporate security guidelines in some companies may only require applications to support passive HMAC key rotation we strongly advise application developers to reconsider this in the context of
their application functionality and requirements! Supporting only passive HMAC key rotation for long-lived data means that there's a strong possibility that you cannot ever
deprecate any of a tenant's HMAC keys. Every time you update a tenant to a new HMAC key (meaning that you add a new one into the tenant's list of keys) then your application's
search operations will progressively get slower. This is because if a tenant has N HMAC keys we have to calculate N HMACs when we search for something. Since we can't remove a
tenant's keys (because there may be records that have never been modified since being written with the key in use at that time) then we'll have to keep using all of them to
generate HMACs for every operation until the end of time.
Also, although challenge 1 above will still be solved in a functional sense, challenge 2 still remains a serious problem in your application (if you have unique constraint
requirements). The only way to solve challenge 2 for passive key rotation for long-lived data would be to use the List HMAC Strategy. Otherwise, some re-keying job is necessary.

## How do I use this library?

The main interface to this library is the EncryptionService interface. To use it you need to implement just a few things:

* Implement the CryptoKeyProvider interface.
* During the startup of your application register your CryptoKeyProvider implementation with CryptoKeyProviderSupplier.register()
* Create any concrete implementations of EncryptionService that your application will need to use to carry out cryptographic operations.
* During the startup of your application register your EncryptionService implementations using EncryptionService.register()
* Your EncryptionService implementation needs to return a unique text value from the EncryptionService.supportedKeyType() method. You can see an example of this by looking at the
  Base64EncryptionService class which comes with the library for testing/local development purposes.

That's it. Once you do this you can execute cryptographic operations from your code by calling EncryptionService.encrypt(), EncryptionService.decrypt() and
EncryptionService.hmac().

Please see the [mango4j-crypto-core-example](TBD) module in
the [mango4j-examples repository](TBD) for a sample spring application that uses this library.

## FAQ

### What is a tenant?

For applications which store data from different client applications or enterprises there is often the requirement that
we need to segregate the data belonging to each client (i.e. your application has multiple customer enterprises which use it:
Bank A, Bank B and Bank C). Company requirements (and regulations) can sometimes dictate that the data which your
application stores related to each of these customers must be logically separated from each other. This is to limit the exposure
which might happen in a security breach. Your application could spin up 3 separate (and isolated) environments for each
customer but this is often too costly to maintain. An easier and equivalent way to do this is to have the concept of
'tenants' in your application. Each customer is a 'tenant' and each tenant has separate encryption keys. So your application will store and configure tenant information (which will
include the details of encryption and HMAC keys for each tenant).
Each operation in your application will then be within the context of one of these tenants. Any data stored
for Bank A is encrypted with an encryption key which is _only_ used by Bank A, And similarly, any data stored
for Bank B is encrypted with an encryption key which is _only_ used by Bank B. If your system doesn't have/need the concept
of tenants then you can just think of a tenant as "your entire application" in this documentation.

### What's a HMAC?

A HMAC is just a hash which uses a secret key to perform the hashing operation.

### What's a hash?

A hash is a one-way encryption cipher. One-way means that for a given piece of data, hashing it will produce a ciphertext but that ciphertext can never be used to reproduce the
original value, it can never be decrypted. HMACs are useful for storing data that needs to be matched but which we never want to risk an attacker knowing. HMACs are often used to
store passwords in a secure system.

### What's an IV?

An IV (Initialization Vector) essentially introduces randomness into the data being encrypted. A new IV should be generated and used for every encryption operation which means that
even if you encrypt the same piece of data with the same secret key, the resulting cipher text will be different. The cipher text will always decrypt successfully to the original
data (the IV is actually stored inside the blob containing the ciphertext) but the fact that the resulting ciphertext is always unique for any encryption operation makes
cryptanalysis more difficult to potential attackers.

### Why do I need to HMAC data in order to make it searchable?

When we encrypt a piece of data we should use an [IV](#What's-an-IV?) as well as the secret key - so make sure to consider using IVs in any
custom EncryptionService implementations you create for your application. Due to the fact that each time we encrypt a piece of data it never generates the same ciphertext this means
that we cannot search on that attribute. It may be tempting to think that for a search operation you could encrypt the incoming search value and look for matching ciphertext in the
DB but since the ciphertext just generated for the incoming search term is guaranteed unique (due to the use of an IV) it won't match anything. The solution is to store a HMAC
value alongside any attribute that needs to be searchable. HMACs will always give the same HMAC value for the same piece of data so they can be used for search purposes. If an
encrypted attribute also has a HMAC value stored separately for it, search operations can HMAC the incoming search term and search for the resulting HMAC value in the DB.
Their irreversibility makes them secure.

### Why do I need to HMAC confidential data in order to make it unique?

As explained above the encryption of confidential data always results in a unique ciphertext (due to the use of IVs) so that particular ciphertext cannot be used to enforce a
unique constraint. The solution is to also store a HMAC for this attribute and place a unique constraint on that field instead.

### What is a key rotation?

A key rotation is the action of changing an encryption or HMAC key that you're using right now (for a tenant) to a different one.

### Why would I perform a key rotation?

Many corporate and regulatory guidelines require encryption keys to be updated when certain criteria are met. The criteria definitions are a bit fuzzy and there's no permanent concrete
criteria for all applications. Some criteria are usage based, e.g. changing the key after it's been used X times. Other criteria are time based, e.g. change the key every X period.
Please consult the official SE Guild publications for prescriptions on when you need to rotate encryption and HMAC keys in your application.

The following points should also be taken into consideration when deciding when a key might need to be rotated:

* If a key becomes compromised then it is necessary to remove it from the system, which requires rotating to a new key. This could happen at any time.
* If new company or national regulations decide that the crypto period needs to be some different prescription than current company/national requirements.
* Some Cryptographic providers may stop being available in certain regions or deployment environments. In that case, application instances deployed in those environments will have
  to rotate keys onto a different cryptographic provider.

### What does passive key rotation mean?

Passive key rotation is the act of only using a new key going forward. So existing data can be left encrypted with the old key, whereas normal write operations encrypt with the new
key. Even for read operations, when existing data is read from the database the application should also re-encrypt this data with the new key and re-save it again before returning
the data. Passive key rotation can more easily satisfy applications with lenient data retention rules. Because if data is not long-lived then you know that you can eventually
delete the older keys (and any data encrypted with them). However, many applications won't have this luxury and should consider re-keying data over time so that they can eventually
remove old keys.

### What does re-keying mean?

Re-keying (referred to as _active key rotation_ in previous iterations of this document) is where after introducing a new encryption/HMAC key, some background job goes through the
database record by record, decrypting (with the old key) and re-encrypting (re-keying) with the new key(s) until there are no more records left that use the old key(s).

### Why should I re-key

If an encryption or HMAC key is compromised then you'll probably have to make sure that no data in your DB is encrypted or contains HMACs calculated with that key. This means you
have to consider re-keying all the data that used that compromised key with a new encryption/HMAC key.
If your application uses HMACs for data that has a long data retention policy then it's probably unrealistic to keep adding more and more HMAC keys over time but never being able
to remove them from the system. With each addition of a HMAC key the performance of your application will degrade because that's one extra HMAC to calculate (and search for/and
possibly store) for every HMAC operation.
If your application is multi-instance, caches keys and doesn't use the List HMAC Strategy then you'll need to re-key if your application uses HMACs for unique constraint
enforcement.

### Why does mango4j-crypto-core have the concept of only 1 encryption key but multiple HMAC keys?

When a piece of data is encrypted a reference to the encryption key is contained in the actual ciphertext along with the encrypted data. This means that it will always be possible
to decrypt the data even if the current encryption key is changed (i.e. for a key rotation), assuming you didn't delete the old key. HMACs however are different, [HMACs don't have a reference to the HMAC key stored alongside them.](#Why-don't-HMACs-have-a-reference-to-the-HMAC-key-stored-alongside-them-the-same-way-encrypted-ciphertext-does?) So, once a HMAC key is updated/rotated to a new HMAC key, some
records will have HMACs calculated using the old key (since they haven't been recalculated yet) and some records will have HMACs calculated using the new key (new records and newly
updated records). This means that there will be periods where we need to perform searches using more than 1 HMAC key since we can't be sure what records have been calculated with
what keys unless all HMACs in the DB are recalculated with the new key (re-keyed). This is just a re-statement of the HMAC key rotation challenges.

In a simpler statement: To decrypt something, we have already obtained the data we need to decrypt. But HMACs are used to _search_ for data, we don't know where it is yet.
Therefore, we don't know which HMAC key might have been used to HMAC it. So we have to try all the HMAC keys to search for it.

### Why don't HMACs have a reference to the HMAC key stored alongside them the same way encrypted ciphertext does?

Elaborating on previous statements: It would serve no real purpose to store a reference to the HMAC key beside the HMAC since they are used primarily for search purposes and by
definition that means that you have no information about the row where the HMAC is or even where it is in the DB. With encrypted ciphertext it's different, you already know the
row (maybe your application accessed it directly by ID, or maybe you've searched for and found it). So once you have the row then it's good that it has a reference to the
encryption key that was used, so now you can use that key to decrypt it. With HMACs you're trying to use them to find the row in the first place and once you've found the row,
having a reference to the HMAC key would serve no extra purpose. In summary, you never know where the HMAC you're looking for is or which HMAC key might have been used to calculate
it, so storing the HMAC key reference alongside it won't help you with anything. And again, This is why you must try all possible HMAC keys when searching.

*Caveat:* In saying that, it is useful to store a reference to the HMAC key alongside the HMAC so that re-key jobs know which records need re-keyed and which do not. But this is a
convenience related to re-keying functionality only and is not related to normal HMAC functionality.

### What is HMAC tokenization?

HMAC Tokenization is the process of chopping an input value into separate pieces and calculating HMACs for each piece for more flexible search support. i.e. for a PAN you could
HMAC the full PAN, HMAC the last 4 digits of the PAN, HMAC the PAN without dashes/spaces giving a total of 3 resulting HMACs for a single input. This allows you to support richer
search capabilities because now your application can support searching on the last 4 digits of the PAN and allows searches to find a PAN whether it has dashes/spaces in it or not.

## Appendixes

### A: Decisioning diagram for which HMAC strategy to use

![rekeying-decision-flow-diagram](basic/rekeying-decision-flow-diagram.png)

### B: Possible re-keying process when using list HMAC strategy when the application need to delete a specific HMAC key

When your application mostly runs on passive key rotation, but you need to support a process whereby a HMAC key needs to be completely removed from the system then you can follow
steps similar to below:

Terminology:
Deprecated HMAC Key: HMAC key that you want to completely remove from your system.

Substitute key: The HMAC key that was next introduced to the system after the deprecated key was originally introduced.

1. Run a query which finds records that have HMACs calculated with the key that's getting removed but do not have HMACs generated with the next most recent key (e.g. if you're
   removing the oldest HMAC key then find records that have HMACs calculated with that key but have no HMACs calculated with the second oldest key).
2. For each record generate HMACs with the substitute key and save them to the record (or to both the LOOKUP and UNIQUE_VALUES table if using separate SQL tables)
3. Delete the HMAC key to be removed from the system.
4. Delete the HMACs calculated with the deleted HMAC key from all records.



#### Case study for an application which uses the List HMAC Strategy and has the unique constraint challenge

Background: ACME unique application is an application that relies primarily on passive HMAC key rotation to manage the keying of records over time. ACME application also requires
unique constraints on highly confidential fields, so it uses the List HMAC Strategy. The application introduces a new HMAC key into the system every year. After 4 years it is
required to remove the oldest HMAC key from the system. ACME application data is long-lived and so it's not possible to rely on data retention rules to simply delete records that
used the old HMAC key so a re-keying job is necessary.

* ACME application always has 4 HMAC keys in use at any one time. Currently, it is year 8 of the application's lifetime so the HMACs keys are HMAC_KEY_5, HMAC_KEY_6, HMAC_KEY_7,
  HMAC_KEY_8. (This corresponds to the 2nd last column in the diagram below)
* At the start of year 9 (last column in the table below) a new HMAC key (HMAC_KEY_9) is introduced to the system (meaning that temporarily there are now 5 HMAC keys in use).
* A background re-keying task is then kicked off. We need to remove HMAC_KEY_5 from the system as it's now too old.
* The re-keying job executes a batched query. This query looks for records that do not have HMACs calculated with HMAC_KEY_6, which is now the oldest allowed HMAC key.
  *  When using the List HMAC Strategy correctly, this would be records that were written up to and during the 5th year which was the year before HMAC_KEY_6 was introduced into
     the system. During that 5th year all records would have been written with keys HMAC_KEY_2, HMAC_KEY_3, HMAC_KEY_4, HMAC_KEY_5. So those records wouldn't have any HMACs calculated
     with HMAC_KEY_6. But in the year after that (year 6), the system would have keyed off the oldest HMAC key (at that time HMAC_KEY_2) and records would have started to get written
     with keys HMAC_KEY_3, HMAC_KEY_4, HMAC_KEY_5 and HMAC_KEY_6.
* For each record found, the application decrypts the record, calculates any HMACS with HMAC_KEY_6 and inserts those new HMACs into the record LOOKUP and UNIQUE_VALUES lists.
* When all records have been modified as above, HMAC_KEY_5 is deleted from the system.
* All HMAC entries that were made with HMAC_KEY_5 are removed from every record in the system.

You can see that using this strategy that the re-keying job has to re-key an increasing number of records each year.

![sample_unique_constraint_scenario](basic/sample_unique_constraint_scenario.png)

**Key points about the diagram above:**

In each year, HMAC keys with a strikethrough depict the deprecated HMAC keys that are getting keyed off and subsequently deleted.

The => symbol depicts a re-key operation from the deprecated HMAC key to the next most recent HMAC key.

Observations on the table above:

Notice that as the years go on (as we go right through the columns) that the number of blocks with a re-key operation (depicted using the => symbol) grows? This is a visualization
of the fact that over time the re-key job will have more and more periods of records to re-key, so the re-key job will get progressively slower over time.

Visually the diagram shows us that in order to guarantee uniqueness enforcement, all cells in a column must share at least one number. We can see that using this re-keying approach
this number is always the current oldest HMAC key number.

With keys having a rotation period of 1 year and an expiry of 4 years there will mostly be 4 active HMAC keys in use at all times (excluding the first 3 years).
The only exceptions to this is when a new HMAC key is introduced there will be 5 HMAC keys in use until the re-keying job is finished and the oldest key is deleted. So each year
the HMAC key list will temporarily grow to 5 keys and shrink back to 4.

In any year, there are only ever a maximum of N-1 periods which don't need re-keying.

The approach to using 4 HMAC keys (as in this example) probably doesn't make much sense in the end. Using 2 keys and just doing a full re-key to the new key each year would most
likely make more sense. The reason being is that as the application continues in time the number of periods that need re-keyed will be much greater than the number that don't (in
this case 3). But the normal day-to-day operations of the application will always need to calculate 4 HMACs for every operation (5 during a re key), which will have a negative
impact on performance. If you switch to just having a single active HMAC key and changing it every year the normal performance of the app will improve as it will only have to
calculate a single HMAC for every operation (2 during a re key). This will come at the expense of the re-keying job having to do 3 extra periods, which would likely be an
acceptable trade off.

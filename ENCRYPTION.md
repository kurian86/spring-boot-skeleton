# Tenant Password Encryption

This project implements AES-256-GCM encryption for tenant passwords stored in the catalog database.

## Configuration

### Environment Variables (REQUIRED in Production)

```bash
export ENCRYPTION_MASTER_KEY="your-super-secret-master-key-minimum-32-characters"
export ENCRYPTION_SALT="your-unique-pbkdf2-salt-minimum-16-characters"
```

⚠️ **IMPORTANT**: 
- **NEVER** hardcode these keys in the code
- Use environment variables or a secrets manager (AWS Secrets Manager, HashiCorp Vault, etc.)
- If you lose these keys, YOU WILL NOT be able to recover encrypted passwords
- If you change the keys, you must re-encrypt ALL passwords

### Local Development

For development, there are default values in `application.yaml`, but DO NOT use them in production:

```yaml
app:
  security:
    encryption:
      master-key: ${ENCRYPTION_MASTER_KEY:change-this-in-production-use-env-var}
      salt: ${ENCRYPTION_SALT:change-this-salt-in-production-use-env-var}
```

## Security

### Algorithm: AES-256-GCM

- **AES-256**: Industry-standard symmetric encryption
- **GCM (Galois/Counter Mode)**: Provides confidentiality + integrity
- **PBKDF2**: Derives the key from the master key using 65,536 iterations
- **Random IV**: Each encryption uses a unique 12-byte IV
- **GCM Tag**: 128-bit authentication tag to detect tampering

### Encrypted text format

```
Base64( IV[12 bytes] + Ciphertext + GCM_Tag[16 bytes] )
```

### Security characteristics

✅ **Confidentiality**: Only someone with the master key can decrypt  
✅ **Integrity**: The GCM tag detects any modification  
✅ **Uniqueness**: Each encryption produces a different value (thanks to the random IV)  
✅ **Non-deterministic**: Encrypting "password" twice yields different results

## Existing Data Migration

If you already have tenants with plaintext passwords:

## Troubleshooting

### Error: "javax.crypto.BadPaddingException"
- The master key or salt have changed
- The encrypted text was modified/corrupted
- Verify that `ENCRYPTION_MASTER_KEY` and `ENCRYPTION_SALT` are correct

### Error: "IllegalArgumentException: Invalid Base64"
- The password in the DB is not Base64
- It is probably plaintext

### The application won't start
- Make sure environment variables are configured
- Verify that all passwords in the DB are correctly encrypted

## Best Practices

✅ **Key rotation**: Plan how to rotate keys periodically  
✅ **Key backup**: Store keys in a safe place (1Password, etc.)  
✅ **Auditing**: Log who accesses/modifies tenant passwords  
✅ **Separation**: Use different keys for dev/staging/prod  
✅ **Monitoring**: Alert on decryption failures

## Future Alternatives

- **AWS KMS**: AWS key management
- **HashiCorp Vault**: Centralized secret manager
- **Azure Key Vault**: Key management in Azure
- **Google Cloud KMS**: Key management in GCP

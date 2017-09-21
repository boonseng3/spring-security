Server that uses JWT for authentication
========================

Build on the [Token Authentication](../token-authentication/README.md) with additional changes:
- Use JWS
- Uses Private key for the signing of the token so that client can use the public key to verify the signature. There is no sharing of secret ehcne safer.

## Steps used to generate the keystore

```
 keytool -genkeypair -keysize 2048 -keyalg RSA -alias jwt -storetype jceks -keystore jwt.jks
```


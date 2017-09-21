Server that uses token for authentication
========================

Build on the [Basic Server with DaoAuthenticationProvider](../basic-server-userdetails-db/README.md) with additional changes:
- Generates a token after successful login
- Stores the token in EhCache
- Access to protected resource requires a valid token


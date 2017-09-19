Basic Server with DaoAuthenticationProvider
========================

Build on the [Basic Server](../basic-server/README.md) with additional changes:
- Uses dao authentication
- Uses custom UserDetailsService
- Uses BCryptPasswordEncoder to hash the password
- Uses Flyway to manage the database scripts
- Uses Spring Data for access database


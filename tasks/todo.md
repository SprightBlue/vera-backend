- [x] Save and push current Android `main` state.
- [x] Save current backend `main` state locally.
- [x] Create backend branch `eliguerra-dev`.
- [x] Add backend device token persistence and registration endpoint.
- [x] Add Firebase push sender behind a backend port.
- [x] Fan out FCM pushes from existing notification creation without rolling back saves.
- [x] Add focused backend tests for registration, update, push fanout, failure isolation, and invalid token cleanup.
- [x] Run backend verification with `mvn test`.

## Review / Results

- Android `main` pushed successfully.
- Backend remote push is blocked by GitHub permission for `keliasdev` on `SprightBlue/vera-backend`.
- Backend verification passed with `./mvnw clean test -Djava.version=17` because this environment does not support the repo's configured Java 25 release.

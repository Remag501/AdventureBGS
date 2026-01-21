# Unit Testing Game Plan

This plan outlines the steps to add unit tests for the world penalty system.

- [x] Create the test file: `src/test/java/me/remag501/adventurebgs/listener/JoinListenerTest.java`.
- [x] Set up mocks using Mockito for dependencies (`PDCManager`, `PenaltyManager`, `RotationManager`) and Bukkit objects (`Player`, `World`).
- [x] Write the first test case to verify that the penalty is applied when a player joins an "outdated" world.
- [ ] Write additional test cases for `JoinListener` and ensure all tests pass:
    - [ ] Re-enable and verify the test for an outdated player in an adventure world.
    - [ ] Add and verify test for a player joining a non-adventure world.
    - [ ] Add and verify test for an up-to-date player in an adventure world.
- [ ] Complete all unit tests for `JoinListener`.
- [ ] Analyze project architecture and propose the next file for unit testing.

# FineClaim Manual Testing Checklist

Use this checklist on a **Folia 1.21.x** test server with at least two non-OP players and one OP/admin account.

## Prerequisites

- Java 21 installed
- Folia server jar for Minecraft 1.21.x
- FineClaim built: `.\gradlew.bat build` (Windows) or `./gradlew build` (Linux/macOS)
- Plugin copied to the server `plugins/` folder
- Two regular test accounts: **PlayerA**, **PlayerB**
- One OP/admin account: **Admin**

## 1. Start server with FineClaim

**Steps**

1. Copy the latest FineClaim JAR into the test server `plugins/` folder.
2. Start the Folia server.
3. Watch the console during startup.

**Expected**

- Server starts without errors.
- Console shows `FineClaim enabled`.
- `plugins/FineClaim/` folder is created.
- No stack trace related to FineClaim.

## 2. Player A runs `/claim`

**Steps**

1. Join as **PlayerA**.
2. Stand in an unclaimed chunk.
3. Run `/claim`.

**Expected**

- Message: `Claim created.`
- Running `/claiminfo` in the same chunk shows **PlayerA** as owner.

## 3. Player B tries break / place / interact in the claim

**Steps**

1. Join as **PlayerB**.
2. Enter the chunk claimed by **PlayerA**.
3. Try to break a block.
4. Try to place a block.
5. Right-click a block (for example a chest, door, or button).
6. Left-click a block.

**Expected**

- Break is cancelled.
- Place is cancelled.
- Interact is cancelled.
- **PlayerB** receives: `You cannot do that in this claim.`

## 4. Player A runs `/trust PlayerB`

**Steps**

1. Join as **PlayerA** in the claimed chunk.
2. Run `/trust PlayerB`.

**Expected**

- Message: `Player trusted.`
- `/claiminfo` shows trusted player count increased by 1.

## 5. Player B tries again

**Steps**

1. Join as **PlayerB** in the same claimed chunk.
2. Repeat break, place, and interact tests from step 3.

**Expected**

- Break succeeds.
- Place succeeds.
- Interact succeeds.
- No protection denial message.

## 6. Player A runs `/untrust PlayerB`

**Steps**

1. Join as **PlayerA** in the claimed chunk.
2. Run `/untrust PlayerB`.

**Expected**

- Message: `Player untrusted.`
- `/claiminfo` shows trusted player count decreased.

## 7. Player A runs `/claiminfo`

**Steps**

1. As **PlayerA**, stand in the claimed chunk.
2. Run `/claiminfo`.

**Expected**

- Output includes:
  - Owner UUID matching **PlayerA**
  - Chunk world name and chunk X/Z
  - Trusted players count
  - Created-at timestamp

## 8. Restart server

**Steps**

1. Stop the Folia server cleanly.
2. Start the server again.
3. Confirm FineClaim loads successfully.

**Expected**

- Server restarts without FineClaim errors.
- Console shows claim load log from storage (for example: `Loaded 1 claim(s) from claims.yml.`).

## 9. Verify claim persists after restart

**Steps**

1. Join as **PlayerA** and run `/claiminfo` in the same chunk.
2. Join as **PlayerB** and try to break a block in that chunk.

**Expected**

- `/claiminfo` still shows the same owner and chunk data.
- **PlayerB** is blocked again with `You cannot do that in this claim.`
- Trusted list matches state before restart (PlayerB untrusted if step 6 was completed).

## 10. Test OP / admin bypass

**Steps**

1. Join as **Admin** (OP account).
2. Enter a chunk claimed by **PlayerA**.
3. Try break, place, and interact without being owner or trusted.

**Expected**

- All actions succeed.
- No protection denial message.
- Admin can inspect any claim with `/claiminfo` even when not owner/trusted.

## 11. Test command permission deny

**Steps**

1. Remove a command permission from a test player (for example deny `fineclaim.command.claim`).
2. Have that player run the matching command (for example `/claim`).
3. Repeat for at least one other command (for example `/claiminfo` with `fineclaim.command.info` denied).

**Expected**

- Command does not execute.
- Player receives: `You do not have permission to use this command.`
- No claim changes occur when `/claim` is denied.

## Optional cleanup

- Run `/unclaim` as **PlayerA** to remove the test claim.
- Confirm `/claiminfo` reports: `This chunk is not claimed.`

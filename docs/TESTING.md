# FineClaim Manual Testing Checklist

Use this checklist on a **Folia 1.21.x** test server with at least two non-OP players and one OP/admin account.

## Prerequisites

- Java 21 installed
- Folia server jar for Minecraft 1.21.x
- FineClaim built: `.\gradlew.bat build` (Windows) or `./gradlew build` (Linux/macOS)
- Plugin copied to the server `plugins/` folder
- Two regular test accounts: **PlayerA**, **PlayerB**
- One OP/admin account: **Admin**
- Default `config.yml` limits: `MaxChunksPerMember: 16`, `MaxChunksPerServer: 10000`, `PreviewDisplaySeconds: 120`

## 1. Start server with FineClaim

**Steps**

1. Copy the latest FineClaim JAR into the test server `plugins/` folder.
2. Start the Folia server.
3. Watch the console during startup.

**Expected**

- Server starts without errors.
- Console shows `FineClaim enabled`.
- `plugins/FineClaim/` folder is created with `config.yml` and `claims.yml`.
- No stack trace related to FineClaim.

## 2. Preview claim with `/claim`

**Steps**

1. Join as **PlayerA**.
2. Stand in an unclaimed chunk.
3. Run `/claim` (no arguments).

**Expected**

- Message: `Preview active. Use /claim confirm or /claim cancel.`
- Light-blue **BlockDisplay** corner markers appear at the four corners of the current chunk.
- Running `/claiminfo` still reports: `This chunk is not claimed.` (preview does not create a claim yet).

## 3. Cancel preview with `/claim cancel`

**Steps**

1. As **PlayerA**, with an active preview from step 2, run `/claim cancel`.

**Expected**

- Message: `Claim preview cancelled.`
- BlockDisplay markers disappear.
- Chunk remains unclaimed.

## 4. Confirm claim with `/claim confirm`

**Steps**

1. As **PlayerA**, stand in an unclaimed chunk.
2. Run `/claim`.
3. Run `/claim confirm`.

**Expected**

- Message: `Claim created.`
- Region border is shown for the new 1-chunk claim.
- `/claiminfo` shows **PlayerA** as owner and **Chunks: 1**.

## 5. Expand claim with `/claim expand`

**Steps**

1. As **PlayerA**, move to an **adjacent unclaimed** chunk (north, east, south, or west of the existing claim).
2. Run `/claim expand`.

**Expected**

- Message: `Claim expanded.`
- Border displays update to cover both chunks.
- `/claiminfo` in either chunk shows **Chunks: 2** and the same owner.

## 6. Shrink claim with `/claim shrink`

**Steps**

1. As **PlayerA**, stand on an **edge chunk** of the region (a chunk with at least one unclaimed neighbor).
2. Run `/claim shrink`.

**Expected**

- Message: `Claim shrunk.` (or `Claim removed.` if shrinking the last chunk).
- Border updates to reflect the smaller region.
- `/claiminfo` shows the reduced chunk count.

**Edge case — interior chunk**

1. Expand to at least 3 chunks in a row.
2. Stand on the middle chunk and run `/claim shrink`.

**Expected**

- Message: `You can only shrink edge chunks of your claim.`
- Region size unchanged.

## 7. Player B tries break / place / interact in the claim

**Steps**

1. Join as **PlayerB**.
2. Enter a chunk claimed by **PlayerA**.
3. Try to break a block.
4. Try to place a block.
5. Right-click a block (for example a chest, door, or button).
6. Left-click a block.

**Expected**

- Break is cancelled.
- Place is cancelled.
- Interact is cancelled.
- **PlayerB** receives: `You cannot do that in this claim.`

## 8. Player A runs `/trust PlayerB`

**Steps**

1. Join as **PlayerA** in the claimed chunk.
2. Run `/trust PlayerB`.

**Expected**

- Message: `Player trusted.`
- `/claiminfo` shows trusted player count increased by 1.

## 9. Player B tries again after trust

**Steps**

1. Join as **PlayerB** in the same claimed chunk.
2. Repeat break, place, and interact tests from step 7.

**Expected**

- Break succeeds.
- Place succeeds.
- Interact succeeds.
- No protection denial message.

## 10. Player A runs `/untrust PlayerB`

**Steps**

1. Join as **PlayerA** in the claimed chunk.
2. Run `/untrust PlayerB`.

**Expected**

- Message: `Player untrusted.`
- `/claiminfo` shows trusted player count decreased.

## 11. Player A runs `/claiminfo`

**Steps**

1. As **PlayerA**, stand in any chunk of the region.
2. Run `/claiminfo`.

**Expected**

- Output includes:
  - Owner name matching **PlayerA**
  - Current chunk world name and chunk X/Z
  - **Chunks:** total region size
  - Trusted players count
  - Created-at date (`dd/MM/yyyy`)
- Region border is displayed for all chunks in the claim.

## 12. Unclaim entire region with `/unclaim`

**Steps**

1. As **PlayerA**, stand in any chunk of the region.
2. Run `/unclaim`.

**Expected**

- Message: `Claim deleted.`
- `/claiminfo` reports: `This chunk is not claimed.`
- All chunks in the former region are unclaimed.

## 13. Claim limits (`MaxChunksPerMember`)

**Steps**

1. Temporarily set `MaxChunksPerMember: 1` in `config.yml`.
2. Run `/fineclaim reload` as **Admin**.
3. As **PlayerA**, claim and confirm one chunk.
4. Move to another unclaimed chunk and run `/claim confirm` (after preview) or `/claim expand`.

**Expected**

- Operation is denied with a limit message.
- No additional chunks are added.

Restore `MaxChunksPerMember` to `16` and reload when finished.

## 14. Preview auto-expire

**Steps**

1. Temporarily set `PreviewDisplaySeconds: 5` in `config.yml`.
2. Run `/fineclaim reload`.
3. As **PlayerA**, run `/claim` in an unclaimed chunk.
4. Wait at least 5 seconds without confirming.

**Expected**

- BlockDisplay markers disappear automatically.
- `/claim confirm` reports: `You do not have an active claim preview.`

Restore `PreviewDisplaySeconds` to `120` and reload when finished.

## 15. Restart server

**Steps**

1. Stop the Folia server cleanly.
2. Start the server again.
3. Confirm FineClaim loads successfully.

**Expected**

- Server restarts without FineClaim errors.
- Console shows claim load log from storage (for example: `Loaded N chunk(s) from claims.yml.`).
- Legacy single-chunk entries in `claims.yml` are migrated to the v2 `chunks:` format on load.

## 16. Verify claim persists after restart

**Steps**

1. Join as **PlayerA** and run `/claiminfo` in a claimed chunk.
2. Join as **PlayerB** and try to break a block in that chunk.

**Expected**

- `/claiminfo` still shows the same owner, region size, and chunk data.
- **PlayerB** is blocked again with `You cannot do that in this claim.`
- Trusted list matches state before restart.

## 17. Test OP / admin bypass

**Steps**

1. Join as **Admin** (OP account).
2. Enter a chunk claimed by **PlayerA**.
3. Try break, place, and interact without being owner or trusted.

**Expected**

- All actions succeed.
- No protection denial message.
- Admin can inspect any claim with `/claiminfo` even when not owner/trusted.

## 18. Test command permission deny

**Steps**

1. Remove a command permission from a test player (for example deny `fineclaim.command.claim`).
2. Have that player run the matching command (for example `/claim`).
3. Repeat for at least one other command (for example `/claiminfo` with `fineclaim.command.info` denied).

**Expected**

- Command does not execute.
- Player receives: `You do not have permission to use this command.`
- No claim changes occur when `/claim` is denied.

## 19. Test `/fineclaim reload`

**Steps**

1. As **Admin**, edit `config.yml` (for example change `BorderBlock`).
2. Run `/fineclaim reload`.

**Expected**

- Message confirms config and claims were reloaded.
- New settings apply to subsequent previews and borders.

## Optional cleanup

- Run `/unclaim` as **PlayerA** on each region to remove test claims.
- Confirm `/claiminfo` reports: `This chunk is not claimed.`

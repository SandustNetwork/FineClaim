# FineClaim Manual Testing Checklist

Use this checklist on a **Folia 1.21.x** test server with at least two non-OP players and one OP/admin account.

## Prerequisites

- Java 21 installed
- Folia server jar for Minecraft 1.21.x
- FineClaim built: `.\gradlew.bat build` (Windows) or `./gradlew build` (Linux/macOS)
- Plugin copied to the server `plugins/` folder
- Two regular test accounts: **PlayerA**, **PlayerB**
- One OP/admin account: **Admin**
- Default limits: `MaxBlocksPerMember: 4096`, `MaxBlocksPerServer: 2500000`, `PreviewDisplaySeconds: 120`

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

## 2. Start claim selection with `/claim`

**Steps**

1. Join as **PlayerA**.
2. Stand in an unclaimed area.
3. Run `/claim`.

**Expected**

- Message explains Claim Wand usage.
- **Claim Wand** (stick with glow) appears in inventory.
- Location is **not** claimed yet.

## 3. Select points A and B with the wand

**Steps**

1. Left-click a block to set point A.
2. Right-click another block to set point B (diagonal corners of desired box).
3. Observe the preview border.

**Expected**

- Messages confirm point A and point B coordinates.
- BlockDisplay markers appear at the box corners.
- `/claiminfo` still reports location is not claimed until confirm.

## 4. Cancel selection with `/claim cancel`

**Steps**

1. With an active wand session, run `/claim cancel`.

**Expected**

- Claim Wand is removed from inventory.
- Preview border disappears.
- No claim is created.

## 5. Confirm claim with `/claim confirm`

**Steps**

1. Run `/claim` again to get a new wand.
2. Set points A and B around a small area (for example 5×5×5 blocks).
3. Run `/claim confirm`.

**Expected**

- Message: `Claim created.`
- Claim Wand is removed.
- Box border is shown briefly.
- `/claiminfo` inside the box shows owner **PlayerA**, block count, and size.

## 6. Tab completion for `/claim`

**Steps**

1. Type `/claim ` and press Tab.
2. Type `/claim resize ` and press Tab.

**Expected**

- First level suggests: `confirm`, `cancel`, `resize`.
- After `resize`, suggests: `confirm`, `cancel`.

## 7. Player B tries break / place / interact in the claim

**Steps**

1. Join as **PlayerB**.
2. Enter the claimed box.
3. Try to break, place, and interact with blocks.

**Expected**

- Actions are cancelled for **PlayerB**.
- Message: `You cannot do that in this claim.`

## 8. Player A runs `/trust PlayerB`

**Steps**

1. As **PlayerA**, stand inside the claim.
2. Run `/trust PlayerB`.

**Expected**

- Message: `Player trusted.`
- `/claiminfo` shows trusted count increased.

## 9. Player B tries again after trust

**Steps**

1. As **PlayerB**, repeat break/place/interact inside the claim.

**Expected**

- Actions succeed.
- No protection denial message.

## 10. Resize claim with `/claim resize`

**Steps**

1. As **PlayerA**, stand inside the claim.
2. Run `/claim resize`.
3. Left-click new point A and right-click new point B (full re-selection).
4. Run `/claim resize confirm`.

**Expected**

- Message: `Claim resized.`
- `/claiminfo` shows updated size and block count.
- New box border preview appears.

## 11. Cancel resize with `/claim resize cancel`

**Steps**

1. Run `/claim resize`, set new A/B but do **not** confirm.
2. Run `/claim resize cancel`.

**Expected**

- Wand removed, preview cleared.
- Original claim boundaries unchanged.

## 12. Block limits (`MaxBlocksPerMember`)

**Steps**

1. Set `MaxBlocksPerMember: 100` in `config.yml`.
2. Run `/fineclaim reload` as **Admin**.
3. Try to confirm a box larger than 100 blocks.

**Expected**

- Confirm is denied with a limit message.
- No claim is created or expanded beyond limit.

Restore `MaxBlocksPerMember` when finished.

## 13. Overlap rejection

**Steps**

1. As **PlayerA**, create a claim box.
2. As **PlayerB**, try to confirm a box that overlaps **PlayerA**'s claim.

**Expected**

- Confirm is denied: overlaps existing claim.

## 14. Preview auto-expire

**Steps**

1. Set `PreviewDisplaySeconds: 5` and reload.
2. Run `/claim`, set A/B, wait 5+ seconds without confirming.

**Expected**

- Preview border disappears.
- `/claim confirm` reports no active selection.

## 15. Restart server and verify persistence

**Steps**

1. Stop and restart the server.
2. As **PlayerA**, run `/claiminfo` inside the claim.
3. As **PlayerB**, try to break a block inside the claim.

**Expected**

- Claim data persists in `claims.yml` (v3 `box:` format).
- Protection still works after restart.

## 16. Test OP / admin bypass

**Steps**

1. As **Admin**, enter **PlayerA**'s claim without trust.
2. Try break, place, and interact.

**Expected**

- All actions succeed.
- `/claiminfo` works for admin.

## 17. Test `/fineclaim reload`

**Steps**

1. As **Admin**, run `/fineclaim reload`.

**Expected**

- Success message with loaded block count.
- Tab on `/fineclaim ` suggests `reload`.

## Optional cleanup

- Run `/unclaim` as **PlayerA** inside the claim to remove it.
- Confirm `/claiminfo` reports location is not claimed.

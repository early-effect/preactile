# preactile

sbt 2 caches aggressively. A green `compile` / `scalafmtCheckAll` that prints
`cache 100%` has not re-read the files.

**When in doubt, `cleanFull`.** Not `clean`. `clean` deletes products; `cleanFull`
clears sbt's local caches. There is no `cleanAll`.

```bash
sbt --server --batch "cleanFull; scalafmtAll; testFull"
```

CI fmt is a cold `scalafmtCheckAll`. Local `scalafmtCheckAll` on a warm server
can pass while CI still wants end-marker rewrites. Match CI with
`cleanFull; scalafmtAll` (then `scalafmtCheckAll`).

On sbt 2, plain `test` is `testQuick` and can skip. Use `testFull`.

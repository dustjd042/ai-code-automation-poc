name: AI Planning Review

on:
pull_request_target:
types: [opened, synchronize, reopened]
paths:
- 'docs/plan/**/*.md'

workflow_run:
workflows: ["Codex Issue Planning"]
types: [completed]

permissions:
contents: read
pull-requests: write
actions: read

jobs:
ai-review:
if: >-
github.event_name == 'pull_request_target' ||
(github.event_name == 'workflow_run' &&
github.event.workflow_run.conclusion == 'success' &&
github.event.workflow_run.repository.full_name == github.repository)
runs-on: ubuntu-latest

    steps:
      - name: Checkout trusted scripts
        uses: actions/checkout@v4
        with:
          ref: ${{ github.event.repository.default_branch }}
          persist-credentials: false

      - name: Setup Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '22'

      - name: Resolve PR number
        id: pr
        env:
          GH_TOKEN: ${{ github.token }}
          EVENT_NAME: ${{ github.event_name }}
          EVENT_PR_NUMBER: ${{ github.event.pull_request.number }}
          RUN_ID: ${{ github.event.workflow_run.id }}
          REPOSITORY: ${{ github.repository }}
        run: |
          set -euo pipefail

          if [ "$EVENT_NAME" = "pull_request_target" ]; then
            PR_NUMBER="$EVENT_PR_NUMBER"
          else
            mkdir -p "$RUNNER_TEMP/planning-artifact"

            if ! gh run download "$RUN_ID" \
              --repo "$REPOSITORY" \
              --name planning-pr \
              --dir "$RUNNER_TEMP/planning-artifact"; then
              echo "No planning PR artifact found; skipping review."
              echo "skip=true" >> "$GITHUB_OUTPUT"
              exit 0
            fi

            PR_NUMBER="$(cat "$RUNNER_TEMP/planning-artifact/pr-number.txt")"
          fi

          if ! [[ "$PR_NUMBER" =~ ^[1-9][0-9]*$ ]]; then
            echo "Invalid PR number"
            exit 1
          fi

          echo "number=$PR_NUMBER" >> "$GITHUB_OUTPUT"

      - name: Validate planning PR
        if: steps.pr.outputs.skip != 'true'
        id: validate
        env:
          GH_TOKEN: ${{ github.token }}
          PR_NUMBER: ${{ steps.pr.outputs.number }}
          REPOSITORY: ${{ github.repository }}
          EVENT_NAME: ${{ github.event_name }}
          DEFAULT_BRANCH: ${{ github.event.repository.default_branch }}
        run: |
          set -euo pipefail

          gh pr view "$PR_NUMBER" \
            --repo "$REPOSITORY" \
            --json state,baseRefName,headRefName > "$RUNNER_TEMP/pr.json"

          STATE="$(jq -r '.state' "$RUNNER_TEMP/pr.json")"
          BASE="$(jq -r '.baseRefName' "$RUNNER_TEMP/pr.json")"
          HEAD="$(jq -r '.headRefName' "$RUNNER_TEMP/pr.json")"

          if [ "$STATE" != "OPEN" ] || [ "$BASE" != "$DEFAULT_BRANCH" ]; then
            echo "PR is not an open PR targeting the default branch."
            echo "skip=true" >> "$GITHUB_OUTPUT"
            exit 0
          fi

          if [ "$EVENT_NAME" = "workflow_run" ]; then
            if ! [[ "$HEAD" =~ ^codex/plan-[1-9][0-9]*$ ]]; then
              echo "Not a Codex planning PR."
              echo "skip=true" >> "$GITHUB_OUTPUT"
              exit 0
            fi
          fi

          gh api --paginate \
            "repos/$REPOSITORY/pulls/$PR_NUMBER/files?per_page=100" \
            --jq '.[].filename' > "$RUNNER_TEMP/changed-files.txt"

          if ! grep -Eq '^docs/plan/.*\.md$' "$RUNNER_TEMP/changed-files.txt"; then
            echo "No planning Markdown files changed."
            echo "skip=true" >> "$GITHUB_OUTPUT"
            exit 0
          fi

          echo "Planning PR validated: #$PR_NUMBER"

      - name: Run AI Review
        if: >-
          steps.pr.outputs.skip != 'true' &&
          steps.validate.outputs.skip != 'true'
        env:
          GITHUB_TOKEN: ${{ github.token }}
          OPENAI_API_KEY: ${{ secrets.OPENAI_API_KEY }}
          PR_NUMBER: ${{ steps.pr.outputs.number }}
          REPOSITORY: ${{ github.repository }}
        run: |
          set -euo pipefail
          test -n "$OPENAI_API_KEY" || {
            echo "OPENAI_API_KEY is missing"
            exit 1
          }
          test -f scripts/ai-review.mjs || {
            echo "scripts/ai-review.mjs is missing"
            exit 1
          }
          node scripts/ai-review.mjs

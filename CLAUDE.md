# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Purpose

This repo is a sandbox for trying out the Claude Code GitHub Actions integration. The Python package `shopcart` is a deliberately small sample project with **intentional bugs**, each tracked as a GitHub issue (#2–#6). When asked to fix an issue, fix only that issue so the others stay reproducible.

## Commands

```bash
pip install -e '.[dev]'                          # install package + pytest
python -m pytest                                 # run all tests (pythonpath=src is set in pyproject.toml)
python -m pytest tests/test_cart.py::test_add_and_total   # run a single test
```

There is no linter, formatter, or Python CI configured yet (adding pytest CI is issue #6).

## Code layout

- `src/` layout; the package is `src/shopcart/`. `__init__.py` re-exports the public API (`Cart`, `Item`, `apply_discount`, `parse_price`).
- `pricing.py` holds pure price helpers; `cart.py` holds `Item` (dataclass) and `Cart`, and `Cart.total()` delegates discounting to `pricing.apply_discount`, so a pricing fix changes cart totals too.
- Tests construct carts with `Cart([])` rather than `Cart()` to sidestep the shared-mutable-default bug (issue #2). Once that is fixed, tests can use `Cart()`.

## GitHub Actions

- `.github/workflows/claude.yml` runs Claude when `@claude` appears in an issue title/body, issue comment, PR review, or PR review comment.
- `.github/workflows/claude-code-review.yml` runs the `code-review` plugin automatically on every PR and posts inline comments.
- Both authenticate with the `CLAUDE_CODE_OAUTH_TOKEN` repo secret.

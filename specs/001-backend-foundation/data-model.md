# Data Model: M1 — Backend Foundation

**Date**: 2026-11-24 | **Feature**: [spec.md](spec.md)

## Overview

M1 introduces **no business-domain entities**. The data model for
this milestone consists solely of infrastructure metadata created
automatically by the migration system (Flyway) and the application
framework.

Business entities (users, wallets, ledger entries, merchants, etc.)
are explicitly out of scope and will be introduced in their
respective milestone specifications (M2–M4).

## Infrastructure Tables (auto-managed)

### flyway_schema_history

Created and managed automatically by Flyway. Tracks applied
migrations, checksums, and execution metadata.

| Column | Purpose |
|--------|---------|
| installed_rank | Ordering of applied migrations |
| version | Migration version identifier |
| description | Migration description |
| type | Migration type (SQL, JAVA, etc.) |
| script | Migration script filename |
| checksum | Integrity hash of the migration file |
| installed_by | Database user that applied the migration |
| installed_on | Timestamp of migration execution |
| execution_time | Duration of migration in milliseconds |
| success | Whether the migration succeeded |

This table is Flyway's internal tracking mechanism. Application
code MUST NOT read from or write to this table directly.

## Migrations

### V1__init_flyway.sql

A baseline migration that establishes the Flyway migration pipeline
without creating any business schema. Contains only a SQL comment
documenting its purpose. Flyway creates the `flyway_schema_history`
table on first run, then records this migration as applied.

```sql
-- V1: Initialize Flyway migration infrastructure.
-- No business tables created. See M2+ feature specifications.
```

## State Transitions

No entity state transitions in M1.

## Validation Rules

No business-domain validation rules in M1. The only validation
is Flyway's migration checksum verification, which is handled
internally by the framework.

## Future Considerations

The following entities are anticipated in future milestones but
are NOT modeled or created in M1:

- **M2**: Customer, Credential (identity and access)
- **M3**: Wallet, Account, LedgerEntry, Transaction (financial core)
- **M4**: Merchant, PaymentRequest, QRPayload (merchant payment)

Each entity will be specified in its milestone's feature
specification and created through a versioned Flyway migration.

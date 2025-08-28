#!/bin/bash

# BingoChain Database Restore Script
# This script restores a PostgreSQL database from backup

set -e

# Configuration
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-bingo_crypto}"
DB_USER="${DB_USER:-Postgres}"
BACKUP_DIR="${BACKUP_DIR:-./backups}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to show usage
show_usage() {
    echo "Usage: $0 [BACKUP_FILE]"
    echo ""
    echo "Options:"
    echo "  BACKUP_FILE    Path to backup file (custom format or SQL)"
    echo ""
    echo "Environment variables:"
    echo "  DB_HOST        Database host (default: localhost)"
    echo "  DB_PORT        Database port (default: 5432)"
    echo "  DB_NAME        Database name (default: bingo_crypto)"
    echo "  DB_USER        Database user (default: Postgres)"
    echo "  BACKUP_DIR     Backup directory (default: ./backups)"
    echo ""
    echo "Examples:"
    echo "  $0 /path/to/backup.custom"
    echo "  $0 /path/to/backup.sql.gz"
    echo "  $0  # Will list available backups to choose from"
}

# Function to list available backups
list_backups() {
    echo -e "${YELLOW}Available backups in $BACKUP_DIR:${NC}"
    echo ""
    
    if ls "$BACKUP_DIR"/bingochain_backup_* 2>/dev/null; then
        echo ""
        echo -e "${YELLOW}Select a backup file by entering its full path, or press Ctrl+C to exit.${NC}"
        read -p "Backup file path: " BACKUP_FILE
    else
        echo -e "${RED}No backup files found in $BACKUP_DIR${NC}"
        echo "Create a backup first using the backup.sh script."
        exit 1
    fi
}

# Check if backup file is provided
if [ $# -eq 0 ]; then
    list_backups
else
    BACKUP_FILE="$1"
fi

# Validate backup file
if [ ! -f "$BACKUP_FILE" ]; then
    echo -e "${RED}Error: Backup file '$BACKUP_FILE' not found.${NC}"
    exit 1
fi

echo -e "${YELLOW}Starting BingoChain database restore...${NC}"
echo "Database: $DB_NAME"
echo "Host: $DB_HOST:$DB_PORT"
echo "User: $DB_USER"
echo "Backup file: $BACKUP_FILE"
echo ""

# Confirm restore operation
echo -e "${RED}WARNING: This will completely replace the existing database!${NC}"
read -p "Are you sure you want to continue? (yes/no): " CONFIRM

if [ "$CONFIRM" != "yes" ]; then
    echo -e "${YELLOW}Restore operation cancelled.${NC}"
    exit 0
fi

# Check if required tools are available
if ! command -v pg_restore &> /dev/null && ! command -v psql &> /dev/null; then
    echo -e "${RED}Error: PostgreSQL client tools not found.${NC}"
    echo "Please install postgresql-client package."
    exit 1
fi

# Determine backup file type and restore accordingly
echo -e "${YELLOW}Detecting backup file format...${NC}"

if [[ "$BACKUP_FILE" == *.custom ]]; then
    # Custom format backup
    echo -e "${YELLOW}Restoring from custom format backup...${NC}"
    
    if pg_restore -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" \
        --verbose \
        --clean \
        --create \
        --exit-on-error \
        --dbname=postgres \
        "$BACKUP_FILE"; then
        
        echo -e "${GREEN}Custom format restore completed successfully!${NC}"
    else
        echo -e "${RED}Error: Failed to restore from custom format backup${NC}"
        exit 1
    fi
    
elif [[ "$BACKUP_FILE" == *.sql.gz ]]; then
    # Compressed SQL backup
    echo -e "${YELLOW}Restoring from compressed SQL backup...${NC}"
    
    if gunzip -c "$BACKUP_FILE" | psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres; then
        echo -e "${GREEN}Compressed SQL restore completed successfully!${NC}"
    else
        echo -e "${RED}Error: Failed to restore from compressed SQL backup${NC}"
        exit 1
    fi
    
elif [[ "$BACKUP_FILE" == *.sql ]]; then
    # Plain SQL backup
    echo -e "${YELLOW}Restoring from SQL backup...${NC}"
    
    if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d postgres -f "$BACKUP_FILE"; then
        echo -e "${GREEN}SQL restore completed successfully!${NC}"
    else
        echo -e "${RED}Error: Failed to restore from SQL backup${NC}"
        exit 1
    fi
    
else
    echo -e "${RED}Error: Unsupported backup file format.${NC}"
    echo "Supported formats: .custom, .sql, .sql.gz"
    exit 1
fi

# Verify restore
echo -e "${YELLOW}Verifying restore...${NC}"

if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "\dt" > /dev/null 2>&1; then
    # Get table count
    TABLE_COUNT=$(psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" | xargs)
    
    echo -e "${GREEN}Database restored successfully!${NC}"
    echo "Tables found: $TABLE_COUNT"
    
    # Show game statistics if available
    if psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT * FROM game_statistics LIMIT 1;" > /dev/null 2>&1; then
        echo -e "${YELLOW}Game statistics:${NC}"
        psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "SELECT * FROM game_statistics;"
    fi
    
else
    echo -e "${RED}Error: Could not verify database restore${NC}"
    exit 1
fi

echo -e "${GREEN}Database restore process completed successfully!${NC}"

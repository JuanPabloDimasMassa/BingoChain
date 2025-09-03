#!/bin/bash

# BingoChain Database Backup Script
# This script creates a backup of the PostgreSQL database

set -e

# Configuration
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"
DB_NAME="${DB_NAME:-bingo_crypto}"
DB_USER="${DB_USER:-Postgres}"
BACKUP_DIR="${BACKUP_DIR:-./backups}"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/bingochain_backup_${TIMESTAMP}.sql"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Starting BingoChain database backup...${NC}"

# Create backup directory if it doesn't exist
mkdir -p "$BACKUP_DIR"

# Check if pg_dump is available
if ! command -v pg_dump &> /dev/null; then
    echo -e "${RED}Error: pg_dump could not be found. Please install PostgreSQL client tools.${NC}"
    exit 1
fi

# Create backup
echo -e "${YELLOW}Creating backup of database '$DB_NAME'...${NC}"
if pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
    --verbose \
    --clean \
    --create \
    --if-exists \
    --format=custom \
    --file="$BACKUP_FILE.custom"; then
    
    echo -e "${GREEN}Custom format backup created: $BACKUP_FILE.custom${NC}"
    
    # Also create a plain SQL backup for easy viewing
    if pg_dump -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" \
        --verbose \
        --clean \
        --create \
        --if-exists \
        --format=plain \
        --file="$BACKUP_FILE"; then
        
        echo -e "${GREEN}Plain SQL backup created: $BACKUP_FILE${NC}"
        
        # Compress the plain SQL backup
        gzip "$BACKUP_FILE"
        echo -e "${GREEN}Backup compressed: $BACKUP_FILE.gz${NC}"
        
    else
        echo -e "${RED}Error: Failed to create plain SQL backup${NC}"
        exit 1
    fi
    
else
    echo -e "${RED}Error: Failed to create custom format backup${NC}"
    exit 1
fi

# Get backup file sizes
CUSTOM_SIZE=$(du -h "$BACKUP_FILE.custom" 2>/dev/null | cut -f1 || echo "Unknown")
PLAIN_SIZE=$(du -h "$BACKUP_FILE.gz" 2>/dev/null | cut -f1 || echo "Unknown")

echo -e "${GREEN}Backup completed successfully!${NC}"
echo -e "Custom format backup size: ${CUSTOM_SIZE}"
echo -e "Compressed SQL backup size: ${PLAIN_SIZE}"

# Clean up old backups (keep last 7 days)
echo -e "${YELLOW}Cleaning up old backups...${NC}"
find "$BACKUP_DIR" -name "bingochain_backup_*.sql.gz" -mtime +7 -delete 2>/dev/null || true
find "$BACKUP_DIR" -name "bingochain_backup_*.custom" -mtime +7 -delete 2>/dev/null || true

echo -e "${GREEN}Cleanup completed.${NC}"

# List recent backups
echo -e "${YELLOW}Recent backups:${NC}"
ls -lah "$BACKUP_DIR"/bingochain_backup_* 2>/dev/null | tail -5 || echo "No backups found"

echo -e "${GREEN}Database backup process completed!${NC}"

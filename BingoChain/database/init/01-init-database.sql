-- Initialize BingoChain Database
-- This script creates the initial database structure

-- Create database if not exists (handled by docker environment variables)
\echo 'Starting BingoChain database initialization...'

-- Create extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Set timezone
SET timezone = 'UTC';

\echo 'Database extensions created successfully!'

-- Create initial indexes for better performance
CREATE INDEX IF NOT EXISTS idx_weekly_lotteries_status ON weekly_lotteries(status);
CREATE INDEX IF NOT EXISTS idx_weekly_lotteries_created_at ON weekly_lotteries(created_at);
CREATE INDEX IF NOT EXISTS idx_weekly_lotteries_sales_times ON weekly_lotteries(sales_start_time, sales_end_time);
CREATE INDEX IF NOT EXISTS idx_lottery_tickets_wallet_address ON lottery_tickets(wallet_address);
CREATE INDEX IF NOT EXISTS idx_lottery_tickets_weekly_lottery_id ON lottery_tickets(weekly_lottery_id);
CREATE INDEX IF NOT EXISTS idx_lottery_tickets_ticket_id ON lottery_tickets(ticket_id);
CREATE INDEX IF NOT EXISTS idx_draw_events_weekly_lottery_id ON draw_events(weekly_lottery_id);
CREATE INDEX IF NOT EXISTS idx_draw_events_draw_day ON draw_events(draw_day);
CREATE INDEX IF NOT EXISTS idx_draw_events_scheduled_time ON draw_events(scheduled_time);

\echo 'Database indexes created successfully!'

-- Insert some initial data for development
INSERT INTO weekly_lotteries (
    contract_address, 
    lottery_name, 
    ticket_price, 
    prize_pool, 
    total_tickets, 
    status, 
    sales_start_time,
    sales_end_time,
    current_draw_day,
    next_draw_time,
    drawn_numbers,
    winner_addresses,
    prizes_distributed,
    created_at, 
    updated_at
) VALUES 
(
    '0x1234567890123456789012345678901234567890',
    'Demo Weekly Lottery #1',
    0.01,
    0.00,
    0,
    'TICKET_SALES',
    NOW() + INTERVAL '1 hour',
    NOW() + INTERVAL '25 hours',
    0,
    NOW() + INTERVAL '25.5 hours',
    '[]',
    '[]',
    false,
    NOW(),
    NOW()
) ON CONFLICT DO NOTHING;

\echo 'Initial demo data inserted successfully!'
\echo 'BingoChain database initialization completed!'

-- Create a view for lottery statistics
CREATE OR REPLACE VIEW lottery_statistics AS
SELECT 
    COUNT(*) as total_lotteries,
    COUNT(CASE WHEN status = 'TICKET_SALES' THEN 1 END) as active_sales,
    COUNT(CASE WHEN status = 'DRAWING_PHASE' THEN 1 END) as in_drawing_phase,
    COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) as completed_lotteries,
    COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as cancelled_lotteries,
    COALESCE(SUM(CASE WHEN status = 'COMPLETED' AND prizes_distributed = true THEN prize_pool ELSE 0 END), 0) as total_prizes_paid,
    COALESCE(AVG(CASE WHEN status = 'COMPLETED' THEN prize_pool END), 0) as average_prize,
    COALESCE(SUM(total_tickets), 0) as total_tickets_sold
FROM weekly_lotteries;

-- Create a view for player statistics
CREATE OR REPLACE VIEW player_statistics AS
SELECT 
    wallet_address,
    COUNT(*) as tickets_purchased,
    COUNT(CASE WHEN is_winner = true THEN 1 END) as winning_tickets,
    COALESCE(SUM(ticket_price_paid), 0) as total_spent,
    COALESCE(SUM(CASE WHEN is_winner = true THEN prize_amount ELSE 0 END), 0) as total_won,
    COUNT(DISTINCT weekly_lottery_id) as lotteries_participated,
    CASE 
        WHEN COUNT(*) > 0 THEN 
            ROUND((COUNT(CASE WHEN is_winner = true THEN 1 END)::DECIMAL / COUNT(*)) * 100, 2)
        ELSE 0 
    END as win_percentage,
    CASE 
        WHEN SUM(ticket_price_paid) > 0 THEN
            ROUND((COALESCE(SUM(CASE WHEN is_winner = true THEN prize_amount ELSE 0 END), 0) / SUM(ticket_price_paid)) * 100, 2)
        ELSE 0
    END as return_on_investment
FROM lottery_tickets 
GROUP BY wallet_address;

\echo 'Database views created successfully!'

-- Create function to update lottery statistics
CREATE OR REPLACE FUNCTION update_lottery_stats()
RETURNS TRIGGER AS $$
BEGIN
    -- Update total_tickets count when a ticket is purchased
    IF TG_OP = 'INSERT' THEN
        UPDATE weekly_lotteries 
        SET total_tickets = (
            SELECT COUNT(*) 
            FROM lottery_tickets 
            WHERE weekly_lottery_id = NEW.weekly_lottery_id
        ),
        updated_at = NOW()
        WHERE id = NEW.weekly_lottery_id;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        UPDATE weekly_lotteries 
        SET total_tickets = (
            SELECT COUNT(*) 
            FROM lottery_tickets 
            WHERE weekly_lottery_id = OLD.weekly_lottery_id
        ),
        updated_at = NOW()
        WHERE id = OLD.weekly_lottery_id;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Create function to calculate next Sunday 12:30
CREATE OR REPLACE FUNCTION next_sunday_lottery_start()
RETURNS TIMESTAMP AS $$
DECLARE
    next_sunday TIMESTAMP;
BEGIN
    -- Find next Sunday at 12:30
    next_sunday := date_trunc('week', NOW()) + INTERVAL '6 days' + TIME '12:30:00';
    
    -- If it's already past this Sunday 12:30, move to next week
    IF next_sunday <= NOW() THEN
        next_sunday := next_sunday + INTERVAL '7 days';
    END IF;
    
    RETURN next_sunday;
END;
$$ LANGUAGE plpgsql;

-- Create triggers
DROP TRIGGER IF EXISTS trigger_update_lottery_stats ON lottery_tickets;
CREATE TRIGGER trigger_update_lottery_stats
    AFTER INSERT OR DELETE ON lottery_tickets
    FOR EACH ROW
    EXECUTE FUNCTION update_lottery_stats();

\echo 'Database functions and triggers created successfully!'
\echo 'BingoChain database setup is complete!'

-- Create indexes for performance optimization
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_weekly_lotteries_contract_address ON weekly_lotteries(contract_address);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_weekly_lotteries_status_created ON weekly_lotteries(status, created_at);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_lottery_tickets_wallet_lottery ON lottery_tickets(wallet_address, weekly_lottery_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_draw_events_lottery_day ON draw_events(weekly_lottery_id, draw_day);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_lottery_tickets_winner ON lottery_tickets(is_winner, weekly_lottery_id);
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_weekly_lotteries_next_draw ON weekly_lotteries(next_draw_time, status);

\echo 'Performance indexes created successfully!'

-- Analyze tables for query optimization
ANALYZE weekly_lotteries;
ANALYZE lottery_tickets;
ANALYZE draw_events;

\echo 'Database analysis completed!'
\echo 'BingoChain database is ready for use!'

-- Display final statistics
SELECT 'Database Setup Summary' as summary;
SELECT 
    schemaname,
    tablename,
    pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size
FROM pg_tables 
WHERE schemaname = 'public'
ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;

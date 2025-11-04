-- Script para crear un sorteo de prueba "CasoDePrueba"
-- Sorteo vigente con 2 días transcurridos, 2 números sorteados, y 2 cupones vendidos

-- Insertar el sorteo
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
    prizes_distributed,
    created_at,
    updated_at
) VALUES (
    '0x1234567890123456789012345678901234567890', -- Contract address ficticio
    'CasoDePrueba',
    0.01, -- Precio del ticket en ETH
    0.02, -- Prize pool (2 tickets * 0.01)
    2, -- Total de tickets vendidos
    'TICKET_SALES', -- Estado: vigente/activo
    NOW() - INTERVAL '2 days', -- Inicio hace 2 días
    NOW() + INTERVAL '5 days', -- Fin en 5 días
    2, -- Día de sorteo actual (2 días transcurridos)
    NOW() + INTERVAL '1 day', -- Próximo sorteo mañana
    '[1, 15]', -- 2 números sorteados en formato JSON
    false, -- Premios no distribuidos aún
    NOW() - INTERVAL '2 days',
    NOW()
) RETURNING id;

-- Obtener el ID del sorteo creado (se usará en las siguientes queries)
-- Nota: En PostgreSQL, necesitamos usar una variable temporal o hacer una subquery

-- Insertar 2 boletos para el usuario especificado
-- Primero necesitamos obtener el ID del sorteo recién creado
DO $$
DECLARE
    lottery_id BIGINT;
BEGIN
    -- Obtener el ID del sorteo "CasoDePrueba"
    SELECT id INTO lottery_id 
    FROM weekly_lotteries 
    WHERE lottery_name = 'CasoDePrueba' 
    ORDER BY created_at DESC 
    LIMIT 1;
    
    -- Insertar primer boleto
    INSERT INTO lottery_tickets (
        ticket_id,
        wallet_address,
        weekly_lottery_id,
        chosen_numbers,
        matched_numbers,
        ticket_price_paid,
        is_winner,
        prize_amount,
        transaction_hash,
        purchased_at,
        updated_at
    ) VALUES (
        'TICKET-' || lottery_id || '-1',
        '0x627306090abab3a6e1400e9345bc60c78a8bef57',
        lottery_id,
        '[1, 15, 33, 45, 67, 89]', -- Números elegidos (2 coinciden con los sorteados)
        2, -- 2 números coincidentes
        0.01,
        false, -- No es ganador aún (necesita más coincidencias)
        0.00,
        '0x' || encode(gen_random_bytes(32), 'hex'), -- Hash de transacción ficticio
        NOW() - INTERVAL '2 days',
        NOW()
    );
    
    -- Insertar segundo boleto
    INSERT INTO lottery_tickets (
        ticket_id,
        wallet_address,
        weekly_lottery_id,
        chosen_numbers,
        matched_numbers,
        ticket_price_paid,
        is_winner,
        prize_amount,
        transaction_hash,
        purchased_at,
        updated_at
    ) VALUES (
        'TICKET-' || lottery_id || '-2',
        '0x627306090abab3a6e1400e9345bc60c78a8bef57',
        lottery_id,
        '[2, 15, 25, 35, 55, 75]', -- Números elegidos (1 coincide con los sorteados)
        1, -- 1 número coincidente
        0.01,
        false,
        0.00,
        '0x' || encode(gen_random_bytes(32), 'hex'), -- Hash de transacción ficticio
        NOW() - INTERVAL '1 day',
        NOW()
    );
    
    RAISE NOTICE 'Sorteo creado con ID: %, 2 boletos insertados para el usuario', lottery_id;
END $$;

-- Verificar que todo se creó correctamente
SELECT 
    l.id,
    l.lottery_name,
    l.status,
    l.current_draw_day,
    l.drawn_numbers,
    l.total_tickets,
    l.prize_pool,
    COUNT(t.id) as tickets_count
FROM weekly_lotteries l
LEFT JOIN lottery_tickets t ON t.weekly_lottery_id = l.id
WHERE l.lottery_name = 'CasoDePrueba'
GROUP BY l.id, l.lottery_name, l.status, l.current_draw_day, l.drawn_numbers, l.total_tickets, l.prize_pool;

-- Mostrar los boletos creados
SELECT 
    t.id,
    t.ticket_id,
    t.wallet_address,
    t.chosen_numbers,
    t.matched_numbers,
    t.ticket_price_paid,
    t.is_winner,
    t.purchased_at
FROM lottery_tickets t
JOIN weekly_lotteries l ON l.id = t.weekly_lottery_id
WHERE l.lottery_name = 'CasoDePrueba'
ORDER BY t.purchased_at;


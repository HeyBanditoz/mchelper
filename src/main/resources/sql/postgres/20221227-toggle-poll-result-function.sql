CREATE OR REPLACE FUNCTION toggle_poll_result(calling_user BIGINT, calling_pq_id INTEGER, calling_poll_type SMALLINT, calling_poll_id INT) RETURNS BOOLEAN
    LANGUAGE plpgsql
AS
'
    DECLARE
        old_pq_id      INT;
    BEGIN
        -- One person can vote on multiple options
        IF (calling_poll_type = 1) THEN
            IF (SELECT 1
                FROM poll_results
                WHERE user_id = calling_user
                  AND pq_id = calling_pq_id) = 1 THEN
                DELETE
                FROM poll_results pr
                WHERE user_id = calling_user
                  AND pq_id = calling_pq_id;
                RETURN FALSE;
            ELSE
                INSERT INTO poll_results (pq_id, user_id)
                VALUES (calling_pq_id, calling_user);
                RETURN TRUE;
            END IF;
        END IF;
        -- One person can vote on only one option
        IF (calling_poll_type = 0) THEN
            IF (SELECT 1
                FROM poll_results
                         INNER JOIN poll_question q on q.id = poll_results.pq_id
                         INNER JOIN poll po on po.id = q.poll_id
                WHERE user_id = calling_user
                  AND po.id = calling_poll_id) = 1 THEN
                old_pq_id := (SELECT pq_id
                              FROM poll_results
                                       INNER JOIN poll_question q on q.id = poll_results.pq_id
                              WHERE poll_id = calling_poll_id
                                AND user_id = calling_user);
                IF (old_pq_id = calling_pq_id) THEN
                    -- User intends to remove their vote.
                    DELETE
                    FROM poll_results pr
                    WHERE user_id = calling_user
                      AND pq_id = old_pq_id;
                    RETURN FALSE;
                ELSE
                    DELETE
                    FROM poll_results pr
                    WHERE user_id = calling_user
                      AND pq_id = old_pq_id;
                    INSERT INTO poll_results (pq_id, user_id)
                    VALUES (calling_pq_id, calling_user);
                    RETURN TRUE;
                END IF;
                RETURN TRUE;
            ELSE
                INSERT INTO poll_results (pq_id, user_id)
                VALUES (calling_pq_id, calling_user);
                RETURN TRUE;
            END IF;
        END IF;
    END';

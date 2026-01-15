-- KEYS[1] = rate limit key
-- ARGV[1] = max_requests
-- ARGV[2] = window_seconds
-- ARGV[3] = current_timestamp (epoch seconds)

local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])
local now = tonumber(ARGV[3])

-- get current count
local current = redis.call("GET", key)

if not current then
    redis.call("SET", key, 1, "EX", window)
    return {1, limit - 1, now + window}
end

current = tonumber(current)

if current >= limit then
    local ttl = redis.call("TTL", key)
    return {current, 0, now + ttl}
end

current = redis.call("INCR", key)
local ttl = redis.call("TTL", key)

return {current, limit - current, now + ttl}
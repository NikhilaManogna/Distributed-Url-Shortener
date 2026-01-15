-- KEYS[1] = rate limit key
-- ARGV[1] = limit
-- ARGV[2] = window seconds

local current = redis.call("INCR", KEYS[1])

if current == 1 then
  redis.call("EXPIRE", KEYS[1], ARGV[2])
end

local ttl = redis.call("TTL", KEYS[1])

return { current, ttl }
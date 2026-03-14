local bucket_key = KEYS[1]
local timestamp_key = KEYS[2]

local capacity = tonumber(ARGV[1])
local refill_tokens = tonumber(ARGV[2])
local refill_period = tonumber(ARGV[3])
local requested_tokens = tonumber(ARGV[4])

local now = redis.call('TIME')
local now_seconds = tonumber(now[1])

local current_tokens = tonumber(redis.call('GET', bucket_key))
if current_tokens == nil then
    current_tokens = capacity
end

local last_refill = tonumber(redis.call('GET', timestamp_key))
if last_refill == nil then
    last_refill = now_seconds
end

local elapsed = now_seconds - last_refill
if elapsed > 0 then
    local refill_count = math.floor((elapsed / refill_period) * refill_tokens)
    if refill_count > 0 then
        current_tokens = math.min(capacity, current_tokens + refill_count)
        last_refill = now_seconds
    end
end

local allowed = 0
local retry_after = 0

if current_tokens >= requested_tokens then
    allowed = 1
    current_tokens = current_tokens - requested_tokens
else
    local missing = requested_tokens - current_tokens
    retry_after = math.ceil((missing * refill_period) / refill_tokens)
end

redis.call('SET', bucket_key, current_tokens)
redis.call('SET', timestamp_key, last_refill)
redis.call('EXPIRE', bucket_key, refill_period * 2)
redis.call('EXPIRE', timestamp_key, refill_period * 2)

return { allowed, current_tokens, retry_after }

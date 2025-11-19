-- KEYS[1] : match:wait:{LEVEL}  (Set)
-- KEYS[2] : match:geo:{LEVEL}   (ZSet as GEO)
-- ARGV[1] : radius meters (e.g., "3000")
-- ARGV[2] : lock ttl ms   (e.g., "5000")

local waitKey = KEYS[1]
local geoKey  = KEYS[2]
local idxKey  = KEYS[3]
local radius  = tonumber(ARGV[1]) or 3000
local lockTtl = tonumber(ARGV[2]) or 5000

local function lockUser(u)
    local lk = "lock:user:" .. u
    local ok = redis.call("SET", lk, "1", "NX", "PX", lockTtl)
    if ok then return true else return false end
end

-- 1) 대기세트에서 앵커 뽑기
-- local anchor = redis.call("SRANDMEMBER", waitKey) -- 완전 랜덤 (들어온 순서 X)
local anchors = redis.call("ZRANGE", idxKey, 0, 0)
if (not anchors) or (#anchors == 0) then
    return {}
end

local anchor = anchors[1]

-- 아직 대기 Set에 남아 있는지 확인 (아니면 idx에서만 제거하고 끝)
if redis.call("SISMEMBER", waitKey, anchor) ~= 1 then
    redis.call("ZREM", idxKey, anchor)
    return {}
end

-- 2) 앵커 락
if not lockUser(anchor) then return {} end

-- 3) 앵커 위치 확인 (없으면 정리)
local pos = redis.call("GEOPOS", geoKey, anchor)
if (not pos) or (not pos[1]) then
    redis.call("SREM", waitKey, anchor)
    redis.call("ZREM", idxKey, anchor)
    redis.call("DEL", "lock:user:" .. anchor)
    return {}
end

-- 4) 반경 내 후보 검색 (가까운 순)
local candidates = redis.call(
        "GEORADIUSBYMEMBER", geoKey, anchor, radius, "m", "WITHDIST", "ASC", "COUNT", 50
)

local partner = nil
if candidates and #candidates > 0 then
    for i = 1, #candidates do
        local c = candidates[i][1]
        if c ~= anchor then
            if redis.call("SISMEMBER", waitKey, c) == 1 then
                if lockUser(c) then
                    partner = c
                    break
                end
            end
        end
    end
end

if not partner then
    redis.call("DEL", "lock:user:" .. anchor)
    return {}
end

-- 5) 성공: 대기/지오에서 제거
redis.call("SREM", waitKey, anchor)
redis.call("SREM", waitKey, partner)
redis.call("ZREM", geoKey, anchor)
redis.call("ZREM", geoKey, partner)
redis.call("ZREM", idxKey, anchor)
redis.call("ZREM", idxKey, partner)

return { anchor, partner }
